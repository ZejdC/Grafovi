package DJeZ;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Graph<Oznaka> {
    List<Vertex<Oznaka>> vertices;
    List<Edge<Oznaka>> edges;

    Graph(){
        vertices = new ArrayList<>();
        edges = new ArrayList<>();
    }

    public boolean addVertex(Oznaka o){
        if(vertices.contains(new Vertex<>(o)))return false;
        vertices.add(new Vertex<>(o));
        return true;
    }
    public void resetAllVertices(){
        for (Vertex<Oznaka> vertex : vertices) {
            vertex.resetBoja();
        }
    }
    public void resetAllEdges(){
        for (Edge<Oznaka> edge : edges) {
            edge.resetBoja();
        }
    }

    public List<Vertex<Oznaka>> getVertices() {
        return vertices;
    }

    public void setVertices(List<Vertex<Oznaka>> vertices) {
        this.vertices = vertices;
    }

    public List<Edge<Oznaka>> getEdges() {
        return edges;
    }

    public void setEdges(List<Edge<Oznaka>> edges) {
        this.edges = edges;
    }

    public int postojiLiGrana(Vertex<Oznaka> polazni, Vertex<Oznaka> dolazni){
        for(int i = 0; i < edges.size(); i++){
            if(edges.get(i).equals(new Edge<>(polazni,dolazni)))return i;
        }
        return -1;
    }

    public Vertex<Oznaka> dajCvor(Vertex<Oznaka> pom){
        for (Vertex<Oznaka> vertex : vertices) {
            if (pom.equals(vertex)) return vertex;
        }
        return null;
    }
    public List<Edge<Oznaka>> getEdgesWithStartVertex(Vertex<Oznaka> poc){
        return edges.stream().filter(e->e.getPolazni().equals(poc)).collect(Collectors.toList());
    }
}
