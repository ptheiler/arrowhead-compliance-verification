/*
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.common.web;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.database.ArrowheadDevice;
import eu.arrowhead.common.exception.DataNotFoundException;
import java.util.HashMap;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.log4j.Logger;

@Path("mgmt/devices")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ArrowheadDeviceApi {

  private final HashMap<String, Object> restrictionMap = new HashMap<>();
  private static final Logger log = Logger.getLogger(ArrowheadDeviceApi.class.getName());
  private static final DatabaseManager dm = DatabaseManager.getInstance();

  @GET
  public List<ArrowheadDevice> getDevices() {
    List<ArrowheadDevice> deviceList = dm.getAll(ArrowheadDevice.class, restrictionMap);
    if (deviceList.isEmpty()) {
      log.info("getDevices throws DataNotFoundException");
      throw new DataNotFoundException("ArrowheadDevices not found in the database.");
    }

    return deviceList;
  }

}
