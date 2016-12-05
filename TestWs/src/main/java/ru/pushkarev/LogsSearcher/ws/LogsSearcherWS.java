package ru.pushkarev.LogsSearcher.ws;

import ru.pushkarev.LogsSearcher.type.Searcher;
import ru.pushkarev.LogsSearcher.type.Domain;
import ru.pushkarev.LogsSearcher.type.Query;
import ru.pushkarev.LogsSearcher.type.Request;
import ru.pushkarev.LogsSearcher.utils.Stopwatch;
import ru.pushkarev.LogsSearcher.type.Response;

import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebService;
import java.util.logging.Logger;


@WebService(name = "LogsSearcher")
@Stateless
public class LogsSearcherWS {
    public static Logger log = Logger.getLogger(LogsSearcherWS.class.getName());
    static Domain domain;

    @WebMethod(operationName = "Search")
    public Response search(Request request) {

        // TODO read about static. Rewrite static modifiers in class Domain

        request.validateRequest();
        Query query = new Query(request);
        Searcher searcher = new Searcher(query);

        Stopwatch stopwatch = new Stopwatch();
        Response response = searcher.run();
        response.setSearchTime(stopwatch.getDuration());

        return response;
    }

    public LogsSearcherWS() {
        domain = new Domain();
    }
}
