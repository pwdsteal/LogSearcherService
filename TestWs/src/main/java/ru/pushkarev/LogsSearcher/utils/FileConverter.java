package ru.pushkarev.LogsSearcher.utils;

import org.apache.fop.apps.*;
import ru.pushkarev.LogsSearcher.type.*;

import javax.ejb.EJB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;


public class FileConverter {
    private static Logger log = Logger.getLogger(FileConverter.class.getName());

    private FileConverter() {}

    public static void main(String[] args) {

        FileConverter.XMLToPDF(new File("C:\\Oracle\\Middleware\\Oracle_Home\\user_projects\\domains\\new_domain\\tmp\\LogsSearcher\\" + "request_1_at_16-45-46_26-12-2016.xml"));
        log.setLevel(Level.ALL);
    }

    public static void writeXMLToFile(Response response, File file) throws JAXBException, IOException {

        file.getParentFile().mkdirs();
        if (file.createNewFile()) {
            log.info("File created: " + file.getName());
        }

        Stopwatch stopwatch = new Stopwatch();
        JAXBContext jaxbContext = JAXBContext.newInstance(Response.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        jaxbMarshaller.marshal(response, file);
        log.info("xml file conversion complete.  " + stopwatch.stop());
    }


    public static void XMLToPDF(File xmlFile) { XMLtoPDForRTF(xmlFile, "pdf"); }
    public static void XMLToRTF(File xmlFile) {
        XMLtoPDForRTF(xmlFile, "rtf");
    }

    private static void XMLtoPDForRTF(File xmlSourceFile, String outputFormat) {
        Stopwatch stopwatch = new Stopwatch();

        StreamSource xmlSource = new StreamSource(xmlSourceFile);
        FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();

        String outFile = xmlSourceFile.getAbsolutePath().replace(".xml", "." + outputFormat);
        try(OutputStream out = new java.io.FileOutputStream(outFile)) {
            Fop fop;
            switch (outputFormat) {
                case "pdf":
                    fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);
                    break;
                case "rtf":
                default:
                    fop = fopFactory.newFop(MimeConstants.MIME_RTF, foUserAgent, out);
                    break;
            }


            // Setup XSLT
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer(new StreamSource(Config.getInstance().getXML_TO_PDF_TEMPLATE()));

            // Resulting SAX events (the generated FO) must be piped through to
            Result res = new SAXResult(fop.getDefaultHandler());

            // Start XSLT transformation and FOP processing
            // That's where the XML is first transformed to XSL-FO and then
            // PDF is created
            transformer.transform(xmlSource, res);
        } catch (TransformerException | IOException | FOPException e) {
            e.printStackTrace();
        }
        log.info(outputFormat + " file conversion complete. " + stopwatch.stop());
    }


    public static void xmlToHTML(File xmlSourceFile) { xmlToHTMLorDOC(xmlSourceFile, "html"); }
    public static void xmlToDOC(File xmlSourceFile) { xmlToHTMLorDOC(xmlSourceFile, "doc"); }

    private static void xmlToHTMLorDOC(File xmlSourceFile, String outputFormat) {
        Stopwatch stopwatch = new Stopwatch();

        File xsltTemplate = null;
        switch (outputFormat) {
            case "html":
                xsltTemplate = Config.getInstance().getXML_TO_HTML_TEMPLATE();
                break;
            case "doc":
                xsltTemplate = Config.getInstance().getXML_TO_DOC_TEMPLATE();
                break;
        }

        String outFile = xmlSourceFile.getAbsolutePath().replace(".xml", "." + outputFormat);
        try(FileInputStream fisTemplate = new FileInputStream(xsltTemplate);
            FileInputStream fisXMLSource = new FileInputStream(xmlSourceFile);
                FileOutputStream fos = new FileOutputStream(outFile)) {

            TransformerFactory factory = TransformerFactory.newInstance();
            Templates template = factory.newTemplates(new StreamSource(fisTemplate));
            Transformer xformer = template.newTransformer();

            Source source = new StreamSource(fisXMLSource);
            Result result = new StreamResult(fos);

            xformer.transform(source, result);
        } catch (TransformerConfigurationException | IOException e) {
            log.log(Level.SEVERE, "Error xml to " + outputFormat + " conversion: " + e.getMessage() + e);
        } catch (TransformerException e) {
            // An error occurred while applying the XSL file
            // Get location of error in input file
            SourceLocator locator = e.getLocator();
            int col = locator.getColumnNumber();
            int line = locator.getLineNumber();
            String publicId = locator.getPublicId();
            String systemId = locator.getSystemId();
            log.log(Level.SEVERE, "Error xml to " + outputFormat + " conversion. An error occurred while applying the XSL file : " +
                    xsltTemplate.getName() + " Line:"+line + " Column:"+col + e.getMessage() + e);
        }
        log.info(outputFormat + " file conversion complete. " + stopwatch.stop());
    }
}
