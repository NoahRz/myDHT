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
	private int startNodeId;
	private HelloWorld startNode;

	public Initializer(String prefix, int startNodeId) {
		// recuperation du pid de la couche applicative
		this.helloWorldPid = Configuration.getPid(prefix + ".helloWorldProtocolPid");
		this.startNodeId = startNodeId;

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

		// if (nodeNb < 1) {
		// System.err.println("Network size is not positive");
		// System.exit(1);
		// }

		// recuperation de la couche applicative de l'emetteur (le noeud 0)
		startNode = (HelloWorld) Network.get(startNodeId).getProtocol(this.helloWorldPid);
		startNode.setTransportLayer(startNodeId);

		int nodeId = this.randomNode();

		turnOnNode(nodeId);

		// pour chaque noeud, on fait le lien entre la couche applicative et la couche
		// transport
		// puis on fait envoyer au noeud 0 un message "Hello"
		for (int i = 1; i < nodeNb; i++) {
			dest = Network.get(i);
			current = (HelloWorld) dest.getProtocol(this.helloWorldPid);
			current.setTransportLayer(i);
			startNode.send(helloMsg, dest);
		}

		System.out.println("Initialization completed");
		return false;
	}

	public void turnOnNode(int nodeId){
		HelloWorld node = (HelloWorld) Network.get(nodeId).getProtocol(this.helloWorldPid);
		node.setTransportLayer(nodeId);

		if (node.isTurnedOff()){
			node.turnOn();
			this.addNode(node);
		}
	}

	public void addNode(HelloWorld node) {
		// check if startNode is alone

		// general case
		// if node sup to current node -> add node left
		// otherwise -> recursive add through all node neighbours
		/* case
			1) node sup au node courant et inf au node à droite
			2) node sup au node courant et sup au node à droite
			2) node inf au node courant et sup au node à gauche
			2) node inf au node courant et inf au node à gauche
		 */

	}

	public int randomNode(){
		Random random = new Random();
		int nodeId;
		int min = 0;
		int max = Network.size() -1; // à vérifier si c'est bien -1
		do{
			nodeId = random.nextInt(max - min + 1) + min;
		}while(nodeId != startNodeId);


	}
}