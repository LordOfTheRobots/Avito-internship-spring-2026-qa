package com.avito.test.client;

import com.avito.test.model.ItemRequest;
import com.avito.test.util.Constants;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public class AvitoApiClient {

    private final String apiVersion;
    private final RequestSpecification baseSpec;

    static {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    public AvitoApiClient(String apiVersion) {
        this.apiVersion = apiVersion;

        HttpClientConfig httpClientConfig = HttpClientConfig.httpClientConfig()
                .setParam("http.connection.timeout", Constants.REQUEST_TIMEOUT_MS)
                .setParam("http.socket.timeout", Constants.REQUEST_TIMEOUT_MS);

        this.baseSpec = given()
                .config(RestAssuredConfig.config().httpClient(httpClientConfig))
                .contentType("application/json")
                .accept("application/json")
                .basePath("/api/" + apiVersion)
                .baseUri(Constants.BASE_URL);
    }

    private RequestSpecification request() {
        return baseSpec;
    }

    public Response createItem(ItemRequest payload) {
        return request()
                .body(payload)
                .log().ifValidationFails()
                .when()
                .post("/item")
                .then()
                .log().ifValidationFails()
                .extract()
                .response();
    }

    public Response getItemById(String itemId) {
        return request()
                .when()
                .get("/item/" + itemId)
                .then()
                .extract()
                .response();
    }

    public Response getItemsBySeller(Integer sellerId) {
        return request()
                .when()
                .get("/" + sellerId + "/item")
                .then()
                .extract()
                .response();
    }

    public Response getStatistic(String itemId) {
        return request()
                .when()
                .get("/statistic/" + itemId)
                .then()
                .extract()
                .response();
    }

    public Response getStatisticV2(String itemId) {
        return given()
                .config(RestAssuredConfig.config().httpClient(
                        HttpClientConfig.httpClientConfig()
                                .setParam("http.connection.timeout", Constants.REQUEST_TIMEOUT_MS)
                                .setParam("http.socket.timeout", Constants.REQUEST_TIMEOUT_MS)))
                .contentType("application/json")
                .accept("application/json")
                .baseUri(Constants.BASE_URL)
                .basePath("/api/2")
                .when()
                .get("/statistic/" + itemId)
                .then()
                .extract()
                .response();
    }

    public Response deleteItem(String itemId) {
        return given()
                .config(RestAssuredConfig.config().httpClient(
                        HttpClientConfig.httpClientConfig()
                                .setParam("http.connection.timeout", Constants.REQUEST_TIMEOUT_MS)
                                .setParam("http.socket.timeout", Constants.REQUEST_TIMEOUT_MS)))
                .contentType("application/json")
                .accept("application/json")
                .baseUri(Constants.BASE_URL)
                .basePath("/api/2")
                .when()
                .delete("/item/" + itemId)
                .then()
                .extract()
                .response();
    }
}
