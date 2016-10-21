package org.etsit.uma.androidrsa.server.business;

import java.io.FileInputStream;
import java.util.Properties;

import javax.annotation.Resource;

import org.etsit.uma.androidrsa.server.mail.SupportMailRunnable;
import org.etsit.uma.androidrsa.server.util.ThreadManager;

public class SupportEmailServiceBusinessLogic {

	// PATHS
	@Resource(name = "mailPropertiesPath")
	private String mailPropertiesPath;

	public void sendSupportEmail(final String firstName, final String lastName, final String email,
			final String message) {
		Properties mailProperties = new Properties();
		try {
			mailProperties.load(new FileInputStream(mailPropertiesPath));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		ThreadManager.execute(new SupportMailRunnable(mailProperties, firstName, lastName, email, message));
	}

}
