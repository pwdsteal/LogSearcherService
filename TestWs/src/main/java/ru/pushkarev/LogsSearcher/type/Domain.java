package ru.pushkarev.LogsSearcher.type;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.ejb.Singleton;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class Domain {
    private static Logger log = Logger.getLogger(Domain.class.getName());

    private static String name;

    public static Path path = Paths.get(System.getProperty("user.dir")); // use at release
    private static Set<Cluster> clustersList = new HashSet<>();
    private static Set<Server> serversList = new HashSet<>();

    static {
        parseConfigXml();
    }

    public static String getName() { return name; }
    public static Path getPath() { return path; }
    public static Set<Cluster> getClustersList() { return clustersList; }
    public static Set<Server> getServersList() { return serversList; }


    private static void addCluster(Cluster cluster) {
        clustersList.add(cluster);
    }

    /** Checks existing cluster or create new one */
    public static Cluster getClusterInstance(String name) {
        // Find existing cluster
        for (Cluster cluster : clustersList  ) {
            if (cluster.getName().equals(name))
                return cluster;
        }
        // Create new cluster
        Cluster cluster = new Cluster(name);
        addCluster(cluster);

        return cluster;
    }

    public static Cluster getClusterByName(String name) {
        for (Cluster cluster: clustersList) {
            if(cluster.getName().equals(name))
                return cluster;
        }
        log.severe(" Cant find cluster by name : " + name);
        return new Cluster();
    }

    public static Server getServerByName(String name) {
        for (Server server: serversList  ) {
            if(server.getName().equals(name)) return server;
        }
        log.severe(" Cant find server by name : " + name);
        return new Server();
    }

    public static boolean isCluster(String name) {
        for(Cluster cluster : clustersList) {
            if(cluster.getName().equals(name))
                return true;
        }
        return false;
    }

    public static boolean isServer(String name) {
        for(Server server : serversList) {
            if(server.getName().equals(name))
                return true;
        }
        return false;
    }


    /**
     * Reads config.xml
     * Builds Domain Structure
     */
    private static void parseConfigXml() {
        // Reading config.xml
        Path configPath = path.resolve("config").resolve("config.xml");
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
                    cluster = getClusterInstance(clusterName);
                }
                Server server = new Server(name, machine, port, cluster);
                serversList.add(server);
            }
        }
    }


}
