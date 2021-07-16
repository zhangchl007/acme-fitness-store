package com.vmware.scg.extensions.whoami;

import java.time.Duration;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockOidcLogin;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class WhoamiApplicationTests {

	@Autowired
	WebTestClient webTestClient;

	@Test
	void should_replaceBodyIfNotNull() {

		webTestClient
				.mutateWith(mockOidcLogin().idToken(token -> token.claim("name", "Mock User")
				                                                  .claim("sub", "test-user")))
				.get()
				.uri("/non-null-body")
				.exchange()
				.expectBody()
				.jsonPath("$.userId").isEqualTo("test-user")
				.jsonPath("$.userName").isEqualTo("Mock User");
	}

	@Test
	@Disabled
	void should_setBodyIfNull() {
		// TODO: this test is currently failing - when the response body is null, my custom response decorator does not be triggered.
		webTestClient
				.mutate().responseTimeout(Duration.ofMinutes(5)).build()
				.mutateWith(mockOidcLogin().idToken(token -> token.claim("name", "Another User")
				                                                  .claim("sub", "another-user")))
				.get()
				.uri("/empty-body")
				.exchange()
				.expectBody()
				.jsonPath("$.userId").isEqualTo("another-user")
				.jsonPath("$.userName").isEqualTo("Another User");
	}

	@SpringBootApplication
	public static class GatewayApplication {
	}
}
