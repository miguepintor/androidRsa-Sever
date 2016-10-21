package org.etsit.uma.androidrsa.server.util.rsa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMEncryptor;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaMiscPEMGenerator;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.openssl.jcajce.JcePEMEncryptorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.io.pem.PemGenerationException;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemObjectGenerator;
import org.bouncycastle.util.io.pem.PemWriter;

public class CertificateGenerator {
	private static BigInteger serialNumberCounter = BigInteger.ONE;
	private static final int RSA_KEY_SIZE = 2048;

	public static BigInteger getSerialNumberCounter() {
		return serialNumberCounter;
	}

	public RsaCertificate generateCertificate(String caPrivateKeyPath, String commonName) {
		RsaCertificate genertatedCertificate = null;
		try {
			PrivateKey caPrivKey = readPrivateKey(caPrivateKeyPath, null);
			ContentSigner signature = new JcaContentSignerBuilder("SHA1withRSAEncryption")
					.setProvider(BouncyCastleProvider.PROVIDER_NAME).build(caPrivKey);

			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(RSA_KEY_SIZE);
			KeyPair keyPair = keyGen.generateKeyPair();

			SubjectPublicKeyInfo subPubKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded());

			Calendar c = Calendar.getInstance();
			c.set(Calendar.HOUR_OF_DAY, 0);

			Date startDate = c.getTime();
			c.add(Calendar.YEAR, 10);
			Date endDate = c.getTime();

			X509v1CertificateBuilder certificateBuilder = new X509v1CertificateBuilder(
					new X500Name("CN=AndroidRsa CA,O=etsit uma,C=ES"), serialNumberCounter, startDate, endDate,
					new X500Name("CN=" + commonName + ",O=etsit uma,C=ES"), subPubKeyInfo);

			serialNumberCounter = serialNumberCounter.add(BigInteger.ONE);

			X509CertificateHolder certHolder = certificateBuilder.build(signature);
			JcaX509CertificateConverter jcaConverter = new JcaX509CertificateConverter()
					.setProvider(BouncyCastleProvider.PROVIDER_NAME);
			X509Certificate certificate = jcaConverter.getCertificate(certHolder);

			genertatedCertificate = new RsaCertificate(certificate, keyPair.getPrivate());
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}

		return genertatedCertificate;
	}

	public String save(String path, Object obj) {
		String encryptionPassword = null;
		try (PemWriter writer = new PemWriter(new PrintWriter(new File(path)));) {
			PemObjectGenerator generator = null;
			if (obj instanceof X509Certificate) {
				generator = new PemObjectGenerator() {
					@Override
					public PemObject generate() throws PemGenerationException {

						try {
							return new PemObject("CERTIFICATE", ((X509Certificate) obj).getEncoded());
						} catch (CertificateEncodingException e) {
							throw new RuntimeException(e);
						}

					}
				};
			} else if (obj instanceof PrivateKey) {
				SecureRandom random = new SecureRandom();
				byte[] keyBytes = new byte[16];
				random.nextBytes(keyBytes);
				encryptionPassword = Base64.toBase64String(keyBytes);

				PEMEncryptor encryptor = new JcePEMEncryptorBuilder("AES-128-ECB")
						.setProvider(BouncyCastleProvider.PROVIDER_NAME).setSecureRandom(new SecureRandom())
						.build(encryptionPassword.toCharArray());
				generator = new JcaMiscPEMGenerator((PrivateKey) obj, encryptor);
			} else {
				throw new RuntimeException("The object to save is not a X509Certificate or PrivateKey");
			}

			writer.writeObject(generator);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}

		return encryptionPassword;
	}

	public X509Certificate readCertificate(String path) {
		X509Certificate certificate = null;
		try (BufferedReader in = new BufferedReader(new FileReader(path));) {
			String base64 = new String();
			String line;

			while ((line = in.readLine()) != null) {
				if (!line.contains("-----")) {
					base64 += line;
				}
			}

			byte[] certifacteData = Base64.decode(base64);

			X509CertificateHolder certHolder = new X509CertificateHolder(certifacteData);
			certificate = new JcaX509CertificateConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME)
					.getCertificate(certHolder);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}

		return certificate;
	}

	public PrivateKey readPrivateKey(String path, String encryptionPassword) throws FileNotFoundException, IOException {
		PrivateKey privKey = null;
		try (PEMParser pemParser = new PEMParser(new FileReader(path));) {
			Object object = pemParser.readObject();
			JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME);
			if (object instanceof PEMEncryptedKeyPair) {
				PEMEncryptedKeyPair ckp = (PEMEncryptedKeyPair) object;
				PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder()
						.build(encryptionPassword.toCharArray());
				KeyPair kp = converter.getKeyPair(ckp.decryptKeyPair(decProv));
				privKey = kp.getPrivate();
			} else {
				PEMKeyPair ukp = (PEMKeyPair) object;
				KeyPair kp = converter.getKeyPair(ukp);
				privKey = kp.getPrivate();
			}
		}
		return privKey;
	}
}
