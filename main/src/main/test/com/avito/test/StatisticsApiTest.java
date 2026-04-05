package com.avito.test;

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

@Feature("Statistic API")
class StatisticsApiTest extends BaseTest {

    @Story("Get Statistic")
    @Tag("positive")
    @Test
    @DisplayName("TC-004: Получение статистики по существующему объявлению")
    void testGetStatisticSuccess() {
        ItemRequest createPayload = TestDataProvider.validItemRequest(testSellerId);
        Item created = createTestItem(createPayload);

        Response response = apiV1.getStatistic(created.getId());
        attachResponseBody(response);

        assertThat(response.getStatusCode()).isEqualTo(200);

        List<Statistics> stats = response.getBody().jsonPath().getList(".", Statistics.class);

        if (!stats.isEmpty()) {
            Statistics stat = stats.get(0);
            assertThat(stat.getLikes()).isNotNull().isGreaterThanOrEqualTo(0);
            assertThat(stat.getViewCount()).isNotNull().isGreaterThanOrEqualTo(0);
            assertThat(stat.getContacts()).isNotNull().isGreaterThanOrEqualTo(0);
        }
    }

    @Story("Get Statistic")
    @Tag("negative")
    @Test
    @DisplayName("Статистика для несуществующего ID")
    void testGetStatisticNotFound() {
        Response response = apiV1.getStatistic("fake-id-not-exists");
        attachResponseBody(response);

        assertThat(response.getStatusCode())
                .as("Should return 404 or 200 with empty array")
                .isIn(200, 404);
    }

    @Story("API Versions")
    @Tag("corner")
    @Test
    @DisplayName("Сравнение ответов statistic v1 и v2")
    void testStatisticVersionsConsistency() {
        Item created = createTestItem(TestDataProvider.validItemRequest(testSellerId));

        Response v1 = apiV1.getStatistic(created.getId());
        Response v2 = apiV2.getStatisticV2(created.getId());

        assertThat(v1.getStatusCode())
                .as("API versions should return same status code")
                .isEqualTo(v2.getStatusCode());
    }
}
