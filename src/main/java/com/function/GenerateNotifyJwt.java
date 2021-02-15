package com.function;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Azure Functions with HTTP Trigger.
 */
public class GenerateNotifyJwt {
    /**
     * This function listens at endpoint "/api/GenerateNotifyJwt". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/GenerateNotifyJwt
     * 2. curl {your host}/api/GenerateNotifyJwt?name=HTTP%20Query
     */
    @FunctionName("GenerateNotifyJwt")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST}, authLevel = AuthorizationLevel.FUNCTION) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        // Parse query parameter
        String query = request.getQueryParameters().get("name");
        String name = request.getBody().orElse(query);

        String apiKey = name.substring(name.length() - 73);
        String iss = apiKey.substring(0, 36); // First UUID
        String secret = apiKey.substring(37); // Second UUID after delimiter
        Algorithm secretAlgorithm = Algorithm.HMAC256(secret);
        String token = JWT.create()
            .withIssuer(iss)
            .withIssuedAt(Date.from(Instant.now()))
            .sign(secretAlgorithm);

        Map<String, String> map = new HashMap<>();
        map.put("token", "Bearer " + token);

        return request.createResponseBuilder(HttpStatus.OK).body(map).build();
        

        // if (name == null) {
        //     return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a name on the query string or in the request body").build();
        // } else {
        //     return request.createResponseBuilder(HttpStatus.OK).body("Hello, " + name).build();
        // }
    }
}
