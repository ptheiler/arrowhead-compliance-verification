package eu.arrowhead.core.onboarding.model;

import eu.arrowhead.common.misc.CoreSystem;
import java.net.URI;

public class ServiceEndpoint {

  private final CoreSystem system;
  private final URI uri;

  public ServiceEndpoint(final CoreSystem system, final URI uri) {
    this.system = system;
    this.uri = uri;
  }

  public CoreSystem getSystem() {
    return system;
  }

  public URI getUri() {
    return uri;
  }
}
