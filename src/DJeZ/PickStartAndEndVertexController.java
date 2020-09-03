package DJeZ;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;

import java.util.List;

public class PickStartAndEndVertexController {
    private ObservableList<Vertex<String>> izbor;
    public ChoiceBox start;
    public ChoiceBox end;

    PickStartAndEndVertexController(List<Vertex<String>> lista){
        izbor = FXCollections.observableArrayList(lista);
    }

    @FXML
    public void initialize(){
        start.setItems(izbor);
        start.setValue(izbor.get(0));
        end.setItems(izbor);
        end.setValue(izbor.get(0));
    }

    public void okAction(ActionEvent actionEvent) {
        Node n = (Node) actionEvent.getSource();
        Stage stage = (Stage) n.getScene().getWindow();
        stage.close();
    }

    public void cancelAction(ActionEvent actionEvent) {
        start.setValue(null);
        end.setValue(null);
        Node n = (Node) actionEvent.getSource();
        Stage stage = (Stage) n.getScene().getWindow();
        stage.close();
    }

    public Vertex dajStart(){
        return (Vertex) start.getValue();
    }
    public Vertex dajEnd(){return (Vertex)start.getValue();}
}
