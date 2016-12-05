package ru.pushkarev.LogsSearcher.type;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public class ServerElement {

    @XmlAttribute
    private String name;

    @XmlElement
    Set<LogBlock> logBlock = new LinkedHashSet<>();

    public ServerElement(String name, Set<LogBlock> logBlock) {
        this.name = name;
        this.logBlock = logBlock;
    }

    public ServerElement() {
    }
}
