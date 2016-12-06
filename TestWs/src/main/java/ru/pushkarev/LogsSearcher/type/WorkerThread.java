package ru.pushkarev.LogsSearcher.type;

import ru.pushkarev.LogsSearcher.utils.FileConverter;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class WorkerThread implements Runnable {
    private static Logger log = Logger.getLogger(WorkerThread.class.getName());

    private Request request;
    private Response response;
    private int threadId;

    public WorkerThread(Request request, int threadId) {
        this.request = request;
        this.threadId = threadId;
    }

    @Override
    public void run() {
        log.warning("Thread started id:" + threadId);

        response = new Searcher(request).run();

        File file = new File(request.getOutputFilename() + ".xml");
        try {
            FileConverter.writeXMLToFile(response, file);
        } catch (JAXBException e) {
            log.log(Level.SEVERE, " Error converting response object to XML file " + e.getMessage() + e);
            throw new RuntimeException();
        } catch (IOException e) {
            log.log(Level.SEVERE, " Error creating XML file. path: "+ request.getOutputFilename() + "\n" + e.getMessage() + e);
            throw new RuntimeException();
        }

        log.warning("Thread STOPPED id:" + threadId);
    }




}
