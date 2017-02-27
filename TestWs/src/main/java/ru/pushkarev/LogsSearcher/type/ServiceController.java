package ru.pushkarev.LogsSearcher.type;

import ru.pushkarev.LogsSearcher.utils.FileConverter;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class ServiceController {
    private static final ServiceController instance = new ServiceController();
    public static ServiceController getInstance() {
        return instance;
    }

    private ServiceController() {}

    private final Logger log = Logger.getLogger(ServiceController.class.getName());
    private final int MAX_THREADS = 5;
    private int requestCount = 1;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(MAX_THREADS);


    public int getRequestCount() {
        return requestCount;
    }

    public Response processRequest(Request request) {
        requestCount++;

        List<String> fatalErrors = request.validateRequest();
        if (!fatalErrors.isEmpty()) {
            Response response = new Response();
            response.setErrorsList(fatalErrors);
            return response;
        }

        log.info("Got request " + requestCount + " ***** " + request.toString());

        Response response;
        if(request.isFileRequested()) {
            if (request.isCachedExtensionMatch()) {  // is cached file is exactly what we need (matches Output format)
                response = new Response(request.getCachedFile().getName());
            } else {
                queueRequest(request); // run search/convertion asynchronous
                response = new Response(request.getFilenameWithExtension());  // return file link
            }
        } else {
            if(request.isCached()) {
                response = FileConverter.readResponseFromXML(request.getCachedFile());
            } else {
                response = new Searcher(request).run();
            }
        }

        log.info("Searching complete. " + " * * * * *  " + response.toString());
        return response;
    }

    public synchronized void queueRequest(Request request) {
        threadPool.submit(new WorkerThread(request));
    }

}
