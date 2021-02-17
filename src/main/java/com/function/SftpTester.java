package com.function;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.util.Optional;

/**
 * Azure Functions with HTTP Trigger.
 */
public class SftpTester {
    /**
     * This function listens at endpoint "/api/SftpTester". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/SftpTester
     * 2. curl {your host}/api/SftpTester?name=HTTP%20Query
     */
    @FunctionName("SftpTester")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        // Parse query parameter
        String query = request.getQueryParameters().get("name");
        String name = request.getBody().orElse(query);
        String remoteUser = request.getQueryParameters().get("user");
        String remoteHost = request.getQueryParameters().get("host");
        String port = request.getQueryParameters().get("port");
        String remotePassword = request.getQueryParameters().get("pass");

        System.out.println("query are"
         + name + ","
         + remoteHost + ","
         + remoteUser + ","
         + port + ","
         + remotePassword + ","
        );

        if (name == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a name on the query string or in the request body").build();
        } else {
            Session session = null;
            try {
                session = new JSch().getSession(remoteUser, remoteHost, Integer.parseInt(port));
                session.setPassword(remotePassword);
                session.setConfig("StrictHostKeyChecking", "no");
                session.connect(20000);
                ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
                channelSftp.connect(20000);
            } catch (JSchException e) {
                throw new RuntimeException(e.getMessage(), e);
            } finally {
                if (session != null) {
                    session.disconnect();
                }
            }
            return request.createResponseBuilder(HttpStatus.OK).body("Hello, " + name).build();
        }
    }
}
