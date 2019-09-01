package eu.arrowhead.client.consumer;

import eu.arrowhead.client.common.ArrowheadClientMain;
import eu.arrowhead.client.common.Utility;
import eu.arrowhead.client.common.exception.ArrowheadException;
import eu.arrowhead.client.common.exception.ExceptionType;
import eu.arrowhead.client.common.misc.ClientType;
import eu.arrowhead.client.common.model.ArrowheadService;
import eu.arrowhead.client.common.model.ArrowheadSystem;
import eu.arrowhead.client.common.model.ServiceRegistryEntry;
import eu.arrowhead.demo.*;
import eu.arrowhead.demo.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iot.raspberry.grovepi.GrovePi;
import org.iot.raspberry.grovepi.devices.GroveLed;
import org.iot.raspberry.grovepi.pi4j.GrovePi4J;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/*
  BasicProviderMain class has the following mandatory functionalities:
    1) Read in command line, and config file properties
    2) Start a normal HTTP server with REST resource classes
    3) Register its service into the Service Registry
 */
public class ComplianceDemo extends ArrowheadClientMain {

    private static String SR_BASE_URI;
    private final Logger logger = LogManager.getLogger();
    private final OnboardingStatus onboardingStatus;
    private final AtomicBoolean connected;

    private DeviceRegistryEntry deviceRegistryEntry;
    private ArrowheadDevice arrowheadDevice;
    private SystemRegistryEntry systemRegistryEntry;
    private ArrowheadSystem arrowheadSystem;
    private ServiceRegistryEntry serviceRegistryEntry;
    private ServiceQueryForm serviceQueryForm;
    private ArrowheadService arrowheadService;

