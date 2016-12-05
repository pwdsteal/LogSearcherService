package ru.pushkarev.LogsSearcher.type;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class Response {


    @XmlAttribute
    private long searchTime;

    @XmlElement
    private List<ServerElement> server;

    public List<ServerElement> getServer() {
        return server;
    }

    public void setServer(List<ServerElement> server) {
        this.server = server;
    }

    public Response() {
        server = new ArrayList<>();
    }

    public void addServerElement(ServerElement serverElement) {
        this.server.add(serverElement);
    }

    public long getSearchTime() {
        return searchTime;
    }

    public void setSearchTime(long searchTime) {
        this.searchTime = searchTime;
    }

}
