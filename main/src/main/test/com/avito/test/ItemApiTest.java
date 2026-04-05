package com.avito.test;

import io.qameta.allure.SeverityLevel;
import com.avito.test.model.Item;
import com.avito.test.model.ItemRequest;
import com.avito.test.model.Statistics;
import com.avito.test.util.TestDataProvider;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Item API")
@Story("CRUD операции с объявлениями")
class ItemApiTest extends BaseTest {

    @Test
    @Tag("positive")
    @DisplayName("TC-001: Успешное создание объявления")
    @Description("Создание объявления с валидными данными возвращает 200 и объект с ID")
    @Severity(SeverityLevel.CRITICAL)
    void testCreateItemSuccess() {
        ItemRequest payload = TestDataProvider.validItemRequest(testSellerId);

        Response response = createItemWithLogging(payload);
        verifyItemCreated(response, payload);
    }

    @Test
    @Tag("positive")
    @DisplayName("TC-002: Получение созданного объявления по ID")
    @Severity(SeverityLevel.CRITICAL)
    void testGetItemByIdSuccess() {
        ItemRequest createPayload = TestDataProvider.validItemRequest(testSellerId);
        Item createdItem = createItemWithLogging(createPayload).getBody().as(Item.class);

        Response response = getItemByIdWithLogging(createdItem.getId());
        verifyItemRetrieved(response, createPayload);
    }

    @Test
    @Tag("positive")
    @DisplayName("TC-003: Получение списка объявлений продавца")
    @Severity(SeverityLevel.NORMAL)
    void testGetItemsBySeller() {
        Item item1 = createItemWithLogging(TestDataProvider.validItemRequest(testSellerId)).getBody().as(Item.class);
        Item item2 = createItemWithLogging(TestDataProvider.validItemRequest(testSellerId)).getBody().as(Item.class);

        Response response = getItemsBySellerWithLogging(testSellerId);
        verifyItemsList(response, List.of(item1, item2));
    }


    @Test
    @Tag("negative")
    @DisplayName("TC-103: Получение несуществующего объявления")
    @Severity(SeverityLevel.NORMAL)
    void testGetItemNotFound() {
        String fakeId = "non-existent-id-" + System.currentTimeMillis();

        Response response = getItemByIdWithLogging(fakeId);
        verifyNotFound(response);
    }

    @ParameterizedTest(name = "Invalid payload: {0}")
    @Tag("negative")
    @MethodSource("com.avito.test.util.TestDataProvider#invalidItemRequests")
    @DisplayName("TC-101/102: Создание с невалидными данными")
    @Severity(SeverityLevel.NORMAL)
    void testCreateItemInvalidPayload(ItemRequest invalidPayload) {
        Response response = createItemWithLogging(invalidPayload);
        verifyBadRequest(response);
    }


    @Test
    @Tag("corner")
    @DisplayName("TC-005: Проверка идемпотентности создания")
    @Severity(SeverityLevel.MINOR)
    void testCreateItemIdempotencyCheck() {
        ItemRequest identicalPayload = ItemRequest.builder()
                .sellerId(testSellerId)
                .name("IdempotencyTest-" + System.currentTimeMillis())
                .price(999)
                .statistics(new Statistics())
                .build();

        Item result1 = createItemWithLogging(identicalPayload).getBody().as(Item.class);
        Item result2 = createItemWithLogging(identicalPayload).getBody().as(Item.class);
        Item result3 = createItemWithLogging(identicalPayload).getBody().as(Item.class);

        verifyIdempotency(result1, result2, result3);
    }


    @Step("Create item: {0}")
    private Response createItemWithLogging(ItemRequest payload) {
        attachRequestBody(payload);

        Response response = apiV1.createItem(payload);
        attachResponseBody(response);

        Allure.parameter("sellerId", String.valueOf(payload.getSellerId()));
        Allure.parameter("name", payload.getName());
        Allure.parameter("price", String.valueOf(payload.getPrice()));

        return response;
    }

    @Step("Get item by ID: {0}")
    private Response getItemByIdWithLogging(String itemId) {
        Response response = apiV1.getItemById(itemId);
        attachResponseBody(response);

        Allure.parameter("itemId", itemId);

        return response;
    }

    @Step("Get items by seller ID: {0}")
    private Response getItemsBySellerWithLogging(Integer sellerId) {
        Response response = apiV1.getItemsBySeller(sellerId);
        attachResponseBody(response);

        Allure.parameter("sellerId", String.valueOf(sellerId));

        return response;
    }


    @Step("Verify item created successfully")
    private void verifyItemCreated(Response response, ItemRequest expected) {
        assertThat(response.getStatusCode())
                .as("Status code should be 200")
                .isEqualTo(200);

        Item created = response.getBody().as(Item.class);

        assertThat(created.getId())
                .as("Response should contain generated ID")
                .isNotNull()
                .isNotEmpty();

        assertThat(created.getSellerId())
                .as("sellerId should match request")
                .isEqualTo(expected.getSellerId());

        assertThat(created.getName())
                .as("name should match request")
                .isEqualTo(expected.getName());

        assertThat(created.getPrice())
                .as("price should match request")
                .isEqualTo(expected.getPrice());

        assertThat(created.getCreatedAt())
                .as("Response should contain createdAt timestamp")
                .isNotNull();
    }

    @Step("Verify item retrieved matches original")
    private void verifyItemRetrieved(Response response, ItemRequest expected) {
        assertThat(response.getStatusCode()).isEqualTo(200);

        Item retrieved = response.getBody().as(Item.class);
        if (retrieved == null) {
            List<Item> items = response.jsonPath().getList(".", Item.class);
            retrieved = items.isEmpty() ? null : items.get(0);
        }

        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getName()).isEqualTo(expected.getName());
        assertThat(retrieved.getPrice()).isEqualTo(expected.getPrice());
    }

    @Step("Verify items list contains all created items")
    private void verifyItemsList(Response response, List<Item> expectedItems) {
        assertThat(response.getStatusCode()).isEqualTo(200);

        List<Item> items = response.jsonPath().getList(".", Item.class);
        assertThat(items).isNotEmpty();

        List<String> expectedIds = expectedItems.stream()
                .map(Item::getId)
                .toList();

        assertThat(items)
                .extracting(Item::getId)
                .containsAll(expectedIds);
    }

    @Step("Verify 404 Not Found response")
    private void verifyNotFound(Response response) {
        assertThat(response.getStatusCode())
                .as("Non-existent ID should return 404")
                .isEqualTo(404);
    }

    @Step("Verify 400 Bad Request for invalid payload")
    private void verifyBadRequest(Response response) {
        assertThat(response.getStatusCode())
                .as("Invalid payload should return 400")
                .isIn(400, 422);
    }

    @Step("Verify idempotency: each request creates unique item")
    private void verifyIdempotency(Item result1, Item result2, Item result3) {
        assertThat(result1.getId())
                .as("Each request should create unique item")
                .isNotEqualTo(result2.getId());

        assertThat(result2.getId())
                .isNotEqualTo(result3.getId());

        assertThat(result1.getId())
                .isNotEqualTo(result3.getId());

        assertThat(result1.getName()).isEqualTo(result2.getName());
        assertThat(result1.getPrice()).isEqualTo(result2.getPrice());
    }
}