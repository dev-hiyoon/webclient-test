package com.hiyoon.java8test;

import io.netty.handler.timeout.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/mgw-account")
public class AccountController {
    private final WebClient accountWebClient;

//    private Mono<AccountData> getAccountData(String uri, Integer seq) {
//        return this.accountWebClient.get()
//                .uri(uriBuilder -> uriBuilder.path(uri).queryParam("seq", seq).build())
//                .retrieve()
//                .bodyToMono(AccountData.class)
//                .retryWhen(Retry.backoff(3, Duration.ofMillis(5))
//                        .filter(throwable -> throwable instanceof TimeoutException)
//                        .onRetryExhaustedThrow((x, y) -> new RuntimeException()))
//                .onErrorReturn(new AccountData(0, "Error"));
//    }
//
//    private List getAccountList(List<EndPoint> endPointList) {
//        List<Mono<AccountData>> monoList = new ArrayList<>();
//        for (EndPoint endpoint : endPointList) {
//            monoList.add(getAccountData(endpoint.getEndPoint(), endpoint.getSeq()).subscribeOn(Schedulers.elastic()));
//        }
//
//        return monoList.stream().map(Mono::block).collect(Collectors.toList());
//    }

    private Mono<?> getAccountData(Queue<EndPoint> queue, List results) {
        EndPoint tempData = queue.poll();
        return this.accountWebClient.get()
                .uri(uriBuilder -> uriBuilder.path(tempData.getEndPoint()).queryParam("seq", tempData.getSeq()).build())
                .retrieve()
                .bodyToMono(AccountData.class)
                .retryWhen(Retry.backoff(3, Duration.ofMillis(5))
                        .filter(throwable -> throwable instanceof TimeoutException)
                        .onRetryExhaustedThrow((x, y) -> new RuntimeException()))
                .onErrorReturn(new AccountData(0, "Error"))
                .zipWhen(x -> {
                    results.add(x);
                    if (!queue.isEmpty() && x.getAccountId() != 0) {
                        var mResult = getAccountData(queue, results);
                        return mResult;
                    } else {
                        return Mono.just(x);
                    }
                });
    }

    private Mono getAccountList(List<EndPoint> endPointList, List results) {
        return getAccountData(new LinkedList(endPointList), results);
    }

    @GetMapping
    public Map<String, Object> getAccountMapData() {
        List results = new ArrayList();
        getAccountList(CommonData.getAccountEndPointList(), results).block();
        log.info("######################## 11111111111111");
        Map<String, Object> result = new HashMap<>();
        result.put("data1", results);
        result.put("data2", "data2");
        result.put("globId", "globId##########1234");
        log.info("######################## 222222222222222");
        return result;
    }
}
