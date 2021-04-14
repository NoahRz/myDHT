package helloWorld;

import peersim.edsim.*;
import peersim.graph.NeighbourListGraph;
import peersim.core.*;
import peersim.config.*;

import java.util.Hashtable;
import java.util.UUID;

public class HelloWorld implements EDProtocol {

    // identifiant de la couche transport
    private int transportPid;

    // objet couche transport
    private HWTransport transport;

    // identifiant de la couche courante (la couche applicative)
    private int mypid;

    // le numero de noeud
    private int nodeId; // comme l'IP, on peut pas changer

    private long uuid = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE; // 2e id qu'on va utiliser | à
    // déterminer au hasard

    public class NeighborNode {
        private int id;
        private long uuid;

        public NeighborNode(int id, long uuid) {
            this.id = id;
            this.uuid = uuid;
        }

        public boolean isEmpty() {
            return (this.id == 0 && this.uuid == 0L);
        }

        public int getId() {
            return this.id;
        }

        public long getUUID() {
            return this.uuid;
        }

        public void setId(int id) {
            this.id = id;
        }

        public void setUUID(long uuid) {
            this.uuid = uuid;
        }

    }

    private NeighborNode leftNeighborNode = new NeighborNode(0, 0L); // dict avec <nodeId, UUID> du
    // left

    private NeighborNode rightNeighborNode = new NeighborNode(0, 0L);// dict avec <nodeId, UUID> du
    // right

    private boolean on = false;

    // prefixe de la couche (nom de la variable de protocole du fichier de config)
    private String prefix;

    public HelloWorld(String prefix) {
        this.prefix = prefix;
        // initialisation des identifiants a partir du fichier de configuration
        this.transportPid = Configuration.getPid(prefix + ".transport");
        this.mypid = Configuration.getPid(prefix + ".myself");
        this.transport = null;
    }

    // methode appelee lorsqu'un message est recu par le protocole HelloWorld du
    // noeud
    public void processEvent(Node node, int pid, Object event) {
        this.receive((Message) event);
    }

    // methode necessaire pour la creation du reseau (qui se fait par clonage d'un
    // prototype)
    public Object clone() {

        HelloWorld dolly = new HelloWorld(this.prefix);

        return dolly;
    }

    // liaison entre un objet de la couche applicative et un
    // objet de la couche transport situes sur le meme noeud
    public void setTransportLayer(int nodeId) {
        this.nodeId = nodeId;
        this.transport = (HWTransport) Network.get(this.nodeId).getProtocol(this.transportPid);
    }

    // envoi d'un message (l'envoi se fait via la couche transport)
    public void send(Message msg, Node dest) {
        this.transport.send(getMyNode(), dest, msg, this.mypid);
    }

    // affichage a la reception
    private void receive(Message msg) {
        System.out.println(this + ": Received " + msg.getContent());
    }

    // retourne le noeud courant
    private Node getMyNode() {
        return Network.get(this.nodeId);
    }

    public String toString() {
        return "Node " + this.nodeId;
    }

    public void turnOn() {
        this.on = true;
    }

    public NeighborNode getRightNeighbour() {
        if (this.rightNeighborNode.isEmpty()) {
            return new NeighborNode(nodeId, uuid);
        }
        return this.rightNeighborNode;
    }

    public NeighborNode getLeftNeighbour() {
        if (this.leftNeighborNode.isEmpty()) {
            return new NeighborNode(nodeId, uuid);
        }
        return this.leftNeighborNode;
    }

    public boolean isTurnedOff() {
        return !this.on;
    }

    public boolean isTurnedOn() {
        return this.on;
    }

    public boolean isAlone() {
        return this.leftNeighborNode == null;
    }

    public long getUUID() {
        return this.uuid;
    }

    public int getNodeId() {
        return this.nodeId;
    }

    public void setLeftNeighborNode(int nodeId, long uuid) {
        this.leftNeighborNode.setId(nodeId);
        this.leftNeighborNode.setUUID(uuid);
    }

    public void setRightNeighborNode(int nodeId, long uuid) {
        this.rightNeighborNode.setId(nodeId);
        this.rightNeighborNode.setUUID(uuid);
    }
}