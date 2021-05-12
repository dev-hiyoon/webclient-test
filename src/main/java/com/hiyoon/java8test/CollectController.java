package com.hiyoon.java8test;

import io.netty.handler.timeout.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/collect")
public class CollectController {

    private static List<EndPoint> cardEndPointList;
    private static List<EndPoint> accountEndPointList;
    private final WebClient accountWebClient;
    private final WebClient cardWebClient;

    @PostConstruct
    public static void setData() {
        // account
        accountEndPointList = new ArrayList<>();
        accountEndPointList.add(new EndPoint("/account/deposit/basic", "/account", 0));
        accountEndPointList.add(new EndPoint("/account/deposit/detail", "/account", 0));
        accountEndPointList.add(new EndPoint("/account/deposit/transactions", "/account", 0));

        // card
        cardEndPointList = new ArrayList<>();
        cardEndPointList.add(new EndPoint("/card", "/card", 1));
        cardEndPointList.add(new EndPoint("/card/approval-domestic", "/card", 2));
        cardEndPointList.add(new EndPoint("/card/approval-overseas", "/card", 3));
    }

    private boolean isSeqYn(List<EndPoint> list) {
        boolean result = false;
        for (EndPoint endpoint : list) {
            if (endpoint.getSeq() > 1) {
                result = true;
                break;
            }
        }

        return result;
    }

    public Flux<AccountData> getAccountData(List<EndPoint> list) {
        return null;
    }

    private Mono<CardData> getCardData(String uri, Integer seq) {
        return this.cardWebClient.get()
                .uri(uriBuilder -> uriBuilder.path(uri).queryParam("seq", seq).build())
                .retrieve()
                .bodyToMono(CardData.class)
                .retryWhen(
                        Retry.backoff(3, Duration.ofMillis(5))
                                .filter(throwable -> throwable instanceof TimeoutException)
                                .onRetryExhaustedThrow((x, y) -> {
                                    return new RuntimeException();
                                })

                )
                .onErrorReturn(new CardData(0, "Error"));
    }

    private Mono<?> zipMonos(List<EndPoint> endPointList) {
        List<Mono<CardData>> monoList = new ArrayList<>();
        for (EndPoint endpoint : endPointList) {
            monoList.add(getCardData(endpoint.getEndPoint(), endpoint.getSeq()));
        }

        var result = Mono.zip(monoList, x -> {
            for (Object obj : x) {
                log.info("###################### obj: {}", obj.toString());
            }
            return x;
        });

//        var result = Mono.zip(monoList, Arrays::asList).flatMapIterable(x -> x)
//                .doOnEach(x -> {
//                    log.info("################ " + x.toString());
//                })
//                .doOnComplete(() -> log.info("################ done"));
//
//        var result = Mono.zip(monoList, Arrays::asList)
//                .doOnEach(x -> {
//                    log.info("################ " + x.toString());
//                });

        return result;

//        return Mono.zip(monoList, Arrays::asList);
    }

    @GetMapping(value = "/account")
    public String getAccount() {
        return "";
    }

    @GetMapping(value = "/card")
    public Mono<?> getCard() {
        return zipMonos(cardEndPointList);
    }
}
