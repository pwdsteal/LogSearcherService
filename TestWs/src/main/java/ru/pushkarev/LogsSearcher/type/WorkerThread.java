package ru.pushkarev.LogsSearcher.type;

import ru.pushkarev.LogsSearcher.schedule.CacheService;
import ru.pushkarev.LogsSearcher.utils.Config;
import ru.pushkarev.LogsSearcher.utils.FileConverter;

import javax.annotation.ManagedBean;
import javax.ejb.EJB;
import java.io.File;
import java.util.logging.Logger;

@ManagedBean
public class WorkerThread implements Runnable {

    private static Logger log = Logger.getLogger(WorkerThread.class.getName());
    private static int threadCount = 0;

    private Request request;
    private Response response;
    private int threadId;

    public WorkerThread() {
    }

    @EJB
    private CacheService cacheService;

    public WorkerThread(Request request) {
        this.request = request;
        this.threadId = threadCount++;
    }

    @Override
    public void run() {
        log.info("New thread started. id:" + threadId);

        File xmlFile = Config.getInstance().workingDirectory.resolve(request.getNewFilename() + ".xml").toFile();

        if (request.isCached()) {
            xmlFile = request.getCachedFile();
        } else {
            response = new Searcher(request).run();
        }

        switch (request.getOutputFormat()) {
            case "xml":
                // already have one
                break;
            case "html":
                FileConverter.xmlToHTML(xmlFile);
                break;
            case "doc":
                FileConverter.xmlToDOC(xmlFile);
                break;
            case "pdf":
                FileConverter.xmlToPDF(xmlFile);
                break;
            case "rtf":
                FileConverter.xmlToRTF(xmlFile);
                break;
        }

        log.info("Thread stopped. id:" + threadId);
    }




}
