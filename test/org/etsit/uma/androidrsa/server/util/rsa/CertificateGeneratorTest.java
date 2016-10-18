package org.etsit.uma.androidrsa.server.util.rsa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.junit.After;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CertificateGeneratorTest {

	private final CertificateGenerator generator = new CertificateGenerator();
	private final String testFilePath = "./test.cert";
	private String caPrivateKeyPath;
	private String caCertificatePath;

	public CertificateGeneratorTest() throws Exception {
		Security.addProvider(new BouncyCastleProvider());
		resolvePathsFromWebXml();
	}

	private void resolvePathsFromWebXml() throws Exception {
		// two ways to do that: read xml file or use arquillian to have
		// integration test (rise a container with web.xml injection)
		File fXmlFile = new File("./WebContent/WEB-INF/web.xml");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();

		NodeList nList = doc.getElementsByTagName("env-entry");

		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				switch (eElement.getElementsByTagName("env-entry-name").item(0).getTextContent()) {
				case "caPrivateKeyPath":
					caPrivateKeyPath = eElement.getElementsByTagName("env-entry-value").item(0).getTextContent();
					break;
				case "caCertificatePath":
					caCertificatePath = eElement.getElementsByTagName("env-entry-value").item(0).getTextContent();
					break;
				}
			}
		}

	}

	@After
	public void deleteTestFile() throws IOException {
		File testFile = new File(testFilePath);
		Files.deleteIfExists(testFile.toPath());
	}

	@Test
	public void aNewGeneratedCertificateIsSignedByTheCaAndNotByItsOwn() throws Exception {
		RsaCertificate generatedCertificate = generator.generateCertificate(caPrivateKeyPath, "Mike");
		X509CertificateHolder generatedCertificateHolder = new X509CertificateHolder(
				generatedCertificate.getX509certificate().getEncoded());
		X509Certificate caCertificate = generator.readCertificate(caCertificatePath);

		ContentVerifierProvider contentVerifierProvider = new JcaContentVerifierProviderBuilder().setProvider("BC")
				.build(caCertificate.getPublicKey());

		assertTrue(generatedCertificateHolder.isSignatureValid(contentVerifierProvider));

		contentVerifierProvider = new JcaContentVerifierProviderBuilder().setProvider("BC")
				.build(generatedCertificate.getX509certificate().getPublicKey());

		assertFalse(generatedCertificateHolder.isSignatureValid(contentVerifierProvider));

	}

	@Test
	public void aGeneratedCertificateIsReadable() {
		RsaCertificate certificate = generator.generateCertificate(caPrivateKeyPath, "Mike");
		generator.save(testFilePath, certificate.getX509certificate());
		X509Certificate readedCertificate = generator.readCertificate(testFilePath);
		assertEquals(certificate.getX509certificate(), readedCertificate);
	}

	@Test
	public void aGeneratedCertificateOverwritesTheOldOne() throws CertificateEncodingException {
		generator.save(testFilePath, generator.generateCertificate(caPrivateKeyPath, "Mike").getX509certificate());
		generator.save(testFilePath, generator.generateCertificate(caPrivateKeyPath, "Vincent").getX509certificate());
		X509Certificate readedCertificate = generator.readCertificate(testFilePath);

		X500PrincipalHelper subject = new X500PrincipalHelper(readedCertificate.getSubjectX500Principal());
		assertEquals(subject.getCN(), "Vincent");
	}

	@Test
	public void aGeneratedCertificateHasExpectedData() {

		RsaCertificate certificate = generator.generateCertificate(caPrivateKeyPath, "Mike");

		X500PrincipalHelper subject = new X500PrincipalHelper(
				certificate.getX509certificate().getSubjectX500Principal());

		assertEquals(subject.getCN(), "Mike");
		assertEquals(subject.getO(), "etsit uma");
		assertEquals(subject.getC(), "ES");

		X500PrincipalHelper issuer = new X500PrincipalHelper(certificate.getX509certificate().getIssuerX500Principal());

		assertEquals(issuer.getCN(), "AndroidRsa CA");
		assertEquals(issuer.getO(), "etsit uma");
		assertEquals(issuer.getC(), "ES");

		Calendar cal = Calendar.getInstance();
		cal.setTime(certificate.getX509certificate().getNotBefore());
		int startYear = cal.get(Calendar.YEAR);
		cal.setTime(certificate.getX509certificate().getNotAfter());
		int endYear = cal.get(Calendar.YEAR);

		assertEquals(endYear - startYear, 10);
		assertEquals(certificate.getX509certificate().getSerialNumber(),
				CertificateGenerator.getSerialNumberCounter().subtract(BigInteger.ONE));
	}

	@Test(expected = RuntimeException.class)
	public void ifThereIsAnErrorInCaPrivateKeyPathThenAnErrorIsThrown() {
		generator.generateCertificate("./false.cert", "Mike");
	}

}
