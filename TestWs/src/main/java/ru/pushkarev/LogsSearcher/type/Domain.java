package ru.pushkarev.LogsSearcher.type;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ru.pushkarev.LogsSearcher.utils.Config;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Domain {
    private static final Logger log = Logger.getLogger(Domain.class.getName());

    private static Domain instance;
    public static synchronized Domain getInstance(){
        if(instance == null){
            instance = new Domain();
        }
        return instance;
    }

    private Domain() {
        parseDomainConfig();
    }


    private String name;
    private final Set<Cluster> clustersList = new HashSet<>();
    private final Set<Server> serversList = new HashSet<>();


    public String getName() { return name; }
    public Set<Cluster> getClustersList() { return clustersList; }
    public Set<Server> getServersList() { return serversList; }


    private void addCluster(Cluster cluster) {
        clustersList.add(cluster);
    }

    public static Set<Server> getTargetServers(Request request) {
        Set<Server> targetServers = new HashSet<>();

        for (String target : request.getTarget().split(",")) {
            target = target.trim();
            if (target.equalsIgnoreCase(Domain.getInstance().getName())) {
                targetServers.addAll(Domain.getInstance().getServersList());
            } else if (Domain.getInstance().isCluster(target)) {
                targetServers.addAll(Domain.getInstance().getClusterByName(target).getServersList());
            } else if (Domain.getInstance().isServer(target)) {
                targetServers.add(Domain.getInstance().getServerByName(target));
            } else {
                log.info("Invalid entity : " + target);
            }
        }

        if(targetServers.isEmpty()) {
            log.info("Cant find domain, cluster or server with given names [" + request.getTarget() + "]. Using whole domain as a target.");
            targetServers.addAll(Domain.getInstance().getServersList());
        }

        return targetServers;
    }



    /** Checks existing cluster or create new one */
    public Cluster getOrCreateCluster(String name) {
        // Find existing cluster
        for (Cluster cluster : clustersList  ) {
            if (cluster.getName().equalsIgnoreCase(name))
                return cluster;
        }
        // Create new cluster
        Cluster cluster = new Cluster(name);
        addCluster(cluster);

        return cluster;
    }

    public Cluster getClusterByName(String name) {
        for (Cluster cluster: clustersList) {
            if(cluster.getName().equalsIgnoreCase(name))
                return cluster;
        }
        log.info(" Cant find cluster by name : " + name);
        return new Cluster();
    }

    public Server getServerByName(String name) {
        for (Server server: serversList  ) {
            if(server.getName().equalsIgnoreCase(name)) return server;
        }
        log.info(" Cant find server by name : " + name);
        return new Server();
    }

    public boolean isCluster(String name) {
        for(Cluster cluster : clustersList) {
            if(cluster.getName().equalsIgnoreCase(name))
                return true;
        }
        return false;
    }

    public boolean isServer(String name) {
        for(Server server : serversList) {
            if(server.getName().equalsIgnoreCase(name))
                return true;
        }
        return false;
    }


    /**
     * Reads config.xml
     * Builds Domain Structure
     */
    public void parseDomainConfig() {
        // Reading config.xml
        Path configPath = Config.getInstance().domainPath.resolve("config").resolve("config.xml");
        Document doc;
        try {
            File fXmlFile = new File(configPath.toString());
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(fXmlFile);
        } catch (Exception e) {
            String msg = "Cannot open and read config.xml : ";
            log.log(Level.SEVERE, msg + e.getMessage() + e);
            throw new RuntimeException(msg);
        }

        doc.getDocumentElement().normalize();
        // Sets Domain Name
        name = doc.getElementsByTagName("name").item(0).getTextContent();
        // Get server records
        NodeList nList = doc.getElementsByTagName("server");

        // Create Servers and Clusters
        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;

                String name = "", machine = "", port = "";
                Cluster cluster = Cluster.NO_CLUSTER;

                if(eElement.getElementsByTagName("name").item(0) != null) {
                    name = eElement.getElementsByTagName("name").item(0).getTextContent();
                }
                if(eElement.getElementsByTagName("machine").item(0) != null) {
                    machine = eElement.getElementsByTagName("machine").item(0).getTextContent();
                }
                if(eElement.getElementsByTagName("listen-port").item(0) != null) {
                    port = eElement.getElementsByTagName("listen-port").item(0).getTextContent();
                }
                if(eElement.getElementsByTagName("cluster").item(0) != null) {
                    String clusterName = eElement.getElementsByTagName("cluster").item(0).getTextContent();
                    cluster = getOrCreateCluster(clusterName);
                }
                Server server = new Server(name, machine, port, cluster);
                serversList.add(server);
            }
        }
        log.info("Domain structure built.");
    }

}
