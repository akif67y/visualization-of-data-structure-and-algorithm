package com.example.demo.prim;



import java.util.Objects;

public class Edge {
    public final Integer target;
    public final double weight;

    public Edge(Integer target, double weight) {
        this.target = target;
        this.weight = weight;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Edge edge = (Edge) obj;
        return Double.compare(edge.weight, weight) == 0 && target.equals(edge.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, weight);
    }

    @Override
    public String toString() {
        return "Edge{" +
                "target=" + target +
                ", weight=" + weight +
                '}';
    }
}
