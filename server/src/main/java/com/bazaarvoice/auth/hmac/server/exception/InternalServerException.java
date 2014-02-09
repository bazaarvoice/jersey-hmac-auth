package com.bazaarvoice.auth.hmac.server.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class InternalServerException extends WebApplicationException {
    public InternalServerException() {
        super(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
    }
}
