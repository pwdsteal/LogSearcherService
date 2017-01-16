package ru.pushkarev.LogsSearcher.type;

import javax.ejb.*;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

@Singleton
public class ServiceController {
    private static Logger log = Logger.getLogger(ServiceController.class.getName());
    private static final int MAX_THREADS = 5;
    private static int requestCount = 1;
    private static ExecutorService threadPool = Executors.newFixedThreadPool(MAX_THREADS);

    public static int getRequestCount() {
        return requestCount;
    }

    @Lock(LockType.READ)
    public Response processRequest(Request request) {
        requestCount++;

        List<String> fatalErrors = request.validateRequest();
        if (!fatalErrors.isEmpty()) {
            Response response = new Response();
            response.setErrorsList(fatalErrors);
            return response;
        }

        log.info("Got request " + requestCount + request.toString());

        Response response;
        if(request.isFileRequested()) {
            // check cached result if matches hashcode and extension
            if (request.isCached() && request.isCachedExtensionMatch()) {
                response = new Response(request.getCachedFile().getName());
            } else {
                // run asynchronous, in a separate thread
                queueRequest(request);
                // return file link
                response = new Response(request.getResultFilename());
            }
        } else {
            // run search in this thread
            response = new Searcher(request).run();
        }

        log.info("Searching complete. " + response.toString());
        return response;
    }

    @Lock(LockType.WRITE)
    public void queueRequest(Request request) {
        threadPool.submit(new WorkerThread(request));
    }

}
