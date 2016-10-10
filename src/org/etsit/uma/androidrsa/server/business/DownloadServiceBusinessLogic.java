package org.etsit.uma.androidrsa.server.business;

import javax.annotation.Resource;

import org.etsit.uma.androidrsa.server.utility.Compressor;

public class DownloadServiceBusinessLogic {

	@Resource(name = "androidRsaApkPath")
	private String androidRsaApkPath;

	@Resource(name = "temporalOutputFolderPath")
	private String temporalOutputFolderPath;

	public void download() {
		String outputUnpackedFolderPath = temporalOutputFolderPath + "/unpacked";
		String outputCompressFilePath = temporalOutputFolderPath
				+ androidRsaApkPath.substring(androidRsaApkPath.lastIndexOf("/") );

		Compressor compressor = new Compressor();
		compressor.decompressFile(androidRsaApkPath, outputUnpackedFolderPath);
		compressor.compressFolder(outputUnpackedFolderPath, outputCompressFilePath);

	}

}
