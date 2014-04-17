package com.bazaarvoice.auth.hmac.sample.client;

import com.bazaarvoice.auth.hmac.sample.server.auth.SimpleAuthenticator;
import com.bazaarvoice.auth.hmac.sample.server.auth.User;
import com.bazaarvoice.auth.hmac.sample.server.model.Note;

import java.net.URI;

public class NoteClientTest {
    // This is the default endpoint for the Pizza service when you run it
    private static final URI ENDPOINT = URI.create("http://localhost:8080");

    public static void main(String[] args) throws Exception {
        for (User user : SimpleAuthenticator.USERS) {
            NoteClient client = new NoteClient(ENDPOINT, user.getApiKey(), user.getSecretKey());

            Note typoNote = new Note(user.getName() + " was here.");
            client.createNote("note1", typoNote);
            client.deleteNote("note1");

            Note wuzHere = new Note(user.getName() + " wuz here.");
            client.createNote("note2", wuzHere);

            System.out.println("Reading as User: " + user.getName());
            for (Note note : client.getNotes()) {
                System.out.println(note.getContent());
            }
        }
    }
}
