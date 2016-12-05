package ru.pushkarev.LogsSearcher.type;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Query {
    private static Logger log = Logger.getLogger(Query.class.getName());
    private List<String> errors = new ArrayList<>();

    private Request request;
    private String targetType;
    private Set<Server> targetServers;


    public Set<Server> getTargetServers() {
        return targetServers;
    }

    public String getSearchText() { return request.getSearchString(); }

    public List<DateInterval> getDateIntervals() { return request.getDateIntervals(); }

    public boolean isRegExp() { return request.isRegExp(); }

    public int getMaxMatches() { return request.getMaxMatches(); }

    public boolean isCaseSensetive() { return request.isCaseSensitive(); }


    public Query(Request request) {
        this.request = request;
        determineTargetServers();
    }


/***  checks targetString and get target servers list */
    private void determineTargetServers() {
        targetServers = new HashSet<>();
        if (Domain.getName().equals(request.getTarget())) {
            targetType = "Domain";
            targetServers.addAll(Domain.getServersList());
        }
        else if (Domain.isCluster(request.getTarget())) {
            targetType = "Cluster";
            targetServers.addAll(Domain.getClusterByName(request.getTarget()).getServersList());
        }
        else if (Domain.isServer(request.getTarget())) {
            targetType = "ServerElement";
            targetServers.add(Domain.getServerByName(request.getTarget()));
        }
        else {
            String msg = " Cant find domain, cluster or server with name [" + request.getTarget() + "].\nUsing whole domain as a target.";
            targetType = "Domain";
            targetServers.addAll(Domain.getServersList());
            addErrorMessage(msg);
            log.log(Level.WARNING, msg);
        }
    }

    public void addErrorMessage(String text) {
        errors.add(text);
    }
}
