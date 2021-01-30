package com.pido.pidolabyrinthgame;

public class Node {
    private final Node parent;

    private int x;
    private int y;
    private int weight;

    public Node(Node parent, int x, int y, int weight) {
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.weight = weight;
    }

    public long getTotalWeight() {
        Node n = this;
        long totalWeight = 0;
        do {
            totalWeight += n.weight;
            n = n.parent;
        } while(n != null);

        return totalWeight;
    }

}
