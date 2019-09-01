package eu.arrowhead.core.onboarding.model;

import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class OnboardingRequest {

  @NotNull
  @Valid
  private String name;

  public OnboardingRequest() {
  }

  public OnboardingRequest(final String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }


  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OnboardingRequest that = (OnboardingRequest) o;
    return Objects.equals(name, that.getName());
  }

  @Override
  public String toString() {
    return "OnboardingWithCertificateRequest{" + "name='" + name + '\'';
  }
}
