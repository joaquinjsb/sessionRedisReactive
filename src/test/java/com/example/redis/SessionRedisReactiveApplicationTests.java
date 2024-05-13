package com.example.redis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@SpringBootTest
class SessionRedisReactiveApplicationTests {
    WebTestClient client;

    @Autowired
    public void setApplicationContext(ApplicationContext context) {
        this.client = WebTestClient.bindToApplicationContext(context).build();
    }

    @Test
    void loginWhenMaxSessionDoesNotPreventLoginThenSecondLoginSucceedsAndFirstSessionIsInvalidated() {

        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("username", "user");
        data.add("password", "password");

        ResponseCookie firstLoginSessionCookie = loginReturningCookie(data);
        ResponseCookie secondLoginSessionCookie = loginReturningCookie(data);

        // first login should not be valid
        this.client.get()
                .uri("/")
                .cookie(firstLoginSessionCookie.getName(), firstLoginSessionCookie.getValue())
                .exchange()
                .expectStatus()
                .isFound()
                .expectHeader()
                .location("/login");

        // second login should be valid
        this.client.get()
                .uri("/")
                .cookie(secondLoginSessionCookie.getName(), secondLoginSessionCookie.getValue())
                .exchange()
                .expectStatus()
                .isOk();
    }
    private ResponseCookie loginReturningCookie(MultiValueMap<String, String> data) {
        return login(data).expectCookie()
                .exists("SESSION")
                .returnResult(Void.class)
                .getResponseCookies()
                .getFirst("SESSION");
    }

    private WebTestClient.ResponseSpec login(MultiValueMap<String, String> data) {
        return this.client.mutateWith(csrf())
                .post()
                .uri("/login")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromFormData(data))
                .exchange();
    }
}
