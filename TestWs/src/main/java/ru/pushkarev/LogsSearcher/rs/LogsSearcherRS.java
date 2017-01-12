package ru.pushkarev.LogsSearcher.rs;



import ru.pushkarev.LogsSearcher.type.Domain;
import ru.pushkarev.LogsSearcher.type.ServiceController;
import ru.pushkarev.LogsSearcher.utils.Config;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.File;

@Path("/get")
public class LogsSearcherRS {

    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ru.pushkarev.LogsSearcher.type.Response search(ru.pushkarev.LogsSearcher.type.Request request) {
        return new ServiceController().processRequest(request);
    }

    @GET
    @Path("{filename}")
    @Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.TEXT_PLAIN})
    public Response getFile(@PathParam("filename") String filename) {
        Response.ResponseBuilder response = Response.status(200);

        File file = Config.getInstance().workingDirectory.resolve(filename).toFile();
        if(file != null && file.isFile() && file.canWrite()) {
            response = Response.ok(file);
            response.header("Content-Disposition", "attachment; filename=" + file.getName());
        } else {
            response.status(404);
            response.entity("File is not ready yet.");
        }

        return response.build();
    }

}