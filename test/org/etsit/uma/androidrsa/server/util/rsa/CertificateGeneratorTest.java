package org.etsit.uma.androidrsa.server.util.rsa;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.junit.After;
import org.junit.Test;

public class CertificateGeneratorTest {

	private final CertificateGenerator generator = new CertificateGenerator();
	private final String testPath = "./test.cert";
	private final String caPrivateKeyPath = "/Users/Mike/Desktop/AndroidRsa_CA.pem";
	private final String caCertificatePath = "/Users/Mike/Desktop/AndroidRsa_CA.crt";

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	@After
	public void deleteTestFile() throws IOException {
		File testFile = new File(testPath);
		Files.deleteIfExists(testFile.toPath());
	}

	@Test
	public void aNewGeneratedCertificateIsSignedByTheCaAndNotByItsOwn() throws Exception {
		X509Certificate generatedCertificate = generator.generateCertificate(caPrivateKeyPath, "Mike");
		X509CertificateHolder generatedCertificateHolder = new X509CertificateHolder(generatedCertificate.getEncoded());
		X509Certificate caCertificate = generator.readCertificate(caCertificatePath);

		ContentVerifierProvider contentVerifierProvider = new JcaContentVerifierProviderBuilder().setProvider("BC")
				.build(caCertificate.getPublicKey());

		assertTrue(generatedCertificateHolder.isSignatureValid(contentVerifierProvider));
		
		contentVerifierProvider = new JcaContentVerifierProviderBuilder().setProvider("BC")
				.build(generatedCertificate.getPublicKey());
		
		assertFalse(generatedCertificateHolder.isSignatureValid(contentVerifierProvider));
		
	}

	@Test
	public void aNewGeneratedCertificateIsReadable() {
		X509Certificate certificate = generator.generateCertificate(caPrivateKeyPath, "Mike");
		generator.saveCertificate(testPath, certificate);
		X509Certificate readedCertificate = generator.readCertificate(testPath);
		assertEquals(certificate, readedCertificate);
	}

	@Test
	public void newGeneratedCertificateOverwritesTheOldOne() throws CertificateEncodingException {
		generator.saveCertificate(testPath, generator.generateCertificate(caPrivateKeyPath, "Mike"));
		generator.saveCertificate(testPath, generator.generateCertificate(caPrivateKeyPath, "Vincent"));
		X509Certificate readedCertificate = generator.readCertificate(testPath);
		assertEquals(getCommonNameFromCertificate(readedCertificate), "Vincent");
	}

	private String getCommonNameFromCertificate(final X509Certificate certificate) throws CertificateEncodingException {
		X500Name x500name = new JcaX509CertificateHolder(certificate).getSubject();
		RDN cn = x500name.getRDNs(BCStyle.CN)[0];
		return IETFUtils.valueToString(cn.getFirst().getValue());
	}

	@Test(expected = RuntimeException.class)
	public void ifThereIsAnErrorInCaPrivateKeyPathThenAnErrorIsThrown() {
		generator.generateCertificate("./false.cert", "Mike");
	}

}
