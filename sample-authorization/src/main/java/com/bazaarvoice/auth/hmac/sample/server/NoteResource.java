package com.bazaarvoice.auth.hmac.sample.server;

import com.bazaarvoice.auth.hmac.sample.server.auth.SecureRPC;
import com.bazaarvoice.auth.hmac.sample.server.auth.User;
import com.bazaarvoice.auth.hmac.sample.server.auth.UserRight;
import com.bazaarvoice.auth.hmac.sample.server.model.Note;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Iterator;
import java.util.Map;

@Path ("/notes")
@Produces (MediaType.APPLICATION_JSON)
@Consumes (MediaType.APPLICATION_JSON)
public class NoteResource {
    private static final Logger log = LoggerFactory.getLogger(NoteResource.class);

    private Map<String, Note> noteMap = Maps.newConcurrentMap();

    @PUT
    @Path("/{id}")
    public Note createNote(@PathParam("id") String id,
                           @SecureRPC(requiredRights = UserRight.CREATE_NOTE) User user,
                           Note note) {
        log.info("Note {} created by: {}", id, user.getName());
        return noteMap.put(id, note);
    }

    @DELETE
    @Path("/{id}")
    public Note deleteNote(@PathParam("id") String id,
                           @SecureRPC(requiredRights = UserRight.DELETE_NOTE) User user) {
        log.info("Note {} deleted by: {}", id, user.getName());
        return noteMap.remove(id);
    }

    @GET
    public Iterator<Note> viewNotes(@SecureRPC(requiredRights = UserRight.VIEW_NOTES) User user) {
        log.info("{} Notes viewed by: {}", noteMap.size(), user.getName());
        return noteMap.values().iterator();
    }

}
