/*
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.common.exception;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.glassfish.jersey.server.ContainerRequest;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

  @Inject
  private javax.inject.Provider<ContainerRequest> requestContext;

  @Override
  public Response toResponse(ConstraintViolationException ex) {
    ex.printStackTrace();
    StringBuilder messageBuilder = new StringBuilder();
    for (ConstraintViolation cv : ex.getConstraintViolations()) {
      messageBuilder.append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(", ");
    }
    int errorCode = 400; //Bad Request
    String origin = requestContext.get() != null ? requestContext.get().getAbsolutePath().toString() : "unknown";

    ErrorMessage errorMessage = new ErrorMessage(messageBuilder.toString(), errorCode, ExceptionType.BAD_PAYLOAD, origin);
    return Response.status(errorCode).entity(errorMessage).header("Content-type", "application/json").build();
  }
}
