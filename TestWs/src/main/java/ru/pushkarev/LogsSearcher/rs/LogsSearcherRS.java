package ru.pushkarev.LogsSearcher.rs;

import ru.pushkarev.LogsSearcher.type.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/get")
public class LogsSearcherRS {
    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response search(Request request) {

        return new ServiceController().processRequest(request);
    }

}