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
    //private Map<Node, Double> exitPaths;
    private Graph graph;

    public Dijkstra(String xmlData){

        this.data = xmlData;
        this.graph = null;
    }

    // Ok
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
                boolean compromised = Boolean.parseBoolean(edgeElement.getElementsByTagName("compromised").item(0).getTextContent());

                Node fromNode = graph.nodes.get(fromId);
                Node toNode = graph.nodes.get(toId);

                if (fromNode != null && toNode != null) {
                    Edge edge = new Edge(id, fromNode, toNode, length, disabilityAccessible, speed, disabilitySpeed, compromised);
                    Edge edge2 = new Edge(id, toNode, fromNode, length, disabilityAccessible, speed, disabilitySpeed, compromised);
                    graph.addEdge(edge);
                    graph.addEdge(edge2);
                }
            }

            this.graph = graph;

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    // Ok
    public static List<Node> findShortestPath(Graph graph, String startId, Boolean disability) {
        if (!graph.nodes.containsKey(startId)){
            return Collections.emptyList();
        }

        PriorityQueue<PathNode> pq = new PriorityQueue<>(Comparator.comparingDouble(n -> n.cost));
        Map<Node, Double> distances = new HashMap<>();
        Map<Node, Node> predecessors = new HashMap<>();
        Set<Node> visited = new HashSet<>();

        Node startNode = graph.nodes.get(startId);
        pq.add(new PathNode(startNode, 0));
        distances.put(startNode, 0.0);

        Node targetNode = null;

        while (!pq.isEmpty()) {
            PathNode current = pq.poll();
            Node currentNode = current.node;

            if (visited.contains(currentNode)){
                continue;
            }
            visited.add(currentNode);

            if (currentNode.exit) {
                targetNode = currentNode;
                break;
            }

            for (Edge edge : graph.adjacencyList.getOrDefault(currentNode, Collections.emptyList())) {
                if (edge.compromised || edge.to.compromised) {
                    continue;
                }

                if(disability){
                    if(!edge.disabilityAccessible){
                        continue;
                    }
                }

                double newDist = distances.getOrDefault(currentNode, Double.MAX_VALUE) + edge.length;
                if (newDist < distances.getOrDefault(edge.to, Double.MAX_VALUE)) {
                    distances.put(edge.to, newDist);
                    predecessors.put(edge.to, currentNode);
                    pq.add(new PathNode(edge.to, newDist));
                }
            }
        }

        if (targetNode == null) {
            distances.clear();
            pq.clear();
            visited.clear();
            predecessors.clear();
            pq.add(new PathNode(startNode, 0));
            distances.put(startNode, 0.0);

            while (!pq.isEmpty()) {
                PathNode current = pq.poll();
                Node currentNode = current.node;

                if (visited.contains(currentNode)){
                    continue;
                }
                visited.add(currentNode);

                if (currentNode.shelter) {
                    targetNode = currentNode;
                    break;
                }

                for (Edge edge : graph.adjacencyList.getOrDefault(currentNode, Collections.emptyList())) {
                    if (edge.compromised || edge.to.compromised){
                        continue;
                    }

                    if(disability){
                        if(!edge.disabilityAccessible){
                            continue;
                        }
                    }

                    double newDist = distances.getOrDefault(currentNode, Double.MAX_VALUE) + edge.length;
                    if (newDist < distances.getOrDefault(edge.to, Double.MAX_VALUE)) {
                        distances.put(edge.to, newDist);
                        predecessors.put(edge.to, currentNode);
                        pq.add(new PathNode(edge.to, newDist));
                    }
                }
            }
        }

        if (targetNode == null){
            return Collections.emptyList();
        }

        List<Node> path = new ArrayList<>();
        for (Node at = targetNode; at != null; at = predecessors.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);

        return path;
    }

    // Ok
    private static class PathNode {
        Node node;
        double cost;
        PathNode(Node node, double cost) {
            this.node = node;
            this.cost = cost;
        }
    }

    // Ok
    public static Edge getEdge(Graph graph, Node n1, Node n2){
        for (Map.Entry<Node, List<Edge>> entry : graph.adjacencyList.entrySet()) {
            List<Edge> edges = entry.getValue();

            for (Edge edge : edges) {
                if((edge.from == n1 && edge.to == n2) || (edge.to == n1 && edge.from == n2)){
                    return edge;
                }
            }
        }

        return null;
    }

    // Ok
    public static double[] getPathLength(Graph graph, List<Node> path, Boolean disability){
        Double distance=0.0,time=0.0;

        for(int i=0;i<path.size()-1;i++){
            Double lngth = Objects.requireNonNull(getEdge(graph, path.get(i), path.get(i + 1))).length;
            distance += lngth;
            if(disability){
                time += lngth / Objects.requireNonNull(getEdge(graph, path.get(i), path.get(i + 1))).disabilitySpeed;
            }
            else{
                time += lngth / Objects.requireNonNull(getEdge(graph, path.get(i), path.get(i + 1))).speed;
            }
        }

        return new double[]{distance, time};
    }

    // TODO Change path for saving the json
    public JSONObject createJSON(){

        JSONObject mainObject = new JSONObject();

        // Create a JSONArray to hold multiple entries
        JSONArray jsonArray = new JSONArray();

        for(String startNode : graph.getNodeIds()){
            List<Node> lst = findShortestPath(graph, startNode, false);
            List<Node> disabilitylst = findShortestPath(graph, startNode, true);

            Map<String, Object> orderedMap = new LinkedHashMap<>();


            JSONArray pathArray = new JSONArray();
            JSONArray disabilityPathArray = new JSONArray();

            for (Node node : lst) {
                pathArray.put(node.id);
            }

            for (Node node : disabilitylst) {
                disabilityPathArray.put(node.id);
            }

            Node currNode = graph.nodes.get(startNode);
            orderedMap.put("Disability Path", disabilityPathArray);

            orderedMap.put("Path", pathArray);
            orderedMap.put("Id", currNode.id);

            double[] result = getPathLength(graph,lst,false);
            double[] disability_result = getPathLength(graph,disabilitylst,true);

            orderedMap.put("Path length", result[0]);
            orderedMap.put("Disability path length", disability_result[0]);
            orderedMap.put("Path time", result[1]);
            orderedMap.put("Disability path time", disability_result[1]);

            JSONObject item = new JSONObject(orderedMap);
            jsonArray.put(item);
        }

        mainObject.put("items", jsonArray);

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedNow = now.format(formatter);

        String filename = "/home/serveruser/SavedXMLs/JSONFiles/" + formattedNow + ".json";
        //String filename = "/home/trifon/SavedXMLs/JSONFiles/" + formattedNow + ".json";

        try (FileWriter file = new FileWriter(filename)) { //"/home/trifon/SavedXMLs/JSONFiles/output.json"
            file.write(mainObject.toString(4)); // 4 is the indentation level for pretty printing
            System.out.println("JSON file created successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mainObject;
    }
}