    private ComplianceDemo(String[] args) throws IOException {
        Set<Class<?>> classes = new HashSet<>();
        String[] packages = {"eu.arrowhead.client.common", "eu.arrowhead.client.consumer"};
        init(ClientType.PROVIDER, args, classes, packages);
        final GrovePi grovePi = new GrovePi4J();
        connected = new AtomicBoolean(false);
        onboardingStatus = new OnboardingStatus(Executors.newCachedThreadPool(), grovePi);
        onboardingStatus.disconnected();

        try {
            onboardingStatus.connecting();
            performOnboarding();
            onboardingStatus.connected();
            listenForInput();
        }
        catch(Exception ex)
        {
            System.out.println(ex.getMessage());
            onboardingStatus.error();
        }
        finally
        {
            try {
                finalize();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        System.getProperties().put("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
        //Remove the command line argument for secure mode, if present, since the Basic Provider only operates in insecure mode.
        List<String> list = new ArrayList<>(Arrays.asList(args));
        list.remove("-tls");
        args = list.toArray(new String[0]);
        new ComplianceDemo(args);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        disconnectFromOnboarding();
        onboardingStatus.disconnected();
    }

    private void performOnboarding() {
        final String onboardingUrl = getOnboardingServiceUrl();
        final OnboardingRequest request = new OnboardingRequest();
        final OnboardingResponse response;

        request.setName("provider-peter");
        request.setSharedKey(props.getProperty("shared_secret", "secret"));

        response = connectToOnboardingController(request, onboardingUrl);
        final ServiceEndpoint[] services = response.getServices();

        logger.debug("Received services from onboarding controller: {}", Arrays.toString(services));

        //Compile the base of the Service Registry URL
        getServiceRegistryUrl();

        logger.debug("Registering Device");
        deviceRegistryEntry = register(compileDeviceRegistrationPayload(), getDeviceRegistryUrl());
        arrowheadDevice = deviceRegistryEntry.getProvidedDevice();

        logger.debug("Registering System");
        systemRegistryEntry = register(compileSystemRegistrationPayload(), getSystemRegistryUrl());
        arrowheadSystem = systemRegistryEntry.getProvidedSystem();

        connected.set(true);
    }

    private void disconnectFromOnboarding() {
        try{
            connected.set(false);

            if (systemRegistryEntry != null) {
                unregister(systemRegistryEntry, getSystemRegistryUrl());
                systemRegistryEntry = null;
                arrowheadSystem = null;
            }
            if (deviceRegistryEntry != null) {
                unregister(deviceRegistryEntry, getDeviceRegistryUrl());
                deviceRegistryEntry = null;
                arrowheadDevice = null;
            }
        }
        catch(Exception ex)
        {
            logger.warn("Error during disconnect: {}", ex.getMessage());
        }
        finally
        {
            onboardingStatus.disconnected();
        }
    }

    private String getOnboardingServiceUrl() {
        String srAddress = props.getProperty("onboarding_address", "0.0.0.0");
        int srPort = props.getIntProperty("onboarding_insecure_port", 8434);
        return Utility.getUri(srAddress, srPort, "onboarding", false, false);
    }

    private String getDeviceRegistryUrl() {
        String srAddress = props.getProperty("device_address", "0.0.0.0");
        int srPort = props.getIntProperty("device_insecure_port", 8438);
        return Utility.getUri(srAddress, srPort, "deviceregistry", false, false);
    }

    private String getSystemRegistryUrl() {
        String srAddress = props.getProperty("system_address", "0.0.0.0");
        int srPort = props.getIntProperty("system_insecure_port", 8436);
        return Utility.getUri(srAddress, srPort, "systemregistry", false, false);
    }

    private void getServiceRegistryUrl() {
        String srAddress = props.getProperty("sr_address", "0.0.0.0");
        int srPort = props.getIntProperty("sr_insecure_port", 8442);
        SR_BASE_URI = Utility.getUri(srAddress, srPort, "serviceregistry", false, false);
    }

    private DeviceRegistryEntry compileDeviceRegistrationPayload() {
        if (deviceRegistryEntry == null) {
            deviceRegistryEntry = new DeviceRegistryEntry();
            deviceRegistryEntry.setProvidedDevice(compileArrowheadDevice());
        }
        return deviceRegistryEntry;
    }

    private ArrowheadDevice compileArrowheadDevice() {
        if (arrowheadDevice == null) {
            arrowheadDevice = new ArrowheadDevice();
            arrowheadDevice.setDeviceName("ComplianceTestDevice");
        }
        return arrowheadDevice;
    }

    private SystemRegistryEntry compileSystemRegistrationPayload() {
        if (systemRegistryEntry == null) {
            systemRegistryEntry = new SystemRegistryEntry();
            systemRegistryEntry.setProvider(compileArrowheadDevice());
            systemRegistryEntry.setProvidedSystem(compileArrowheadSystem());
        }
        return systemRegistryEntry;
    }

    private ArrowheadSystem compileArrowheadSystem() {
        if (arrowheadSystem == null) {
            arrowheadSystem = new ArrowheadSystem();
            arrowheadSystem.setSystemName("RaspberryPi");
            arrowheadSystem.setPort(props.getIntProperty("insecure_port", 8460));
            try {
                arrowheadSystem.setAddress(props.getProperty("address", InetAddress.getLocalHost().getHostAddress()));
            } catch (final UnknownHostException e) {
                arrowheadSystem.setAddress("0.0.0.0");
            }
        }
        return arrowheadSystem;
    }

    private ServiceQueryForm compileServiceLookup() {
        if (serviceQueryForm == null) {
            serviceQueryForm = new ServiceQueryForm();
            serviceQueryForm.setService(compileArrowheadService());
        }
        return serviceQueryForm;
    }

    private ArrowheadService compileArrowheadService() {
        if (arrowheadService == null) {
            final Set<String> interfaces = new HashSet<>();
            interfaces.add("JSON");

            arrowheadService = new ArrowheadService();
            arrowheadService.setServiceDefinition("BlueLedService");
            arrowheadService.setInterfaces(interfaces);
        }
        return arrowheadService;
    }

    private OnboardingResponse connectToOnboardingController(OnboardingRequest request, String onboardingUrl) {
        //Create the full URL (appending "register" to the base URL)
        final String registerUri = UriBuilder.fromPath(onboardingUrl).path("sharedKey").toString();
        final OnboardingResponse response;


        //Send the registration request
        final Response r = Utility.sendRequest(registerUri, "POST", request);
        response = r.readEntity(OnboardingResponse.class);

        if (!response.isSuccess()) {
            throw new ArrowheadException("Onboarding failed for unknown reasons!");
        }
        logger.info("Onboarding controller returned certificate!");
        return response;
    }

    private <T> T register(final T o, final String url) {
        return register(o, url, "publish", "unpublish");
    }

    private <T> T register(final T o, final String url, final String suffix, final String errorSuffix) {
        final String registerUri = UriBuilder.fromPath(url).path(suffix).toString();
        Response response;
        T entity;

        //Send the registration request
        try {
            response = Utility.sendRequest(registerUri, "POST", o);
            entity = (T) response.readEntity(o.getClass());
        } catch (ArrowheadException e) {
      /*
        Service Registry might return duplicate entry exception, if a previous instance of the web server already registered this service,
        and the deregistration did not happen. It's better to unregister the old entry, in case the request payload changed.
       */
            if (e.getExceptionType() == ExceptionType.DUPLICATE_ENTRY) {
                logger.info("Received DuplicateEntryException from SR, sending delete request and then registering again.");
                unregister(o, url, errorSuffix);
                response = Utility.sendRequest(registerUri, "POST", o);
                entity = (T) response.readEntity(o.getClass());
            } else {
                throw e;
            }
        }
        logger.info("Registration successful!");
        return entity;
    }

    private void unregister(final Object o, final String url) {
        unregister(o, url, "unpublish");
    }

    private void unregister(final Object o, final String url, final String suffix) {
        try {

            String removeUri = UriBuilder.fromPath(url).path(suffix).toString();
            logger.debug("Contacting {}", removeUri);
            Utility.sendRequest(removeUri, "POST", o);
            logger.info("Removed object successfully!");
        } catch(Exception ex)
        {
            logger.warn("Unknown exception during unregister: {}", ex.getMessage());
        }
    }
}