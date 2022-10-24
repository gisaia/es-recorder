package com.gisaia.recorder.test;

import io.arlas.commons.exceptions.ArlasException;
import io.arlas.server.core.app.ElasticConfiguration;
import io.arlas.server.core.impl.elastic.utils.ElasticTool;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.sniff.Sniffer;
import org.junit.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWithIgnoringCase;
import io.arlas.server.core.impl.elastic.utils.ElasticClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class RecorderIT {
    protected static String recorderAppPath;
    public static ElasticClient client;
    public final static String RECORDS_INDEX_NAME="records";

    static {
        HttpHost[] nodes = ElasticConfiguration.getElasticNodes(Optional.ofNullable(System.getenv("ES_RECORDER_ELASTIC_NODES")).orElse("localhost:9200"), false);
        ImmutablePair<RestHighLevelClient, Sniffer> pair = ElasticTool.getRestHighLevelClient(nodes,false, null, true, true);
        client = new ElasticClient(pair.getLeft(), pair.getRight());
        String recorderHost = Optional.ofNullable(System.getenv("ES_RECORDER_HOST")).orElse("localhost");
        int recorderPort = Integer.parseInt(Optional.ofNullable(System.getenv("ES_RECORDER_PORT")).orElse("9997"));
        RestAssured.baseURI = "http://" + recorderHost;
        RestAssured.port = recorderPort;
        RestAssured.basePath = "";
        String recorderPrefix = Optional.ofNullable(System.getenv("ES_RECORDER_PREFIX")).orElse("/es_recorder");
        recorderAppPath = Optional.ofNullable(System.getenv("ES_RECORDER_APP_PATH")).orElse("/");
        if (recorderAppPath.endsWith("/")) recorderAppPath = recorderAppPath.substring(0, recorderAppPath.length() - 1);
        recorderAppPath = recorderAppPath + recorderPrefix;
        if (recorderAppPath.endsWith("//"))
            recorderAppPath = recorderAppPath.substring(0, recorderAppPath.length() - 1);
        if (!recorderAppPath.endsWith("/")) recorderAppPath = recorderAppPath + "/";
    }

    @BeforeClass
    public static  void before() {
        try {
            createIndex(RECORDS_INDEX_NAME,"mapping.json");
        } catch (ArlasException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static  void afterClass() throws IOException {
        clearDataSet();
    }

    @Test
    public void test01StoreRecord() {
        String id = storeRecord().then().statusCode(201)
                .extract().body().jsonPath().get("id");
        getRecord(id).then().statusCode(200)
                .body("download.email_user", equalTo("foo@bar.com"))
                .body("client.browser", startsWithIgnoringCase("Apache-HttpClient"));
        deleteRecord().then().statusCode(202);
    }

    @Test
    public void test02DeleteRecord() throws InterruptedException {
        String id = storeRecord().then().statusCode(201)
                .extract().body().jsonPath().get("id");
        getRecord(id).then().statusCode(200)
                .body("download.email_user", equalTo("foo@bar.com"))
                .body("client.browser", startsWithIgnoringCase("Apache-HttpClient"));
        deleteRecord().then().statusCode(202);
        TimeUnit.SECONDS.sleep(5);
        getRecord(id).then().statusCode(404);
    }

    @Test
    public void test03DeleteManyRecords() throws InterruptedException {
        String id = storeRecord().then().statusCode(201)
                .extract().body().jsonPath().get("id");
        String id2 = storeRecord().then().statusCode(201)
                .extract().body().jsonPath().get("id");
        TimeUnit.SECONDS.sleep(5);
        getRecord(id).then().statusCode(200)
                .body("download.email_user", equalTo("foo@bar.com"))
                .body("client.browser", startsWithIgnoringCase("Apache-HttpClient"));
        getRecord(id2).then().statusCode(200)
                .body("download.email_user", equalTo("foo@bar.com"))
                .body("client.browser", startsWithIgnoringCase("Apache-HttpClient"));
        deleteRecord().then().statusCode(202);
        TimeUnit.SECONDS.sleep(5);
        getRecord(id).then().statusCode(404);
        getRecord(id2).then().statusCode(404);
    }

    protected Response storeRecord() {
        return given()
                .contentType("application/json")
                .body("""
                        {
                        	"product": {
                        		"id":"1",
                        		"date":"2022-08-09",
                        		"collection":"SPOT6",
                        		"source": "SPOT",
                        		"geometrie":"POINT(-71.126 40.967)",
                        		"centroid":"POINT(-71.126 40.967)"
                        	},
                        	"download": {
                        		"date":"2022-08-09T14:00:00Z",
                        		"email_user":"foo@bar.com",
                        		"status":"OK"
                        	},
                        	"metadata": {
                                        "foo": "bar"
                        	}
                        }                                                                                  
                        """)
                .post(recorderAppPath.concat("records/").concat(RECORDS_INDEX_NAME));
    }

    protected Response deleteRecord() {
        return given()
                .contentType("application/json")
                .queryParam("field", "download.email_user")
                .queryParam("value", "foo@bar.com")
                .delete(recorderAppPath.concat("records/").concat(RECORDS_INDEX_NAME));
    }

    protected Response getRecord(String id) {
        return given()
                .pathParam("id", id)
                .contentType("application/json")
                .get(recorderAppPath.concat("records/").concat(RECORDS_INDEX_NAME).concat("/{id}"));
    }

    private static void createIndex(String indexName, String mappingFileName) throws ArlasException, IOException {
        String mapping = IOUtils.toString(new InputStreamReader(RecorderIT.class.getClassLoader().getResourceAsStream(mappingFileName)));
        try {
            client.deleteIndex(indexName);
        } catch (Exception e) {
        }
        client.createIndex(indexName, mapping);
    }

    private static void clearDataSet() {
        try {
            client.deleteIndex(RECORDS_INDEX_NAME);
        } catch (ArlasException e) {
            e.printStackTrace();
        }
    }
}
