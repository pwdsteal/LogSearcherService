package ru.pushkarev.LogsSearcher.rs;

import ru.pushkarev.LogsSearcher.type.Query;
import ru.pushkarev.LogsSearcher.type.Request;
import ru.pushkarev.LogsSearcher.type.Searcher;
import ru.pushkarev.LogsSearcher.utils.Stopwatch;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import ru.pushkarev.LogsSearcher.type.Response;

@Path("/get")
public class LogsSearcherRS {
    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAll(Request request) {

        System.out.println(request.getSearchString());

        request.validateRequest();
        Query query = new Query(request);
        Searcher searcher = new Searcher(query);

        Stopwatch stopwatch = new Stopwatch();
        Response response =  searcher.run();
        response.setSearchTime(stopwatch.getDuration());

        return response;
    }

}