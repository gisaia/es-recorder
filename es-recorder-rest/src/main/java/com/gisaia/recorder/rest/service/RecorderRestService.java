package com.gisaia.recorder.rest.service;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gisaia.recorder.core.RecordStorageService;
import com.gisaia.recorder.util.EsRecorderConfiguration;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.rest.response.Error;
import io.swagger.annotations.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.Optional;

import static com.google.common.net.HttpHeaders.X_FORWARDED_FOR;

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

    private final RecordStorageService service;

    public RecorderRestService(EsRecorderConfiguration configuration) {
        this.service = new RecordStorageService(configuration);
    }

    @Timed
    @Path("/{index}")
    @POST
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "Store a new record in elasticsearch", produces = UTF8JSON, consumes = UTF8JSON)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Successful operation", response = String.class),
            @ApiResponse(code = 404, message = "Index not found.", response = Error.class),
            @ApiResponse(code = 500, message = "Application error.", response = Error.class)})

    public Response store(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest httpServletRequest,
            @Context HttpHeaders headers,

            @ApiParam(name = "index",
                    value = "index",
                    required = true)
            @PathParam(value = "index") String index,

            @ApiParam(name = "record")
            @Valid ObjectNode record
    ) throws ArlasException {
        String ip = Optional.ofNullable(headers.getHeaderString(X_FORWARDED_FOR))
                .orElseGet(() -> httpServletRequest.getRemoteAddr())
                .split(",")[0].trim();
        return Response.created(uriInfo.getRequestUriBuilder().build())
                .entity(service.store(record,
                        headers.getHeaderString(HttpHeaders.USER_AGENT),
                        headers.getHeaderString("Referer"),
                        ip, index))
                .type(UTF8JSON)
                .build();
    }

    @Timed
    @Path("/{index}/{id}")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "Get a record from elasticsearch", produces = UTF8JSON, consumes = UTF8JSON)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = JsonNode.class),
            @ApiResponse(code = 404, message = "Index not found.", response = Error.class),
            @ApiResponse(code = 500, message = "Application error.", response = Error.class)})

    public Response get(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest httpServletRequest,
            @Context HttpHeaders headers,
            @ApiParam(name = "index",
                    value = "index",
                    required = true)
            @PathParam(value = "index") String index,
            @ApiParam(name = "id", required = true)
            @PathParam(value = "id") String id
    ) throws ArlasException {
        return Response.ok(uriInfo.getRequestUriBuilder().build())
                .entity(service.get(id,index))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    @Timed
    @DELETE
    @Path("/{index}")
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "Delete records from elasticsearch", produces = UTF8JSON, consumes = UTF8JSON)
    @ApiResponses(value = {@ApiResponse(code = 202, message = "Successful operation", response = String.class),
            @ApiResponse(code = 404, message = "Index not found.", response = Error.class),
            @ApiResponse(code = 500, message = "Application error.", response = Error.class)})

    public Response delete(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest httpServletRequest,
            @Context HttpHeaders headers,
            @ApiParam(name = "index",
                    value = "index",
                    required = true)
            @PathParam(value = "index") String index,

            @ApiParam(name = "field")
            @QueryParam(value = "field") String field,

            @ApiParam(name = "value")
            @QueryParam(value = "value") String value


    ) {
        service.delete(field, value, index);
        return Response.accepted(uriInfo.getRequestUriBuilder().build())
                .entity("request executing in background")
                .type(MediaType.TEXT_PLAIN)
                .build();
    }
}
