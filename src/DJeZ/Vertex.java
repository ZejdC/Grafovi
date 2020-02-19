package DJeZ;

import javafx.scene.paint.Color;

public class Vertex<Oznaka> {
    private Oznaka oznaka;
    private Color color;
    private double x,y;

    public Vertex(Oznaka oznaka, double x, double y) {
        this.oznaka = oznaka;
        this.x = x;
        this.y = y;
        color = Color.BLUE;
    }

    public Vertex(Oznaka oznaka) {
        this.oznaka = oznaka;
        color = Color.BLUE;
    }

    public void resetBoja(){
        color = Color.BLUE;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Oznaka getOznaka() {
        return oznaka;
    }

    public void setOznaka(Oznaka oznaka) {
        this.oznaka = oznaka;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null)return false;
        return oznaka.toString().equals(((Vertex<Oznaka>) o).getOznaka().toString());
    }

    @Override
    public String toString() {
        return oznaka.toString();
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }
}
