package DijkstraAlgorithm;

public class Edge {
    String id;
    Node from;
    Node to;
    double length;
    boolean disabilityAccessible;
    double speed;
    double disabilitySpeed;

    public Edge(String id, Node from, Node to, double length, boolean disabilityAccessible, double speed, double disabilitySpeed) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.length = length;
        this.disabilityAccessible = disabilityAccessible;
        this.speed = speed;
        this.disabilitySpeed = disabilitySpeed;
    }

    @Override
    public String toString() {
        return from + " → " + to + " (Length: " + length + ")";
    }
}
