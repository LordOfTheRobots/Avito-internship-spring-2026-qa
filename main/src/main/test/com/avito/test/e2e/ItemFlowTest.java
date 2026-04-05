package com.avito.test.e2e;

import com.avito.test.BaseTest;
import com.avito.test.model.Item;
import com.avito.test.model.ItemRequest;
import com.avito.test.model.Statistics;
import com.avito.test.util.TestDataProvider;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("E2E Flows")
class ItemFlowTest extends BaseTest {

    @Story("Full Item Lifecycle")
    @Tag("e2e")
    @Tag("positive")
    @Test
    @DisplayName("E2E-001: Полный цикл работы с объявлением")
    @Description("Создание -> Чтение по ID -> Список продавца -> Статистика")
    void testFullItemLifecycle() {
        Allure.step("1. Создать объявление", step -> {
            ItemRequest payload = ItemRequest.builder()
                    .sellerId(testSellerId)
                    .name("E2E-Product-" + System.currentTimeMillis())
                    .price(5000)
                    .statistics(Statistics.builder()
                            .likes(10)
                            .viewCount(100)
                            .contacts(5)
                            .build())
                    .build();

            Response createResp = apiV1.createItem(payload);
            assertThat(createResp.getStatusCode()).isEqualTo(200);

            Item created = createResp.as(Item.class);
            step.parameter("created_id", created.getId());

            Allure.getLifecycle().updateTestCase(tc ->
                    tc.setTestCaseId(created.getId()));
        });

        Item created = apiV1.createItem(ItemRequest.builder()
                .sellerId(testSellerId)
                .name("E2E-Product")
                .price(5000)
                .statistics(new Statistics())
                .build()).as(Item.class);

        Allure.step("2. Получить объявление по ID", step -> {
            Response getResp = apiV1.getItemById(created.getId());
            assertThat(getResp.getStatusCode()).isEqualTo(200);

            Item retrieved = getResp.as(Item.class);
            if (retrieved == null) {
                List<Item> list = getResp.jsonPath().getList(".", Item.class);
                retrieved = list.isEmpty() ? null : list.get(0);
            }

            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getName()).isEqualTo("E2E-Product");
            step.parameter("retrieved_name", retrieved.getName());
        });

        Allure.step("3. Получить все объявления продавца", step -> {
            Response listResp = apiV1.getItemsBySeller(testSellerId);
            assertThat(listResp.getStatusCode()).isEqualTo(200);

            List<Item> items = listResp.jsonPath().getList(".", Item.class);
            assertThat(items)
                    .extracting(Item::getId)
                    .contains(created.getId());
            step.parameter("items_count", String.valueOf(items.size()));
        });

        Allure.step("4. Получить статистику", step -> {
            Response statResp = apiV1.getStatistic(created.getId());
            assertThat(statResp.getStatusCode()).isEqualTo(200);
            step.parameter("stat_status", String.valueOf(statResp.getStatusCode()));
        });
    }

    @Story("Create and Delete")
    @Tag("e2e")
    @Test
    @DisplayName("E2E-002: Создание через v1 и удаление через v2")
    void testCreateAndDelete() {
        ItemRequest payload = TestDataProvider.validItemRequest(testSellerId);
        Item created = apiV1.createItem(payload).as(Item.class);
        String itemId = created.getId();

        assertThat(itemId).isNotNull();

        Allure.step("DELETE /api/2/item/" + itemId, step -> {
            Response deleteResp = apiV2.deleteItem(itemId);
            assertThat(deleteResp.getStatusCode())
                    .as("Delete should return 200")
                    .isEqualTo(200);
            step.parameter("delete_status", String.valueOf(deleteResp.getStatusCode()));
        });

        Allure.step("Verify item is deleted", step -> {
            Response getResp = apiV1.getItemById(itemId);
            assertThat(getResp.getStatusCode())
                    .as("Deleted item should return 404")
                    .isEqualTo(404);
        });
    }
}
