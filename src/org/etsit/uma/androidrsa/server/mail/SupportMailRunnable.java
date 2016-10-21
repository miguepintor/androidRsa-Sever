package org.etsit.uma.androidrsa.server.mail;

import java.util.Properties;

public class SupportMailRunnable extends MailRunnable {
	private final String firstName;
	private final String lastName;
	private final String email;
	private final String message;

	public SupportMailRunnable(final Properties mailProperties, final String firstName, final String lastName,
			final String email, final String message) {
		super(mailProperties, mailProperties.getProperty("gmail.username"));
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.message = message;

	}

	@Override
	protected String getBody() {
		return "Dear supporters,\n\n " + firstName + " " + lastName + " with email " + email
				+ " wrote this message through the web: \n\n" + message + "\n\n Bests regards!";
	}

	@Override
	protected String getSubject() {
		return "RsaApplicaion Support Email";
	}
}
