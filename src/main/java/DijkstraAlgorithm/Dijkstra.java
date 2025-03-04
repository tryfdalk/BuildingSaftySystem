package DijkstraAlgorithm;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Dijkstra {

    private  String data;
    private Map<Node, Double> exitPaths;
    private Graph graph;

    public Dijkstra(String xmlData){

        this.data = xmlData;
        this.graph = null;
    }

    public void createGraph(){

        try{
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document document = builder.parse(inputStream);
            document.getDocumentElement().normalize();

            // Create graph
            Graph graph = new Graph();

            NodeList nodes = document.getElementsByTagName("node");
            for(int i=0;i<nodes.getLength();i++){
                Element nodeElement = (Element) nodes.item(i);
                String id = nodeElement.getElementsByTagName("id").item(0).getTextContent();
                String label = nodeElement.getElementsByTagName("label").item(0).getTextContent();
                boolean exit = Boolean.parseBoolean(nodeElement.getElementsByTagName("exit").item(0).getTextContent());
                boolean shelter = Boolean.parseBoolean(nodeElement.getElementsByTagName("shelter").item(0).getTextContent());
                boolean compromised = Boolean.parseBoolean(nodeElement.getElementsByTagName("compromised").item(0).getTextContent());

                graph.addNode(new Node(id, label, exit, shelter, compromised));
            }

            NodeList edges = document.getElementsByTagName("edge");
            for (int i = 0; i < edges.getLength(); i++) {
                Element edgeElement = (Element) edges.item(i);
                String id = edgeElement.getElementsByTagName("id").item(0).getTextContent();
                String fromId = edgeElement.getElementsByTagName("from").item(0).getTextContent();
                String toId = edgeElement.getElementsByTagName("to").item(0).getTextContent();
                double length = Double.parseDouble(edgeElement.getElementsByTagName("lengths").item(0).getTextContent());
                boolean disabilityAccessible = Boolean.parseBoolean(edgeElement.getElementsByTagName("disability").item(0).getTextContent());
                double speed = Double.parseDouble(edgeElement.getElementsByTagName("speed").item(0).getTextContent());
                double disabilitySpeed = Double.parseDouble(edgeElement.getElementsByTagName("disability_speed").item(0).getTextContent());

                Node fromNode = graph.nodes.get(fromId);
                Node toNode = graph.nodes.get(toId);

                if (fromNode != null && toNode != null) {
                    Edge edge = new Edge(id, fromNode, toNode, length, disabilityAccessible, speed, disabilitySpeed);
                    graph.addEdge(edge);
                }
            }

            this.graph = graph;

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void findShortestPath(Graph graph, String startId) {
        Node start = graph.nodes.get(startId);
        if (start == null) {
            throw new IllegalArgumentException("Start node not found in graph.");
        }

        // Distance map to store the shortest distance from start to each node
        Map<Node, Double> distances = new HashMap<>();
        for (Node node : graph.nodes.values()) {
            distances.put(node, Double.MAX_VALUE);
        }
        distances.put(start, 0.0);

        Set<Node> visited = new HashSet<>();
        Map<Node, Double> exitPaths = new HashMap<>();

        PriorityQueue<NodeDistancePair> pq = new PriorityQueue<>(Comparator.comparingDouble(pair -> pair.distance));
        pq.add(new NodeDistancePair(start, 0.0));

        while (!pq.isEmpty()) {
            NodeDistancePair currentPair = pq.poll();
            Node currentNode = currentPair.node;
            double currentDistance = currentPair.distance;

            // Skip if already visited
            if (visited.contains(currentNode)) continue;
            visited.add(currentNode);

            // If current node is an exit, store the result and continue
            if (currentNode.exit || currentNode.shelter) {
                exitPaths.put(currentNode, currentDistance);
                // If all exits are found, we can stop searching
                if (exitPaths.size() == graph.getExitNodes().size()) {
                    break;
                }
            }

            // Process neighbors
            for (Edge edge : graph.adjacencyList.getOrDefault(currentNode, new ArrayList<>())) {
                Node neighbor = edge.to;
                double newDist = currentDistance + edge.length * edge.speed; // Added the * edge.speed

                if (newDist < distances.get(neighbor)) {
                    distances.put(neighbor, newDist);
                    pq.add(new NodeDistancePair(neighbor, newDist));
                }
            }
        }

        this.exitPaths = exitPaths;
    }

    public void printPaths(String startNode){

        if(exitPaths.isEmpty()){
            System.out.println("\nNo paths to exits or shelters where found from " + startNode + ".");
            return;
        }

        System.out.println("\nShortest Paths from " + startNode + ":");
        for (Map.Entry<Node, Double> entry : exitPaths.entrySet()) {
            System.out.println("To " + entry.getKey().id + " → Distance: " + entry.getValue());
        }
    }

    public JSONObject createJSON(){

        JSONObject mainObject = new JSONObject();

        // Create a JSONArray to hold multiple entries
        JSONArray jsonArray = new JSONArray();

        for(String startNode : graph.getNodeIds()){
            findShortestPath(graph, startNode);
            JSONObject item = new JSONObject();
            Node currNode = graph.nodes.get(startNode);
            item.put("id", currNode.id);
            item.put("label", currNode.label);
            for(Map.Entry<Node, Double> entry : exitPaths.entrySet()){
                String tmp = entry.getKey().id + " " + entry.getKey().label;
                item.put(tmp, entry.getValue());
            }
            jsonArray.put(item);
        }

        mainObject.put("items", jsonArray);

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedNow = now.format(formatter);

        String filename = "/home/trifon/SavedXMLs/JSONFiles/" + formattedNow + ".json";

        try (FileWriter file = new FileWriter(filename)) { //"/home/trifon/SavedXMLs/JSONFiles/output.json"
            file.write(mainObject.toString(4)); // 4 is the indentation level for pretty printing
            System.out.println("JSON file created successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mainObject;
    }
}

class NodeDistancePair {
    Node node;
    double distance;

    public NodeDistancePair(Node node, double distance) {
        this.node = node;
        this.distance = distance;
    }
}
