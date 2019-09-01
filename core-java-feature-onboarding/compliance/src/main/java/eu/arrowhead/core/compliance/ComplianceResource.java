/*
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.core.compliance;

import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.messages.ComplianceResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.io.IOException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * @author FHB
 */
@Path("compliance")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ComplianceResource {

    private final ComplianceChecker complianceChecker;

    public ComplianceResource() {
        complianceChecker = new ComplianceChecker();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response ping() {
        return Response.status(Status.OK).entity("This is the Arrowhead Compliance Support System.").build();
    }

    @GET
    @Path("device/{ip}")
    @Operation(summary = "Device compliance check", responses = {
        @ApiResponse(content = @Content(schema = @Schema(implementation = ComplianceResult.class)))})
    public Response device(@PathParam("ip") final String ipAddress) throws ArrowheadException, IOException {
        Response response;
        ComplianceResult complianceResult;

        complianceResult = complianceChecker.checkDevice(ipAddress);
        response = Response.status(Status.CREATED).entity(complianceResult).build();

        return response;
    }

    @GET
    @Path("system/{ip}")
    @Operation(summary = "System compliance check", responses = {
        @ApiResponse(content = @Content(schema = @Schema(implementation = ComplianceResult.class)))})
    public Response system(@PathParam("ip") final String ipAddress) throws ArrowheadException, IOException {
        Response response;
        ComplianceResult complianceResult;

        complianceResult = complianceChecker.checkSystem(ipAddress);
        response = Response.status(Status.CREATED).entity(complianceResult).build();

        return response;
    }

    @GET
    @Path("service/{ip}")
    @Operation(summary = "Service compliance check", responses = {
        @ApiResponse(content = @Content(schema = @Schema(implementation = ComplianceResult.class)))})
    public Response service(@PathParam("ip") final String ipAddress) throws ArrowheadException, IOException {
        Response response;
        ComplianceResult complianceResult;

        complianceResult = complianceChecker.checkService(ipAddress);
        response = Response.status(Status.CREATED).entity(complianceResult).build();

        return response;

    }
}