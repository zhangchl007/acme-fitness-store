package com.vmware.scg.extensions.whoami;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.setResponseStatus;

@Component
public class WhoAmIGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

	private final Logger log = LoggerFactory.getLogger(WhoAmIGatewayFilterFactory.class);

	public WhoAmIGatewayFilterFactory() {
		log.info("Loading custom filter factory: [{}]", this.getClass().getSimpleName());
	}

	@Override
	public GatewayFilter apply(Object config) {
		return new WhoAmIGatewayFilter();
	}

	public class WhoAmIGatewayFilter implements GatewayFilter {

		@Override
		public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
			log.debug("Triggering custom filter: {}", this.getClass().getSimpleName());
			setResponseStatus(exchange, HttpStatus.FOUND);
			final ServerHttpResponse response = exchange.getResponse();
			response.getHeaders().set(HttpHeaders.LOCATION, "/whoami");
			return response.setComplete();
		}

	}

}
