package DJeZ;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Transform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class GraphController {
    private static final double ARR_SIZE = 10;
    private static Color DEFAULT_EDGE_ALGORITHM_COLOR = Color.RED;
    private static Color DEFAULT_VERTEX_COLOR = Color.BLUE;
    private static final double STROKE_WIDTH = 1.5;
    private static long WAIT = 2000;
    public Canvas platno;
    public CheckBox latex;
    Graph<String> graph;
    final int POLUPRECNIK_CVOR = 15;
    private static Semaphore semaphore = new Semaphore(1,true);

    @FXML
    public void initialize(){
        platno.getGraphicsContext2D().setLineWidth(STROKE_WIDTH);
        graph = new Graph<>();
    }

    public void reset(){
        graph.resetAllVertices();
        graph.resetAllEdges();
        while (semaphore.hasQueuedThreads()){
            semaphore.release();
        }
        azurirajSve();
    }

    public void newAction(){
        graph.getEdges().clear();
        graph.getVertices().clear();
        azurirajSve();
    }

    public void options(){
        Stage secondaryStage = new Stage();
        OptionsController oc = new OptionsController(WAIT);
        FXMLLoader loader;
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
        assert root != null;
        secondaryStage.setScene(new Scene(root, 650, 275));
        secondaryStage.show();
        secondaryStage.setOnHiding(event -> Platform.runLater(() -> WAIT = oc.dajBrzinu()));
    }

    public void addVertex(){
        Stage secondaryStage = new Stage();
        AddVertexController cc = new AddVertexController();
        FXMLLoader loader;
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
        assert root != null;
        secondaryStage.setScene(new Scene(root, 650, 275));
        secondaryStage.show();
        secondaryStage.setOnHiding(event -> Platform.runLater(() -> {
            String s = cc.dajOznaku();
            if (s.equals("")) return;

            if (!graph.addVertex(s)) return;
            platno.getGraphicsContext2D().clearRect(0, 0, platno.getWidth(), platno.getHeight());
            azurirajSve();
        }));
    }

    public void addEdge(){
        if(graph.getVertices().size()<2){
            System.out.println("Broj cvorova mora biti veci od 1!");
            return;
        }
        Stage secondaryStage = new Stage();
        AddEdgeController<String> gc = new AddEdgeController<>(graph.getVertices());
        FXMLLoader loader;
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
        assert root != null;
        secondaryStage.setScene(new Scene(root, 650, 275));
        secondaryStage.show();
        secondaryStage.setOnHiding(event -> Platform.runLater(() -> {
            String rezultat = gc.vratiPodatke();
            String[] niz = rezultat.split(",");
            if (niz[2].equals("")) return;
            int redniBroj = graph.postojiLiGrana(new Vertex<>(niz[0]), new Vertex<>(niz[1]));
            if (redniBroj != -1) {
                graph.getEdges().get(redniBroj).setTezina(Integer.parseInt(niz[2]));
                return;
            }
            Vertex<String> pom = graph.dajCvor(new Vertex<>(niz[0]));
            Vertex<String> pom2 = graph.dajCvor(new Vertex<>(niz[1]));
            Edge<String> ubaci = new Edge<>(pom, pom2, Double.parseDouble(niz[2]));
            graph.getEdges().add(ubaci);
            //AKO JE TEZINA 0 PRETPOSTAVLJA SE DA JE NEUSMJERENI
            if (Double.parseDouble(niz[2]) == 0) {
                graph.getEdges().add(new Edge<>(pom2, pom, 0));
            }
            if (Integer.parseInt(niz[2]) != 0) {
                connectVerticesByArrow(platno.getGraphicsContext2D(), ubaci, niz[2]);
            } else {
                connectVerticesByLine(platno.getGraphicsContext2D(), ubaci);
            }

        }));
    }

    private void connectVerticesByArrow(GraphicsContext gc, Edge<String> e, String tezina) {
        gc.setFill(e.getColor());
        gc.setStroke(e.getColor());
        drawArrow(gc, e.getPolazni().getX() + POLUPRECNIK_CVOR, e.getPolazni().getY() + POLUPRECNIK_CVOR, e.getDolazni().getX() + POLUPRECNIK_CVOR, e.getDolazni().getY() + POLUPRECNIK_CVOR, tezina);
    }

    private void connectVerticesByLine(GraphicsContext gc, Edge<String> e) {
        gc.setFill(e.getColor());
        gc.setStroke(e.getColor());
        drawLine(gc,e.getPolazni().getX()+POLUPRECNIK_CVOR,e.getPolazni().getY()+POLUPRECNIK_CVOR,e.getDolazni().getX()+POLUPRECNIK_CVOR,e.getDolazni().getY()+POLUPRECNIK_CVOR);
    }

    private void drawLine(GraphicsContext gc, double x1, double y1, double x2, double y2){
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

    private void drawArrow(GraphicsContext gc, double x1, double y1, double x2, double y2,String tezina) {
            Affine prije = gc.getTransform();
            double dx = x2 - x1, dy = y2 - y1;
            double miny,maxy;
            miny = Math.min(y1, y2);
            maxy = Math.max(y1, y2);
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

    private void azurirajSve(){
        platno.getGraphicsContext2D().clearRect(0,0,platno.getWidth(),platno.getHeight());
        double angle = 360f/ graph.getVertices().size();
        for(int i = 0; i < graph.getVertices().size(); i++){
            double v1 = 345 * Math.cos(Math.toRadians(angle*i)) + 375;
            double v2 = 345 * Math.sin(Math.toRadians(angle*i)) + 375;
            graph.getVertices().get(i).setX(v1);
            graph.getVertices().get(i).setY(v2);
            platno.getGraphicsContext2D().setFill(DEFAULT_VERTEX_COLOR);
            platno.getGraphicsContext2D().fillOval(v1, v2,POLUPRECNIK_CVOR*2,POLUPRECNIK_CVOR*2);
            platno.getGraphicsContext2D().setFill(Color.RED);
            platno.getGraphicsContext2D().fillText(graph.getVertices().get(i).getOznaka(), v1, v2);
        }
        //DRAWS FIRST BLACK EDGES
        for(int i = 0; i< graph.getEdges().size(); i++){
            Edge<String> e = graph.getEdges().get(i);
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

    public void saveAction(){
        FileChooser fc = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("graph file(*.graf)","*.graf");
        fc.getExtensionFilters().add(extFilter);
        File f = fc.showSaveDialog(new Stage());
        StringBuilder zapisi = new StringBuilder();
        for(int i = 0; i < graph.getVertices().size(); i++){
            Vertex<String> v = graph.getVertices().get(i);
            zapisi.append(v).append(";").append(v.getX()).append(";").append(v.getY()).append(";").append(v.getColor().toString(), 2, v.getColor().toString().length() - 2);
            zapisi.append(",");
        }
        zapisi = new StringBuilder(zapisi.substring(0, zapisi.length() - 1));
        zapisi.append("\n");
        for(int i = 0; i < graph.getEdges().size(); i++){
            Edge<String> e = graph.getEdges().get(i);
            zapisi.append(e).append(";").append(e.getTezina()).append(";").append(e.getColor().toString(), 2, e.getColor().toString().length() - 2);
            zapisi.append(",");
        }
        zapisi = new StringBuilder(zapisi.substring(0, zapisi.length() - 1));
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(f.getAbsoluteFile());
            pw.print(zapisi);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        assert pw != null;
        pw.close();
    }

    public void openAction(){
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
            for (String s : niz1) {
                String[] niz2 = s.split(";");
                Vertex<String> v = new Vertex<>(niz2[0], Double.parseDouble(niz2[1]), Double.parseDouble(niz2[2]));
                v.setColor(Color.web(niz2[3]));
                graph.getVertices().add(v);
            }
            niz1 = vxEg[1].split(",");
            for (String s : niz1) {
                String[] niz2 = s.split(";");
                String[] oznake = niz2[0].split("-");
                Edge<String> e = new Edge<>(graph.dajCvor(new Vertex<>(oznake[0])), graph.dajCvor(new Vertex<>(oznake[1])), Double.parseDouble(niz2[1]));
                e.setColor(Color.web(niz2[2]));
                graph.getEdges().add(e);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        azurirajSve();
    }

    private void threadDraw(Edge<String> e){
        new Thread(()->{
            try {
//                System.out.println(e + " zahtjeva semafor");
                semaphore.acquire();
//                System.out.println(e + " dobija semafor");
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

    private void drawEdgeWithColor(Edge<String> e, Color color){
        e.setColor(color);
        if(e.getTezina()==0){
            connectVerticesByLine(platno.getGraphicsContext2D(),e);
        }
        else{
            connectVerticesByArrow(platno.getGraphicsContext2D(),e,Double.toString(e.getTezina()));
        }
    }

    public void bfs(){
        if(graph.getVertices().size()==0)return;
        reset();
        Stage secondaryStage = new Stage();
        StartVertexController svc = new StartVertexController(graph.getVertices());
        FXMLLoader loader;
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
        assert root != null;
        secondaryStage.setScene(new Scene(root, 650, 275));
        secondaryStage.show();
        secondaryStage.setOnHiding(event -> Platform.runLater(() -> {
            azurirajSve();
            new Thread(()-> {
                //THE ACTUAL BFS ALGORITHM BEGINS HERE
                Vertex<String> pocetak = svc.dajStart();
                StringBuilder zapisi= new StringBuilder("\\documentclass[12pt, a4paper]{report}\n" + "\n" +
                        "\\begin{document} BFS oblilazak grafa iz čvora " + pocetak.getOznaka() + ": \\begin{center} ");
                HashMap<Vertex<String>, Boolean> bio = new HashMap<>();

                for (int i = 0; i < graph.getVertices().size(); i++) {
                    bio.put(graph.getVertices().get(i), false);
                }
                bio.put(pocetak, true);
                Queue<Vertex<String>> cekanje = new LinkedList<>();
                cekanje.add(pocetak);
                while (!cekanje.isEmpty()) {
                    Vertex<String> trenutni = cekanje.remove();
                    for (int i = 0; i < graph.getEdges().size(); i++) {
                        Edge<String> e = graph.getEdges().get(i);
                        Vertex<String> ide = e.getPolazni();
                        Vertex<String> ideU = e.getDolazni();
                        if (trenutni.equals(ide) && !bio.get(ideU)) {
                            bio.put(ideU, true);
                            cekanje.add(ideU);
                            zapisi.append(e).append(",");
                            try {
                                Thread.sleep(WAIT);
                            } catch (InterruptedException interruptedException) {
                                interruptedException.printStackTrace();
                            }
                            System.out.println("Crtam "+e);
                            threadDraw(e);
                        }
                    }
                }
                zapisi.setLength(zapisi.length()-1);
                zapisi.append("\\end{center} \\end{document}");
                if(latex.isSelected()) {
                    File f = new File("output/bfsD/bfs.tex");
                    PrintWriter pw = null;
                    try {
                        pw = new PrintWriter(f.getPath());
                        pw.print(zapisi);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    assert pw != null;
                    pw.close();
                }
                graph.resetAllEdges();
                graph.resetAllVertices();
            }).start();

        }));
    }

    public void dfs(){
        if(graph.getVertices().size()==0)return;
        reset();
        Stage secondaryStage = new Stage();
        StartVertexController svc = new StartVertexController(graph.getVertices());
        FXMLLoader loader;
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
        assert root != null;
        secondaryStage.setScene(new Scene(root, 650, 275));
        secondaryStage.show();
        secondaryStage.setOnHiding(event -> Platform.runLater(() -> {
            azurirajSve();
            new Thread(()->{
                Vertex<String> pocetak = svc.dajStart();
                StringBuilder zapisi= new StringBuilder("\\documentclass[12pt, a4paper]{report}\n" + "\n" +
                        "\\begin{document} DFS oblilazak grafa iz čvora " + pocetak.getOznaka() + ": \\begin{center} ");
                List<Vertex<String>> vertices = graph.getVertices();
                HashMap<Vertex<String>, Boolean> bio = new HashMap<>();

                for (int i = 0; i < graph.getVertices().size(); i++) {
                    bio.put(vertices.get(i), false);
                }
                Stack<Vertex<String>> posjetiti = new Stack<>();
                bio.put(pocetak, true);
                do {
                    int j;
                    for (j = 0; j < graph.getEdges().size(); j++) {
                        Edge<String> e = graph.getEdges().get(j);
                        Vertex<String> pol = e.getPolazni();
                        Vertex<String> dol = e.getDolazni();
                        if (pocetak.equals(pol) && !bio.get(dol)) {
                            bio.put(dol, true);
                            posjetiti.push(dol);
                            zapisi.append(e).append(",");
                            try {
                                Thread.sleep(WAIT);
                            } catch (InterruptedException interruptedException) {
                                interruptedException.printStackTrace();
                            }
                            System.out.println("Crtam "+e);
                            threadDraw(e);
                            break;
                        }
                    }
                    if (j == graph.getEdges().size()) posjetiti.pop();
                    if (posjetiti.empty()) break;
                    pocetak = posjetiti.peek();
                } while (!posjetiti.empty());
                zapisi.setLength(zapisi.length()-1);
                zapisi.append("\\end{center} \\end{document}");
                if(latex.isSelected()) {
                    File f = new File("output/DfsD/dfs.tex");
                    PrintWriter pw = null;
                    try {
                        pw = new PrintWriter(f.getPath());
                        pw.print(zapisi);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    assert pw != null;
                    pw.close();
                }
                graph.resetAllEdges();
                graph.resetAllVertices();
            }).start();
        }));
    }
    public void dijikstra(){
        if(graph.getVertices().size()==0)return;
        if(graph.getEdges().stream().anyMatch(e -> e.getTezina() < 0)){
            System.out.println("ZA DIJIKSTRIN ALGORITAM TEZINE MORAJU BITI POZITIVNE");
            return;
        }
        Stage secondaryStage = new Stage();
        StartVertexController svc = new StartVertexController(graph.getVertices());
        FXMLLoader loader;
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
        assert root != null;
        secondaryStage.setScene(new Scene(root, 650, 275));
        secondaryStage.show();
        secondaryStage.setOnHiding(event -> Platform.runLater(() -> {
            azurirajSve();
            Vertex<String> pocetak = svc.dajStart();
            HashMap<Vertex<String>, Double> udaljenost= new HashMap<>();
            HashMap<Vertex<String>, Boolean> bio = new HashMap<>();

            for(Vertex<String> v: graph.getVertices()){
                udaljenost.put(v,Double.POSITIVE_INFINITY);
                bio.put(v,false);
            }
            bio.put(pocetak,true);
            udaljenost.put(pocetak,0.);
            for(int i = 0; i < graph.getVertices().size()-1; i++){
                List<Edge<String>> udalj = graph.getEdgesWithStartVertex(pocetak);

                for(Edge<String> e: udalj){
                    if(!bio.get(e.getDolazni()) && udaljenost.get(e.getDolazni())>e.getTezina()){
                        udaljenost.put(e.getDolazni(),e.getTezina());
                    }
                }
                double min = Double.POSITIVE_INFINITY;
                Vertex<String> minimalni = graph.getVertices().get(0);
                Edge<String> e = graph.getEdges().get(0);
                for(Vertex<String> v: graph.getVertices()){
                    if(!bio.get(v)){
                        if(min > udaljenost.get(v)){
                            min = udaljenost.get(v);
                            minimalni = v;
                            
                        }
                    }
                }
                bio.put(minimalni,true);
                pocetak = minimalni;
            }

        }));
        graph.resetAllEdges();
        graph.resetAllVertices();
    }
}
