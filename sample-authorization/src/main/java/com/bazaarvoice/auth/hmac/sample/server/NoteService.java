package com.bazaarvoice.auth.hmac.sample.server;

import com.bazaarvoice.auth.hmac.sample.server.auth.SecureRPC;
import com.bazaarvoice.auth.hmac.sample.server.auth.SecureRPCAuthorizer;
import com.bazaarvoice.auth.hmac.sample.server.auth.SimpleAuthenticator;
import com.bazaarvoice.auth.hmac.sample.server.auth.User;
import com.bazaarvoice.auth.hmac.server.DefaultRequestHandler;
import com.bazaarvoice.auth.hmac.server.HmacAuthProvider;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;

public class NoteService extends Service<Configuration> {

    public static void main(String[] args) throws Exception {
        new NoteService().run(args);
    }

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.setName("pizza-application");
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
        environment.addResource(new NoteResource());
        environment.addHealthCheck(new NoteHealthCheck());
        environment.addProvider(new HmacAuthProvider<SecureRPC, User>(new DefaultRequestHandler<>(new SimpleAuthenticator(), new SecureRPCAuthorizer())) {});
    }
}
