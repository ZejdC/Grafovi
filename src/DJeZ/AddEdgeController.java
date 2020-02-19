package DJeZ;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;

public class AddEdgeController<Oznaka> {
    public ChoiceBox cvor1;
    public ChoiceBox cvor2;
    public TextField tezina;
    private ObservableList<Vertex<Oznaka>> izbor;



    @FXML
    public void initialize(){
        cvor1.setItems(izbor);
        cvor2.setItems(izbor);
        cvor1.setValue(cvor1.getItems().get(0));
        cvor2.setValue(cvor2.getItems().get(1));
    }

    public AddEdgeController(List<Vertex<Oznaka>> lista){
        izbor = FXCollections.observableArrayList(lista);
    }

    public void okAction(ActionEvent actionEvent) {
        if(cvor1.getValue().equals(cvor2.getValue())){
            tezina.setText("Cvorovi moraju biti razliciti!");
            return;
        }
        try{
            Integer.parseInt(tezina.getText());
        }catch(Exception e1){
            tezina.setText("Tezina mora biti broj!");
            return;
        }

        Node n = (Node) actionEvent.getSource();
        Stage stage = (Stage) n.getScene().getWindow();
        stage.close();
    }

    public void cancelAction(ActionEvent actionEvent){
        tezina.clear();
        Node n = (Node) actionEvent.getSource();
        Stage stage = (Stage) n.getScene().getWindow();
        stage.close();
    }

    public String vratiPodatke(){
        return cvor1.getValue().toString()+","+cvor2.getValue().toString()+","+tezina.getText();
    }
}
