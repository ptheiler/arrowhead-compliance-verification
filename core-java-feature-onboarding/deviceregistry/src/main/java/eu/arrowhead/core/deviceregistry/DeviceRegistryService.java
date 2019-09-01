/*
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.core.deviceregistry;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.Utility;
import eu.arrowhead.common.database.ArrowheadDevice;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.DeviceRegistryEntry;
import eu.arrowhead.common.database.ServiceRegistryEntry;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.messages.ComplianceResult;
import eu.arrowhead.common.messages.ServiceQueryForm;
import eu.arrowhead.common.messages.ServiceQueryResult;
import eu.arrowhead.common.misc.CoreSystemService;
import eu.arrowhead.common.misc.registry_interfaces.RegistryService;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

public class DeviceRegistryService implements RegistryService<DeviceRegistryEntry> {

	private final DatabaseManager databaseManager;

	public DeviceRegistryService() throws ExceptionInInitializerError {
		databaseManager = DatabaseManager.getInstance();
	}

	public DeviceRegistryEntry lookup(final long id) throws EntityNotFoundException, ArrowheadException {
		final DeviceRegistryEntry returnValue;

		try {
			Optional<DeviceRegistryEntry> optional = databaseManager.get(DeviceRegistryEntry.class, id);
			returnValue = optional.orElseThrow(() -> {
				return new DataNotFoundException("The requested entity does not exist", Status.NOT_FOUND.getStatusCode());
			});
		} catch (final ArrowheadException e) {
			throw e;
		} catch (Exception e) {
			throw new ArrowheadException(e.getMessage(), Status.NOT_FOUND.getStatusCode(), e);
		}

		return returnValue;
	}

	private ComplianceResult checkCompliance(final String deviceAddress)
	{
		final CoreSystemService css = CoreSystemService.COMPLIANCE_SERVICE;
		final ArrowheadService service = new ArrowheadService(Utility.createSD(css.getServiceDef(), false),
															  Collections.singleton("HTTP-INSECURE-JSON"), null);
		final ServiceQueryForm sqf = new ServiceQueryForm(service, true, false);

		Response response = Utility.sendRequest(Utility.getSrQueryUri(), "PUT", sqf);
		ServiceQueryResult sqr = response.readEntity(ServiceQueryResult.class);
		if(sqr.getServiceQueryData().isEmpty())
		{
			throw new RuntimeException("Unable to find compliance service");
		}

		ServiceRegistryEntry entry = sqr.getServiceQueryData().get(0);
		ArrowheadSystem provider = entry.getProvider();
		URI complianceUri = UriBuilder
			.fromUri(String.format("http://%s:%d", provider.getAddress(), provider.getPort()))
			.path(entry.getServiceURI())
			.path("device")
			.path(deviceAddress)
			.build();

		response = Utility.sendRequest(complianceUri.toString(), "GET", null);
		return response.readEntity(ComplianceResult.class);
	}

	public DeviceRegistryEntry publish(DeviceRegistryEntry entity, SocketAddress address) {
		final DeviceRegistryEntry returnValue;

		try {
			if(Objects.nonNull(address)) {
				ComplianceResult complianceResult =
					checkCompliance(((InetSocketAddress)address).getAddress().getHostAddress());
				System.out.println(complianceResult);
			}
			else
			{
				System.err.println("Remote Address is NULL");
			}
			entity.setProvidedDevice(resolve(entity.getProvidedDevice()));
			returnValue = databaseManager.save(entity);
		} catch (final ArrowheadException e) {
			throw e;
		} catch (Exception e) {
			throw new ArrowheadException(e.getMessage(), Status.NOT_FOUND.getStatusCode(), e);
		}
		return returnValue;
	}


	public DeviceRegistryEntry publish(final DeviceRegistryEntry entity) throws ArrowheadException {
		return publish(entity, null);
	}

	public DeviceRegistryEntry unpublish(final DeviceRegistryEntry entity) throws EntityNotFoundException, ArrowheadException {
		final DeviceRegistryEntry returnValue;

		try {
			databaseManager.delete(entity);
			returnValue = entity;
		} catch (final ArrowheadException e) {
			throw e;
		} catch (Exception e) {
			throw new ArrowheadException(e.getMessage(), Status.NOT_FOUND.getStatusCode(), e);
		}
		return returnValue;
	}

	protected ArrowheadDevice resolve(final ArrowheadDevice provider) {
		final ArrowheadDevice returnValue;

		if (provider.getId() != null) {
			Optional<ArrowheadDevice> optional = databaseManager.get(ArrowheadDevice.class, provider.getId());
			returnValue = optional.orElseThrow(() -> new ArrowheadException("ArrowheadDevice does not exist", Status.BAD_REQUEST.getStatusCode()));
		} else {
			returnValue = databaseManager.save(provider);
		}

		return returnValue;
	}
}
