package ru.pushkarev.LogsSearcher.type;

import ru.pushkarev.LogsSearcher.schedule.CacheService;
import ru.pushkarev.LogsSearcher.utils.RegExUtils;

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
    private static final Logger log = Logger.getLogger(Request.class.getName());
    private static final int MAX_MATCHES_PER_SERVER_DEFAULT = 32768;

    @XmlElement(required = true)
    @NotNull @Size(min = 3)
    private String searchString;


    // optional parameters
    @XmlElement
    private String target;
    @XmlElement
    private Set<DateInterval> dateIntervals = new HashSet<>();
    @XmlElement
    private String outputFormat;
    @XmlElement
    private int maxMatches = MAX_MATCHES_PER_SERVER_DEFAULT;  // max blocks per server
    @XmlElement(defaultValue = "false")
    private boolean isCaseSensitive;
    @XmlElement(defaultValue = "false")
    private boolean isRegExp;


    // TODO seprate this to RawRequest and ValidatedRequest or Query
    // TODO Avoid generate null dates? improve speed
    @XmlTransient
    private String filename;
    @XmlTransient
    private File cachedFile;
    @XmlTransient
    private boolean isFileRequested;
    @XmlTransient
    private boolean isCached;
    @XmlTransient
    private boolean isCacheExtensionMatch;
    @XmlTransient
    private boolean isDatesPresented;

    public boolean isFileRequested() {return isFileRequested;}

    public boolean isCached() {return isCached;}

    public boolean isCachedExtensionMatch() {return isCacheExtensionMatch;}

    public File getCachedFile() {
        return cachedFile;
    }

    public String getFilename() {return filename;}

//    public String getFilenameWithExtension() {
//        return filename + "." + outputFormat;
//    }

    public String getFilenameWithExtension() {
        if (isCached) {
            if (isCacheExtensionMatch) {
                return cachedFile.getName();  // return existing file
            } else {
                // return existing filename with replaced extenstion
                return cachedFile.getName().replace(".xml", '.' + outputFormat);
            }
        } else {
            return filename + "." + outputFormat;  // create new file
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
        if (maxMatches > 0 ) {
            this.maxMatches = maxMatches;
        }
    }

    public boolean isDatesPresented() { return isDatesPresented; }


    public Request() {}


    public List<String> validateRequest() {
        List<String> fatalErrors = new ArrayList<>();

        searchString = searchString.trim();
        if (searchString.length() < 3) {
            String msg = "searchString cannot be less than 3 printable characters. [" + searchString + ']';
            log.warning(msg);
            fatalErrors.add(msg);
        }

        // check target
        if (null == target || target.isEmpty()) {
            log.info("Target not specified. Using whole domain as a target.");
            target = Domain.getInstance().getName();
        }

        // check dateIntervals
        if(!dateIntervals.isEmpty()) {
            isDatesPresented = true;
        }

        // Check RegExpression
        if(isRegExp && !RegExUtils.isValidRegExp(searchString)) {
            isRegExp = false;
            String msg = "Error compile " + searchString + " as a RegExp";
            log.warning(msg);
            fatalErrors.add(msg);
        }

        if(null != outputFormat) {
            outputFormat = outputFormat.toLowerCase();
            switch (outputFormat) {
                case "xml":
                case "html":
                case "doc":
                case "rtf":
                case "pdf":
                    isFileRequested = true;
                    break;
                default:
                    log.warning("Incorrect output file format: " + outputFormat);
                    outputFormat = null;
            }
        }

        if(maxMatches <= 0) maxMatches = MAX_MATCHES_PER_SERVER_DEFAULT;
        // reduce output to browser page
        if(!isFileRequested && maxMatches >= MAX_MATCHES_PER_SERVER_DEFAULT)  // requested to much result to browser
            this.maxMatches = MAX_MATCHES_PER_SERVER_DEFAULT;

        generateFilename();
        tryFindCached();

        return fatalErrors;
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
        Date dNow = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat ("'_at_'HH-mm-ss_dd-MM-yyyy" );
        filename = ServiceController.getInstance().getRequestCount() + "-request_hashcode[" + this.hashCode() + ']' + sdf.format(dNow);
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
    public int hashCode() {
        int result = searchString.hashCode();
        result = 31 * result + Domain.getTargetServers(this).hashCode();
        result = 31 * result + (dateIntervals != null ? dateIntervals.hashCode() : 0);
        result = 31 * result + (isCaseSensitive ? 1 : 0);
        result = 31 * result + (isRegExp ? 1 : 0);
        return result;
    }

/*    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Request request = (Request) o;

        if (isCaseSensitive != request.isCaseSensitive) return false;
        if (isRegExp != request.isRegExp) return false;
        if (!searchString.equals(request.searchString)) return false;
        if (target != null ? !target.equals(request.target) : request.target != null) return false;
        return dateIntervals != null ? dateIntervals.equals(request.dateIntervals) : request.dateIntervals == null;

    }*/

}
