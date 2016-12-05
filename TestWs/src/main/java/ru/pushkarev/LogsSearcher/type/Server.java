package ru.pushkarev.LogsSearcher.type;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

        this.path = Domain.getPath().resolve("servers").resolve(this.name);
    }

    public void printServerInfo() {
        System.out.println("name : " + name);
        System.out.println("machine : " + machine);
        System.out.println("listen-port : " + port);
        System.out.println("cluster : " + cluster.getName());
        System.out.println("path : " + path.toString());
        System.out.println();
    }

    public Set<File> getLogFilesList() {
        Set<File> filesList = new HashSet<>();

        File[] files = new File(this.path.resolve("logs").toString()).listFiles();

        for (File file : files) {
            if (file.isFile() &&
                    file.getName().contains(this.name + ".log") ||
                    file.getName().contains(Domain.getName() + ".log")) {  // add %domain_name% logs

                filesList.add(file);
            }
        }
        return filesList;
    }
}
