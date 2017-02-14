package ru.pushkarev.LogsSearcher.type;

import ru.pushkarev.LogsSearcher.schedule.CacheService;
import ru.pushkarev.LogsSearcher.utils.Config;
import ru.pushkarev.LogsSearcher.utils.FileConverter;

import javax.annotation.ManagedBean;
import javax.ejb.EJB;
import java.io.File;
import java.util.logging.Logger;

public class WorkerThread implements Runnable {

    private static Logger log = Logger.getLogger(WorkerThread.class.getName());
    private static int threadCount = 0;

    private Request request;
    private int threadId;

    public WorkerThread() {}

    @EJB
    private CacheService cacheService;

    public WorkerThread(Request request) {
        this.request = request;
        synchronized (this) {
            this.threadId = threadCount++;
        }
    }

    @Override
    public void run() {
        log.info("New thread started. id:" + threadId);

        File xmlFile;

        if (request.isCached()) {
            xmlFile = request.getCachedFile();
        } else {
            Searcher searcher = new Searcher(request);
            searcher.run();
            xmlFile = searcher.getXmlFile();
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
