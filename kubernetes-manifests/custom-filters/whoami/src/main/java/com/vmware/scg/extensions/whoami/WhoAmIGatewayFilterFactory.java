package com.vmware.scg.extensions.whoami;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyDecoder;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyEncoder;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.server.ServerWebExchange;

import static java.util.Collections.emptyMap;
import static java.util.function.Function.identity;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR;

@Component
public class WhoAmIGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

	private final Logger log = LoggerFactory.getLogger(WhoAmIGatewayFilterFactory.class);

	private final List<HttpMessageReader<?>> messageReaders;

	private final Map<String, MessageBodyDecoder> messageBodyDecoders;

	private final Map<String, MessageBodyEncoder> messageBodyEncoders;

	private static final ObjectMapper objectMapper = new ObjectMapper();

	public WhoAmIGatewayFilterFactory(ServerCodecConfigurer codecConfigurer,
	                                  Set<MessageBodyDecoder> messageBodyDecoders,
	                                  Set<MessageBodyEncoder> messageBodyEncoders) {
		this.messageReaders = codecConfigurer.getReaders();
		this.messageBodyDecoders = messageBodyDecoders.stream()
		                                              .collect(Collectors.toMap(MessageBodyDecoder::encodingType, identity()));
		this.messageBodyEncoders = messageBodyEncoders.stream()
		                                              .collect(Collectors.toMap(MessageBodyEncoder::encodingType, identity()));
		log.info("Loading custom filter factory: [{}]", this.getClass().getSimpleName());
	}

	@Override
	public GatewayFilter apply(Object config) {
		return new WhoAmIGatewayFilter();
	}

	public class WhoAmIGatewayFilter implements GatewayFilter, Ordered {

		@Override
		public int getOrder() {
			return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1;
		}

		@Override
		public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
			log.debug("Triggering custom filter: {}", this.getClass().getSimpleName());
			return ReactiveSecurityContextHolder
					.getContext()
					.map(context -> {
						final Authentication authentication = context.getAuthentication();
						if (authentication instanceof OAuth2AuthenticationToken) {

							OAuth2User principal = ((OAuth2AuthenticationToken) authentication).getPrincipal();
							return Map.of(
									"userId", principal.getName(),
									"userName", (String) principal.getAttributes().get("name"));
						}
						return emptyMap();

					})
					.defaultIfEmpty(emptyMap())
					.flatMap(userInfo -> chain.filter(
							exchange.mutate()
							        .response(new ModifiedServerHttpResponse(exchange, userInfo))
							        .build()));
		}

	}

	// Copied from ModifyResponseBodyGatewayFilterFactory and will apply arbitrary response body instead of a RewriteFilter
	protected class ModifiedServerHttpResponse extends ServerHttpResponseDecorator {

		private final ServerWebExchange exchange;

		private final Object newResponseBody;

		public ModifiedServerHttpResponse(ServerWebExchange exchange, Object newResponseBody) {
			super(exchange.getResponse());
			this.exchange = exchange;
			this.newResponseBody = newResponseBody;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {

			String originalResponseContentType = exchange.getAttribute(ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR);
			HttpHeaders httpHeaders = new HttpHeaders();
			// explicitly add it in this way instead of
			// 'httpHeaders.setContentType(originalResponseContentType)'
			// this will prevent exception in case of using non-standard media
			// types like "Content-Type: image"
			httpHeaders.add(HttpHeaders.CONTENT_TYPE, originalResponseContentType);

			Mono modifiedBody;
			try {
				log.debug("Adding preset body: {}", newResponseBody);
				modifiedBody = Mono.just(objectMapper.writeValueAsString(newResponseBody));
			}
			catch (JsonProcessingException e) {
				log.error("Failed to serialize response: {}", newResponseBody, e);
				modifiedBody = Mono.empty();
			}

			BodyInserter bodyInserter = BodyInserters.fromPublisher(modifiedBody, String.class);
			CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(exchange,
					exchange.getResponse().getHeaders());
			return bodyInserter.insert(outputMessage, new BodyInserterContext()).then(Mono.defer(() -> {
				Mono<DataBuffer> messageBody = writeBody(getDelegate(), outputMessage, String.class);
				HttpHeaders headers = getDelegate().getHeaders();
				if (!headers.containsKey(HttpHeaders.TRANSFER_ENCODING)
						|| headers.containsKey(HttpHeaders.CONTENT_LENGTH)) {
					messageBody = messageBody.doOnNext(data -> headers.setContentLength(data.readableByteCount()));
				}
				return getDelegate().writeWith(messageBody);
			}));
		}

		@Override
		public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
			return writeWith(Flux.from(body).flatMapSequential(p -> p));
		}

		private ClientResponse prepareClientResponse(Publisher<? extends DataBuffer> body, HttpHeaders httpHeaders) {
			ClientResponse.Builder builder;
			builder = ClientResponse.create(exchange.getResponse().getStatusCode(), messageReaders);
			return builder.headers(headers -> headers.putAll(httpHeaders)).body(Flux.from(body)).build();
		}

		private <T> Mono<T> extractBody(ServerWebExchange exchange, ClientResponse clientResponse, Class<T> inClass) {
			// if inClass is byte[] then just return body, otherwise check if
			// decoding required
			if (byte[].class.isAssignableFrom(inClass)) {
				return clientResponse.bodyToMono(inClass);
			}

			List<String> encodingHeaders = exchange.getResponse().getHeaders().getOrEmpty(HttpHeaders.CONTENT_ENCODING);
			for (String encoding : encodingHeaders) {
				MessageBodyDecoder decoder = messageBodyDecoders.get(encoding);
				if (decoder != null) {
					return clientResponse.bodyToMono(byte[].class).publishOn(Schedulers.parallel()).map(decoder::decode)
					                     .map(bytes -> exchange.getResponse().bufferFactory().wrap(bytes))
					                     .map(buffer -> prepareClientResponse(Mono.just(buffer),
							                     exchange.getResponse().getHeaders()))
					                     .flatMap(response -> response.bodyToMono(inClass));
				}
			}

			return clientResponse.bodyToMono(inClass);
		}

		private Mono<DataBuffer> writeBody(ServerHttpResponse httpResponse, CachedBodyOutputMessage message,
		                                   Class<?> outClass) {
			Mono<DataBuffer> response = DataBufferUtils.join(message.getBody());
			if (byte[].class.isAssignableFrom(outClass)) {
				return response;
			}

			List<String> encodingHeaders = httpResponse.getHeaders().getOrEmpty(HttpHeaders.CONTENT_ENCODING);
			for (String encoding : encodingHeaders) {
				MessageBodyEncoder encoder = messageBodyEncoders.get(encoding);
				if (encoder != null) {
					DataBufferFactory dataBufferFactory = httpResponse.bufferFactory();
					response = response.publishOn(Schedulers.parallel()).map(buffer -> {
						byte[] encodedResponse = encoder.encode(buffer);
						DataBufferUtils.release(buffer);
						return encodedResponse;
					}).map(dataBufferFactory::wrap);
					break;
				}
			}

			return response;
		}

	}

}
