package com.bazaarvoice.auth.hmac.sample.client;

import com.bazaarvoice.auth.hmac.client.HmacClientFilter;
import com.bazaarvoice.auth.hmac.sample.server.model.Note;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Collection;

/**
 * This is a Java SDK for the Pizza API. It uses jersey-hmac-auth to build requests such that they can be
 * authenticated by the API.
 */
public class NoteClient {
    private final UriBuilder uriBuilder;
    private final Client jerseyClient;

    public NoteClient(URI serviceUrl, String apiKey, String secretKey) {
        this.uriBuilder = UriBuilder.fromUri(serviceUrl);
        this.jerseyClient = createClient(apiKey, secretKey);
    }

    public Note createNote(String id, Note note) {
        // Expect the second user to fail to create and delete.
        try {
            URI uri = uriBuilder.clone()
                    .segment("notes",  id)
                    .build();

            Note n = jerseyClient.resource(uri)
                    .type(MediaType.APPLICATION_JSON)
                    .put(Note.class, note);

            if (n != null) {
                System.out.println("Overwrote note: " + n.getContent());
            }

            return n;
        } catch (Exception ignored) {
            return null;
        }
    }

    public Note deleteNote(String id) {
        // Expect the second user to fail to create and delete.
        try {
            URI uri = uriBuilder.clone()
                    .segment("notes", id)
                    .build();

            Note n = jerseyClient.resource(uri)
                    .type(MediaType.APPLICATION_JSON)
                    .delete(Note.class);

            if (n != null) {
                System.out.println("Deleted note: " + n.getContent());
            }

            return n;
        } catch (Exception ignored) {
            return null;
        }
    }

    public Collection<Note> getNotes() {
        URI uri = uriBuilder.clone()
                .segment("notes")
                .build();

        return jerseyClient.resource(uri)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(new GenericType<Collection<Note>>() {});
    }

    private static Client createClient(String apiKey, String secretKey) {
        Client client = Client.create();
        client.addFilter(new HmacClientFilter(apiKey, secretKey, client.getMessageBodyWorkers()));
        return client;
    }
}
