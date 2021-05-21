package com.hiyoon.java8test;

import io.netty.handler.timeout.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/mgw-account")
public class AccountController {
    private final WebClient accountWebClient;

    private Mono<AccountData> getAccountData(String uri, Integer seq) {
        return this.accountWebClient.get()
                .uri(uriBuilder -> uriBuilder.path(uri).queryParam("seq", seq).build())
                .retrieve()
                .bodyToMono(AccountData.class)
                .retryWhen(Retry.backoff(3, Duration.ofMillis(5))
                        .filter(throwable -> throwable instanceof TimeoutException)
                        .onRetryExhaustedThrow((x, y) -> new RuntimeException()))
                .onErrorReturn(new AccountData(0, "Error"));
    }

    private List getAccountList(List<EndPoint> endPointList) {
        List<Mono<AccountData>> monoList = new ArrayList<>();
        for (EndPoint endpoint : endPointList) {
            monoList.add(getAccountData(endpoint.getEndPoint(), endpoint.getSeq()).subscribeOn(Schedulers.elastic()));
        }

        return monoList.stream().map(Mono::block).collect(Collectors.toList());
    }

    @GetMapping
    public Map<String, Object> getAccountMapData() {
        Map<String, Object> result = new HashMap<>();
        result.put("data1", getAccountList(CommonData.getAccountEndPointList()));
        result.put("data2", "data2");
        result.put("globId", "globId##########1234");
        return result;
    }
}
