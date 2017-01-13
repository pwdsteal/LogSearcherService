package ru.pushkarev.LogsSearcher.type;

import javax.xml.bind.annotation.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Response {

    @XmlElement
    private String filename;

    @XmlAttribute
    private long searchTime;

    @XmlElement(name = "server")
    private List<ServerElement> servers;

    public List<ServerElement> getServers() {
        return servers;
    }

    public String getFilename() { return filename; }

    public void setFilename(String filename) { this.filename = filename; }

    public void setServers(List<ServerElement> servers) {
        this.servers = servers;
    }

    public Response() {
        servers = new ArrayList<>();
    }

    public Response(String filename) {
        this.filename = filename;
    }

    public void addServerElement(ServerElement serverElement) {
        this.servers.add(serverElement);
    }

    public long getSearchTime() {
        return searchTime;
    }

    public void setSearchTime(long searchTime) {
        this.searchTime = searchTime;
    }

}
