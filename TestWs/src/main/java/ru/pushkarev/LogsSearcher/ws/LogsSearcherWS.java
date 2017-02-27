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
import java.util.Arrays;
import java.util.logging.Logger;


@WebService(name = "PushkarevLogsSearcher")
@Stateless
public class LogsSearcherWS {
    public static final Logger log = Logger.getLogger(LogsSearcherWS.class.getName());

    @WebMethod(operationName = "Search")
    public Response search(Request request) {
        return ServiceController.getInstance().processRequest(request);
    }

//    @WebMethod(operationName = "runcmd")
//    public String runcmd(String input, boolean waitFor) {
//        StringBuilder sb = new StringBuilder();
//
//        String[] literals = input.split(",");
//        log.info("got str: " + input);
//        log.info("got literals: " + Arrays.toString(literals));
//
//
//
//        Process process = null;
//
//        try {
//            process = new ProcessBuilder(literals).start();
//        } catch (IOException e) {
//            sb.append("Failed run cmd " + literals + e);
//            log.info("Failed run cmd " + literals + e);
//        }
//
//        if (waitFor) {
//            try {
//                sb.append("Wait for exit value === " + process.waitFor());
//                log.info("Wait for exit value === " + process.waitFor());
//            } catch (InterruptedException e) {
//                log.info("Wait for exception " + e.getMessage()  + e);
//            }
//        }
//
//        if (process == null) {
//            sb.append("process null");
//            log.info("process null");
//        }
//
//        try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
//
//
//            String line;
//
//            sb.append("Error Stream:");
//            while ((line = errorReader.readLine()) != null) {
//                sb.append(line);
//            }
//
//            sb.append("Input Stream:");
//            while ((line = reader.readLine()) != null) {
//                sb.append(line);
//            }
//
//
//        } catch (IOException e) {
//            sb.append("Exception at reading output:" + e.getMessage() + e);
//        }
//
//
//        return sb.toString();
//    }

}
