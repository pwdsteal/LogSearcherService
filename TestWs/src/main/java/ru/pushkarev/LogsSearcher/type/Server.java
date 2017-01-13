package ru.pushkarev.LogsSearcher.type;

import ru.pushkarev.LogsSearcher.utils.Config;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;


public class Server {
    private String name;
    private String machine;
    private int port;
    private Cluster cluster;
    private Path path;

    public String getName() {return name; }

    public Server() {}

    public Server(String name, String machine, String port, Cluster cluster) {
        this.name = name;
        this.machine = machine;
        this.port = Integer.parseInt(port);

        this.cluster = cluster;
        cluster.addServer(this);

        this.path = Config.getInstance().domainPath.resolve("servers").resolve(this.name);
    }


    public Set<File> getLogFilesList() {
        Set<File> filesList = new HashSet<>();

        File[] files = new File(this.path.resolve("logs").toString()).listFiles();

        for (File file : files) {
            if (file.isFile() &&
                    file.getName().contains(this.name + ".log") ||
                    file.getName().contains(Domain.getInstance().getName() + ".log")) {  // add %domain_name% logs

                filesList.add(file);
            }
        }
        return filesList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Server server = (Server) o;

        if (port != server.port) return false;
        if (name != null ? !name.equals(server.name) : server.name != null) return false;
        return cluster != null ? cluster.equals(server.cluster) : server.cluster == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + port;
        result = 31 * result + (cluster != null ? cluster.hashCode() : 0);
        return result;
    }
}
