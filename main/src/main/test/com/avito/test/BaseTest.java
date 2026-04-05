package com.avito.test;

import com.avito.test.client.AvitoApiClient;
import com.avito.test.model.Item;
import com.avito.test.model.ItemRequest;
import com.avito.test.util.TestDataProvider;
import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;

public class BaseTest {

    protected AvitoApiClient apiV1;
    protected AvitoApiClient apiV2;
    protected Integer testSellerId;

    @BeforeEach
    void setUp() {
        apiV1 = new AvitoApiClient("1");
        apiV2 = new AvitoApiClient("2");
        testSellerId = TestDataProvider.generateSellerId();
    }

    @Attachment(value = "Request body", type = "application/json")
    protected byte[] attachRequestBody(Object body) {
        if (body == null) return "null".getBytes();
        return body.toString().getBytes();
    }

    @Attachment(value = "Response body", type = "application/json")
    protected byte[] attachResponseBody(Response response) {
        if (response == null) return "null".getBytes();
        return response.getBody().asString().getBytes();
    }

    @Step("Create test item: {0}")
    protected Item createTestItem(ItemRequest payload) {
        Allure.step("Creating item: " + payload.getName());

        Response response = apiV1.createItem(payload);
        attachRequestBody(payload);
        attachResponseBody(response);

        if (response.getStatusCode() != 200) {
            throw new AssertionError("Failed to create item: " +
                    response.getStatusCode() + " - " + response.getBody().asString());
        }

        return response.getBody().as(Item.class);
    }
}