/*
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.core.gateway.filter;

import eu.arrowhead.common.filter.AccessControlFilter;
import eu.arrowhead.common.misc.SecurityUtils;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHORIZATION) //2nd highest priority constant, this filter gets executed after the SecurityFilter
public class GatewayACF extends AccessControlFilter {

  @Override
  public boolean isClientAuthorized(String clientCN, String method, String requestTarget, String requestJson) {
    if (!SecurityUtils.isKeyStoreCNArrowheadValid(clientCN)) {
      log.info(clientCN + " is not valid common name, access denied!");
      return false;
    }

    String serverCN = (String) configuration.getProperty("server_common_name");
    String[] serverFields = serverCN.split("\\.", 2);

    if (requestTarget.contains("mgmt")) {
      // Only the local System Operator can use the API methods
      return clientCN.equalsIgnoreCase("sysop." + serverFields[1]);
    } else {
      // Only the local Gatekeeper can use the resource methods
      return clientCN.equalsIgnoreCase("gatekeeper." + serverFields[1]);
    }
  }

}
