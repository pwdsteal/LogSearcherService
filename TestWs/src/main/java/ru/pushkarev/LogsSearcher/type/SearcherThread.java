package ru.pushkarev.LogsSearcher.type;

import ru.pushkarev.LogsSearcher.schedule.CacheService;
import ru.pushkarev.LogsSearcher.utils.FileConverter;

import javax.ejb.EJB;
import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class SearcherThread implements Runnable {

    private static final Logger log = Logger.getLogger(SearcherThread.class.getName());
    private static AtomicInteger threadCount = new AtomicInteger(0);

    private Request request;
    private int threadId;

    public SearcherThread() {}

    @EJB
    private CacheService cacheService;

    public SearcherThread(Request request) {
        this.request = request;
        this.threadId = threadCount.incrementAndGet();
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
