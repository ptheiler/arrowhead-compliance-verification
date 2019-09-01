/*
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.core.gatekeeper.filter;

import eu.arrowhead.common.filter.AccessControlFilter;
import eu.arrowhead.common.misc.SecurityUtils;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHORIZATION) //2nd highest priority constant, this filter gets executed after the SecurityFilter
public class GatekeeperACF extends AccessControlFilter {

  @Override
  public boolean isClientAuthorized(String clientCN, String method, String requestTarget, String requestJson) {
    if (!SecurityUtils.isKeyStoreCNArrowheadValid(clientCN) && !SecurityUtils.isTrustStoreCNArrowheadValid(clientCN)) {
      log.info(clientCN + " is not valid common name, access denied!");
      return false;
    }

    String serverCN = (String) configuration.getProperty("server_common_name");
    String[] serverFields = serverCN.split("\\.", 2);

    if (requestTarget.contains("mgmt")) {
      //Only the local System Operator can use these methods
      return clientCN.equalsIgnoreCase("sysop." + serverFields[1]);
    } else {
      if (requestTarget.endsWith("init_gsd") || requestTarget.endsWith("init_icn")) {
        // Only requests from the local Orchestrator are allowed
        return clientCN.equalsIgnoreCase("orchestrator." + serverFields[1]);
      } else {
        // Only requests from other Gatekeepers are allowed
        return SecurityUtils.isTrustStoreCNArrowheadValid(clientCN);
      }
    }
  }

}
