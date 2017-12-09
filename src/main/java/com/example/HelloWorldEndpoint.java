package com.example;

import com.authentication.ClientIdsAuthenticator;
import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.UnauthorizedException;

@Api(
        name = "helloworld",
        version = "v1",
        // ClientIds whitelisting algorithm is defined in the below authenticator
        clientIds = "*", // we basically say, accept any token's origin, we take care of it into our Authenticator
        authenticators = ClientIdsAuthenticator.class

)
/**
 * Model endpoint
 */
public class HelloWorldEndpoint {

    @ApiMethod(
            httpMethod = ApiMethod.HttpMethod.GET
    )
    public Message getUserEmail(User user, @Named("hello") String hello) throws UnauthorizedException {

        if (user == null) {
            throw new UnauthorizedException("Invalid credentials");
        }

        Message message = new Message();
        message.setMessage(hello);
        return message;
    }
}
