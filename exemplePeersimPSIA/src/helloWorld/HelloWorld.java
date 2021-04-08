package helloWorld;

import peersim.edsim.*;
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

    private Hashtable<Integer, Long> leftNeighborNode; // dict avec <nodeId, UUID> du left
    private Hashtable<Integer, Long> rightNeighborNode;// dict avec <nodeId, UUID> du right

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

    public void setLeftNeighborNodeId(long leftNeighborNodeId) {
        this.leftNeighborNodeId = leftNeighborNodeId;
    }

    public void setRightNeighborNodeId(long rightNeighborNodeId) {
        this.rightNeighborNodeId = rightNeighborNodeId;
    }

    public boolean isTurnedOff() {
        return this.on == false;
    }
}