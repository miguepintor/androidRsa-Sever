package org.etsit.uma.androidrsa.server.mail;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public abstract class MailRunnable implements Runnable{
	private final Properties mailProperties;
	private final String destinationEmail;
	
	public MailRunnable(final Properties mailProperties, final String destinationEmail) {
		this.mailProperties = mailProperties;
		this.destinationEmail = destinationEmail;
	}

	@Override
	public void run() {
		Session session = Session.getInstance(mailProperties, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(mailProperties.getProperty("mail.smtp.user"), mailProperties.getProperty("mail.smtp.password"));
			}
		});

		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("androidRsa@gmail.com"));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinationEmail));
			message.setSubject(getSubject());
			message.setText(getBody());
			Transport.send(message);
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}

	abstract protected String getBody();

	abstract protected String getSubject();
}
