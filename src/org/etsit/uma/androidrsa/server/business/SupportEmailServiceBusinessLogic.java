package org.etsit.uma.androidrsa.server.business;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Resource;

import org.etsit.uma.androidrsa.server.mail.SupportMailRunnable;

public class SupportEmailServiceBusinessLogic {

	// PATHS
	@Resource(name = "mailPropertiesPath")
	private String mailPropertiesPath;

	public void sendSupportEmail(final String firstName, final String lastName, final String email,
			final String message) {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Properties mailProperties = new Properties();
		try {
			mailProperties.load(new FileInputStream(mailPropertiesPath));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		executor.submit(new SupportMailRunnable(mailProperties, firstName, lastName, email, message));
	}

}
