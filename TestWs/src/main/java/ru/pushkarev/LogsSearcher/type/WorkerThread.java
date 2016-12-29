package ru.pushkarev.LogsSearcher.type;

import ru.pushkarev.LogsSearcher.utils.Config;
import ru.pushkarev.LogsSearcher.utils.FileConverter;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class WorkerThread implements Runnable {
    private static Logger log = Logger.getLogger(WorkerThread.class.getName());
    private static int threadCount = 0;

    private Request request;
    private Response response;
    private int threadId;

    public WorkerThread(Request request) {
        this.request = request;
        this.threadId = threadCount++;
    }

    @Override
    public void run() {
        log.info("Thread started id:" + threadId);

        response = new Searcher(request).run();

        // write XML File
        File xmlFile = Config.getWorkingDirectory().resolve(request.getOutputFilename() + ".xml").toFile();
        try {
            FileConverter.writeXMLToFile(response, xmlFile);
        } catch (JAXBException e) {
            log.log(Level.SEVERE, " Error converting response object to XML file " + e.getMessage() + e);
            throw new RuntimeException();
        } catch (IOException e) {
            log.log(Level.SEVERE, " Error creating XML file. path: "+ xmlFile.getAbsolutePath() + "\n" + e.getMessage() + e);
            throw new RuntimeException();
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
                FileConverter.XMLToPDF(xmlFile);
                break;
            case "rtf":
                FileConverter.XMLToRTF(xmlFile);
                break;
        }


        log.info("Thread stopped id:" + threadId);
    }




}
