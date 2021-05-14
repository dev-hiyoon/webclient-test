package com.hiyoon.java8test;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.io.IOException;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.main.allow-bean-definition-overriding=true"})
@ContextConfiguration(classes = {Java8TestApplication.class, Test4.TestConfig.class})
public class Test4 {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper mapper = new ObjectMapper();

    private static String cardJson = "{\n" +
            "\"cardId\": 224027103,\n" +
            "\"cardName\": \"CCEwOlJlcG9zaXRvcnkxMDQwMjcxMDM=\"}";

    public static MockWebServer mockBackEnd;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    private static ClientHttpConnector getConnector() {
        HttpClient httpClient = HttpClient.create()
                .tcpConfiguration(client ->
                        client.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                                .doOnConnected(conn -> conn
                                        .addHandlerLast(new ReadTimeoutHandler(300))
                                        .addHandlerLast(new WriteTimeoutHandler(300))));
        return new ReactorClientHttpConnector(httpClient.wiretap(true));
    }

    @Configuration
    public static class TestConfig {
        @Bean(name = "cardWebClient")
        WebClient cardWebClient() {
            return WebClient.builder()
                    .baseUrl("http://localhost:" + mockBackEnd.url("/").port() + "/")
                    .clientConnector(getConnector())
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
        }
    }

    @org.junit.jupiter.api.Test
    public void getCardData() throws Exception {
        log.info("getCardData started!");
        MockResponse mockResponse = new MockResponse().setResponseCode(HttpStatus.OK.value())
                .setBody(cardJson)
                .addHeader("Content-Type", "application/json");
        mockBackEnd.enqueue(mockResponse);
        ResultActions perform = mockMvc.perform(MockMvcRequestBuilders.get("/collect/card-to-map")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());

        log.info("getCardData ended!");
    }
}
