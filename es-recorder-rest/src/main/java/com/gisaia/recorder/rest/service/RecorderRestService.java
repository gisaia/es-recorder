package com.gisaia.recorder.rest.service;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gisaia.recorder.core.RecordStorageService;
import com.gisaia.recorder.util.EsRecorderConfiguration;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.rest.response.Error;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.Arrays;
import java.util.List;

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
    private final List<String> authorizedReferrers;
    private final String userHeader;

    public RecorderRestService(EsRecorderConfiguration configuration) {
        this.service = new RecordStorageService(configuration);
        this.authorizedReferrers = Arrays.asList(configuration.authorizedReferrers.split(","));
        this.userHeader = configuration.userHeader;
    }

    @Timed
    @Path("store")
    @POST
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "Store a new record in elasticsearch", produces = UTF8JSON, consumes = UTF8JSON)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = String.class),
            @ApiResponse(code = 403, message = "Forbidden referrer.", response = Error.class),
            @ApiResponse(code = 404, message = "Index not found.", response = Error.class),
            @ApiResponse(code = 500, message = "Application error.", response = Error.class)})

    public Response store(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest httpServletRequest,
            @Context HttpHeaders headers,

            @ApiParam(name = "record")
            @Valid ObjectNode record
    ) throws ArlasException {
        checkAuthorization(headers);
        return Response.ok(uriInfo.getRequestUriBuilder().build())
                .entity(service.store(record,
                        headers.getHeaderString(HttpHeaders.USER_AGENT),
                        headers.getHeaderString("Referer"),
                        httpServletRequest.getRemoteAddr()))
                .type(MediaType.TEXT_PLAIN)
                .build();
    }



    private void checkAuthorization(HttpHeaders headers) throws ForbiddenException {
        authorizedReferrers.stream()
                .filter(s -> headers.getHeaderString("Referer").startsWith(s))
                .findAny()
                .orElseThrow(ForbiddenException::new);

        String key = headers.getHeaderString(userHeader);
        if (StringUtils.isBlank(key)) {
            throw new ForbiddenException(userHeader + " is empty.");
        }
    }
}
