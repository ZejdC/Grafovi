package DJeZ;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Transform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.Semaphore;

public class GraphController {
    private static final double ARR_SIZE = 10;
    private static Color DEFAULT_EDGE_ALGORITHM_COLOR = Color.RED;
    private static Color DEFAULT_VERTEX_COLOR = Color.BLUE;
    private static final double STROKE_WIDTH = 1.5;
    private static long WAIT = 2000;
    public Canvas platno;
    Graph<String> graph;
    final int POLUPRECNIK_CVOR = 15;
    private static Semaphore semaphore = new Semaphore(1,true);

    @FXML
    public void initialize(){
        platno.getGraphicsContext2D().setLineWidth(STROKE_WIDTH);
        graph = new Graph<>();
    }

    public void addVertex(){
        Stage secondaryStage = new Stage();
        AddVertexController cc = new AddVertexController();
        FXMLLoader loader=null;
        Parent root = null;
        try {
            ResourceBundle rb = ResourceBundle.getBundle("Translation");
            loader = new FXMLLoader(getClass().getResource("/fxml/AddVertex.fxml"),rb);
            loader.setController(cc);
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        secondaryStage.setTitle("Add vertex");
        secondaryStage.setScene(new Scene(root, 650, 275));
        secondaryStage.show();
        secondaryStage.setOnHiding(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        String s = cc.dajOznaku();
                        if(s.equals(""))return;

                        if(!graph.addVertex(s))return;
                        platno.getGraphicsContext2D().clearRect(0,0,platno.getWidth(),platno.getHeight());
                        azurirajSve();
                    }
                });
            }
        });
    }

    public void addEdge(ActionEvent actionEvent){
        if(graph.getVertices().size()<2){
            System.out.println("Broj cvorova mora biti veci od 1!");
            return;
        }
        Stage secondaryStage = new Stage();
        AddEdgeController<String> gc = new AddEdgeController<>(graph.getVertices());
        FXMLLoader loader=null;
        Parent root = null;
        try {
            ResourceBundle rb = ResourceBundle.getBundle("Translation");
            loader = new FXMLLoader(getClass().getResource("/fxml/AddEdge.fxml"),rb);
            loader.setController(gc);
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        secondaryStage.setTitle("Add edge");
        secondaryStage.setScene(new Scene(root, 650, 275));
        secondaryStage.show();
        secondaryStage.setOnHiding(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        String rezultat = gc.vratiPodatke();
                        String[] niz = rezultat.split(",");
                        if(niz[2].equals(""))return;
                        int redniBroj = graph.postojiLiGrana(new Vertex(niz[0]),new Vertex(niz[1]));
                        if(redniBroj != -1){
                            graph.getEdges().get(redniBroj).setTezina(Integer.parseInt(niz[2]));
                            return;
                        }
                        Vertex<String> pom = graph.dajCvor(new Vertex(niz[0]));
                        Vertex<String> pom2 = graph.dajCvor(new Vertex(niz[1]));
                        Edge ubaci = new Edge(pom,pom2,Double.parseDouble(niz[2]));
                        graph.getEdges().add(ubaci);
                        //AKO JE TEZINA 0 PRETPOSTAVLJA SE DA JE NEUSMJERENI
                        if(Double.parseDouble(niz[2])==0){
                            graph.getEdges().add(new Edge(pom2,pom,0));
                        }
                        if(Integer.parseInt(niz[2])!=0) {
                            connectVerticesByArrow(platno.getGraphicsContext2D(), ubaci, niz[2]);
                        }
                        else{
                            connectVerticesByLine(platno.getGraphicsContext2D(), ubaci);
                        }

                    }
                });
            }
        });
    }

    private void connectVerticesByArrow(GraphicsContext gc, Edge e, String tezina) {
        gc.setFill(e.getColor());
        gc.setStroke(e.getColor());
        drawArrow(gc, e.getPolazni().getX() + POLUPRECNIK_CVOR, e.getPolazni().getY() + POLUPRECNIK_CVOR, e.getDolazni().getX() + POLUPRECNIK_CVOR, e.getDolazni().getY() + POLUPRECNIK_CVOR, tezina);
    }

    private void connectVerticesByLine(GraphicsContext gc, Edge e) {
        gc.setFill(e.getColor());
        gc.setStroke(e.getColor());
        drawLine(gc,e.getPolazni().getX()+POLUPRECNIK_CVOR,e.getPolazni().getY()+POLUPRECNIK_CVOR,e.getDolazni().getX()+POLUPRECNIK_CVOR,e.getDolazni().getY()+POLUPRECNIK_CVOR);
    }

    void drawLine(GraphicsContext gc, double x1, double y1, double x2, double y2){
        Affine prije = gc.getTransform();
        double dx = x2 - x1, dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        int len = (int) Math.sqrt(dx * dx + dy * dy);
        Transform transform = Transform.translate(x1, y1);
        transform = transform.createConcatenation(Transform.rotate(Math.toDegrees(angle), 0, 0));
        Transform finalTransform = transform;
        gc.setTransform(new Affine(finalTransform));
        gc.strokeLine(0, 0, len, 0);
        gc.setTransform(prije);
    }

    void drawArrow(GraphicsContext gc, double x1, double y1, double x2, double y2,String tezina) {
            Affine prije = gc.getTransform();
            double dx = x2 - x1, dy = y2 - y1;
            double minx,miny,maxy;
            miny = (y1<y2)?y1:y2;
            maxy = (y1<y2)?y2:y1;
            double angle = Math.atan2(dy, dx);
            int len = (int) Math.sqrt(dx * dx + dy * dy);
            Transform transform = Transform.translate(x1, y1);
            transform = transform.createConcatenation(Transform.rotate(Math.toDegrees(angle), 0, 0));
            gc.setTransform(new Affine(transform));
            gc.strokeLine(0, 0, len, 0);
            gc.fillPolygon(new double[]{len, len - ARR_SIZE, len - ARR_SIZE, len}, new double[]{0, -ARR_SIZE, ARR_SIZE, 0},4);
            gc.setTransform(prije);
            gc.strokeText(tezina,(x1+x2)/2.,miny+(maxy-miny)/2+10);
    }

    public void azurirajSve(){
        platno.getGraphicsContext2D().clearRect(0,0,platno.getWidth(),platno.getHeight());
        double angle = 360f/ graph.getVertices().size();
        for(int i = 0; i < graph.getVertices().size(); i++){
            double v1 = 345 * Math.cos(Math.toRadians(angle*i)) + 375;
            double v2 = 345 * Math.sin(Math.toRadians(angle*i)) + 375;
            graph.getVertices().get(i).setX(v1);
            graph.getVertices().get(i).setY(v2);
            platno.getGraphicsContext2D().setFill(graph.getVertices().get(i).getColor());
            platno.getGraphicsContext2D().fillOval(v1, v2,POLUPRECNIK_CVOR*2,POLUPRECNIK_CVOR*2);
            platno.getGraphicsContext2D().setFill(Color.RED);
            platno.getGraphicsContext2D().fillText(graph.getVertices().get(i).getOznaka(), v1, v2);
        }
        //DRAWS FIRST BLACK EDGES
        for(int i = 0; i< graph.getEdges().size(); i++){
            Edge e = graph.getEdges().get(i);
            Vertex<String> pom = e.getPolazni();
            Vertex<String> pom2 = e.getDolazni();
            if(e.getTezina()!=0) {
                platno.getGraphicsContext2D().setFill(e.getColor());
                platno.getGraphicsContext2D().setStroke(e.getColor());
                drawArrow(platno.getGraphicsContext2D(), pom.getX() + POLUPRECNIK_CVOR, pom.getY() + POLUPRECNIK_CVOR, pom2.getX() + POLUPRECNIK_CVOR, pom2.getY() + POLUPRECNIK_CVOR, Double.toString(graph.getEdges().get(i).getTezina()));
            }
            else{
                platno.getGraphicsContext2D().setFill(e.getColor());
                platno.getGraphicsContext2D().setStroke(e.getColor());
                drawLine(platno.getGraphicsContext2D(), pom.getX() + POLUPRECNIK_CVOR, pom.getY() + POLUPRECNIK_CVOR, pom2.getX() + POLUPRECNIK_CVOR, pom2.getY() + POLUPRECNIK_CVOR);
            }
        }
//        //AND THEN THE OTHER COLORS
//        for(int i = 0; i< graph.getEdges().size(); i++){
//            Edge e = graph.getEdges().get(i);
//            Vertex<String> pom = e.getPolazni();
//            Vertex<String> pom2 = e.getDolazni();
//            if(e.getTezina()!=0 && !e.getColor().equals(Color.BLACK)) {
//                platno.getGraphicsContext2D().setFill(e.getColor());
//                platno.getGraphicsContext2D().setStroke(e.getColor());
//                drawArrow(platno.getGraphicsContext2D(), pom.getX() + POLUPRECNIK_CVOR, pom.getY() + POLUPRECNIK_CVOR, pom2.getX() + POLUPRECNIK_CVOR, pom2.getY() + POLUPRECNIK_CVOR, Double.toString(graph.getEdges().get(i).getTezina()));
//            }
//            else if(!e.getColor().equals(Color.BLACK)){
//                platno.getGraphicsContext2D().setFill(e.getColor());
//                platno.getGraphicsContext2D().setStroke(e.getColor());
//                drawLine(platno.getGraphicsContext2D(), pom.getX() + POLUPRECNIK_CVOR, pom.getY() + POLUPRECNIK_CVOR, pom2.getX() + POLUPRECNIK_CVOR, pom2.getY() + POLUPRECNIK_CVOR);
//            }
//        }
    }

    public void saveAction(ActionEvent actionEvent){
        FileChooser fc = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("graph file(*.graf)","*.graf");
        fc.getExtensionFilters().add(extFilter);
        File f = fc.showSaveDialog(new Stage());
        String zapisi = "";
        for(int i = 0; i < graph.getVertices().size(); i++){
            Vertex v = graph.getVertices().get(i);
            zapisi += v+";"+ v.getX()+";"+v.getY()+";"+v.getColor().toString().substring(2,v.getColor().toString().length()-2);
            zapisi += ",";
        }
        zapisi = zapisi.substring(0,zapisi.length()-1);
        zapisi+="\n";
        for(int i = 0; i < graph.getEdges().size(); i++){
            Edge e = graph.getEdges().get(i);
            zapisi += e +";"+ e.getTezina()+";"+e.getColor().toString().substring(2,e.getColor().toString().length()-2);
            zapisi += ",";
        }
        zapisi = zapisi.substring(0,zapisi.length()-1);
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(f.getAbsoluteFile());
            pw.print(zapisi);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        pw.close();
    }

    public void openAction(ActionEvent actionEvent){
        graph.getEdges().clear();
        graph.getVertices().clear();
        FileChooser fc = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("graph file(*.graf)","*.graf");
        fc.getExtensionFilters().add(extFilter);
        File f = fc.showOpenDialog(new Stage());

        try {
            String content = Files.readString(f.toPath());
            String[] vxEg = content.split("\n");
            String[] niz1 = vxEg[0].split(",");
            for(int i = 0; i < niz1.length; i++){
                String[] niz2 = niz1[i].split(";");
                Vertex v = new Vertex(niz2[0],Double.parseDouble(niz2[1]),Double.parseDouble(niz2[2]));
                v.setColor(Color.web(niz2[3]));
                graph.getVertices().add(v);
            }
            niz1 = vxEg[1].split(",");
            for(int i = 0; i < niz1.length; i++){
                String[]niz2 = niz1[i].split(";");
                String[]oznake = niz2[0].split("-");
                Edge e = new Edge(graph.dajCvor(new Vertex(oznake[0])), graph.dajCvor(new Vertex(oznake[1])),Double.parseDouble(niz2[1]));
                e.setColor(Color.web(niz2[2]));
                graph.getEdges().add(e);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        azurirajSve();
    }
    public void bfs(ActionEvent actionEvent){
        Stage secondaryStage = new Stage();
        StartVertexController svc = new StartVertexController(graph.getVertices());
        FXMLLoader loader=null;
        Parent root = null;
        try {
            ResourceBundle rb = ResourceBundle.getBundle("Translation");
            loader = new FXMLLoader(getClass().getResource("/fxml/PickStartVertex.fxml"),rb);
            loader.setController(svc);
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        secondaryStage.setTitle("Pick start vertex");
        secondaryStage.setScene(new Scene(root, 650, 275));
        secondaryStage.show();
        secondaryStage.setOnHiding(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        //THE ACTUAL BFS ALGORITHM BEGINS HERE
                        Vertex pocetak = svc.dajStart();
                        HashMap<Vertex, Boolean> bio= new HashMap<>();

                        for(int i = 0; i < graph.getVertices().size(); i++){
                            bio.put(graph.getVertices().get(i),false);
                        }
                        bio.put(pocetak,true);
                        Queue<Vertex> cekanje = new LinkedList<>();
                        cekanje.add(pocetak);
                        while(!cekanje.isEmpty()){
                            Vertex trenutni = cekanje.remove();
                            for(int i = 0; i < graph.getEdges().size(); i++){
                                Edge e = graph.getEdges().get(i);
                                Vertex ide = e.getPolazni();
                                Vertex ideU = e.getDolazni();
                                if(trenutni.equals(ide) && !bio.get(ideU)){
                                    bio.put(ideU,true);
                                    cekanje.add(ideU);
                                    //System.out.println("Crtam "+e);
                                    threadDraw(e);
                                }
                            }
                        }
                        graph.resetAllEdges();
                        graph.resetAllVertices();
                    }
                });
            }
        });
    }

    private void threadDraw(Edge e){
        new Thread(()->{
            try {
//                System.out.println(e + " zahtjeva semafor");
                semaphore.acquire();
//                System.out.println(e + " dobija semafor");
                Thread.sleep(WAIT);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            Platform.runLater(()->{
//              System.out.println("Crtam "+e);
                drawEdgeWithColor(e,DEFAULT_EDGE_ALGORITHM_COLOR);
                semaphore.release();
            });
        }).start();
    }

    public void drawEdgeWithColor(Edge e, Color color){
        e.setColor(color);
        if(e.getTezina()==0){
            connectVerticesByLine(platno.getGraphicsContext2D(),e);
        }
        else{
            connectVerticesByArrow(platno.getGraphicsContext2D(),e,Double.toString(e.getTezina()));
        }
    }

    public void dfs(ActionEvent actionEvent){
        Stage secondaryStage = new Stage();
        StartVertexController svc = new StartVertexController(graph.getVertices());
        FXMLLoader loader=null;
        Parent root = null;
        try {
            ResourceBundle rb = ResourceBundle.getBundle("Translation");
            loader = new FXMLLoader(getClass().getResource("/fxml/PickStartVertex.fxml"),rb);
            loader.setController(svc);
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        secondaryStage.setTitle("Pick start vertex");
        secondaryStage.setScene(new Scene(root, 650, 275));
        secondaryStage.show();
        secondaryStage.setOnHiding(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        Vertex pocetak = svc.dajStart();
                        List<Vertex<String>> vertices= graph.getVertices();
                        List<Edge<String>> edges = graph.getEdges();
                        HashMap<Vertex, Boolean> bio= new HashMap<>();

                        for(int i = 0; i < graph.getVertices().size(); i++){
                            bio.put(vertices.get(i),false);
                        }
                        Stack<Vertex> posjetiti = new Stack<>();
                        bio.put(pocetak,true);
                        do {
                                int j;
                                for (j = 0; j < graph.getEdges().size(); j++) {
                                    Edge e = graph.getEdges().get(j);
                                    Vertex pol = e.getPolazni();
                                    Vertex dol = e.getDolazni();
                                    if (pocetak.equals(pol) && !bio.get(dol)) {
                                        bio.put(dol,true);
                                        posjetiti.push(dol);
                                        threadDraw(e);
                                        break;
                                    }
                                }
                                if(j==graph.getEdges().size())posjetiti.pop();
                                if(posjetiti.empty())break;
                                pocetak = posjetiti.peek();
                        } while (!posjetiti.empty());
                    }
                });
            }
        });
        graph.resetAllEdges();
        graph.resetAllVertices();
    }

    public void reset(ActionEvent actionEvent){
        graph.resetAllVertices();
        graph.resetAllEdges();
        azurirajSve();
    }

    public void newAction(ActionEvent actionEvent){
        graph.getEdges().clear();
        graph.getVertices().clear();
        azurirajSve();
    }

    public void options(ActionEvent actionEvent){
        Stage secondaryStage = new Stage();
        OptionsController oc = new OptionsController(WAIT);
        FXMLLoader loader=null;
        Parent root = null;
        try {
            ResourceBundle rb = ResourceBundle.getBundle("Translation");
            loader = new FXMLLoader(getClass().getResource("/fxml/Options.fxml"),rb);
            loader.setController(oc);
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        secondaryStage.setTitle("Options");
        secondaryStage.setScene(new Scene(root, 650, 275));
        secondaryStage.show();
        secondaryStage.setOnHiding(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        WAIT = oc.dajBrzinu();
                    }
                });
            }
        });
    }
}
