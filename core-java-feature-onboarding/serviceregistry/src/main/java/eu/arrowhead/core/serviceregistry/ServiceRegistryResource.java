/*
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.core.serviceregistry;

import eu.arrowhead.common.Utility;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.messages.ComplianceResult;
import eu.arrowhead.common.misc.CoreSystemService;
import eu.arrowhead.core.compliance.ComplianceChecker;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ServiceRegistryEntry;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.messages.ServiceQueryForm;
import eu.arrowhead.common.messages.ServiceQueryResult;
import java.net.URI;
import java.util.Collections;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import org.apache.log4j.Logger;

@Path("serviceregistry")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ServiceRegistryResource {

  private static final Logger log = Logger.getLogger(ServiceRegistryResource.class.getName());

  public final ComplianceChecker complianceChecker;
  public ServiceRegistryResource() {
    complianceChecker = new ComplianceChecker();
  }

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String getIt() {
    return "This is the Service Registry.";
  }

  private ComplianceResult checkCompliance(final String serviceAddress)
  {
    final CoreSystemService css = CoreSystemService.COMPLIANCE_SERVICE;
    final ArrowheadService service = new ArrowheadService(Utility.createSD(css.getServiceDef(), false),
                                                          Collections.singleton("HTTP-INSECURE-JSON"), null);
    final ServiceQueryForm sqf = new ServiceQueryForm(service, true, false);

    Response response = Utility.sendRequest(Utility.getSrQueryUri(), "PUT", sqf);
    ServiceQueryResult sqr = response.readEntity(ServiceQueryResult.class);

    if(sqr.getServiceQueryData().isEmpty())
    {
      throw new RuntimeException("Unable to find compliance service");
    }

    ServiceRegistryEntry entry = sqr.getServiceQueryData().get(0);
    ArrowheadSystem provider = entry.getProvider();
    URI complianceUri = UriBuilder
        .fromUri(String.format("http://%s:%d", provider.getAddress(), provider.getPort()))
        .path(entry.getServiceURI())
        .path("service")
        .path(serviceAddress)
        .build();

    response = Utility.sendRequest(complianceUri.toString(), "GET", null);
    return response.readEntity(ComplianceResult.class);
  }

  @POST
  @Path("register")
  public Response publishEntriesToRegistry(@Valid ServiceRegistryEntry entry, @Context ContainerRequestContext requestContext) {
    log.debug(
        "SR reg service: " + entry.getProvidedService().getServiceDefinition() + " provider: " + entry.getProvider().getSystemName() + " serviceURI: "
            + entry.getServiceURI());

    String remoteAddr = entry.getProvider().getAddress();

    try {
      ComplianceResult complianceResult = checkCompliance(remoteAddr);
      System.out.println(complianceResult);
      if (ServiceRegistry.register(entry)) {
        return Response.status(Response.Status.OK).build();
      } else {
        return Response.status(Response.Status.RESET_CONTENT).build();
      }
    } catch (Exception e) {
      log.error("SR Registration failed:" + e.getMessage());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PUT
  @Path("remove")
  public Response removeEntriesFromRegistry(@Valid ServiceRegistryEntry entry, @Context ContainerRequestContext requestContext) {
    log.debug("SR remove service: " + entry.getProvidedService().getServiceDefinition() + " provider: " + entry.getProvider().getSystemName()
                  + " serviceURI: " + entry.getServiceURI());

    if (ServiceRegistry.unRegister(entry)) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.PARTIAL_CONTENT).build();
    }
  }

  /*
     Backwards compatibility
   */
  @POST
  @Path("{service}/{interf}")
  public Response publishingToRegistry(@PathParam("service") String service, @PathParam("interf") String interf, ServiceRegistryEntry entry) {
    if (service == null || interf == null || entry == null) {
      log.info("publishingToRegistry throws BadPayloadException");
      throw new BadPayloadException("Bad payload: service request form has missing/incomplete mandatory fields.");
    }
    entry.setProvidedService(new ArrowheadService(service, Collections.singleton(interf), null));

    if (ServiceRegistry.register(entry)) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.RESET_CONTENT).build();
    }
  }

  /*
     Backwards compatibility
   */
  @PUT
  @Path("{service}/{interf}")
  public Response removingFromRegistry(@PathParam("service") String service, @PathParam("interf") String interf, ServiceRegistryEntry entry) {
    if (service == null || interf == null || entry == null) {
      log.info("removingFromRegistry throws BadPayloadException");
      throw new BadPayloadException("Bad payload: service de-registration form has missing/incomplete mandatory fields.");
    }
    entry.setProvidedService(new ArrowheadService(service, Collections.singleton(interf), null));

    if (ServiceRegistry.unRegister(entry)) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.PARTIAL_CONTENT).build();
    }
  }

  /*
      Interface towards the Orchestrator
   */
  @PUT
  @Path("query")
  public Response getServiceQueryForm(@Valid ServiceQueryForm queryForm) {
    ServiceQueryResult sqr = ServiceRegistry.provideServices(queryForm);
    if (!sqr.getServiceQueryData().isEmpty()) {
      return Response.status(Response.Status.OK).entity(sqr).build();
    } else {
      return Response.status(Response.Status.PARTIAL_CONTENT).entity(sqr).build();
    }
  }

  /**
   * Public function for checking all entries
   *
   * @return All registered service
   */
  @GET
  @Path("all")
  public Response getAllServices() {
    ServiceQueryResult sqr = ServiceRegistry.provideAllServices();
    if (sqr.getServiceQueryData().isEmpty()) {
      return Response.status(Status.PARTIAL_CONTENT).entity(sqr).build();
    } else {
      return Response.status(Response.Status.OK).entity(sqr).build();
    }
  }

  /**
   * Public function for removing all entries in the DNS.
   *
   * @return Removes all registered service
   */
  @DELETE
  @Path("all")
  public Response removeAllServices() {
    if (ServiceRegistry.removeAllServices()) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

}
