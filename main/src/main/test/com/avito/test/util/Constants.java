package com.avito.test.util;

public final class Constants {

    public static final String BASE_URL =
            System.getProperty("baseUrl", "https://qa-internship.avito.com");

    public static final String API_V1 = "/api/1";
    public static final String API_V2 = "/api/2";

    public static final int SELLER_ID_MIN = 111111;
    public static final int SELLER_ID_MAX = 999999;

    public static final int REQUEST_TIMEOUT_MS =
            Integer.parseInt(System.getProperty("requestTimeout", "10000"));

    private Constants() {
        throw new UnsupportedOperationException("Utility class");
    }
}
