/*
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.core.systemregistry;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.Utility;
import eu.arrowhead.common.database.ArrowheadDevice;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.ServiceRegistryEntry;
import eu.arrowhead.common.database.SystemRegistryEntry;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.messages.ComplianceResult;
import eu.arrowhead.common.messages.ServiceQueryForm;
import eu.arrowhead.common.messages.ServiceQueryResult;
import eu.arrowhead.common.misc.CoreSystemService;
import eu.arrowhead.common.misc.registry_interfaces.RegistryService;
import java.net.URI;
import java.util.Collections;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;


public class SystemRegistryService implements RegistryService<SystemRegistryEntry> {

	private final DatabaseManager databaseManager;

  public SystemRegistryService() {
		databaseManager = DatabaseManager.getInstance();
	}

	public SystemRegistryEntry lookup(final long id) throws EntityNotFoundException, ArrowheadException {
		final SystemRegistryEntry returnValue;

		try {
			Optional<SystemRegistryEntry> optional = databaseManager.get(SystemRegistryEntry.class, id);
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

	private ComplianceResult checkDeviceCompliance(final String systemAddress)
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
			.path(systemAddress)
			.build();
		response = Utility.sendRequest(complianceUri.toString(), "GET", null);
		return response.readEntity(ComplianceResult.class);
	}

	private ComplianceResult checkSystemCompliance(final String systemAddress)
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
			.path("system")
			.path(systemAddress)
			.build();

		response = Utility.sendRequest(complianceUri.toString(), "GET", null);
		return response.readEntity(ComplianceResult.class);
	}

	private ComplianceResult checkServiceCompliance(final String systemAddress)
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
			.path("service")
			.path(systemAddress)
			.build();

		response = Utility.sendRequest(complianceUri.toString(), "GET", null);
		return response.readEntity(ComplianceResult.class);
	}

	public SystemRegistryEntry publish(final SystemRegistryEntry entity) throws ArrowheadException {
		final SystemRegistryEntry returnValue;
		String remoteAddr = entity.getProvidedSystem().getAddress();
		try {
			ComplianceResult complianceResult = checkDeviceCompliance(remoteAddr);
			System.out.println(complianceResult);
			if(complianceResult.getHardeningIndex() < 30)
				throw new ArrowheadException("Device is not compliant!");
			complianceResult = checkSystemCompliance(remoteAddr);
			System.out.println(complianceResult);
			if(complianceResult.getHardeningIndex() < 30)
				throw new ArrowheadException("System is not compliant!");
			complianceResult = checkServiceCompliance(remoteAddr);
			System.out.println(complianceResult);
			if(complianceResult.getHardeningIndex() < 30)
				throw new ArrowheadException("Service is not compliant!");

			entity.setProvidedSystem(resolve(entity.getProvidedSystem()));
			entity.setProvider(resolve(entity.getProvider()));
			returnValue = databaseManager.save(entity);
		} catch (final ArrowheadException e) {
			throw e;
		} catch (Exception e) {
			throw new ArrowheadException(e.getMessage(), Status.NOT_FOUND.getStatusCode(), e);
		}
		return returnValue;
	}

	public SystemRegistryEntry unpublish(final SystemRegistryEntry entity) throws EntityNotFoundException, ArrowheadException {
		final SystemRegistryEntry returnValue;

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

	protected ArrowheadSystem resolve(final ArrowheadSystem providedSystem) {
		final ArrowheadSystem returnValue;

		if (providedSystem.getId() != null) {
			Optional<ArrowheadSystem> optional = databaseManager.get(ArrowheadSystem.class, providedSystem.getId());
			returnValue = optional.orElseThrow(() -> new ArrowheadException("ProvidedSystem does not exist", Status.BAD_REQUEST.getStatusCode()));
		} else {
			returnValue = databaseManager.save(providedSystem);
		}

		return returnValue;
	}

	protected ArrowheadDevice resolve(final ArrowheadDevice provider) {
		final ArrowheadDevice returnValue;

		if (provider.getId() != null) {
			Optional<ArrowheadDevice> optional = databaseManager.get(ArrowheadDevice.class, provider.getId());
			returnValue = optional.orElseThrow(() -> new ArrowheadException("Provider does not exist", Status.BAD_REQUEST.getStatusCode()));
		} else {
			returnValue = databaseManager.save(provider);
		}

		return returnValue;
	}
}
