package ru.pushkarev.LogsSearcher.type;

import ru.pushkarev.LogsSearcher.utils.Config;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.LinkedHashSet;
import java.util.Set;

@Stateless
public class ServerElement {

    @XmlAttribute
    private String name;

    @XmlElement(name = "logBlock")
    Set<LogBlock> logBlocks = new LinkedHashSet<>();

    public ServerElement(String name, Set<LogBlock> logBlocks) {
        this.name = name;
        this.logBlocks = logBlocks;
    }

    public ServerElement() {}
}
