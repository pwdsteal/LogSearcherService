package ru.pushkarev.LogsSearcher.type;

import ru.pushkarev.LogsSearcher.schedule.CacheService;
import ru.pushkarev.LogsSearcher.utils.DateParser;
import ru.pushkarev.LogsSearcher.utils.RegExUtils;
import ru.pushkarev.LogsSearcher.utils.Stopwatch;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

@XmlType(propOrder = { "searchString", "target", "outputFormat", "dateIntervals", "maxMatches", "isCaseSensitive", "isRegExp" })
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Request {
    private static Logger log = Logger.getLogger(Request.class.getName());

    @XmlElement(required = true, defaultValue = "error")
    @NotNull @Size(min = 3)
    private String searchString;


    // optional parameters
    @XmlElement(defaultValue = "domain")
    private String target;
    @XmlElement
    private Set<DateInterval> dateIntervals = new HashSet<>();
    @XmlElement(defaultValue = " ")
    private String outputFormat;
    @XmlElement(defaultValue = "65535")
    private int maxMatches = 65535;
    @XmlElement(defaultValue = "false")
    private boolean isCaseSensitive;
    @XmlElement(defaultValue = "false")
    private boolean isRegExp;


    @XmlTransient
    private String outputFilename;
    @XmlTransient
    private File cachedFile;
    @XmlTransient
    private boolean isFileRequested;
    @XmlTransient
    private boolean isCached;
    @XmlTransient
    private boolean isCacheExtensionMatch;

    public boolean isFileRequested() {return isFileRequested;}

    public boolean isCached() {return isCached;}

    public boolean isCacheExtensionMatch() {return isCacheExtensionMatch;}

    public File getCachedFile() {
        return cachedFile;
    }

    public String getOutputFilename() {return outputFilename;}

//    public String getResultFilename() {
//        return outputFilename + "." + outputFormat;
//    }

    public String getResultFilename() {
        if (isCached) {
            if (isCacheExtensionMatch) {
                return cachedFile.getName();  // return existing file
            } else {
                return cachedFile.getName().replace(".xml", + '.' + outputFormat);  // use cache xml to gen new file
            }
        } else {
            return outputFilename + "." + outputFormat;  // create new file
        }
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

    public Set<DateInterval> getDateIntervals() { return dateIntervals; }

    public void setDateIntervals(Set<DateInterval> dateIntervals) { this.dateIntervals = dateIntervals; }

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
            target = Domain.getInstance().getName();
        }

        // check dateIntervals
        if(dateIntervals.isEmpty()) {
            dateIntervals.add(new DateInterval(
                    DateParser.toXMLGregorianCalendar(new Date(0)),
                    DateParser.toXMLGregorianCalendar(new Date())));
        }

        // Check RegExpression
        if(isRegExp && !RegExUtils.isValidRegExp(searchString)) {
            isRegExp = false;
            log.info("Error compile " + searchString + " as a RegExp. Using as a literal string.");
        }

        // check maxMatches
        if (maxMatches <= 1) {
            maxMatches = 65535;
        }

            if(null != outputFormat) {
            switch (outputFormat.toLowerCase()) {
                case "xml":
                case "html":
                case "doc":
                case "rtf":
                case "pdf":
                    outputFormat = outputFormat.toLowerCase();
                    isFileRequested = true;
                    break;
                default:
                    log.fine("Incorrect output file format: " + outputFormat);
                    outputFormat = null;
            }
        }

        generateFilename();

        tryFindCached();
    }

    private void tryFindCached() {
        cachedFile = CacheService.tryFindCachedFile(this);
        if (null != cachedFile) {
            isCached = true;
            String msg = "Found cached File: " + cachedFile.getName();
            if (cachedFile.getName().toLowerCase().endsWith('.' + outputFormat)) {
                isCacheExtensionMatch = true;
                msg += ". Extension matches: true" ;
            }
            log.info(msg);
        }
    }

    private void generateFilename(){
        Date dNow = new Date( );
        SimpleDateFormat sdf = new SimpleDateFormat ("'_at_'HH-mm-ss_dd-MM-yyyy" );
        String filename = ServiceController.getRequestCount() + "-request_hashcode[" + this.hashCode() + ']' + sdf.format(dNow);
        outputFilename = filename;
    }

    @Override
    public String toString() {
        return "\nRequest{" +
                "searchString='" + searchString + '\'' +
                ", target='" + target + '\'' +
                ", dateIntervals=" + dateIntervals +
                ", outputFormat='" + outputFormat + '\'' +
                ", maxMatches=" + maxMatches +
                ", isCaseSensitive=" + isCaseSensitive +
                ", isRegExp=" + isRegExp +
                ", isCached=" + isCached +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Request request = (Request) o;

        if (isCaseSensitive != request.isCaseSensitive) return false;
        if (isRegExp != request.isRegExp) return false;
        if (!searchString.equals(request.searchString)) return false;
        if (target != null ? !target.equals(request.target) : request.target != null) return false;
        return dateIntervals != null ? dateIntervals.equals(request.dateIntervals) : request.dateIntervals == null;

    }

    @Override
    public int hashCode() {
        int result = searchString.hashCode();
        result = 31 * result + (target != null ? target.hashCode() : 0);
        result = 31 * result + (dateIntervals != null ? dateIntervals.hashCode() : 0);
        result = 31 * result + (isCaseSensitive ? 1 : 0);
        result = 31 * result + (isRegExp ? 1 : 0);
        return result;
    }
}
