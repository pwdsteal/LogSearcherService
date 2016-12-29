package ru.pushkarev.LogsSearcher.type;

import ru.pushkarev.LogsSearcher.utils.DateParser;
import ru.pushkarev.LogsSearcher.utils.RegExUtils;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.*;
import java.io.File;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@XmlType(propOrder = { "searchString", "target", "outputFormat", "dateIntervals", "maxMatches", "isCaseSensitive", "isRegExp" })
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Request {
    private static Logger log = Logger.getLogger(Request.class.getName());
    private static Path outputFolder = Domain.getPath().resolve("tmp");


    @XmlElement(required = true, defaultValue = "error")
    @NotNull @Size(min = 3)
    private String searchString;

    // optional parameters
    @XmlElement(defaultValue = "domain")
    private String target;
    @XmlElement
    private List<DateInterval> dateIntervals = new ArrayList<>();
    @XmlElement(defaultValue = " ")
    private String outputFormat;
    @XmlElement(defaultValue = "65535")
    private int maxMatches = 1024;
    @XmlElement(defaultValue = "false")
    private boolean isCaseSensitive;
    @XmlElement(defaultValue = "false")
    private boolean isRegExp;

    // not visible
    @XmlTransient
    private Set<Server> targetServers;
    @XmlTransient
    private String outputFilename;

    public String getOutputFilename() {return outputFilename;}

    public String getResultFilename() {
        return outputFilename + "." + outputFormat.toLowerCase();
    }

    public Set<Server> getTargetServers() {
        return targetServers;
    }

    public String getOutputFormat() { return outputFormat; }

    public void setOutputFormat(String outputFormat) { this.outputFormat = outputFormat; }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public List<DateInterval> getDateIntervals() { return dateIntervals; }

    public void setDateIntervals(List<DateInterval> dateIntervals) { this.dateIntervals = dateIntervals; }

    public boolean isRegExp() {
        return isRegExp;
    }

    public void setRegExp(boolean regExp) {
        isRegExp = regExp;
    }

    public boolean isCaseSensitive() {
        return isCaseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        isCaseSensitive = caseSensitive;
    }

    public int getMaxMatches() {
        return maxMatches;
    }

    public void setMaxMatches(int maxMatches) {
        if (maxMatches <= 65535) {
            this.maxMatches = maxMatches;
        }
    }


    public Request() {}


    public void validateRequest() {
        // check target
        if (null == target || target.isEmpty()) {
            log.info("Target not specified. Using whole domain as a target.");
            target = Domain.getName();
        }
        determineTargetServers();

        // check dateIntervals
        if(dateIntervals.isEmpty()) {
            dateIntervals.add(new DateInterval(
                    DateParser.toXMLGregorianCalendar(new Date(0)),
                    DateParser.toXMLGregorianCalendar(new Date())));
        } else {
            String dates = "";
            for (DateInterval dateInterval : dateIntervals) {
                dates += "\nstart:" + dateInterval.getStartXMLGC() + " end:" + dateInterval.getEndXMLGC();
            }
            log.info("Dates presented:" + dates);
        }

        // Check RegExpression
        if(isRegExp && !RegExUtils.isValidRegExp(searchString)) {
            isRegExp = false;
            log.info("Error compile " + searchString + " as a RegExp. Using as a literal string.");
        }

        // check maxMatches
        if (maxMatches <= 1) {
            maxMatches = 300;
        }

        if(null != outputFormat) {
            switch (outputFormat.toLowerCase()) {
                case "xml":
                case "html":
                case "doc":
                case "rtf":
                case "pdf":
                    outputFormat = outputFormat.toLowerCase();
                    break;
                default:
                    log.fine("Incorrect output file format: " + outputFormat);
                    outputFormat = null;
            }
        }

        if(null != outputFormat) {
            generateOutputFilePath();
        }

    }

    private void determineTargetServers() {
        targetServers = new HashSet<>();
        if (Domain.getName().equals(target)) {
            targetServers.addAll(Domain.getServersList());
        }
        else if (Domain.isCluster(target)) {
            targetServers.addAll(Domain.getClusterByName(target).getServersList());
        }
        else if (Domain.isServer(target)) {
            targetServers.add(Domain.getServerByName(target));
        }
        else {
            log.info("Cant find domain, cluster or server with name [" + target + "].\nUsing whole domain as a target.");
            targetServers.addAll(Domain.getServersList());
        }
    }

    private void generateOutputFilePath(){
        Date dNow = new Date( );
        SimpleDateFormat sdf = new SimpleDateFormat ("'_at_'HH-mm-ss_dd-MM-yyyy" );
        String filename = "request_" + ServiceController.getRequestCount() + sdf.format(dNow);
        outputFilename = filename;
    }

}
