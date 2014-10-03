package com.bazaarvoice.auth.hmac.cli;

import com.bazaarvoice.auth.hmac.client.HmacClientFilter;
import com.bazaarvoice.auth.hmac.common.RequestConfiguration;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.MutuallyExclusiveGroup;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static net.sourceforge.argparse4j.impl.Arguments.storeConst;
import static net.sourceforge.argparse4j.impl.Arguments.storeTrue;

public class HurlCli {
    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("hurl")
            .description("Like curl, for hmac-protected resources")
            .defaultHelp(true);

        parser.addArgument("-X", "--request").help("GET (default), POST, PUT, or DELETE").type(String.class).choices("GET", "POST", "PUT", "DELETE").setDefault("GET");
        parser.addArgument("--apiKey").required(true);
        parser.addArgument("--secretKey").required(true);
        parser.addArgument("-v", "--verbose").action(storeTrue()).help("Prints additional information to stderr");
        parser.addArgument("--data", "--data-binary").required(false).help("The data to use in a POST (or @filename for a file full of data)");

        MutuallyExclusiveGroup contentTypes = parser.addMutuallyExclusiveGroup();
        contentTypes.addArgument("-C", "--content-type").help("Content type to send in the Content-Type request header");
        contentTypes.addArgument("-J", "--json").dest("content_type").setConst("application/json").action(storeConst()).help("Specifies application/json in the Content-Type request header");

        parser.addArgument("--headerSignature").setDefault(RequestConfiguration.DEFAULT_SIGNATURE_HTTP_HEADER).help("Override the http header");
        parser.addArgument("--headerTimestamp").setDefault(RequestConfiguration.DEFAULT_TIMESTAMP_HTTP_HEADER).help("Override the timestamp header");
        parser.addArgument("--headerVersion").setDefault(RequestConfiguration.DEFAULT_VERSION_HTTP_HEADER).help("Override the version header");
        parser.addArgument("--apiKeyParamName").setDefault(RequestConfiguration.DEFAULT_API_KEY_QUERY_PARAM).help("Override the API KEY query parameter name");

        parser.addArgument("url").required(true);

        try {
            Namespace ns = parser.parseArgs(args);
            String method = ns.getString("request");
            String url = ns.getString("url");
            String apiKey = ns.getString("apiKey");
            String secretKey = ns.getString("secretKey");
            String contentType = ns.getString("content_type");
            boolean verbose = ns.getBoolean("verbose");

            byte[] data = getData(ns.getString("data"));

            final RequestConfiguration requestConfiguration = RequestConfiguration.builder()
                    .withSignatureHttpHeader(ns.getString("headerSignature"))
                    .withTimestampHttpHeader(ns.getString("headerTimestamp"))
                    .withVersionHttpHeader(ns.getString("headerVersion"))
                    .withApiKeyQueryParamName(ns.getString("apiKeyParamName"))
                    .build();

            String payload = run(method, url, apiKey, secretKey, data, contentType, verbose, requestConfiguration);

            if (payload != null) {
                System.out.println(payload); // TODO: do we want this newline?  may screw up binary data... but do we want to use Strings then?
            }

        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(-1);
        } catch (IOException e) {
            System.err.println("Could not handle input data: " + e.getMessage());
            e.printStackTrace();
            System.exit(-2);
        }

        System.out.flush();
        System.err.flush();
    }

    private static byte[] getData(String data) throws IOException {
        // if this starts with @ then let's try to find the file
        if (data != null) {
            if (data.startsWith("@")) {
                String filename = data.substring(1);
                if (new File(filename).exists()) {
                    return Files.readAllBytes(Paths.get(filename));
                } else {
                    throw new FileNotFoundException(filename);
                }
            } else {
                return data.getBytes();
            }
        } else {
            return null;
        }
    }

    private static String run(String method, String url, String apiKey, String secretKey, byte[] requestData,
                              String contentType, boolean verbose, RequestConfiguration requestConfiguration) {
        Client client = createClient(apiKey, secretKey, requestConfiguration);

        if (verbose) {
            System.err.printf("method: %s, api key: '%s', url: '%s'%n", method, apiKey, url);
            if (requestData != null && requestData.length > 0) {
                System.err.printf("request data is %d bytes.%n", requestData.length);
            }
        }

        WebResource.Builder request = client.resource(url).getRequestBuilder();
        if (contentType != null && contentType.length() > 0) {
            request.header("Content-Type", contentType);
        }

        if ("POST".equalsIgnoreCase(method)) {
            return request.post(String.class, requestData);
        } else if ("PUT".equalsIgnoreCase(method)) {
            return request.put(String.class, requestData);
        } else if ("GET".equalsIgnoreCase(method)) {
            return request.get(String.class);
        } else if ("DELETE".equalsIgnoreCase(method)) {
            request.delete();
            return null;
        } else {
            return null;
        }
    }

    private static Client createClient(String apiKey, String secretKey, RequestConfiguration requestConfiguration) {
        Client client = Client.create();
        client.addFilter(new HmacClientFilter(apiKey, secretKey, client.getMessageBodyWorkers(), requestConfiguration));
        return client;
    }
}
