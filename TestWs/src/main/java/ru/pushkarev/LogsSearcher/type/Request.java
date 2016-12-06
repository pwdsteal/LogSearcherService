package ru.pushkarev.LogsSearcher.type;

import ru.pushkarev.LogsSearcher.utils.DateParser;
import ru.pushkarev.LogsSearcher.utils.RegExUtils;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

//@XmlType(propOrder = { "searchString", "target", "dateIntervals", "maxMatches",  })
@XmlRootElement
public class Request {
    private static Logger log = Logger.getLogger(Request.class.getName());
    private static Path outputFolder = Domain.getPath().resolve("tmp");

    private Path outputFilename;
    private Set<Server> targetServers;

    @XmlElement(required = true)
    private String searchString;

    // optional parameters
    @XmlElement
    private String target;
    @XmlElement
    private List<DateInterval> dateIntervals = new ArrayList<>();
    @XmlElement
    private String outputFormat;
    @XmlElement(defaultValue = "65535")
    private int maxMatches = 1024;

    private boolean isCaseSensitive;
    private boolean isRegExp;

    public Path getOutputFilename() { return outputFilename; }

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

    public List<DateInterval> getDateIntervals() {
        return dateIntervals;
    }

    public void setDateIntervals(List<DateInterval> dateIntervals) {
        this.dateIntervals = dateIntervals;
    }

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
        // check searchString
        if (null == searchString
                || searchString.isEmpty()
                || searchString.trim().length() < 3) {

            String msg = "searchString cannot be less than 3 alphanumeric characters.";
//            log.(Level.SEVERE, msg);
            throw new IllegalArgumentException(msg);
        }

        // check target
        if (null == target || target.isEmpty()) {
            String msg = "Target not specified. Using whole domain as a target.";
//            log.fine(msg);
            target = Domain.getName();
        }
        determineTargetServers();

        // check dateIntervals
        if(dateIntervals.isEmpty()) {
            dateIntervals.add(new DateInterval(
                    DateParser.toXMLGregorianCalendar(new Date(0)),
                    DateParser.toXMLGregorianCalendar(new Date())));
        }

        // Check RegExpression
        if(isRegExp && !RegExUtils.isValidRegExp(searchString)) {
            isRegExp = false;
            //                log.warning("Error compile " + searchString + " as a RegExp. Using as a literal string.");
        }

        // check maxMatches
        if (maxMatches <= 1) {
            maxMatches = 300;
        }

        if(null != outputFormat) {
            switch (outputFormat.toLowerCase()) {
                case "xml": break;
                case "html": break;
                case "doc": break;
                case "rtf": break;
                default:
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
            String msg = " Cant find domain, cluster or server with name [" + target + "].\nUsing whole domain as a target.";
            targetServers.addAll(Domain.getServersList());
            log.log(Level.WARNING, msg);
        }
    }

    private void generateOutputFilePath(){
        Date dNow = new Date( );
        SimpleDateFormat sdf = new SimpleDateFormat ("'_at_'hh-mm-ss_dd-MM-yyyy" );
        String filename = "request_" + ServiceController.getRequestCount() + sdf.format(dNow) + "." + outputFormat;

        outputFilename = outputFolder.resolve("LogsSearcher").resolve(filename);
    }

}
