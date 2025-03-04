package DijkstraAlgorithm;

public class Node {
    String id;
    String label;
    boolean exit;
    boolean shelter;
    boolean compromised;

    public Node(String id, String label, boolean exit, boolean shelter, boolean compromised) {
        this.id = id;
        this.label = label;
        this.exit = exit;
        this.shelter = shelter;
        this.compromised = compromised;
    }

    @Override
    public String toString() {
        return id + " (" + label + ")";
    }
}
