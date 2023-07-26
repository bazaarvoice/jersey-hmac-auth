package com.bazaarvoice.auth.hmac.dropwizard.server;

import com.bazaarvoice.auth.hmac.dropwizard.Pizza;
import com.bazaarvoice.auth.hmac.server.HmacAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;

@Path("/pizza")
@Produces(MediaType.APPLICATION_JSON)
public class PizzaResource {
    private static final Logger log = LoggerFactory.getLogger(PizzaResource.class);

    @GET
    public Pizza get(@HmacAuth String principal) {
        log.info("Pizza requested by: " + principal);

        Pizza pizza = new Pizza();
        pizza.setSize("medium");
        pizza.setToppings(Arrays.asList("cheese", "olives"));
        return pizza;
    }
}
