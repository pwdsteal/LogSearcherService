package ru.pushkarev.LogsSearcher.utils;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import javax.ejb.Schedule;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;


public class Config {
    private Logger log = Logger.getLogger(Config.class.getName());
    public final String APP_NAME = "LogsSearcher";

    public final Path domainPath = Paths.get(System.getProperty("user.dir"));
    public final Path workingDirectory = domainPath.resolve("tmp").resolve(APP_NAME);
    public final Path configPath = domainPath.resolve("config").resolve(APP_NAME + ".cfg");

    private Configurations configurations = new Configurations();
    private Configuration configuration;

    private int allowedSpaceMbytes;
    private final int allowedSpaceMbytesDefault = 128;

    private File XML_TO_HTML_TEMPLATE;
    private final Path XML_TO_HTML_TEMPLATE_DEFAULT = workingDirectory.resolve("XML_TO_HTML_TEMPLATE.xsl");

    private File XML_TO_DOC_TEMPLATE;
    private final Path XML_TO_DOC_TEMPLATE_DEFAULT = workingDirectory.resolve("XML_TO_DOC_TEMPLATE.xsl");

    private File XML_TO_PDF_TEMPLATE;
    private final Path XML_TO_PDF_TEMPLATE_DEFAULT = workingDirectory.resolve("XML_TO_PDF_TEMPLATE.xsl");

    public File getXML_TO_HTML_TEMPLATE() {
        return XML_TO_HTML_TEMPLATE;
    }

    public void setAllowedSpaceMbytes(int allowedSpaceMbytes) {

        if (this.allowedSpaceMbytes != allowedSpaceMbytes) {
            log.info("allowedSpaceMbytes updated from:" + this.allowedSpaceMbytes
                    + " to:" + allowedSpaceMbytes);
            this.allowedSpaceMbytes = allowedSpaceMbytes;
        }
    }

    public void setXML_TO_HTML_TEMPLATE(File XML_TO_HTML_TEMPLATE) {
        if (!java.util.Objects.equals(this.XML_TO_HTML_TEMPLATE, XML_TO_HTML_TEMPLATE)) {
            log.info("XML_TO_HTML_TEMPLATE updated from:" + this.XML_TO_HTML_TEMPLATE
                    + " to:" + XML_TO_HTML_TEMPLATE);
            this.XML_TO_HTML_TEMPLATE = XML_TO_HTML_TEMPLATE;
        }
    }

    public void setXML_TO_DOC_TEMPLATE(File XML_TO_DOC_TEMPLATE) {
        if (!java.util.Objects.equals(this.XML_TO_DOC_TEMPLATE, XML_TO_DOC_TEMPLATE)) {
            log.info("XML_TO_DOC_TEMPLATE updated from:" + this.XML_TO_DOC_TEMPLATE
                    + " to:" + XML_TO_DOC_TEMPLATE);
            this.XML_TO_DOC_TEMPLATE = XML_TO_DOC_TEMPLATE;
        }
    }

    public void setXML_TO_PDF_TEMPLATE(File XML_TO_PDF_TEMPLATE) {
        if (!java.util.Objects.equals(this.XML_TO_PDF_TEMPLATE, XML_TO_PDF_TEMPLATE)) {
            log.info("XML_TO_PDF_TEMPLATE updated from:" + this.XML_TO_PDF_TEMPLATE
                    + " to:" + XML_TO_PDF_TEMPLATE);
            this.XML_TO_PDF_TEMPLATE = XML_TO_PDF_TEMPLATE;
        }
    }

    public File getXML_TO_DOC_TEMPLATE() {
        return XML_TO_DOC_TEMPLATE;
    }

    public File getXML_TO_PDF_TEMPLATE() {
        return XML_TO_PDF_TEMPLATE;
    }

    public int getAllowedSpaceMbytes() {
        return allowedSpaceMbytes;
    }

    private static Config instance;

    public static synchronized Config getInstance(){
        if(instance == null){
            instance = new Config();
        }
        return instance;
    }

    private Config() {
        configuration = loadConfiguration();
        if (null == configuration) {
            configuration = createNewConfiguration();
        }
        extractDefaultXsltTemplates();
        readProperties(configuration);
        log.info("Config initialized.");
    }

    public void reload() {
        Configuration configuration = loadConfiguration();
        if (null != configuration) {
            readProperties(configuration);
        }
    }

