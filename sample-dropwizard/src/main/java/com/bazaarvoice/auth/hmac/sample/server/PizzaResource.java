package com.bazaarvoice.auth.hmac.sample.server;

import com.bazaarvoice.auth.hmac.sample.Pizza;
import com.bazaarvoice.auth.hmac.server.HmacAuth;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;

@Path("/pizza")
@Produces(MediaType.APPLICATION_JSON)
public class PizzaResource {

    @GET
    public Pizza get(@HmacAuth String principal) {
        Pizza pizza = new Pizza();
        pizza.setSize("medium");
        pizza.setToppings(Arrays.asList("cheese", "olives"));
        return pizza;
    }
}
