package ru.pushkarev.LogsSearcher.utils;

import ru.pushkarev.LogsSearcher.type.Request;
import ru.pushkarev.LogsSearcher.type.Response;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;


public class FileConverter {

    static public void writeXMLToFile(Response response, File file) throws JAXBException, IOException {

        file.getParentFile().mkdirs();
        file.createNewFile();

        JAXBContext jaxbContext = JAXBContext.newInstance(Response.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        jaxbMarshaller.marshal(response, file);

    }
}
