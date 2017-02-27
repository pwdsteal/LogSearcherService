import ru.pushkarev.logssearcher.ws.*;
import util.EventLogger;


import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import java.io.IOException;
import java.io.PrintWriter;


@WebServlet(urlPatterns = "/gui")
//@ServletSecurity(value = @HttpConstraint(rolesAllowed = "webuser"))
public class GuiServlet extends HttpServlet {
    // TODO what for?
//    @WebServiceRef(wsdlLocation = "http://localhost:8080/helloservice-war/HelloService?WSDL")
//    private static LogsSearcherWSService service;


    protected void doPost(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        EventLogger.getInstance().logEvent(request);
        ru.pushkarev.logssearcher.ws.Request WSrequest = parseRequest(request);

        PushkarevLogsSearcher service = new LogsSearcherWSService().getPushkarevLogsSearcherPort();
        Response WSresponse = service.search(WSrequest);

        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {

            if (!WSresponse.getErrorsList().isEmpty()) {
                out.println("<h3> LogsSearcher service failed validate your request. <br> Error List: </h3>");

                out.print("<ol class=\"ui list\">");
                for (String errorMessage : WSresponse.getErrorsList()) {
                    out.print("<li>" + errorMessage + "</li>");
                }
                out.print("</ol>");

                return;
            }

            out.println("<p> Searching time " + WSresponse.getSearchTime() + " ms</p>");

            if (WSresponse.getServer().isEmpty() && WSresponse.getFilename() == null) {
                out.print("<h3> Nothing Found </h3>");
            }

            // print button Download
            if (WSresponse.getFilename() != null) {
                out.print("<form method=\"get\" action=\"getFile\">\n" +
                        "<button class=\"positive ui button\" type=\"submit\" name=\"filename\" value=\""
                        + WSresponse.getFilename() + "\">Download File</button>\n" + "</form>");
            } else {

                // print result on page
                for (ServerElement server : WSresponse.getServer()) {
                    if (server.getLogBlock().isEmpty()) {
                        break;
                    }
                    out.print("<h2> " + server.getName() + "</h2>");
                    out.print("<div class=\"ui divider\"></div>");

                    for (LogBlock logBlock : server.getLogBlock()) {
                        String tag;

                        if (logBlock.getLog().contains("<Info>")) {
                            tag = "<div class=\"ui info message\">";
                        } else if (logBlock.getLog().contains("<Warning>")) {
                            tag = "<div class=\"ui warning message\">";
                        } else if (logBlock.getLog().contains("<Error>")) {
                            tag = "<div class=\"ui error message\">";
                        } else {
                            tag = "<div class=\"ui positive message\">";
                        }

                        out.write(tag + "<h5>" + logBlock.getDate() + "</h5>");
                        out.write(logBlock.getLog().replaceAll("<", "&lt").substring(4) + "</div>");
                    }
                    out.println("<div class=\"ui divider\"></div>");
                }
            }

        }

    }

    protected void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        try (PrintWriter out = response.getWriter()) {
            out.println("GET RESPONSE");
        }

    }

    private ru.pushkarev.logssearcher.ws.Request parseRequest( javax.servlet.http.HttpServletRequest request ) {
        ru.pushkarev.logssearcher.ws.Request WSrequest = new ru.pushkarev.logssearcher.ws.Request();

        WSrequest.setSearchString(request.getParameter("searchString"));
        WSrequest.setTarget(request.getParameter("target"));
        WSrequest.setIsRegExp(Boolean.parseBoolean(request.getParameter("isRegExp")));
        WSrequest.setIsCaseSensitive(Boolean.parseBoolean(request.getParameter("isCaseSensitive")));
        WSrequest.setOutputFormat(request.getParameter("outputFormat"));

        String dates = request.getParameter("dates");
        if (!(null == dates || dates.isEmpty())) {
            for (String dateRange : request.getParameter("dates").split(",")) {
                String[] datePair = dateRange.split(" - ");

                DateInterval dateInterval = new DateInterval();
                dateInterval.setStartXMLGC(datePair[0].replaceAll(" ", ""));
                dateInterval.setEndXMLGC(datePair[1].replaceAll(" ", ""));

                WSrequest.getDateIntervals().add(dateInterval);
            }
        }

        return WSrequest;
    }

}
