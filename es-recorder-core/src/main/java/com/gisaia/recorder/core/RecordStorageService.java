package com.gisaia.recorder.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gisaia.recorder.util.EsRecorderConfiguration;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.exceptions.NotFoundException;
import io.arlas.server.core.impl.elastic.utils.ElasticClient;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class RecordStorageService {
    private static Logger LOGGER = LoggerFactory.getLogger(RecordStorageService.class);
    private final ElasticClient esClient;

    private final ObjectMapper mapper = new ObjectMapper();
    public RecordStorageService(EsRecorderConfiguration configuration) {
        this.esClient = new ElasticClient(configuration.elasticConfiguration);
    }

    public JSONObject store(ObjectNode record, String userAgent, String referer, String remoteAddr, String esIndex) throws ArlasException {
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
        JSONObject json = new JSONObject();
        json.put("id", id);
        return json;
    }

    public void delete(String field, String value, String esIndex) {
        DeleteByQueryRequest request = new DeleteByQueryRequest(esIndex);
        request.setConflicts("proceed");
        request.setQuery(new TermQueryBuilder(field, value));
        request.setRefresh(true);
        esClient.getClient().deleteByQueryAsync(request, RequestOptions.DEFAULT, new ActionListener<>() {
            @Override
            public void onResponse(BulkByScrollResponse bulkResponse) {
                LOGGER.info("Deleted docs with " + field + "=" + value + " (" + bulkResponse.getDeleted() + " doc(s) deleted)");
            }

            @Override
            public void onFailure(Exception e) {
                LOGGER.warn("Failed deletion of docs with " + field + "=" + value + " because of " + e.getMessage());
            }
        });
    }

    public JsonNode get(String id, String esIndex) throws ArlasException {
        try {
            String record = esClient.getHit(esIndex, id, null, null);
            if (record != null) {
                return mapper.readValue(record, JsonNode.class);
            } else {
                throw new NotFoundException();
            }
        } catch (JsonProcessingException e) {
            throw new ArlasException(e.getMessage());
        }
    }
}
