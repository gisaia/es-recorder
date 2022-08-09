package com.gisaia.recorder.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gisaia.recorder.util.EsRecorderConfiguration;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.server.core.impl.elastic.utils.ElasticClient;

import java.util.UUID;

public class RecordStorageService {
    private final String esIndex;
    private final ElasticClient esClient;

    private final ObjectMapper mapper = new ObjectMapper();
    public RecordStorageService(EsRecorderConfiguration configuration) {
        this.esIndex = configuration.esIndex;
        this.esClient = new ElasticClient(configuration.elasticConfiguration);
    }

    public String store(ObjectNode record, String userAgent, String referer, String remoteAddr) throws ArlasException {
        String id = UUID.randomUUID().toString();
        record.put("id", id);
        ObjectNode client = record.putObject("client");
        client.put("hostname", referer)
                .put("browser", userAgent)
                .put("ip", remoteAddr);

        try {
            esClient.index(esIndex, id, mapper.writeValueAsString(record));
        } catch (JsonProcessingException e) {
            throw new ArlasException(e.getMessage());
        }
        return id;
    }
}
