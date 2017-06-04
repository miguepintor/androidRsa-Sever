package org.etsit.uma.androidrsa.server.util;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Test;

public class CompressorTest {

	private final Compressor compressor = new Compressor();

	private final String tempFolderPath = "./temp";

	private final String testFilePath = tempFolderPath + "/test.text";
	private final String compresssedFilePath = tempFolderPath + "/test.zip";
	private final String decompressedFolderPath = tempFolderPath + "/decompress";
	private final String decompressedFilePath = decompressedFolderPath + "/test.text";

	@After
	public void removeTempFolder() throws IOException {
		delete(new File(tempFolderPath));
	}

	@Test
	public void fileCompressedAndDecompressedDoesNotChangeItsContent() throws IOException {
		String sampleContent = "Hello World!";

		createFileWithContent(testFilePath, sampleContent);

		compressor.compressFolder(tempFolderPath, compresssedFilePath);
		compressor.decompressFile(compresssedFilePath, decompressedFolderPath);

		String contentOfdecompressedFile = readFile(decompressedFilePath);

		assertTrue(contentOfdecompressedFile.contains(sampleContent));
	}

	private void createFileWithContent(String path, String content) throws IOException {
		File file = new File(path);
		file.getParentFile().mkdirs();
		try (PrintWriter out = new PrintWriter(file)) {
			out.println(content);
		}
	}

	private String readFile(String path) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		String decoded = new String(encoded);
		return decoded.substring(0, decoded.length() - 1);
	}

	private void delete(File file) throws IOException {
		if (file.isDirectory()) {
			if (file.list().length == 0) {
				file.delete();
			} else {
				String files[] = file.list();
				for (String temp : files) {
					File fileDelete = new File(file, temp);
					delete(fileDelete);
				}

				if (file.list().length == 0) {
					file.delete();
				}
			}
		} else {
			file.delete();
		}
	}

}
