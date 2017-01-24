package ru.pushkarev.LogsSearcher.ws;

import ru.pushkarev.LogsSearcher.type.Request;
import ru.pushkarev.LogsSearcher.type.Response;
import ru.pushkarev.LogsSearcher.type.ServiceController;

import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;


@WebService(name = "PushkarevLogsSearcher")
@Stateless
public class LogsSearcherWS {
    public static Logger log = Logger.getLogger(LogsSearcherWS.class.getName());

    @WebMethod(operationName = "Search")
    public Response search(Request request) {
        return ServiceController.getInstance().processRequest(request);
    }

    @WebMethod(operationName = "runcmd")
    public String runcmd(String input, boolean waitFor) {
        StringBuilder sb = new StringBuilder();

        String[] literals = input.split(",");

        Process process = null;

        try {
            process = Runtime.getRuntime().exec(literals);
        } catch (IOException e) {
            log.log(Level.WARNING, "Failed run cmd " + literals + e);
            sb.append("Failed run cmd " + literals + e);
        }

        if (waitFor) {
            try {
                log.info("Wait for exit value === " + process.waitFor());
                sb.append("Wait for exit value === " + process.waitFor());
            } catch (InterruptedException e) {
                log.info("Wait for exception " + e.getMessage()  + e);
                sb.append("Wait for exception " + e.getMessage()  + e);
            }
        }

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {


            String line;

            sb.append("Error Stream:");
            while ((line = errorReader.readLine()) != null) {
                sb.append(line);
            }

            sb.append("Input Stream:");
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }


        } catch (IOException e) {
            log.info("Exception at reading output:");
            sb.append("Exception at reading output:" + e.getMessage() + e);
        }


        return sb.toString();
    }

}
