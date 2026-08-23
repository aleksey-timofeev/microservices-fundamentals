package com.alextim.resource.configuration;

import lombok.Getter;

@Getter
public class TimeoutProperties {
    private static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 1000;

    private int connectTimeout = DEFAULT_CONNECT_TIMEOUT_MILLIS;
    private int readTimeout = 3; //in seconds
}
