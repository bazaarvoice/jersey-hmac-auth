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
 * This is a Java SDK for the Note API. It uses jersey-hmac-auth to build requests such that they can be
 * authenticated and authorized by the API.
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

            Collection<Note> cn = jerseyClient.resource(uri)
                    .type(MediaType.APPLICATION_JSON)
                    .put(new GenericType<Collection<Note>>() {}, note);

            Note n = cn.size() > 0 ? cn.iterator().next() : null;

            if (n != null) {
                System.out.println("Overwrote note: " + n.getContent() + " with: " + note.getContent());
            } else {
                System.out.println("Wrote note: " + note.getContent());
            }

            return n;
        } catch (Exception ignored) {
            System.out.println("Unauthorized to write note: " + id);
            return null;
        }
    }

    public Note deleteNote(String id) {
        // Expect the second user to fail to create and delete.
        try {
            URI uri = uriBuilder.clone()
                    .segment("notes", id)
                    .build();

            Collection<Note> cn = jerseyClient.resource(uri)
                    .type(MediaType.APPLICATION_JSON)
                    .delete(new GenericType<Collection<Note>>() {});

            Note n = cn.size() > 0 ? cn.iterator().next() : null;

            if (n != null) {
                System.out.println("Deleted note: " + n.getContent());
            } else {
                System.out.println("No note to delete with id: " + id);
            }

            return n;
        } catch (Exception ignored) {
            System.out.println("Unauthorized to delete note: " + id);
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
