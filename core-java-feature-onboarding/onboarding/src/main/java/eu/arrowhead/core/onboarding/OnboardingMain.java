/*
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.core.onboarding;

import eu.arrowhead.common.ArrowheadMain;
import eu.arrowhead.common.misc.CoreSystem;

public class OnboardingMain extends ArrowheadMain {

  private OnboardingMain(String[] args) {
    String[] packages = {"eu.arrowhead.common.exception", "eu.arrowhead.common.json", "eu.arrowhead.common.filter",
        "eu.arrowhead.core.onboarding"};
    init(CoreSystem.ONBOARDING, args, null, packages);

    listenForInput();
  }

  public static void main(String[] args) {
    new OnboardingMain(args);
  }
}
