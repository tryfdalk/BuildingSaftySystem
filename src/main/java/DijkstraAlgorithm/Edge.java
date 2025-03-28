package DijkstraAlgorithm;

public class Edge {
    String id;
    Node from;
    Node to;
    double length;
    boolean disabilityAccessible;
    double speed;
    double disabilitySpeed;
    boolean compromised;

    public Edge(String id, Node from, Node to, double length, boolean disabilityAccessible, double speed, double disabilitySpeed, boolean compromised) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.length = length;
        this.disabilityAccessible = disabilityAccessible;
        this.speed = speed;
        this.disabilitySpeed = disabilitySpeed;
        this.compromised = compromised;
    }

    @Override
    public String toString() {
        return from + " → " + to + " (Length: " + length + ")";
    }
}
