package ru.pushkarev.LogsSearcher.type;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

public class ServerElement {

    @XmlAttribute
    private String name;

    @XmlElement(name = "logBlock")
    List<LogBlock> logBlocks = new ArrayList<>();

    public ServerElement(String name, List<LogBlock> logBlocks) {
        this.name = name;
        this.logBlocks = logBlocks;
    }

    public ServerElement() {}

    @Override
    public String toString() {
        return "ServerElement{" +
                "name='" + name + '\'' +
                ", logBlocks=" + logBlocks.size() +
                '}';
    }
}
