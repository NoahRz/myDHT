package helloWorld;

import peersim.edsim.*;
import peersim.graph.NeighbourListGraph;
import peersim.core.*;
import peersim.config.*;

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

    private HelloWorld leftNeighbourNode;

    private HelloWorld rightNeighbourNode;

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

    public void turnOff() {
        this.on = false;
    }

    public HelloWorld getRightNeighbour() {
        if (this.rightNeighbourNode == null) {
            return this;
        }
        return this.rightNeighbourNode;
    }

    public HelloWorld getLeftNeighbour() {
        if (this.leftNeighbourNode == null) {
            return this;
        }
        return this.leftNeighbourNode;
    }

    public boolean isTurnedOff() {
        return !this.on;
    }

    public boolean isTurnedOn() {
        return this.on;
    }

    public long getUUID() {
        return this.uuid;
    }

    public int getNodeId() {
        return this.nodeId;
    }

    public void setLeftNeighbourNode(HelloWorld node) {
        this.leftNeighbourNode = node;
    }

    public void setRightNeighbourNode(HelloWorld node) {
        this.rightNeighbourNode = node;
    }

    public boolean addNeighbour(int startNodeId, HelloWorld node) {
        if (this.placeIsBetweenCurrentNodeAndRightNeighbourNode(startNodeId, node)) {
            HelloWorld rightNeighbourNode = this.getRightNeighbour();

            this.setNeighbourhoodBetweenCurrentNodeAndRightNeighbourNode(this, node, rightNeighbourNode);
            return true;

        } else if (placeIsBetweenCurrentNodeAndLeftNeighbourNode(startNodeId, node)) {
            HelloWorld leftNeighbourNode = this.getLeftNeighbour();

            this.setNeighbourhoodBetweenCurrentNodeAndLeftNeighbourNode(this, node, leftNeighbourNode);
            return true;

        } else if (this.placeIsFartherRightward(node)) {
            return this.getRightNeighbour().addNeighbour(startNodeId, node);
        } else {
            // place is farther leftward
            return this.getLeftNeighbour().addNeighbour(startNodeId, node);
        }
    }

    public boolean placeIsFartherRightward(HelloWorld node) {
        return this.uuid <= node.getUUID() && this.getRightNeighbour().getUUID() <= node.getUUID();
    }

    public boolean placeIsBetweenCurrentNodeAndRightNeighbourNode(int startNodeId, HelloWorld node) {
        return (this.uuid < node.getUUID() && node.getUUID() <= this.getRightNeighbour().getUUID())
                || (this.uuid < node.getUUID() && this.getRightNeighbour().getNodeId() == startNodeId);
    }

    public boolean placeIsBetweenCurrentNodeAndLeftNeighbourNode(int startNodeId, HelloWorld node) {
        return (this.uuid >= node.getUUID() && node.getUUID() > this.getLeftNeighbour().getUUID())
                || (this.uuid >= node.getUUID() && this.getLeftNeighbour().getNodeId() == startNodeId);
    }

    public void setNeighbourhoodBetweenCurrentNodeAndRightNeighbourNode(HelloWorld leftNeighbourNode, HelloWorld node,
            HelloWorld rightNeighbourNode) {
        this.setNeighbours(node, leftNeighbourNode);
        this.setNeighbours(rightNeighbourNode, node);
    }

    public void setNeighbourhoodBetweenCurrentNodeAndLeftNeighbourNode(HelloWorld leftNeighbourNode, HelloWorld node,
            HelloWorld rightNeighbourNode) {
        this.setNeighbours(leftNeighbourNode, node);
        this.setNeighbours(node, rightNeighbourNode);
    }

    public void setNeighbours(HelloWorld rightNeighbourNode, HelloWorld leftNeighbourNode) {
        rightNeighbourNode.setLeftNeighbourNode(leftNeighbourNode);
        leftNeighbourNode.setRightNeighbourNode(rightNeighbourNode);
    }

    public void leave() {
        this.turnOff();
        System.out.println("left " + this.leftNeighbourNode);
        this.leftNeighbourNode.setRightNeighbourNode(this.rightNeighbourNode);
        System.out.println("right " + this.rightNeighbourNode);
        this.rightNeighbourNode.setLeftNeighbourNode(this.leftNeighbourNode);
    }

}