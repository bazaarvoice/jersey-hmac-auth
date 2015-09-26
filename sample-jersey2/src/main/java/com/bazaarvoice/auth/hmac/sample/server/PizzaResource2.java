package com.bazaarvoice.auth.hmac.sample.server;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bazaarvoice.auth.hmac.server.HmacAuth;

/**
 * Jersey 2.x HMAC-authenticated REST resource
 */
@Path("/pizza")
@Produces(APPLICATION_JSON)
public class PizzaResource2 {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @POST
    public void bakePizza(@HmacAuth final String principal) {
        logger.info("Baking a pizza for {}.", principal);
    }

}