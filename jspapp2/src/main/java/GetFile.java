

import util.EventLogger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet(urlPatterns = "/getFile")
public class GetFile extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        EventLogger.getInstance().logEvent(request, "GET FILE");
        String filename = request.getParameter("filename");

        if(filename != null) {
            request.getLocalAddr();
            response.sendRedirect("http://" + request.getLocalAddr() + ":" + request.getLocalPort() + "/LogsSearcherWS/pushkarev/get/" + filename);
        }
    }
}
