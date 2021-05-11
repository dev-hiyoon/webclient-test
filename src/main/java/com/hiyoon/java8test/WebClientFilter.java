package com.hiyoon.java8test;

public class WebClientFilter {
    public static boolean is5xxException(Throwable ex) {
        boolean eligible = false;

        if (ex instanceof ServiceException) {
            ServiceException se = (ServiceException) ex;
            eligible = (se.getStatusCode() > 499 && se.getStatusCode() < 600);
        }

        return eligible;
    }

    ;
}
