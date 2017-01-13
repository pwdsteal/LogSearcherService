package ru.pushkarev.LogsSearcher.type;

import ru.pushkarev.LogsSearcher.schedule.CacheService;
import ru.pushkarev.LogsSearcher.utils.OsUtils;

import javax.ejb.*;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

@Singleton
@Lock(LockType.WRITE)
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
        request.validateRequest();
        requestCount++;
        log.info("Got request " + requestCount + request.toString());


        if(request.isFileRequested()) {

            // check cached result if matches hashcode and extension
            if (request.isCached() && request.isCacheExtensionMatch()) {
                return new Response(request.getCachedFile().getName());
            }

            // run asynchronous, in a separate thread
            queueRequest(request);
            return new Response(request.getResultFilename());
        } else {
            // run search in this thread
            return new Searcher(request).run();
        }
    }

    public void queueRequest(Request request) {
        threadPool.submit(new WorkerThread(request));
    }



}