    private void extractDefaultXsltTemplates() {
        try(InputStream inputStream = getClass().getClassLoader().getResourceAsStream("toHTML.xsl")) {
            Files.copy(inputStream, XML_TO_HTML_TEMPLATE_DEFAULT, REPLACE_EXISTING);
        } catch (IOException e) {
            log.log(Level.WARNING, "Error extracting default HTML xslt template." + e.getMessage() + e);
        }
        try(InputStream inputStream = getClass().getClassLoader().getResourceAsStream("toDOC.xsl")) {
            Files.copy(inputStream, XML_TO_DOC_TEMPLATE_DEFAULT, REPLACE_EXISTING);
        } catch (IOException e) {
            log.log(Level.WARNING, "Error extracting default DOC xslt template." + e.getMessage() + e);
        }
        try(InputStream inputStream = getClass().getClassLoader().getResourceAsStream("toPDF.xsl")) {
            Files.copy(inputStream, XML_TO_PDF_TEMPLATE_DEFAULT, REPLACE_EXISTING);
        } catch (IOException e) {
            log.log(Level.WARNING, "Error extracting default PDF xslt template." + e.getMessage() + e);
        }

    }

    private Configuration createNewConfiguration() {
        PropertiesConfiguration newConfiguration = new PropertiesConfiguration();
        newConfiguration.setProperty("XML_TO_HTML_TEMPLATE", XML_TO_HTML_TEMPLATE_DEFAULT.toString());
        newConfiguration.setProperty("XML_TO_DOC_TEMPLATE", XML_TO_DOC_TEMPLATE_DEFAULT.toString());
        newConfiguration.setProperty("XML_TO_PDF_TEMPLATE", XML_TO_PDF_TEMPLATE_DEFAULT.toString());

        newConfiguration.setProperty("allowedSpaceMbytes", allowedSpaceMbytesDefault);
        newConfiguration.setHeader(APP_NAME + " Configuration File.");

        return newConfiguration;
    }

    private void saveConfiguration(PropertiesConfiguration configuration) {
        try(Writer writer = new FileWriter(configPath.toFile())) {
            configuration.write(writer);
        } catch (IOException | ConfigurationException e) {
            log.log(Level.WARNING, "Error saving config.file " + e.getMessage() + e);
        }
    }



    private Configuration loadConfiguration() {
        log.info("Reading config:" + configPath.getFileName());
        Configuration configuration = null;
        try {
            configuration = configurations.properties(configPath.toFile());
        }
        catch (Exception e) {
            log.log(Level.WARNING,"Error reading config.file " +  e.getMessage() + e);
        }
        return configuration;
    }




    private void readProperties(Configuration configuration) {

        try {
            setXML_TO_HTML_TEMPLATE(new File(configuration.getString("XML_TO_HTML_TEMPLATE")));
        } catch (Exception e){
            log.log(Level.WARNING, "Failed to read XML_TO_HTML_TEMPLATE file property " + e.getMessage());
            setXML_TO_HTML_TEMPLATE(XML_TO_HTML_TEMPLATE_DEFAULT.toFile());
            configuration.setProperty("XML_TO_HTML_TEMPLATE", XML_TO_HTML_TEMPLATE_DEFAULT.toString());
        }
        try {
            setXML_TO_DOC_TEMPLATE(new File(configuration.getString("XML_TO_DOC_TEMPLATE")));
        } catch (Exception e){
            log.log(Level.WARNING, "Failed to read XML_TO_DOC_TEMPLATE file property " + e.getMessage());
            setXML_TO_DOC_TEMPLATE(XML_TO_DOC_TEMPLATE_DEFAULT.toFile());
            configuration.setProperty("XML_TO_DOC_TEMPLATE", XML_TO_DOC_TEMPLATE_DEFAULT.toString());
        }
        try {
            setXML_TO_PDF_TEMPLATE(new File(configuration.getString("XML_TO_PDF_TEMPLATE")));
        } catch (Exception e){
            log.log(Level.WARNING, "Failed to read XML_TO_PDF_TEMPLATE file property " + e.getMessage());
            setXML_TO_PDF_TEMPLATE(XML_TO_PDF_TEMPLATE_DEFAULT.toFile());
            configuration.setProperty("XML_TO_PDF_TEMPLATE", XML_TO_PDF_TEMPLATE_DEFAULT.toString());
        }


        try {
            setAllowedSpaceMbytes(configuration.getInt("allowedSpaceMbytes"));
        } catch (Exception e) {
           log.log(Level.WARNING, "Failed to read allowedSpaceMbytes property " + e.getMessage() + e);
            setAllowedSpaceMbytes(allowedSpaceMbytesDefault);
            configuration.setProperty("allowedSpaceMbytes", allowedSpaceMbytesDefault);
        }

        saveConfiguration((PropertiesConfiguration) configuration);
    }
}
