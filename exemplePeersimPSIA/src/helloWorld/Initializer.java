package helloWorld;

import peersim.edsim.*;
import peersim.core.*;
import peersim.config.*;
import java.util.Random;

/*
  Module d'initialisation de helloWorld: 
  Fonctionnement:
    pour chaque noeud, le module fait le lien entre la couche transport et la couche applicative
    ensuite, il fait envoyer au noeud 0 un message "Hello" a tous les autres noeuds
 */
public class Initializer implements peersim.core.Control { // myDHT

	private int helloWorldPid;
	private int startNodeId = 0;
	private HelloWorld startNode;

	public Initializer(String prefix) {
		// recuperation du pid de la couche applicative
		this.helloWorldPid = Configuration.getPid(prefix + ".helloWorldProtocolPid");

		this.startNode = (HelloWorld) Network.get(startNodeId).getProtocol(this.helloWorldPid);
		startNode.setTransportLayer(startNodeId);
	}

	public boolean execute() {
		int nodeNb;
		HelloWorld startNode, current;
		Node dest;
		Message helloMsg;

		// recuperation de la taille du reseau
		nodeNb = Network.size();
		// creation du message
		helloMsg = new Message(Message.HELLOWORLD, "Hello!!");

		if (nodeNb < 1) {
			System.err.println("Network size is not positive");
			System.exit(1);
		}

		this.joinNode(this.randomNode());
		this.joinNode(this.randomNode());

		// pour chaque noeud, on fait le lien entre la couche applicative et la couche
		// transport
		// puis on fait envoyer au noeud 0 un message "Hello"
		// for (int i = 1; i < nodeNb; i++) {
		// dest = Network.get(i);
		// current = (HelloWorld) dest.getProtocol(this.helloWorldPid);
		// current.setTransportLayer(i);
		// this.startNode.send(helloMsg, dest);
		// }

		this.displayRing();
		System.out.println("Initialization completed");
		return false;
	}

	public void joinNode(int nodeId) {
		HelloWorld node = (HelloWorld) Network.get(nodeId).getProtocol(this.helloWorldPid);
		node.setTransportLayer(nodeId);
		node.turnOn();
		this.addNode(node);
	}

	public void addNode(HelloWorld node) {
		// check if startNode is alone

		if (node.getUUID() >= this.startNode.getUUID()) {
			this.addNodeOnRightWay(node); // go to the right
		} else {
			this.addNodeOnLeftWay(node);
		} // go to the left
	}

	public int randomNode() {
		Random random = new Random();
		int nodeId;
		int min = 0;
		int max = Network.size() - 1; // à vérifier si c'est bien -1

		do {
			nodeId = random.nextInt(max - min + 1) + min;
		} while (this.getNode(nodeId).isTurnedOn());

		return nodeId;
	}

	public HelloWorld getNode(int nodeId) {
		HelloWorld node = (HelloWorld) Network.get(nodeId).getProtocol(this.helloWorldPid);
		node.setTransportLayer(nodeId);
		return node;
	}

	public void addNodeOnRightWay(HelloWorld node) { // OK
		System.out.println("on right");
		HelloWorld currentNode = this.startNode;

		do {
			currentNode = this.getNode(currentNode.getRightNeighbour().getId());
		} while ((node.getUUID() >= currentNode.getUUID()) && (this.startNodeId != currentNode.getNodeId()));

		HelloWorld prevNode = this.getNode(currentNode.getLeftNeighbour().getId());
		currentNode.setLeftNeighborNode(node.getNodeId(), node.getUUID());
		node.setRightNeighborNode(currentNode.getNodeId(), currentNode.getUUID());
		prevNode.setRightNeighborNode(node.getNodeId(), node.getUUID());
		node.setLeftNeighborNode(prevNode.getNodeId(), prevNode.getUUID());
	}

	public void addNodeOnLeftWay(HelloWorld node) {
		System.out.println("on left");
		HelloWorld currentNode = this.startNode;

		do {
			currentNode = this.getNode(currentNode.getLeftNeighbour().getId());
		} while ((node.getUUID() < currentNode.getUUID()) && (this.startNodeId != currentNode.getNodeId()));

		HelloWorld nextNode = this.getNode(currentNode.getRightNeighbour().getId());
		currentNode.setRightNeighborNode(node.getNodeId(), node.getUUID());
		node.setLeftNeighborNode(currentNode.getNodeId(), currentNode.getUUID());
		nextNode.setLeftNeighborNode(node.getNodeId(), node.getUUID());
		node.setRightNeighborNode(nextNode.getNodeId(), nextNode.getUUID());
	}

	public void displayRing() {
		System.out.println("Ring :");
		HelloWorld currentNode = this.startNode;
		do {
			System.out.println("node : " + currentNode.getNodeId());
			System.out.println("left : " + currentNode.getLeftNeighbour().getId());
			System.out.println("right : " + currentNode.getRightNeighbour().getId());
			currentNode = this.getNode(currentNode.getRightNeighbour().getId());
		} while (currentNode.getNodeId() != this.startNode.getNodeId());
	}
}