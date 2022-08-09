package com.gisaia.recorder.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.arlas.commons.exceptions.ArlasConfigurationException;
import io.arlas.server.core.app.ArlasBaseConfiguration;

public class EsRecorderConfiguration extends ArlasBaseConfiguration {

    @JsonProperty("es_index")
    public String esIndex;

    public void check() throws ArlasConfigurationException {
        super.check();
        elasticConfiguration.check();
    }
}
