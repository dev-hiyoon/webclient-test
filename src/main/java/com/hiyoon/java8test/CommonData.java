package com.hiyoon.java8test;

import java.util.ArrayList;
import java.util.List;


public class CommonData {
    private static List<EndPoint> cardEndPointList;
    private static List<EndPoint> accountEndPointList;

    public static List<EndPoint> getCardEndPointList() {
        if (cardEndPointList == null) {
            cardEndPointList = new ArrayList<>();
            cardEndPointList.add(new EndPoint("/card", "/card", 0));
            cardEndPointList.add(new EndPoint("/card/approval-domestic", "/card", 0));
            cardEndPointList.add(new EndPoint("/card/approval-overseas", "/card", 0));
        }

        return cardEndPointList;
    }

    public static List<EndPoint> getAccountEndPointList() {
        if (accountEndPointList == null) {
            accountEndPointList = new ArrayList<>();
            accountEndPointList.add(new EndPoint("/account/deposit/basic", "/account", 1));
            accountEndPointList.add(new EndPoint("/account/deposit/detail", "/account", 2));
            accountEndPointList.add(new EndPoint("/account/deposit/transactions", "/account", 3));
        }

        return accountEndPointList;
    }
}
