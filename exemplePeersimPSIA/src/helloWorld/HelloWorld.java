package helloWorld;

import peersim.edsim.*;
import peersim.graph.NeighbourListGraph;
import peersim.core.*;
import peersim.config.*;

import java.util.TreeMap;
import java.util.ArrayList;
import java.util.UUID;
import java.lang.Math;

public class HelloWorld implements EDProtocol {

    // identifiant de la couche transport
    private int transportPid;

    // objet couche transport
    private HWTransport transport;

    // identifiant de la couche courante (la couche applicative)
    private int mypid;

    // le numero de noeud
    private int nodeId; // comme l'IP, on peut pas changer

    private Long uuid = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE; // 2e id qu'on va utiliser | à
    // déterminer au hasard

    private HelloWorld leftNeighbourNode;

    private HelloWorld rightNeighbourNode;

    private HelloWorld farNeighbourNode;

    private boolean on = false;

    // prefixe de la couche (nom de la variable de protocole du fichier de config)
    private String prefix;

    private ArrayList<Data> listOfData = new ArrayList<>();

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

    public HelloWorld getFarNeighbour() {
        if (this.farNeighbourNode == null) {
            return this;
        }
        return this.farNeighbourNode;
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

    /**
     * 
     * Join/leave Node
     * 
     */

    public boolean addNeighbour(HelloWorld node) {
        if (placeIsBetweenThisNodeAndHisRightNeighbourNode(node)) {
            HelloWorld rightNeighbourNode = this.getRightNeighbour();

            this.setNeighbourhoodBetweenCurrentNodeAndRightNeighbourNode(this, node, rightNeighbourNode);
            return true;

        } else if (placeIsBetweenThisNodeAndHisLeftNeighbourNode(node)) {
            HelloWorld leftNeighbourNode = this.getLeftNeighbour();

            this.setNeighbourhoodBetweenCurrentNodeAndLeftNeighbourNode(this, node, leftNeighbourNode);
            return true;

        } else if (placeIsFartherRightward(node)) {
            return this.getRightNeighbour().addNeighbour(node);
        } else { // place is farther leftward

            return this.getLeftNeighbour().addNeighbour(node);
        }
    }

    public boolean placeIsFartherRightward(HelloWorld node) {
        return this.uuid <= node.getUUID() && this.getRightNeighbour().getUUID() <= node.getUUID();
    }

    public boolean placeIsBetweenThisNodeAndHisRightNeighbourNode(HelloWorld node) {
        return thisNodeIsLowerThanNodeWhichIsLowerThanRightNeighbour(node)
                || thisNodeIsLowerThanNodeButWeArriveAtTheFirstNode(node);
    }

    public boolean placeIsBetweenThisNodeAndHisLeftNeighbourNode(HelloWorld node) {
        return thisNodeIsHigherThanNodeWhichIsHigherThanLeftNeighbour(node)
                || thisNodeIsHigherThanNodeButWeArriveAtTheFirstNode(node);
    }

    public boolean thisNodeIsLowerThanNodeWhichIsLowerThanRightNeighbour(HelloWorld node) {
        return this.uuid < node.getUUID() && node.getUUID() <= this.getRightNeighbour().getUUID();
    }

    public boolean thisNodeIsLowerThanNodeButWeArriveAtTheFirstNode(HelloWorld node) { // FristNode is the node which
                                                                                       // has the lowest UUID
        return this.uuid < node.getUUID() && this.uuid >= this.getRightNeighbour().getUUID();
    }

    public boolean thisNodeIsHigherThanNodeWhichIsHigherThanLeftNeighbour(HelloWorld node) {
        return this.uuid >= node.getUUID() && node.getUUID() > this.getLeftNeighbour().getUUID();
    }

    public boolean thisNodeIsHigherThanNodeButWeArriveAtTheFirstNode(HelloWorld node) { // FristNode is the node which
                                                                                        // has the lowest UUID
        return this.uuid >= node.getUUID() && this.uuid <= this.getLeftNeighbour().getUUID();
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
        this.leftNeighbourNode.setRightNeighbourNode(this.rightNeighbourNode);
        this.rightNeighbourNode.setLeftNeighbourNode(this.leftNeighbourNode);
    }

    /**
     * 
     * send/deliver message
     * 
     */

    // envoi d'un message (l'envoi se fait via la couche transport)
    public void send(Message msg) {
        Node dest = Network.get(this.getClosestNodeForUUID(msg.getTo()).getNodeId()); // to refactor
        this.transport.send(getMyNode(), dest, msg, this.mypid);
    }

    // affichage a la reception
    private void receive(Message msg) { // Deliver
        if (messageToThisNode(msg)) {
            System.out.println(this.nodeId + ": Received " + msg.getContent());
        } else {
            send(msg);
        }
    }

    public boolean messageToThisNode(Message msg) {
        return msg.getTo() == this.uuid;
    }

    /**
     * STORING DATA
     */

    public ArrayList<Data> getListOfDatas() {
        return this.listOfData;
    }

    public boolean storing(Data data, int cpt, HelloWorld lastNode) {
        HelloWorld closestNode = getClosestNodeForUUID(data.getUUID());
        int counter = (closestNode == lastNode) ? cpt + 1 : 1; // we compare pointers
        if (counter == 2) {
            closestNode.store(data);
            closestNode.getLeftNeighbour().store(data);
            closestNode.getRightNeighbour().store(data);
            return true;
        }
        return closestNode.storing(data, counter, closestNode);
    }

    public void store(Data data) {
        this.listOfData.add(data);
    }

    public HelloWorld getClosestNodeForUUID(long uuid) {
        TreeMap<Long, HelloWorld> dist = new TreeMap<Long, HelloWorld>();
        dist.put(Math.abs(this.getUUID() - uuid), this);
        dist.put(Math.abs(this.getLeftNeighbour().getUUID() - uuid), this.getLeftNeighbour());
        dist.put(Math.abs(this.getRightNeighbour().getUUID() - uuid), this.getRightNeighbour());
        dist.put(Math.abs(this.getFarNeighbour().getUUID() - uuid), this.getFarNeighbour());
        return dist.firstEntry().getValue(); // take the first one because it has the shortest distance
    }

    /**
     * Advanced routing : without cheating
     */

    public boolean link(HelloWorld node, int nbOfShift) { // 2 et 5
        int numberOfShift = nbOfShift - 1;
        if (numberOfShift == 0) {
            this.setFarNeighbour(node);
            node.setFarNeighbour(this);
            return true;
        }
        return this.rightNeighbourNode.link(node, numberOfShift);
    }

    public void setFarNeighbour(HelloWorld node) {
        this.farNeighbourNode = node;
    }
}