package com.hiyoon.java8test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class Test5 {

    @Autowired
    private MockMvc mockMvc;

    private static WireMockServer wireMockServer;

    private ObjectMapper mapper = new ObjectMapper();

    private static String cardJson = "{\n" +
            "\"cardId\": 224027103,\n" +
            "\"cardName\": \"CCEwOlJlcG9zaXRvcnkxMDQwMjcxMDM=\"}";

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry r) {
        r.add("card.service.url", () -> "http://localhost:" + wireMockServer.port());
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
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
        configureFor("localhost", wireMockServer.port());
    }

    @AfterAll
    static void afterAll() throws IOException {
        wireMockServer.shutdown();
    }

    @org.junit.jupiter.api.Test
    public void getCardData() throws Exception {
        log.info("getCardData started!");
        stubFor(get(urlEqualTo("/card?seq=1")).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(cardJson)));

        stubFor(get(urlEqualTo("/card/approval-domestic?seq=2")).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(cardJson)));

        ResultActions perform = mockMvc.perform(MockMvcRequestBuilders.get("/collect/card-to-map")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());

        log.info("getCardData ended!");
    }
}
