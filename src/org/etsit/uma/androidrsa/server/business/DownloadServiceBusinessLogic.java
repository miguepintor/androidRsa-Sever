package org.etsit.uma.androidrsa.server.business;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.etsit.uma.androidrsa.server.util.Compressor;
import org.etsit.uma.androidrsa.server.util.rsa.CertificateGenerator;

public class DownloadServiceBusinessLogic {
	
	// PATHS
	@Resource(name = "caPrivateKeyPath")
	private String caPrivateKeyPath;

	@Resource(name = "androidRsaApkPath")
	private String androidRsaApkPath;

	@Resource(name = "temporalOutputFolderPath")
	private String temporalOutputFolderPath;

	private String unpackedOutputFolderPath;
	private String compressOutputFilePath;
	private String generatedCertificatePath;

	@PostConstruct
	public void reset() {
		unpackedOutputFolderPath = temporalOutputFolderPath + "/unpacked";
		compressOutputFilePath = temporalOutputFolderPath
				+ androidRsaApkPath.substring(androidRsaApkPath.lastIndexOf("/"));
		generatedCertificatePath = temporalOutputFolderPath + "/certificate.crt";
	}

	public void download() {

		Compressor compressor = new Compressor();
		CertificateGenerator certGen = new CertificateGenerator();

		compressor.decompressFile(androidRsaApkPath, unpackedOutputFolderPath);
		certGen.saveCertificate(generatedCertificatePath, certGen.generateCertificate(caPrivateKeyPath, "Mike"));
		compressor.compressFolder(unpackedOutputFolderPath, compressOutputFilePath);

	}

}
