package com.bazaarvoice.auth.hmac.sample.server.model;

public class Note {
    private String content;

    public Note(String content) {
        this.content = content;
    }

    /* For Jackson */
    private Note() {}

    public String getContent() {
        return content;
    }

    public void setContent(final String content) {
        this.content = content;
    }
}
