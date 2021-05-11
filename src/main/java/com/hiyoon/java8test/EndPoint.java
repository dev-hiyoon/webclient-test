package com.hiyoon.java8test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class EndPoint {
    private String endPoint;
    private String collectServerurl;
    private Integer seq;
}
