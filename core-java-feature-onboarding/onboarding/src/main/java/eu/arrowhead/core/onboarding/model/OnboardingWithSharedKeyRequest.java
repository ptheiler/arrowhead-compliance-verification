package eu.arrowhead.core.onboarding.model;

import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class OnboardingWithSharedKeyRequest extends OnboardingRequest {

  @Valid
  @NotNull
  private String sharedKey;

  public OnboardingWithSharedKeyRequest() {
  }

  public OnboardingWithSharedKeyRequest(final String name, final String sharedKey) {
    super(name);
    this.sharedKey = sharedKey;
  }

  public String getSharedKey() {
    return sharedKey;
  }

  public void setSharedKey(final String sharedKey) {
    this.sharedKey = sharedKey;
  }

  @Override
  public int hashCode() {
    return super.hashCode() + Objects.hash(sharedKey);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OnboardingWithSharedKeyRequest that = (OnboardingWithSharedKeyRequest) o;
    return super.equals(o) && Objects.equals(sharedKey, that.sharedKey);
  }

  @Override
  public String toString() {
    return "OnboardingWithCertificateRequest{" + "name='" + getName() + '\'' + ", sharedKey=" + sharedKey + '}';
  }
}
