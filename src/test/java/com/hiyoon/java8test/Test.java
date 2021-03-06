package com.hiyoon.java8test;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@SpringBootTest
@TestPropertySource(properties = {"spring.main.allow-bean-definition-overriding=true"})
@AutoConfigureMockMvc(addFilters = false)
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ContextConfiguration(classes = {Java8TestApplication.class, Test.TestConfig.class})
public class Test {

    @Autowired
    private MockMvc mockMvc;

    static MockWebServer mockWebServer;

    private ObjectMapper mapper = new ObjectMapper();

    private static String cardJson = "{\n" +
            "\"cardId\": 224027103,\n" +
            "\"cardName\": \"CCEwOlJlcG9zaXRvcnkxMDQwMjcxMDM=\"}";

    @Configuration
    public static class TestConfig {
        @Bean(name = "cardWebClient")
        WebClient cardWebClient() {
            HttpUrl url = mockWebServer.url("/");
            return WebClient.create(url.toString());
        }
    }

//    @DynamicPropertySource
//    static void properties(DynamicPropertyRegistry r) throws IOException {
//        r.add("card.service.url", () -> "http://localhost:" + mockWebServer.getPort());
//    }

    @BeforeAll
    static void beforeAll() throws IOException, InterruptedException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
//        final Dispatcher dispatcher = new Dispatcher() {
//            @Override
//            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
//                switch (request.getPath()) {
//                    case "/card":
//                        return new MockResponse().setResponseCode(200);
//                    case "/card/approval-domestic":
//                        return new MockResponse().setResponseCode(200).setBody(cardJson);
//                    case "/card/approval-overseas":
//                        return new MockResponse().setResponseCode(200).setBody(cardJson);
//                }
//                return new MockResponse().setResponseCode(404);
//            }
//        };
//        mockWebServer.setDispatcher(dispatcher);
    }

    @BeforeEach
    void beforeEach() throws Exception {
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

//        mockWebServer.enqueue(new MockResponse()
//                .setBody(cardJson)
//                .addHeader("Content-Type", "application/json"));

        ResultActions perform = mockMvc.perform(MockMvcRequestBuilders.get("/collect/card")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());

        log.info("getCardData ended!");
    }
}
