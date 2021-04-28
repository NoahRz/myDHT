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

    // transport layer identifier
    private int transportPid;

    // transport layer object
    private HWTransport transport;

    // identifier of the current layer (the application layer)
    private int mypid;

    // the node number
    private int nodeId; // like the IP, we cannot change

    private Long uuid = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE; // 2nd id

    private HelloWorld leftNeighbourNode;

    private HelloWorld rightNeighbourNode;

    private HelloWorld farNeighbourNode;

    private boolean on = false;

    // layer prefix (name of the protocol variable from the config file)
    private String prefix;

    private ArrayList<Data> listOfData = new ArrayList<>();

    public HelloWorld(String prefix) {
        this.prefix = prefix;
        // initialization of identifiers from the configuration file
        this.transportPid = Configuration.getPid(prefix + ".transport");
        this.mypid = Configuration.getPid(prefix + ".myself");
        this.transport = null;
    }

    /**
     * method called when a message is received by the HelloWorld protocol from the
     * node
     * 
     * @param node  the receiver
     * @param pid   the pid
     * @param event the event received
     */
    public void processEvent(Node node, int pid, Object event) {
        this.receive((Message) event);
    }

    /**
     * method necessary for the creation of the network (which is done by cloning a
     * prototype)
     */
    public Object clone() {

        HelloWorld dolly = new HelloWorld(this.prefix);

        return dolly;
    }

    // link between an object of the application layer and an
    // object of the transport layer located on the same node

    /**
     * link between an object of the application layer and an object of the
     * transport layer located on the same node
     * 
     * @param nodeId
     */
    public void setTransportLayer(int nodeId) {
        this.nodeId = nodeId;
        this.transport = (HWTransport) Network.get(this.nodeId).getProtocol(this.transportPid);
    }

    /**
     * 
     * @return the current node
     */
    private Node getMyNode() {
        return Network.get(this.nodeId);
    }

    /**
     * @return the representation of the object
     */
    public String toString() {
        return "Node " + this.nodeId;
    }

    /**
     * turns on the current node
     */
    public void turnOn() {
        this.on = true;
    }

    /**
     * turns off the current node
     */
    public void turnOff() {
        this.on = false;
    }

    /**
     * 
     * @return the right neighbour
     */
    public HelloWorld getRightNeighbour() {
        if (this.rightNeighbourNode == null) {
            return this;
        }
        return this.rightNeighbourNode;
    }

    /**
     * 
     * @return the left neighbour
     */
    public HelloWorld getLeftNeighbour() {
        if (this.leftNeighbourNode == null) {
            return this;
        }
        return this.leftNeighbourNode;
    }

    /**
     * 
     * @return the far neighbour (not direct neighbour)
     */
    public HelloWorld getFarNeighbour() {
        if (this.farNeighbourNode == null) {
            return this;
        }
        return this.farNeighbourNode;
    }

    /**
     * 
     * @return if it is turned off
     */
    public boolean isTurnedOff() {
        return !this.on;
    }

    /**
     * 
     * @return if it is turned on
     */
    public boolean isTurnedOn() {
        return this.on;
    }

    /**
     * 
     * @return the uuid
     */
    public long getUUID() {
        return this.uuid;
    }

    /**
     * 
     * @return the node id
     */
    public int getNodeId() {
        return this.nodeId;
    }

    /**
     * Set the left neighbour node
     * 
     * @param node the left neighbour node
     */
    public void setLeftNeighbourNode(HelloWorld node) {
        this.leftNeighbourNode = node;
    }

    /**
     * Set the right neighbour node
     * 
     * @param node the right neighbour node
     */
    public void setRightNeighbourNode(HelloWorld node) {
        this.rightNeighbourNode = node;
    }

    /**
     * 
     * Join/leave Node
     * 
     */

    /**
     * Add the neighbour to the ring where this node is (recursive function)
     * 
     * @param node the node to add
     * @return true if the node has been added
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

    /**
     * 
     * @param node node we are looking the place
     * @return if his place is farther rightward
     */
    public boolean placeIsFartherRightward(HelloWorld node) {
        return this.uuid <= node.getUUID() && this.getRightNeighbour().getUUID() <= node.getUUID();
    }

    /**
     * 
     * @param noden ode we are looking the place
     * @return if his place is between this node and his right neighbour
     */
    public boolean placeIsBetweenThisNodeAndHisRightNeighbourNode(HelloWorld node) {
        return thisNodeIsLowerThanNodeWhichIsLowerThanRightNeighbour(node)
                || thisNodeIsLowerThanNodeButWeArriveAtTheFirstNode(node);
    }

    /**
     * 
     * @param node node we are looking the place
     * @return if his place is between this node and his left neighbour
     */
    public boolean placeIsBetweenThisNodeAndHisLeftNeighbourNode(HelloWorld node) {
        return thisNodeIsHigherThanNodeWhichIsHigherThanLeftNeighbour(node)
                || thisNodeIsHigherThanNodeButWeArriveAtTheFirstNode(node);
    }

    /**
     * 
     * @param node node we are looking the place
     * @return if this node is lowe than the node we are looking the place which is
     *         lower than this node's right neighbour
     */
    public boolean thisNodeIsLowerThanNodeWhichIsLowerThanRightNeighbour(HelloWorld node) {
        return this.uuid < node.getUUID() && node.getUUID() <= this.getRightNeighbour().getUUID();
    }

    /**
     * 
     * @param node node we are looking the place
     * @return if this node is lower than the node we are looking the place but we
     *         arrive at the first node (we did a spin)
     */
    public boolean thisNodeIsLowerThanNodeButWeArriveAtTheFirstNode(HelloWorld node) { // FristNode is the node which
                                                                                       // has the lowest UUID
        return this.uuid < node.getUUID() && this.uuid >= this.getRightNeighbour().getUUID();
    }

    /**
     * 
     * @param node node we are looking the place
     * @return if this node is higher than the node we are looking the place which
     *         is higher than this node's left neighbour
     */
    public boolean thisNodeIsHigherThanNodeWhichIsHigherThanLeftNeighbour(HelloWorld node) {
        return this.uuid >= node.getUUID() && node.getUUID() > this.getLeftNeighbour().getUUID();
    }

    /**
     * 
     * @param node node we are looking the place
     * @return if this node is higher than the node we are looking the place bu we
     *         arrive at the first node (we did a spin)
     */
    public boolean thisNodeIsHigherThanNodeButWeArriveAtTheFirstNode(HelloWorld node) { // FristNode is the node which
                                                                                        // has the lowest UUID
        return this.uuid >= node.getUUID() && this.uuid <= this.getLeftNeighbour().getUUID();
    }

    /**
     * Set the new neighbours for each node
     * 
     * @param leftNeighbourNode
     * @param node
     * @param rightNeighbourNode
     */
    public void setNeighbourhoodBetweenCurrentNodeAndRightNeighbourNode(HelloWorld leftNeighbourNode, HelloWorld node,
            HelloWorld rightNeighbourNode) {
        this.setNeighbours(node, leftNeighbourNode);
        this.setNeighbours(rightNeighbourNode, node);
    }

    /**
     * set the new neighbours for each node
     * 
     * @param leftNeighbourNode
     * @param node
     * @param rightNeighbourNode
     */
    public void setNeighbourhoodBetweenCurrentNodeAndLeftNeighbourNode(HelloWorld leftNeighbourNode, HelloWorld node,
            HelloWorld rightNeighbourNode) {
        this.setNeighbours(leftNeighbourNode, node);
        this.setNeighbours(node, rightNeighbourNode);
    }

    /**
     * set the new neighbours for each node
     * 
     * @param rightNeighbourNode
     * @param leftNeighbourNode
     */
    public void setNeighbours(HelloWorld rightNeighbourNode, HelloWorld leftNeighbourNode) {
        rightNeighbourNode.setLeftNeighbourNode(leftNeighbourNode);
        leftNeighbourNode.setRightNeighbourNode(rightNeighbourNode);
    }

    /**
     * this node leaves the ring. We set also the new neighbours of his neighbours.
     */
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

    /**
     * sending a message (sending is done via the transport layer)
     * 
     * @param msg a message
     */
    public void send(Message msg) {
        Node dest = Network.get(this.getClosestNodeForUUID(msg.getTo()).getNodeId()); // to refactor
        this.transport.send(getMyNode(), dest, msg, this.mypid);
    }

    /**
     * display the message when received
     * 
     * @param msg a message
     */
    private void receive(Message msg) { // Deliver
        if (messageToThisNode(msg)) {
            System.out.println(this.nodeId + ": Received " + msg.getContent());
        } else {
            send(msg);
        }
    }

    /**
     * 
     * @param msg a message
     * @return if this node is the receiver of the message
     */
    public boolean messageToThisNode(Message msg) {
        return msg.getTo() == this.uuid;
    }

    /**
     * STORING DATA
     */

    /**
     * 
     * @return the list of data
     */
    public ArrayList<Data> getListOfData() {
        return this.listOfData;
    }

    /**
     * store the data to the ring
     * 
     * @param data     the data we want to store
     * @param cpt      a counter
     * @param lastNode last node we have visited
     * @return true it the data has been stored
     */
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

    /**
     * store the data to this node
     * 
     * @param data the data we want to store
     */
    public void store(Data data) {
        this.listOfData.add(data);
    }

    /**
     * 
     * @param uuid a uuid
     * @return return the node which has the closest uuid to uuid
     */
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

    /**
     * links to non neighbor nodes
     * 
     * @param node      we want to link
     * @param nbOfShift number of shift from the start node
     * @return true if the node has been linked
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

    public boolean linkPiggybacking(HelloWorld node) {
        // we go rightward
        if (this.getRightNeighbour() == node && this.getLeftNeighbour() == node) { // on a qu'un seul noeud
            return false;
        } else if (this.getRightNeighbour() == node) { // on a fait un tour
            return false;
        } else if(){
            
        }
    }

    /**
     * set a far neighbour (non direct neighbour)
     * 
     * @param node the far neighbour
     */
    public void setFarNeighbour(HelloWorld node) {
        this.farNeighbourNode = node;
    }
}