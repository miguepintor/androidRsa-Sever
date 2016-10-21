package org.etsit.uma.androidrsa.server.business;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.etsit.uma.androidrsa.server.mail.EncryptedPasswordMailRunnable;
import org.etsit.uma.androidrsa.server.util.Compressor;
import org.etsit.uma.androidrsa.server.util.rsa.CertificateGenerator;
import org.etsit.uma.androidrsa.server.util.rsa.RsaCertificate;

public class DownloadServiceBusinessLogic {

	// PATHS
	@Resource(name = "caPrivateKeyPath")
	private String caPrivateKeyPath;

	@Resource(name = "androidRsaApkPath")
	private String androidRsaApkPath;

	@Resource(name = "caCertificatePath")
	private String caCertificatePath;
	
	@Resource(name = "mailPropertiesPath")
	private String mailPropertiesPath;

	private String decompressFolderPath;
	private String compressFilePath;

	private String generatedCertificateOutputPath;
	private String generatedCertificatePrivateKeyOutputPath;
	private String caCertificateOutputPath;

	@PostConstruct
	public void resolvePaths() {
		String outputFolderPath = "./out";

		decompressFolderPath = outputFolderPath + "/decompress";
		compressFilePath = outputFolderPath + androidRsaApkPath.substring(androidRsaApkPath.lastIndexOf("/"));

		generatedCertificateOutputPath = decompressFolderPath + "/res/raw/certificate.crt";
		generatedCertificatePrivateKeyOutputPath = decompressFolderPath + "/res/raw/Key_certificate.pem";
		caCertificateOutputPath = decompressFolderPath + "/res/raw/ca.crt";
	}

	public File downloadApkAndSendPasswordByEmail(String ownerName, String email) {
		Compressor compressor = new Compressor();
		CertificateGenerator generator = new CertificateGenerator();

		compressor.decompressFile(androidRsaApkPath, decompressFolderPath);

		RsaCertificate generatedCert = generator.generateCertificate(caPrivateKeyPath, ownerName);

		generator.save(generatedCertificateOutputPath, generatedCert.getX509certificate());
		String encryptionPassword = generator.save(generatedCertificatePrivateKeyOutputPath, generatedCert.getPrivateKey());
		generator.save(caCertificateOutputPath, generator.readCertificate(caCertificatePath));

		compressor.compressFolder(decompressFolderPath, compressFilePath);
		
		sendPasswordThroughEmail(email, encryptionPassword);
		
		return new File(compressFilePath);
	}
	
	private void sendPasswordThroughEmail(String email, String password){
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Properties mailProperties = new Properties();
		try {
			mailProperties.load(new FileInputStream(mailPropertiesPath));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		executor.submit(new EncryptedPasswordMailRunnable(mailProperties, email, password));
	}

}
