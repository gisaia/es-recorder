package com.gisaia.recorder.test;

import io.restassured.RestAssured;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import java.util.Optional;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RecorderIT {
    protected static String recorderAppPath;

    static {
        String recorderHost = Optional.ofNullable(System.getenv("ES_RECORDER_HOST")).orElse("localhost");
        int recorderPort = Integer.parseInt(Optional.ofNullable(System.getenv("ES_RECORDER_PORT")).orElse("9997"));
        RestAssured.baseURI = "http://" + recorderHost;
        RestAssured.port = recorderPort;
        RestAssured.basePath = "";
        String recorderPrefix = Optional.ofNullable(System.getenv("ES_RECORDER_PREFIX")).orElse("/es_recorder");
        recorderAppPath = Optional.ofNullable(System.getenv("ES_RECORDER_APP_PATH")).orElse("/");
        if (recorderAppPath.endsWith("/")) recorderAppPath = recorderAppPath.substring(0, recorderAppPath.length() - 1);
        recorderAppPath = recorderAppPath + recorderPrefix;
        if (recorderAppPath.endsWith("//")) recorderAppPath = recorderAppPath.substring(0, recorderAppPath.length() - 1);
        if (!recorderAppPath.endsWith("/")) recorderAppPath = recorderAppPath + "/";
    }

}
