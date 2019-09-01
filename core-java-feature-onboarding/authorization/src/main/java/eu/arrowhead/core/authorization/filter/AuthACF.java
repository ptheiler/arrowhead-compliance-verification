/*
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.core.authorization.filter;

import eu.arrowhead.common.filter.AccessControlFilter;
import eu.arrowhead.common.misc.SecurityUtils;
import eu.arrowhead.core.authorization.AuthorizationMain;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHORIZATION) //2nd highest priority constant, this filter gets executed after the SecurityFilter
public class AuthACF extends AccessControlFilter {

  @Override
  public boolean isClientAuthorized(String clientCN, String method, String requestTarget, String requestJson) {
    if (!SecurityUtils.isKeyStoreCNArrowheadValid(clientCN)) {
      log.info(clientCN + " is not valid common name, access denied!");
      return false;
    }

    String serverCN = (String) configuration.getProperty("server_common_name");
    String[] serverFields = serverCN.split("\\.", 2);

    if (AuthorizationMain.enableAuthForCloud) {
      if (!requestTarget.contains("mgmt") || (requestTarget.endsWith("intracloud") && method.equalsIgnoreCase("post"))) {
        String[] clientFields = clientCN.split("\\.", 2);
        return serverFields[1].equalsIgnoreCase(clientFields[1]);
      } else {
        return clientCN.equalsIgnoreCase("sysop." + serverFields[1]);
      }
    } else {
      if (requestTarget.contains("mgmt")) {
        return clientCN.equalsIgnoreCase("sysop." + serverFields[1]) || (clientCN.equalsIgnoreCase("certificate_authority." + serverFields[1])
            && requestTarget.endsWith("publickey") && method.equalsIgnoreCase("get"));
      } else {
        return clientCN.equalsIgnoreCase("orchestrator." + serverFields[1]) || clientCN.equalsIgnoreCase("gatekeeper." + serverFields[1]);
      }
    }
  }
}
