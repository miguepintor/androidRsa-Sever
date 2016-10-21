package org.etsit.uma.androidrsa.server.rest.dto;

public class SupportEmailDto {
	private String firstName;
	private String lastName;
	private String email;
	private String message;
	
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SupportEmailDto [firstName=").append(firstName).append(", lastName=").append(lastName)
				.append(", email=").append(email).append(", message=").append(message).append("]");
		return builder.toString();
	}
	
}
