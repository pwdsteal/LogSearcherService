package ru.pushkarev.LogsSearcher.type;

import ru.pushkarev.LogsSearcher.utils.DateParser;
import ru.pushkarev.LogsSearcher.utils.RegExUtils;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//@XmlType(propOrder = { "searchString", "target", "dateIntervals", "maxMatches",  })
@XmlRootElement
public class Request {
    @XmlElement(required = true)
    private String searchString;

    // optional parameters
    @XmlElement
    private String target;
    @XmlElement
    private List<DateInterval> dateIntervals = new ArrayList<>();
    @XmlElement(defaultValue = "300")
    private int maxMatches = 1024;

    private boolean isCaseSensitive;
    private boolean isRegExp;


    public Request() {}

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
    }

}
