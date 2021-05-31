package com.hiyoon.java8test;

import io.netty.channel.ChannelOption;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.JettyClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.net.URI;

import static com.hiyoon.java8test.RequestLogEnhancer.enhance;

@Slf4j
@Configuration
public class WebClientUtils {

    @Value(value = "${card.service.url}")
    private String cardServiceUrl;

    private ClientHttpConnector getConnector() {
        HttpClient httpClient = HttpClient.create()
                .tcpConfiguration(client ->
                        client.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                                .doOnConnected(conn -> conn
                                        .addHandlerLast(new ReadTimeoutHandler(3))
                                        .addHandlerLast(new WriteTimeoutHandler(3))));
        return new ReactorClientHttpConnector(httpClient.wiretap(true));
    }

    @Bean
    public WebClient accountWebClient() {
        return WebClient.builder()
                .baseUrl(cardServiceUrl)
                .clientConnector(getConnector())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    public WebClient cardWebClient() {
        return WebClient.builder()
                .baseUrl(cardServiceUrl)
                .clientConnector(getConnector())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    public WebClient logWebClient() {
        SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
        org.eclipse.jetty.client.HttpClient httpClient = new org.eclipse.jetty.client.HttpClient(sslContextFactory) {
            @Override
            public Request newRequest(URI uri) {
                Request request = super.newRequest(uri);
                return enhance(request);
            }
        };

        return WebClient.builder()
                .baseUrl(cardServiceUrl)
                .clientConnector(new JettyClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    public WebClient logFilterWebClient() {
        return WebClient.builder()
                .baseUrl(cardServiceUrl)
                .filters(exchangeFilterFunctions -> {
                    exchangeFilterFunctions.addAll(LogFilters.prepareFilters());
                })
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

//    @Bean
//    public WebClient logNettyWebClient() {
//        reactor.netty.http.client.HttpClient httpClient = HttpClient.create()
//                .wiretap("reactor.netty.http.client.HttpClient", LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL);
//        return WebClient.builder()
//                .baseUrl(cardServiceUrl)
//                .clientConnector(new ReactorClientHttpConnector(httpClient))
//                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//                .build();
//    }
}
