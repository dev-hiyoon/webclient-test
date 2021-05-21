package com.hiyoon.java8test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
public class AccountTest1 {

    @Autowired
    private MockMvc mockMvc;

    private static WireMockServer wireMockServer;

    private ObjectMapper mapper = new ObjectMapper();

    private static String accountJson = "{\n" +
            "\"accountId\": 224027103,\n" +
            "\"accountName\": \"CCEwOlJlcG9zaXRvcnkxMDQwMjcxMDM=\"}";

//    @DynamicPropertySource
//    static void properties(DynamicPropertyRegistry r) {
//        r.add("card.service.url", () -> "http://localhost:" + wireMockServer.port());
//    }
//
//    @BeforeAll
//    static void beforeAll() throws IOException {
//        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
//        wireMockServer.start();
//        configureFor("localhost", wireMockServer.port());
//    }
//
//    @AfterAll
//    static void afterAll() throws IOException {
//        wireMockServer.shutdown();
//    }

    @org.junit.jupiter.api.Test
    public void getAccountRealData() throws Exception {
        log.info("getAccountRealData started!");

        ResultActions perform = mockMvc.perform(MockMvcRequestBuilders.get("/mgw-account")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());

        log.info("getAccountRealData ended!");
    }

    @org.junit.jupiter.api.Test
    public void getAccountMockData() throws Exception {
        log.info("getCardData started!");
        stubFor(get(urlEqualTo("/account/deposit/basic?seq=1")).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(accountJson)));

        stubFor(get(urlEqualTo("/account/deposit/detail?seq=2")).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(accountJson)));

        stubFor(get(urlEqualTo("/account/deposit/transactions?seq=2")).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(accountJson)));

        ResultActions perform = mockMvc.perform(MockMvcRequestBuilders.get("/mgw-card/card-to-map")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());

        log.info("getAccountMockData ended!");
    }
}
