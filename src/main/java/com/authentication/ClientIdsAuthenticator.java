package com.authentication;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.server.spi.Client;
import com.google.api.server.spi.Strings;
import com.google.api.server.spi.auth.EndpointsAuthenticator;
import com.google.api.server.spi.auth.GoogleAuth;
import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Singleton;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.io.Resources;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class ClientIdsAuthenticator extends EndpointsAuthenticator {

    private static final Logger logger = Logger.getLogger(ClientIdsAuthenticator.class.getName());

    private static final String TOKEN_INFO_ENDPOINT = "https://www.googleapis.com/oauth2/v2/tokeninfo?access_token=";


    private enum Environment {
        LOCAL,
        DEV,
        STAGING,
        RELEASE,
        PROD
    }

    private Supplier<List<String>> clientIdsSupplier = new Supplier<List<String>>() {
        @Override
        public List<String> get() {
            String envVariableValue = System.getenv("FOO_ENVIRONMENT");

            Environment environment = Environment.valueOf(envVariableValue);

            String clientIdsFileName;
            switch (environment) {
                case LOCAL:
                    clientIdsFileName = "client-ids-local";
                    break;
                case DEV:
                    clientIdsFileName = "client-ids-dev";
                    break;
                case STAGING:
                    clientIdsFileName = "client-ids-staging";
                    break;
                case RELEASE:
                    clientIdsFileName = "client-ids-release";
                    break;
                case PROD:
                    clientIdsFileName = "client-ids-prod";
                    break;
                default:
                    throw new IllegalStateException("Unknown environment setting: " + environment);
            }

            try {
                return Resources.readLines(Resources.getResource(clientIdsFileName), Charset.forName("UTF-8"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    };


    @Override
    public User authenticate(HttpServletRequest request) {
        User user = super.authenticate(request);
        if (user == null)
            return null;

        String authToken = GoogleAuth.getAuthToken(request);
        GoogleAuth.TokenInfo tokenInfo = generateTokenInfo(authToken);
        if (tokenInfo == null)
            return null;
        return Strings.isWhitelisted(tokenInfo.clientId, clientIdsSupplier.get()) ? null : user;
    }

    @Nullable
    private GoogleAuth.TokenInfo generateTokenInfo(@Nonnull String token) {
        return getTokenInfoRemote(Preconditions.checkNotNull(token));
    }


    private GoogleAuth.TokenInfo getTokenInfoRemote(String token) {
        try {
            HttpRequest request = Client.getInstance().getJsonHttpRequestFactory()
                    .buildGetRequest(new GenericUrl(TOKEN_INFO_ENDPOINT + token));
            return parseTokenInfo(request);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to retrieve tokeninfo", e);
            return null;
        }
    }

    private GoogleAuth.TokenInfo parseTokenInfo(HttpRequest request) throws IOException {
        GoogleAuth.TokenInfo info = request.execute().parseAs(GoogleAuth.TokenInfo.class);
        if (info == null || Strings.isEmptyOrWhitespace(info.email)) {
            logger.log(Level.WARNING, "Access token does not contain email scope");
            return null;
        }
        return info;
    }
}
