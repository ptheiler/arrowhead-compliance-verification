/*
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.core.onboarding;

import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.core.certificate_authority.model.CertificateSigningResponse;
import eu.arrowhead.core.onboarding.model.OnboardingRequest;
import eu.arrowhead.core.onboarding.model.OnboardingResponse;
import eu.arrowhead.core.onboarding.model.OnboardingWithCertificateRequest;
import eu.arrowhead.core.onboarding.model.OnboardingWithSharedKeyRequest;
import eu.arrowhead.core.onboarding.model.ServiceEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.log4j.Logger;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;

/**
 * @author ZSM
 */
@Path("/onboarding")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OnboardingResource {

    private final Logger log = Logger.getLogger(OnboardingResource.class.getName());
    private final OnboardingService service;

    public OnboardingResource() {
        service = new OnboardingService();
        log.info(OnboardingResource.class.getSimpleName() + " created");
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response ping() {
        return Response.status(Response.Status.OK).entity("This is the Onboarding Arrowhead System.").build();
    }

    @POST
    @Path("/certificate")
    @Operation(summary = "Onboarding with certificate request", responses = {
        @ApiResponse(content = @Content(schema = @Schema(implementation = OnboardingResponse.class)))})
    public Response request(final OnboardingWithCertificateRequest request) throws ArrowheadException {
        final byte[] csrBytes;
        final JcaPKCS10CertificationRequest csr;
        final Response response;

        try {
            csrBytes = Base64.getDecoder().decode(request.getCertificateRequest());
            csr = new JcaPKCS10CertificationRequest(csrBytes);
            if (service.isCertificateAllowed())
            {
                response = createResponse(csr);
            } else {
                response = Response.status(Status.FORBIDDEN).entity(OnboardingResponse.failure()).build();
            }
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
            throw new BadPayloadException(e.getMessage());
        }

        return response;
    }

    @POST
    @Path("/sharedKey")
    @Operation(summary = "Onboarding with shared key", responses = {
        @ApiResponse(content = @Content(schema = @Schema(implementation = OnboardingResponse.class)))})
    public Response request(final OnboardingWithSharedKeyRequest request) throws ArrowheadException {
        final Response response;

        if (service.isSharedKeyAllowed() && service.isKeyValid(request.getSharedKey())) {
            response = createResponse(request.getName());
        } else {
            response = Response.status(Status.FORBIDDEN).entity(OnboardingResponse.failure()).build();
        }

        return response;
    }

    @POST
    @Path("/plain")
    @Operation(summary = "Onboarding without credentials", responses = {
        @ApiResponse(content = @Content(schema = @Schema(implementation = OnboardingResponse.class)))})
    public Response request(final OnboardingRequest request) throws ArrowheadException {
        final Response response;

        if (service.isUnknownAllowed()) {
            response = createResponse(request.getName());
        } else {
            response = Response.status(Status.FORBIDDEN).entity(OnboardingResponse.failure()).build();
        }

        return response;
    }

    private Response createResponse(final String name) {
        try {
            final KeyPair keyPair = service.generateKeyPair("RSA", 1024);
            final CertificateSigningResponse csrResponse = service.createAndSignCertificate(name, keyPair);
            final ServiceEndpoint[] endpoints = service.getEndpoints();
            final OnboardingResponse returnValue = OnboardingResponse.success(csrResponse, keyPair, endpoints);
            return Response.status(Status.OK).entity(returnValue).build();
        } catch (IOException | NoSuchAlgorithmException | URISyntaxException | OperatorCreationException e) {
            throw new ArrowheadException("Unable to create Certificate", e);
        }
    }

    private Response createResponse(final JcaPKCS10CertificationRequest providedCsr) {
        try {
            final KeyPair keyPair = new KeyPair(providedCsr.getPublicKey(), null);
            final CertificateSigningResponse csrResponse = service.signCertificate(providedCsr);
            final ServiceEndpoint[] endpoints = service.getEndpoints();
            final OnboardingResponse returnValue = OnboardingResponse.success(csrResponse, keyPair,
                                                                              endpoints);
            return Response.status(Status.OK).entity(returnValue).build();
        } catch (URISyntaxException | InvalidKeyException | NoSuchAlgorithmException | IOException e) {
            throw new ArrowheadException("Unable to create Certificate", e);
        }
    }

}