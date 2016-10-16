package org.etsit.uma.androidrsa.server.util.rsa;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public class RsaCertificate {
	private X509Certificate x509certificate;
	private PrivateKey privateKey;
	
	public RsaCertificate(X509Certificate x509certificate, PrivateKey privateKey) {
		super();
		this.x509certificate = x509certificate;
		this.privateKey = privateKey;
	}

	public X509Certificate getX509certificate() {
		return x509certificate;
	}
	
	public PrivateKey getPrivateKey() {
		return privateKey;
	}
	
}
