package ru.pushkarev.LogsSearcher.ws;

import ru.pushkarev.LogsSearcher.type.*;
import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebService;
import java.util.logging.Logger;


@WebService(name = "LogsSearcher")
@Stateless
public class LogsSearcherWS {
    public static Logger log = Logger.getLogger(LogsSearcherWS.class.getName());

    @WebMethod(operationName = "Search")
    public Response search(Request request) {

        return new ServiceController().processRequest(request);
    }

}
