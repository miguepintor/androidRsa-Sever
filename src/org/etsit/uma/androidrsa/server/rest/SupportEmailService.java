package org.etsit.uma.androidrsa.server.rest;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.etsit.uma.androidrsa.server.business.SupportEmailServiceBusinessLogic;
import org.etsit.uma.androidrsa.server.rest.dto.SupportEmailDto;

@Path("/support")
public class SupportEmailService {

	@Inject
	private SupportEmailServiceBusinessLogic bl;

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public void sendSupportEmail(SupportEmailDto dto) {
		bl.sendSupportEmail(dto.getFirstName(), dto.getLastName(), dto.getEmail(), dto.getMessage());
	}

}
