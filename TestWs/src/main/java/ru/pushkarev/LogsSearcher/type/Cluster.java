package ru.pushkarev.LogsSearcher.type;

import java.util.HashSet;
import java.util.Set;


public class Cluster {
    public static final Cluster NO_CLUSTER = new Cluster("None");

    private String name;
    private Set<Server> serversList;

    public Set<Server> getServersList() {
        return serversList;
    }

    public String getName() {
        return name;
    }

    public void addServer(Server server) {
        serversList.add(server);

    }

    public Cluster() {}

    public Cluster(String name) {
        this.name = name;
        serversList = new HashSet<>();
    }
}
