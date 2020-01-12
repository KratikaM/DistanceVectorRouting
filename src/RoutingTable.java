
import java.io.*;

@SuppressWarnings("serial")
public class RoutingTable implements Serializable{
	
	private String sourceNode;
	private String destinationNode;
	private String nextHopNode;
	private double cost;
	
	public RoutingTable(String sourceNode, String destinationNode, String nextHopNode, double cost)
	{
		this.sourceNode = sourceNode;
		this.destinationNode = destinationNode;
		this.nextHopNode = nextHopNode;
		this.cost = cost;
	}

	public String getSourceNode() {
		return sourceNode;
	}

	public void setSourceNode(String sourceNode) {
		this.sourceNode = sourceNode;
	}

	public String getDestinationNode() {
		return destinationNode;
	}

	public void setDestinationNode(String destinationNode) {
		this.destinationNode = destinationNode;
	}

	public String getNextHopNode() {
		return nextHopNode;
	}

	public void setNextHopNode(String nextHopNode) {
		this.nextHopNode = nextHopNode;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}
	

}
