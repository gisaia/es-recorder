package com.gisaia.recorder.rest.service;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import io.arlas.commons.rest.response.Error;
import io.swagger.annotations.*;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Path("/records")
@Api(value = "/records")
@SwaggerDefinition(
        info = @Info(contact = @Contact(email = "contact@gisaia.com", name = "Gisaia", url = "http://www.gisaia.com/"),
                title = "ES RECORDER API",
                description = "RECORDER REST services",
                license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0.html"),
                version = "API_VERSION"),
        schemes = { SwaggerDefinition.Scheme.HTTP, SwaggerDefinition.Scheme.HTTPS }
)
public class RecorderRestService {
    public static final String UTF8JSON = MediaType.APPLICATION_JSON + ";charset=utf-8";

    public RecorderRestService() {
    }

    @Timed
    @Path("store")
    @POST
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(authorizations = @Authorization("JWT"),
            value = "Store a new record in elasticsearch",
            produces = UTF8JSON,
            consumes = UTF8JSON
    )
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = String.class),
            @ApiResponse(code = 404, message = "Index not found.", response = Error.class),
            @ApiResponse(code = 500, message = "Application error.", response = Error.class)})

    public Response store(
            @Context UriInfo uriInfo,
            @Context HttpHeaders headers,

            @ApiParam(name = "record")
            @Valid JsonNode record
    ) {

        return Response.ok(uriInfo.getRequestUriBuilder().build())
                .entity("todo")
                .type("application/json")
                .build();
    }

}
