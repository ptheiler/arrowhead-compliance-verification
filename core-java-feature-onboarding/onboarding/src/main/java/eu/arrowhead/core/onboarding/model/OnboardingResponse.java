package eu.arrowhead.core.onboarding.model;

import eu.arrowhead.core.certificate_authority.model.CertificateSigningResponse;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public class OnboardingResponse {

    private final boolean success;
    // TODO provide only service registry
    private final ServiceEndpoint[] services;
    private final String onboardingCertificate;
    private final String immediateCertificate;
    private final String rootCertificate;
    private String keyAlgorithm;
    private String keyFormat;
    private byte[] privateKey;
    private byte[] publicKey;

    private OnboardingResponse(boolean success, CertificateSigningResponse response, ServiceEndpoint[] services) {
        this.success = success;
        if(response != null){
        this.onboardingCertificate = response.getEncodedSignedCert();
        this.immediateCertificate = response.getIntermediateCert();
        this.rootCertificate = response.getRootCert();}
            else
            {
                onboardingCertificate = null;
                immediateCertificate = null;
                rootCertificate = null;
            }
        this.services = services;
    }

    public static OnboardingResponse success(CertificateSigningResponse response, KeyPair keyPair,
                                             ServiceEndpoint[] services) {
        OnboardingResponse resp = new OnboardingResponse(true, response, services);
        resp.setPrivateKey(keyPair.getPrivate());
        resp.setPublicKey(keyPair.getPublic());
        return resp;
    }

    public static OnboardingResponse failure() {
        return new OnboardingResponse(false, null, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public ServiceEndpoint[] getServices() {
        return services;
    }

    public String getOnboardingCertificate() {
        return onboardingCertificate;
    }

    public String getImmediateCertificate() {
        return immediateCertificate;
    }

    public String getRootCertificate() {
        return rootCertificate;
    }

    public String getKeyAlgorithm() {
        return keyAlgorithm;
    }

    public String getKeyFormat() {
        return keyFormat;
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        if (privateKey != null) {
            this.privateKey = privateKey.getEncoded();
        }
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        if (publicKey != null) {
            this.keyAlgorithm = publicKey.getAlgorithm();
            this.keyFormat = publicKey.getFormat();
            this.publicKey = publicKey.getEncoded();
        }
    }
}
