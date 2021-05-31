package com.hiyoon.java8test;

import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;

@Slf4j
public class RequestLogEnhancer {

    public static Request enhance(Request request) {
        StringBuilder sb = new StringBuilder();
        request.onRequestBegin(theRequest -> sb
                .append("Request ")
                .append(theRequest.getMethod())
                .append(" ")
                .append(theRequest.getURI())
                .append("\n"));
        request.onRequestHeaders(theRequest -> {
            for (HttpField header : theRequest.getHeaders())
                sb.append(header).append("\n");
        });
        request.onRequestContent((theRequest, content) -> {
            sb.append(toString(content, getCharset(theRequest.getHeaders())));
        });
        request.onRequestSuccess(theRequest -> {
            log.info("############ onRequestSuccess. request: {}", sb.toString());
            sb.delete(0, sb.length());
        });
        sb.append("\n");
        request.onResponseBegin(theResponse -> {
            sb.append("Response \n")
                    .append(theResponse.getVersion())
                    .append(" ")
                    .append(theResponse.getStatus());
            if (theResponse.getReason() != null) {
                sb.append(" ").append(theResponse.getReason());
            }
            sb.append("\n");
        });
        request.onResponseHeaders(theResponse -> {
            for (HttpField header : theResponse.getHeaders())
                sb.append(header).append("\n");
        });
        request.onResponseContent((theResponse, content) -> {
            sb.append(toString(content, getCharset(theResponse.getHeaders())));
        });
        request.onResponseSuccess(theResponse -> {
            log.info("############ onResponseSuccess. response: {}", sb.toString());
        });
        return request;
    }

    private static String toString(ByteBuffer buffer, Charset charset) {
        byte[] bytes;
        if (buffer.hasArray()) {
            bytes = new byte[buffer.capacity()];
            System.arraycopy(buffer.array(), 0, bytes, 0, buffer.capacity());
        } else {
            bytes = new byte[buffer.remaining()];
            buffer.get(bytes, 0, bytes.length);
        }
        return new String(bytes, charset);
    }

    private static Charset getCharset(HttpFields headers) {
        String contentType = headers.get(HttpHeader.CONTENT_TYPE);
        if (contentType != null) {
            String[] tokens = contentType
                    .toLowerCase(Locale.US)
                    .split("charset=");
            if (tokens.length == 2) {
                String encoding = tokens[1].replaceAll("[;\"]", "");
                return Charset.forName(encoding);
            }
        }
        return StandardCharsets.UTF_8;
    }
}
