package com.fitness.gateway;

import com.fitness.gateway.user.RegisterRequest;
import com.fitness.gateway.user.UserService;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.text.ParseException;

@Component
@Slf4j
@RequiredArgsConstructor
// This WebFilter is basically used to intercept any web request
public class KeycloakUserSyncFilter implements WebFilter {

    private final UserService userService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
//        Mono we used this is basically used for Promise means in future it will return some value for asynchronous call
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-ID"); // this one is optional cause sometimes we send user id in header so to handle this so our code should not break
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        RegisterRequest registerRequest = getUserDetails(token);
        if (userId == null) { // means from header if userId is not found then we will take from RegisterRequest
            userId = registerRequest.getKeycloakId();
        }

        if (userId != null && token != null) {
            String finalUserId = userId;
            return userService.validateUser(userId)
                    .flatMap(exist -> {
                        if (!exist) {
                            if (registerRequest != null){
                                return userService.registerUser(registerRequest)
                                        .then(Mono.empty());
                            } else {
                                return Mono.empty();
                            }
                        } else {
                            log.info("User already exist, Skipping sync");
                            return Mono.empty();
                        }
                    })
                    .then(Mono.defer(() -> { // this Mono.defer means this part of code will not run until the abpve part of code execution finished means run this part of code after finishing the above code execution
                        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                .header("X-User-ID", finalUserId)
                                .build();
                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                    }));
        }

        return chain.filter(exchange);
    }

    private RegisterRequest getUserDetails(String token) {
        String tokenWithoutBearer = token.replace("Bearer", "").trim(); // Bearer <TOKEN> we need to replace this Bearer
        try {
            SignedJWT signedJWT = SignedJWT.parse(tokenWithoutBearer);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet(); // claims means payload from JWT if you see in JWTR.io for decoder claims table


            /*
            {
              "exp": 1788592416,
              "iat": 1788592116,
              "auth_time": 1788590322,
              "jti": "onrtrt:fd1602e8-0ca2-dd9c-5e20-3aef135db234",
              "iss": "http://localhost:8181/realms/fitness-app",
              "aud": "account",
              "sub": "17fe8b27-2200-46d2-ae6c-1f740f6a6afb",
              "typ": "Bearer",
              "azp": "oauth2-pkce-client",
              "sid": "ZvfUjTqHVolAmyXpSD9D6z1x",
              "acr": "1",
              "allowed-origins": [
                "http://localhost:5173"
              ],
              "realm_access": {
                "roles": [
                  "offline_access",
                  "uma_authorization",
                  "default-roles-fitness-app"
                ]
              },
              "resource_access": {
                "account": {
                  "roles": [
                    "manage-account",
                    "manage-account-links",
                    "view-profile"
                  ]
                }
              },
              "scope": "openid email profile",
              "email_verified": false,
              "name": "user1 first user1 last",
              "preferred_username": "user1",
              "given_name": "user1 first",
              "family_name": "user1 last",
              "email": "user1@gmail.com"
            }
            */
            RegisterRequest request = new RegisterRequest();
            // so now we will set those values from jwt to request
            request.setEmail(claims.getStringClaim("email"));
            request.setKeycloakId(claims.getStringClaim("sub")); // if you decode JWT access token then see in sub field it has keycloak id
            request.setFirstName(claims.getStringClaim("given_name"));
            request.setLastName(claims.getStringClaim("family_name"));
            request.setPassword("dummy@123123");

            return request;

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
