package org.etsit.uma.androidrsa.server.mail;

import java.util.Properties;

public class EncryptedPasswordMailRunnable extends MailRunnable {
	private final String encryptionPassword;

	public EncryptedPasswordMailRunnable(final Properties mailProperties, final String destinationEmail,
			final String encryptionPassword) {
		super(mailProperties, destinationEmail);
		this.encryptionPassword = encryptionPassword;
	}

	@Override
	protected String getBody() {
		return "Dear user,\n\n This is your encryption password that you will need for run the androidRsa application: "
				+ encryptionPassword + "\n\n Bests regards!";
	}

	@Override
	protected String getSubject() {
		return "RsaApplicaion Encryption password";
	}
}
