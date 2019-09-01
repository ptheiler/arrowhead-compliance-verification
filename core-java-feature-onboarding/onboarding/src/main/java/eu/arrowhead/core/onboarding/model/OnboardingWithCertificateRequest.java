package eu.arrowhead.core.onboarding.model;

import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class OnboardingWithCertificateRequest {

  @NotNull
  @Valid
  private String certificateRequest;

  public OnboardingWithCertificateRequest() {
    super();
  }

  public OnboardingWithCertificateRequest(final String certificateRequest) {
    this.certificateRequest = certificateRequest;
  }

  public String getCertificateRequest() {
    return certificateRequest;
  }

  public void setCertificateRequest(final String certificateRequest) {
    this.certificateRequest = certificateRequest;
  }

  @Override
  public int hashCode() {
    return super.hashCode() + Objects.hashCode(certificateRequest);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
  }
    OnboardingWithCertificateRequest that = (OnboardingWithCertificateRequest) o;
    return super.equals(o) && Objects.equals(certificateRequest, that.certificateRequest);
  }

  @Override
  public String toString() {
    return "OnboardingWithCertificateRequest{ certificateRequest=" + certificateRequest
        + '}';
  }
}
