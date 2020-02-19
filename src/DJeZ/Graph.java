package DJeZ;

import java.util.ArrayList;
import java.util.List;

public class Graph<Oznaka> {
    List<Vertex<Oznaka>> vertices;
    List<Edge<Oznaka>> edges;

    Graph(){
        vertices = new ArrayList<>();
        edges = new ArrayList<>();
    }

    public boolean addVertex(Oznaka o){
        if(vertices.contains(new Vertex<>(o)))return false;
        vertices.add(new Vertex<Oznaka>(o));
        return true;
    }
    public void resetAllVertices(){
        for(int i = 0; i < vertices.size(); i++){
            vertices.get(i).resetBoja();
        }
    }
    public void resetAllEdges(){
        for(int i = 0; i < edges.size(); i++){
            edges.get(i).resetBoja();
        }
    }
    public int getIndexOfVertex(Vertex v){
        for(int i = 0; i < vertices.size(); i++){
            if(v.equals(vertices.get(i)))return i;
        }
        return -1;
    }
    public Edge<Oznaka> getEdge(Vertex<Oznaka> pol, Vertex<Oznaka> dol){
        for(Edge<Oznaka> e: edges){
            if(e.equals(new Edge<Oznaka>(pol,dol,0))){
                return e;
            }
        }
        return null;
    }
    public void addEdge(Oznaka polazni, Oznaka dolazni){
        edges.add(new Edge(polazni,dolazni));
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
        for(int i = 0; i < vertices.size(); i++){
            if(pom.equals(vertices.get(i)))return vertices.get(i);
        }
        return null;
    }
}
