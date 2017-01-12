package ru.pushkarev.LogsSearcher.type;

import ru.pushkarev.LogsSearcher.schedule.FileCleaner;
import ru.pushkarev.LogsSearcher.utils.Config;
import ru.pushkarev.LogsSearcher.utils.Stopwatch;

import javax.ejb.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
@Lock(LockType.WRITE)
public class ServiceController {
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

        // check cached result


        // run synchronized in This thread
        if(null == request.getOutputFormat()) {
            return new Searcher(request).run();
        } else {
            // run asynchronous, in a separate thread
            queueRequest(request);
            return new Response(request.getResultFilename());
        }
    }

    public void queueRequest(Request request) {
        threadPool.submit(new WorkerThread(request));
    }



}
