package org.etsit.uma.androidrsa.server.business;

import java.io.File;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

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

	public File download(String ownerName) {
		Compressor compressor = new Compressor();
		CertificateGenerator generator = new CertificateGenerator();

		compressor.decompressFile(androidRsaApkPath, decompressFolderPath);

		RsaCertificate generatedCert = generator.generateCertificate(caPrivateKeyPath, ownerName);

		generator.save(generatedCertificateOutputPath, generatedCert.getX509certificate());
		generator.save(generatedCertificatePrivateKeyOutputPath, generatedCert.getPrivateKey());
		generator.save(caCertificateOutputPath, generator.readCertificate(caCertificatePath));

		compressor.compressFolder(decompressFolderPath, compressFilePath);
		
		return new File(compressFilePath);
	}

}
