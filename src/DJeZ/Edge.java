package DJeZ;

import javafx.scene.paint.Color;

import java.util.Objects;

public class Edge<Oznaka> {
    private Vertex<Oznaka> polazni;
    private Vertex<Oznaka> dolazni;
    Color color;
    private double tezina;

    public Edge(Vertex<Oznaka> polazni, Vertex<Oznaka> dolazni, double tezina) {
        this.polazni = polazni;
        this.dolazni = dolazni;
        this.tezina = tezina;
        color = Color.BLACK;
    }

    public Edge(Oznaka polazni, Oznaka dolazni) {
        this.polazni = new Vertex(polazni);
        this.dolazni = new Vertex(dolazni);
        tezina = 0;
        color = Color.BLACK;
    }

    public void resetBoja(){
        color = Color.BLACK;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Vertex<Oznaka> getPolazni() {
        return polazni;
    }

    public void setPolazni(Vertex<Oznaka> polazni) {
        this.polazni = polazni;
    }

    public Vertex<Oznaka> getDolazni() {
        return dolazni;
    }

    public void setDolazni(Vertex<Oznaka> dolazni) {
        this.dolazni = dolazni;
    }

    public double getTezina() {
        return tezina;
    }

    public void setTezina(double tezina) {
        this.tezina = tezina;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge<?> edge = (Edge<?>) o;
        return Objects.equals(getPolazni(), edge.getPolazni()) &&
                Objects.equals(getDolazni(), edge.getDolazni());
    }

    @Override
    public String toString() {
        return polazni.toString()+"-"+dolazni.toString();
    }
}
