package ru.pushkarev.LogsSearcher.type;

import javax.xml.datatype.XMLGregorianCalendar;


public class LogBlock {
    private XMLGregorianCalendar date;
    private String log;

    public XMLGregorianCalendar getDate() {
        return date;
    }

    public void setDate(XMLGregorianCalendar date) {
        this.date = date;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public LogBlock() { }

    public LogBlock(XMLGregorianCalendar date, String log) {
        this.date = date;
        this.log = log;
    }
}
