package ru.pushkarev.LogsSearcher.type;

import ru.pushkarev.LogsSearcher.utils.DateParser;

import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;

@XmlType(propOrder = { "startXMLGC", "endXMLGC" })
public class DateInterval {

    private XMLGregorianCalendar startXMLGC;
    private XMLGregorianCalendar endXMLGC;

    public DateInterval(){}

    public DateInterval(XMLGregorianCalendar startXMLGC, XMLGregorianCalendar endXMLGC) {
        this.startXMLGC = startXMLGC;
        this.endXMLGC = endXMLGC;
    }

    public XMLGregorianCalendar getStartXMLGC() { return startXMLGC; }

    public XMLGregorianCalendar getEndXMLGC() {
        return endXMLGC;
    }

    public void setStartXMLGC(XMLGregorianCalendar startXMLGC) {
        if( startXMLGC != null) {
            this.startXMLGC = startXMLGC;
        } else {
            this.startXMLGC = DateParser.toXMLGregorianCalendar(new Date(0));
        }

    }
    public void setEndXMLGC(XMLGregorianCalendar endXMLGC) {
        if (endXMLGC != null) {
            this.endXMLGC = endXMLGC;
        } else {
            this.endXMLGC = DateParser.toXMLGregorianCalendar(new Date());
        }
    }

    public Date getStart() { return DateParser.toDate(startXMLGC); }
    public Date getEnd() {   return DateParser.toDate(endXMLGC); }

}
