package helloWorld;

import peersim.core.*;
import peersim.config.*;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.File;
import java.io.PrintWriter;
import java.util.stream.*;

/*
  Module d'initialisation de helloWorld: 
  Fonctionnement:
    pour chaque noeud, le module fait le lien entre la couche transport et la couche applicative
    ensuite, il fait envoyer au noeud 0 un message "Hello" a tous les autres noeuds
 */
public class Initializer implements peersim.core.Control { // myDHT

	private int helloWorldPid;
	private dhtNode startNode;
	private List<String[]> dataLines = new ArrayList<>(); // Pour le CSV

	public Initializer(String prefix) {
		// recuperation du pid de la couche applicative
		this.helloWorldPid = Configuration.getPid(prefix + ".helloWorldProtocolPid");
		int startNodeId = 0;
		this.startNode = (dhtNode) Network.get(startNodeId).getProtocol(this.helloWorldPid);
		startNode.setTransportLayer(startNodeId);
		startNode.turnOn();
		log(logType.NODE_JOIN);
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

		System.out.println("\nCreating ring ...");

		// 1st method to create ring
		int nodeId = this.randomNode();
		this.joinNode(nodeId);
		log(logType.NODE_JOIN);
		this.joinNode(this.randomNode());
		log(logType.NODE_JOIN);
		this.joinNode(this.randomNode());
		log(logType.NODE_JOIN);
		this.joinNode(this.randomNode());
		log(logType.NODE_JOIN);
		this.joinNode(this.randomNode());
		log(logType.NODE_JOIN);

		// 2nd method to create ring
		int nbActiveNode = 5;
		// this.generateRing(nbActiveNode);

		this.displayRing();

		/**
		 * Sending message
		 */

		long uuid = ((dhtNode) Network.get(nodeId).getProtocol(this.helloWorldPid)).getUUID();

		Message helloMsg = new Message(Message.HELLOWORLD, "Hello!!", uuid);

		System.out.println("send message to Node : " + nodeId);
		this.startNode.send(helloMsg);
		log(logType.MESSAGE_SEND);

		/**
		 * Creating ring : leave
		 */
		this.leaveNode(nodeId);

		/**
		 * Advanced routing : by cheating
		 */

		this.cheatAdvancedRouting();

		/**
		 * Advanced routing : without cheating
		 */

		// this.nonCheatAdvancedRouting();

		/**
		 * Putting data
		 */
		System.out.println("\nPutting data ...");
		this.startNode.storing(new Data("Bonjour"), 0, this.startNode);

		log(logType.DATA_PUT);
		this.startNode.storing(new Data("Hello"), 0, this.startNode);
		log(logType.DATA_PUT);
		this.startNode.storing(new Data("Guten tag"), 0, this.startNode);
		log(logType.DATA_PUT);
		this.startNode.storing(new Data("hola"), 0, this.startNode);
		log(logType.DATA_PUT);

		this.displayRing();
		System.out.println("Initialization completed");
		try {
			this.givenDataArray_whenConvertToCSV_thenOutputCreated(); // -> save csv file
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * generates a ring of a number of active node
	 * 
	 * @param nbActiveNode number of active node
	 */
	public void generateRing(int nbActiveNode) {
		for (int i = 1; i <= nbActiveNode; i++) {
			this.joinNode(this.randomNode());
			log(logType.NODE_JOIN);
		}
	}

	/**
	 * joins a node to the ring
	 * 
	 * @param nodeId id of the node who joins the ring
	 */
	public void joinNode(int nodeId) {
		dhtNode node = (dhtNode) Network.get(nodeId).getProtocol(this.helloWorldPid);
		node.setTransportLayer(nodeId);
		node.turnOn();
		this.addNode(node);
	}

	/**
	 * add a neighbour to the start node
	 * 
	 * @param node neighbour of the start node
	 */
	public void addNode(dhtNode node) {
		this.startNode.addNeighbour(node); // peut être à changer le nom de la méthode
	}

	/**
	 * generates a random int between 2 numbers included
	 * 
	 * @param min minimum int
	 * @param max maximum int
	 * @return a random int
	 */
	public int randomIntBetween(int min, int max) {
		Random random = new Random();
		return random.nextInt(max - min + 1) + min;
	}

	/**
	 * 
	 * @return a random id of a turned off node
	 */
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

	/**
	 * returns the node who has the id nodeId
	 * 
	 * @param nodeId nodeId of the node we want to return
	 * @return the node
	 */
	public dhtNode getNode(int nodeId) {
		dhtNode node = (dhtNode) Network.get(nodeId).getProtocol(this.helloWorldPid);
		node.setTransportLayer(nodeId);
		return node;
	}

	/**
	 * makes the node who has the id nodeId leave
	 * 
	 * @param nodeId of the node we want it leaves
	 */
	public void leaveNode(int nodeId) {
		dhtNode node = this.getNode(nodeId);
		node.leave();
		log(logType.NODE_LEAVE);
	}

	/**
	 * display the ring
	 */
	public void displayRing() {
		System.out.println("\nRing :");
		dhtNode currentNode = this.startNode;
		do {

			System.out.printf("%s \n	left : %d | right : %d | farNeighbour : %d\n", currentNode.toString(),
					currentNode.getLeftNeighbour().getNodeId(), currentNode.getRightNeighbour().getNodeId(),
					currentNode.getFarNeighbour().getNodeId());
			displayData(currentNode);
			int currentNodeId = currentNode.getRightNeighbour().getNodeId();
			currentNode = this.getNode(currentNodeId);

		} while (currentNode.getNodeId() != this.startNode.getNodeId());
	}

	/**
	 * display the data contained in a node
	 * 
	 * @param node we want to display the data it contains
	 */
	public void displayData(dhtNode node) {
		for (Data data : node.getListOfData()) {
			System.out.println("	Data : uuid :" + data.getUUID() + " content : " + data.getContent());
		}
	}

	public void linkFarNeighbour(int nodeId1, int nodeId2) {

		dhtNode node1 = this.getNode(nodeId1);
		dhtNode node2 = this.getNode(nodeId2);
		if (!this.nodesAreDirectNeighbours(node1, node2)) {
			node1.setFarNeighbour(node2);
			node2.setFarNeighbour(node1);
			this.log(logType.ADD_FAR_LINK);
		}
	}

	public boolean nodesAreDirectNeighbours(dhtNode node1, dhtNode node2) {
		return node1.getLeftNeighbour() == node2 || node1.getRightNeighbour() == node2;
	}

	public ArrayList<dhtNode> getActiveNodeTable() {
		ArrayList<dhtNode> nodesTable = new ArrayList<>();
		dhtNode currentNode = this.startNode;
		do {
			nodesTable.add(currentNode);
			currentNode = currentNode.getRightNeighbour();
		} while (currentNode != this.startNode);

		return nodesTable;
	}

	/* Advanced routing : by cheating */
	public void cheatAdvancedRouting() {
		int nbActiveNode = this.getActiveNodeTable().size();
		if (nbActiveNode > 4) {
			int numberOfShift = randomIntBetween(3, nbActiveNode - 1); // 3 and -1 because we dont want it chooses
																		// himself,
																		// his reightNeighbour and his left neighbour
			System.out.println("\nnumberOfShift :" + numberOfShift);
			if (this.startNode.link(this.startNode, numberOfShift)) {
				this.log(logType.ADD_FAR_LINK);
			}
		}
	}

	/* Advanced routing : without cheating */
	public void nonCheatAdvancedRouting() {
		if (this.startNode.linkPiggybacking(this.startNode)) {
			log(logType.ADD_FAR_LINK);
		}
	}

	/* LOG */

	public void givenDataArray_whenConvertToCSV_thenOutputCreated() throws IOException {
		String csvFileName = "log.csv";
		File csvOutputFile = new File(csvFileName);
		try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
			dataLines.stream().map(this::convertToCSV).forEach(pw::println);
		}
		csvOutputFile.exists(); // je sais pas si utile
	}

	public String convertToCSV(String[] data) {
		return Stream.of(data).map(this::escapeSpecialCharacters).collect(Collectors.joining(","));
	}

	public String escapeSpecialCharacters(String data) {
		String escapedData = data.replaceAll("\\R", " ");
		if (data.contains(",") || data.contains("\"") || data.contains("'")) {
			data = data.replace("\"", "\"\"");
			escapedData = "\"" + data + "\"";
		}
		return escapedData;
	}

	public enum logType {
		NODE_JOIN, NODE_LEAVE, DATA_PUT, DATA_GET, ADD_FAR_LINK, MESSAGE_SEND
	}

	public void log(logType logType) {
		switch (logType) {
			// { nb_node, nb_data, nb_far_link, nb_messages }
			case NODE_JOIN:
				this.dataLines.add(new String[] { "1", "0", "0", "0" });
				break;
			case NODE_LEAVE:
				this.dataLines.add(new String[] { "-1", "0", "0", "0" });
				break;
			case DATA_PUT:
				this.dataLines.add(new String[] { "0", "1", "0", "0" });
				break;
			case DATA_GET:
				// code block
				break;
			case ADD_FAR_LINK:
				this.dataLines.add(new String[] { "0", "0", "1", "0" });
				break;
			case MESSAGE_SEND:
				this.dataLines.add(new String[] { "0", "0", "0", "1" });
				break;
			default:
				// code block
		}

	}

}