package ru.pushkarev.LogsSearcher.utils;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public final class DateParser {
    private static final Logger log = Logger.getLogger(DateParser.class.getName());

    private static DatatypeFactory datatypeFactory;

    static {
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            log.severe("Failed create DatatypeFactory. " + e);
        }
    }

    private String dateFormat;
    private SimpleDateFormat simpleDateFormat;

    private static final Map<String, String> DATE_FORMAT_REGEXPS = new HashMap<String, String>() {{
        put("^\\w{3} (\\d{1,2}), \\d\\d\\d\\d \\d{1,2}:\\d{2}:\\d{2} \\w{2} \\w{3}$", "MMM d, yyyy h:m:s a ");  // <Jul 11, 2016 1:05:01 PM MSK>
        put("^\\w{3} (\\d{1,2}), \\d\\d\\d\\d, \\d{1,2}:\\d{2}:\\d{2} \\w{2} \\w{3}$", "MMM d, yyyy, h:m:s a ");  // <Jul 11, 2016, 1:05:01 PM MSK>
        put("^\\w{3} (\\d{1,2}), \\d\\d\\d\\d, \\d{1,2}:\\d{2}:\\d{2},\\d{0,3} \\w{2} \\w{3}$", "MMM d, yyyy, h:m:s,S a ");  // <Feb 21, 2017, 9:11:18,804 AM EST>
        put("^\\d{1,2}.\\d{2}.\\d{4}, \\d{1,2}:\\d{2}:\\d{2},\\d+ \\w{2} \\w{3}$", "dd.M.yyyy, h:m:s,S a ");  // <15.11.2016, 7:58:53,90 PM MSK>

        put("^\\d{8}$", "yyyyMMdd");
        put("^\\d{1,2}-\\d{1,2}-\\d{4}$", "dd-MM-yyyy");
        put("^\\d{4}-\\d{1,2}-\\d{1,2}$", "yyyy-MM-dd");
        put("^\\d{1,2}/\\d{1,2}/\\d{4}$", "MM/dd/yyyy");
        put("^\\d{4}/\\d{1,2}/\\d{1,2}$", "yyyy/MM/dd");
        put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}$", "dd MMM yyyy");
        put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}$", "dd MMMM yyyy");
        put("^\\d{12}$", "yyyyMMddHHmm");
        put("^\\d{8}\\s\\d{4}$", "yyyyMMdd HHmm");
        put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}$", "dd-MM-yyyy HH:mm");
        put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy-MM-dd HH:mm");
        put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}$", "MM/dd/yyyy HH:mm");
        put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy/MM/dd HH:mm");
        put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMM yyyy HH:mm");
        put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMMM yyyy HH:mm");
        put("^\\d{14}$", "yyyyMMddHHmmss");
        put("^\\d{8}\\s\\d{6}$", "yyyyMMdd HHmmss");
        put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd-MM-yyyy HH:mm:ss");
        put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy-MM-dd HH:mm:ss");
        put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "MM/dd/yyyy HH:mm:ss");
        put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy/MM/dd HH:mm:ss");
        put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMM yyyy HH:mm:ss");
        put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMMM yyyy HH:mm:ss");
    }};

    public DateParser() { }

    public Date parse(String dateString) {
        if (dateFormat == null) {
            dateFormat = determineDateFormat(dateString);
        }
        return parse(dateString, dateFormat);
    }

    public Date parse(String dateString, String dateFormat) {
        if (simpleDateFormat == null) {
            simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.US);
        }
        //simpleDateFormat.setLenient(false); // Don't automatically convert invalid date.
        Date date = null;
        try {
            date = simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            dateFormat = determineDateFormat(dateString);
            simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.US);
            try {
                date = simpleDateFormat.parse(dateString);
            } catch (ParseException e1) {
                log.log(Level.WARNING, "Date parsing error :" + dateString + e);
            }

        }
        return date;
    }

    public static Date parse(String dateString, SimpleDateFormat simpleDateFormat) throws ParseException {
        //simpleDateFormat.setLenient(false); // Don't automatically convert invalid date.
        return simpleDateFormat.parse(dateString);
    }

    public String determineDateFormat(String dateString) {
        for (Map.Entry<String, String> entry : DATE_FORMAT_REGEXPS.entrySet()) {
            if (dateString.toLowerCase().matches(entry.getKey())) {
                return entry.getValue();
            }
        }
        log.warning("Cannot find exact date format pattern for :" + dateString);
        return null; // Unknown format.
    }

    public void resetDateFormat() {
        dateFormat = null;
    }

        /*
* Converts java.util.Date to javax.xml.datatype.XMLGregorianCalendar
*/
    public static XMLGregorianCalendar toXMLGregorianCalendar(Date date){
        GregorianCalendar gCalendar = new GregorianCalendar();
        gCalendar.setTime(date);
        return datatypeFactory.newXMLGregorianCalendar(gCalendar);
    }

    /*
* Converts XMLGregorianCalendar to java.util.Date in Java
*/
    public static Date toDate(XMLGregorianCalendar calendar){
        if(calendar == null) {
            return null;
        }
        return calendar.toGregorianCalendar().getTime();
    }
}

