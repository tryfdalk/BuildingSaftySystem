package DijkstraAlgorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph {

    Map<String, Node> nodes;
    Map<Node, List<Edge>> adjacencyList;

    public Graph(){
        this.nodes = new HashMap<>();
        this.adjacencyList = new HashMap<>();
    }

    public void addNode(Node node){
        nodes.put(node.id, node);
        adjacencyList.putIfAbsent(node, new ArrayList<>());
    }

    public void addEdge(Edge edge){
        adjacencyList.get(edge.from).add(edge);
    }

    public List<Node> getExitNodes() {
        List<Node> exitNodes = new ArrayList<>();
        for (Node node : nodes.values()) {
            if (node.exit || node.shelter) {
                exitNodes.add(node);
            }
        }
        return exitNodes;
    }

    public ArrayList<String> getNodeIds(){
        return new ArrayList<>(nodes.keySet());
    }

    public void printGraph() {
        System.out.println("Graph:");
        for (Node node : adjacencyList.keySet()) {
            System.out.print(node + " → ");
            for (Edge edge : adjacencyList.get(node)) {
                System.out.print(edge.to + " (" + edge.length + "), ");
            }
            System.out.println();
        }
    }
}