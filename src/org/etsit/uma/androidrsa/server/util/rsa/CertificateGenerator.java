package org.etsit.uma.androidrsa.server.util.rsa;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Date;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.io.pem.PemGenerationException;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemObjectGenerator;
import org.bouncycastle.util.io.pem.PemWriter;

public class CertificateGenerator {
	private static BigInteger serialNumberCounter = BigInteger.ONE;
	private static final int RSA_KEY_SIZE = 2048;
	private static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
	private static final String END_CERT = "-----END CERTIFICATE-----";

	public X509Certificate generateCertificate(String caPrivateKeyPath, String name) {
		X509Certificate newCertificate = null;
		try {
			PrivateKey caPrivKey = readPrivateKey(caPrivateKeyPath);
			ContentSigner signature = new JcaContentSignerBuilder("SHA1withRSAEncryption").setProvider("BC").build(caPrivKey);

			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(RSA_KEY_SIZE);
			KeyPair keyPair = keyGen.generateKeyPair();

			SubjectPublicKeyInfo subPubKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded());

			Date startDate = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
			Date endDate = new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000);

			X509v1CertificateBuilder certificateBuilder = new X509v1CertificateBuilder(
					new X500Name("CN=AndroidRsa CA,O=etsit uma,C=ES"), serialNumberCounter, startDate, endDate,
					new X500Name("CN=" + name + ",O=etsit uma,C=ES"), subPubKeyInfo);

			serialNumberCounter.add(BigInteger.ONE);

			X509CertificateHolder certHolder = certificateBuilder.build(signature);
			JcaX509CertificateConverter jcaConverter = new JcaX509CertificateConverter().setProvider("BC");
			newCertificate = jcaConverter.getCertificate(certHolder);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return newCertificate;
	}

	public void saveCertificate(String path, X509Certificate cert) {
		try (PemWriter writer = new PemWriter(new PrintWriter(new File(path)));) {
			// os.write("-----BEGIN CERTIFICATE-----\n".getBytes("US-ASCII"));
			// os.write(Base64.encodeBase64(cert.getEncoded(), true));
			// os.write("-----END CERTIFICATE-----\n".getBytes("US-ASCII"));
			// os.close();

			// write it as PEM file

			// X.509 cert version
			PemObjectGenerator generator = new PemObjectGenerator() {
				@Override
				public PemObject generate() throws PemGenerationException {
					try {
						return new PemObject(cert.getType(), cert.getTBSCertificate());
					} catch (CertificateEncodingException e) {
						e.printStackTrace();
						return null;
					}
				}
			};

			writer.writeObject(generator);
			writer.flush();
			writer.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// private X509Certificate readCertificate(String path) throws
	// CertificateException {
	// String base64 = new String();
	// try (BufferedReader in = new BufferedReader(new FileReader(path));) {
	// String line = in.readLine();
	// if (line.contains("-----BEGIN CERTIFICATE-----") == false) {
	// throw new CertificateException("Invalid file");
	// }
	// line = line.substring(27);
	// boolean end = false;
	// while (!end) {
	// if (line.contains("-----")) {
	// end = true;
	// base64 += line.substring(0, line.indexOf("-----"));
	// } else {
	// base64 += line;
	// line = in.readLine();
	// }
	// }
	// } catch (IOException ex) {
	// ex.printStackTrace();
	// }
	//
	// byte[] certifacteData = Base64.decode(base64);
	// return X509Certificate.getInstance(certifacteData);
	// }

	private PrivateKey readPrivateKey(String path) {
		PrivateKey privKey = null;

		// try (BufferedReader br = new BufferedReader(new FileReader(path));) {
		// String base64 = new String();
		// String line;
		// while ((line = br.readLine()) != null) {
		// base64 += line;
		// }
		//
		// byte[] privKeyBytes = Base64.decode(base64);
		// PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privKeyBytes);
		// KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		// privKey = keyFactory.generatePrivate(keySpec);
		//
		// } catch (Exception ex) {
		// ex.printStackTrace();
		// }

		try (PEMParser pemParser = new PEMParser(new FileReader(path));) {
			Object object = pemParser.readObject();
			JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
			PEMKeyPair ukp = (PEMKeyPair) object;
			KeyPair kp = converter.getKeyPair(ukp);
			privKey = kp.getPrivate();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return privKey;
	}
}
