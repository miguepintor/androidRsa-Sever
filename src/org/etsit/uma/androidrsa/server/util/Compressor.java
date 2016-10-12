package org.etsit.uma.androidrsa.server.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Compressor {

	private static final int BUFFER_LENGTH = 1024;

	public void decompressFile(String filePath, String outputFolderPath) {
		byte[] buffer = new byte[BUFFER_LENGTH];

		try (ZipInputStream zis = new ZipInputStream(new FileInputStream(filePath))) {
			// create output directory is not exists
			File folder = new File(outputFolderPath);
			if (!folder.exists()) {
				folder.mkdir();
			}

			ZipEntry ze = zis.getNextEntry();

			while (ze != null) {
				String fileName = ze.getName();
				File newFile = new File(outputFolderPath + File.separator + fileName);

				new File(newFile.getParent()).mkdirs();

				FileOutputStream fos = new FileOutputStream(newFile);

				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}

				fos.close();
				ze = zis.getNextEntry();
			}

		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

	private ArrayList<String> filesToCompressList;

	public void compressFolder(String folderPath, String outputFilePath) {
		byte[] buffer = new byte[BUFFER_LENGTH];
		filesToCompressList = new ArrayList<String>();

		generateFilesToCompressList(new File(folderPath), folderPath);

		try (FileOutputStream fos = new FileOutputStream(outputFilePath);
				ZipOutputStream zos = new ZipOutputStream(fos);) {
			for (String file : filesToCompressList) {
				ZipEntry ze = new ZipEntry(file);
				zos.putNextEntry(ze);
				
				FileInputStream in = new FileInputStream(folderPath + File.separator + file);

				int len;
				while ((len = in.read(buffer)) > 0) {
					zos.write(buffer, 0, len);
				}

				in.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private void generateFilesToCompressList(File node, final String sourceFolder) {
		if (node.isFile()) {
			String fileStr = node.getPath();
			String fileStrFormatted = fileStr.substring(sourceFolder.length() + 1, fileStr.length());
			filesToCompressList.add(fileStrFormatted);
		}

		if (node.isDirectory()) {
			String[] subNote = node.list();
			for (String filename : subNote) {
				generateFilesToCompressList(new File(node, filename), sourceFolder);
			}
		}

	}
}
