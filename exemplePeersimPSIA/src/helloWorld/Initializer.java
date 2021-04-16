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
	private HelloWorld startNode;

	public Initializer(String prefix) {
		// recuperation du pid de la couche applicative
		this.helloWorldPid = Configuration.getPid(prefix + ".helloWorldProtocolPid");
		int startNodeId = 0;
		this.startNode = (HelloWorld) Network.get(startNodeId).getProtocol(this.helloWorldPid);
		startNode.setTransportLayer(startNodeId);
		startNode.turnOn();
	}

	public boolean execute() {
		int nodeNb;

		/**
		 * Checking configuration
		 */
		// recuperation de la taille du reseau
		nodeNb = Network.size();

		if (nodeNb < 1) {
			System.err.println("Network size is not positive");
			System.exit(1);
		}

		/**
		 * Creating ring : Join
		 */
		int nodeId = this.randomNode();

		System.out.println("\nCreating ring ...");
		this.joinNode(nodeId);
		this.joinNode(this.randomNode());
		this.joinNode(this.randomNode());
		this.joinNode(this.randomNode());
		this.joinNode(this.randomNode());

		/**
		 * Creating ring : leave
		 */
		// this.leaveNode(nodeId);

		this.displayRing();

		/**
		 * Sending message
		 */

		/*
		 * long uuid = ((HelloWorld)
		 * Network.get(nodeId).getProtocol(this.helloWorldPid)).getUUID();
		 * 
		 * helloMsg = new Message(Message.HELLOWORLD, "Hello!!", uuid);
		 * 
		 * System.out.println("send message to Node : " + nodeId);
		 * this.startNode.send(helloMsg);
		 */

		/**
		 * Advanced routing : without cheating
		 */

		int numberOfShift = randomIntBetween(2, 6 - 1); // 1 because we do not want it chooses the right
		// neighbour and -2 because we do not want it
		// chooses the leftNeighbour and itself, because
		// we will do a spin on the right way
		System.out.printf("\nnumberOfShift :%d", numberOfShift);
		this.startNode.link(this.startNode, numberOfShift);

		/**
		 * Putting data
		 */
		System.out.println("\nPutting data ...");
		this.startNode.storing(new Data("Bonjour"), 0, this.startNode);
		this.startNode.storing(new Data("Hello"), 0, this.startNode);
		this.startNode.storing(new Data("Guten tag"), 0, this.startNode);
		this.startNode.storing(new Data("hola"), 0, this.startNode);

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
		this.startNode.addNeighbour(node);
	}

	public int randomIntBetween(int min, int max) {
		Random random = new Random();
		return random.nextInt(max - min + 1) + min;
	}

	public int randomNode() {
		Random random = new Random();
		int nodeId;
		int min = 0;
		int max = Network.size() - 1; // check if it is -1
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

	public void leaveNode(int nodeId) {
		HelloWorld node = this.getNode(nodeId);
		node.leave();
	}

	public void displayRing() {
		System.out.println("\nRing :");
		HelloWorld currentNode = this.startNode;
		do {

			System.out.printf("%s \n	left : %d | right : %d | farNeighbour : %d\n", currentNode.toString(),
					currentNode.getLeftNeighbour().getNodeId(), currentNode.getRightNeighbour().getNodeId(),
					currentNode.getFarNeighbour().getNodeId());
			displayData(currentNode);
			int currentNodeId = currentNode.getRightNeighbour().getNodeId();
			currentNode = this.getNode(currentNodeId);

		} while (currentNode.getNodeId() != this.startNode.getNodeId());
	}

	public void displayData(HelloWorld node) {
		for (Data data : node.getListOfDatas()) {
			System.out.println("	Data : uuid :" + data.getUUID() + " content : " + data.getContent());
		}

	}
}