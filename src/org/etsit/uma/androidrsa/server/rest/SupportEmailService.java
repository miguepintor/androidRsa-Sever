package org.etsit.uma.androidrsa.server.rest;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.etsit.uma.androidrsa.server.business.SupportEmailServiceBusinessLogic;

@Path("/support")
public class SupportEmailService {

	@Inject
	private SupportEmailServiceBusinessLogic bl;

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public void sendSupportEmail(@QueryParam("firstName") String firstName, @QueryParam("lastName") String lastName,
			@QueryParam("email") String email, @QueryParam("message") String message) {
		bl.sendSupportEmail(firstName, lastName, email, message);
	}

}
