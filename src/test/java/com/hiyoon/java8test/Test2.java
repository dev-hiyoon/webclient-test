package com.hiyoon.java8test;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
//@TestPropertySource(properties = {"spring.main.allow-bean-definition-overriding=true"})
//@ContextConfiguration(classes = {Java8TestApplication.class, Test2.TestConfig.class})
public class Test2 {

    @Autowired
    private MockMvc mockMvc;

    static MockWebServer mockWebServer;

    private ObjectMapper mapper = new ObjectMapper();

    private static String cardJson = "{\n" +
            "\"cardId\": 224027103,\n" +
            "\"cardName\": \"CCEwOlJlcG9zaXRvcnkxMDQwMjcxMDM=\"}";

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry r) {
        r.add("card.service.url", () -> "http://localhost:" + mockWebServer.getPort());
    }

//    @Configuration
//    public static class TestConfig {
//        @Bean(name = "cardWebClient")
//        WebClient cardWebClient() {
//            HttpUrl url = mockWebServer.url("/");
//            return WebClient.create("http://localhost:" + url.port());
//        }
//    }

    @BeforeAll
    static void beforeAll() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void afterAll() throws IOException {
        mockWebServer.shutdown();
    }

    @org.junit.jupiter.api.Test
    public void getCardData() throws Exception {
        log.info("getCardData started!");
        MockResponse mockResponse = new MockResponse().setResponseCode(HttpStatus.OK.value())
                .setBody(cardJson)
                .addHeader("Content-Type", "application/json");
        mockWebServer.enqueue(mockResponse);
        ResultActions perform = mockMvc.perform(MockMvcRequestBuilders.get("/collect/card-to-map")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());

        log.info("getCardData ended!");
    }
}
