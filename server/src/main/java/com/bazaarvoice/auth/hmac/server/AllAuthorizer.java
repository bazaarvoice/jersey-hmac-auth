package com.bazaarvoice.auth.hmac.server;

import com.bazaarvoice.auth.hmac.server.Authorizer;

import java.lang.annotation.Annotation;

public class AllAuthorizer<AnnotationType extends Annotation, PrincipalType> implements Authorizer<AnnotationType, PrincipalType> {
    @Override
    public boolean authorize(AnnotationType annotation, PrincipalType principal) {
        return true;
    }
}
