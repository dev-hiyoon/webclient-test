package com.hiyoon.java8test;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import okhttp3.mockwebserver.MockWebServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.reactive.server.WebTestClient;

@Slf4j
@SpringBootTest
//@TestPropertySource(properties = {"spring.main.allow-bean-definition-overriding=true"})
//@ContextConfiguration(classes = {Java8TestApplication.class, Test2.TestConfig.class})
public class Test3 {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private CardController collectController;

    static MockWebServer mockWebServer;

    private ObjectMapper mapper = new ObjectMapper();

    private static String cardJson = "{\n" +
            "\"cardId\": 224027103,\n" +
            "\"cardName\": \"CCEwOlJlcG9zaXRvcnkxMDQwMjcxMDM=\"}";

//    @Configuration
//    public static class TestConfig {
//        @Bean(name = "cardWebClient")
//        WebClient cardWebClient() {
//            HttpUrl url = mockWebServer.url("/");
//            return WebClient.create(url.toString());
//        }
//    }

    @org.junit.jupiter.api.Test
    public void getCardData() throws Exception {
        log.info("getCardData started!");
//        ResultActions perform = mockMvc.perform(MockMvcRequestBuilders.get("/collect/card")
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andDo(print());

        var result = WebTestClient
                .bindToController(collectController)
                .build()
                .get()
                .uri("/collect/card-to-map")
                .exchange()
                .expectStatus().isOk();
        log.info(result.returnResult(String.class).getResponseBody().blockFirst());
        log.info("getCardData ended!");
    }
}
