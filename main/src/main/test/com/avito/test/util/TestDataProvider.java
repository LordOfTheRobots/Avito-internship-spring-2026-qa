package com.avito.test.util;

import com.avito.test.model.ItemRequest;
import com.avito.test.model.Statistics;

import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;

public class TestDataProvider {

    private static final Random RANDOM = new Random();

    public static int generateSellerId() {
        return RANDOM.nextInt(
                Constants.SELLER_ID_MAX - Constants.SELLER_ID_MIN + 1)
                + Constants.SELLER_ID_MIN;
    }

    public static String generateUniqueName(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    public static ItemRequest validItemRequest(Integer sellerId) {
        return ItemRequest.builder()
                .sellerId(sellerId != null ? sellerId : generateSellerId())
                .name(generateUniqueName("TestItem"))
                .price(RANDOM.nextInt(99999) + 1)
                .statistics(Statistics.builder()
                        .likes(0)
                        .viewCount(0)
                        .contacts(0)
                        .build())
                .build();
    }

    public static Stream<ItemRequest> invalidItemRequests() {
        return Stream.of(
                ItemRequest.builder()
                        .name("Test")
                        .price(100)
                        .statistics(new Statistics())
                        .build(),
                ItemRequest.builder()
                        .sellerId(generateSellerId())
                        .price(100)
                        .statistics(new Statistics())
                        .build(),
                ItemRequest.builder()
                        .sellerId(generateSellerId())
                        .name("Test")
                        .statistics(new Statistics())
                        .build(),
                ItemRequest.builder()
                        .sellerId(generateSellerId())
                        .name("")
                        .price(100)
                        .statistics(new Statistics())
                        .build(),
                ItemRequest.builder()
                        .sellerId(generateSellerId())
                        .name("Test")
                        .price(-1)
                        .statistics(new Statistics())
                        .build()
        );
    }
}