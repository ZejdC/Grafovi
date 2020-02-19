package DJeZ;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddVertexController {
    public TextField tekst;

    public AddVertexController() {
    }

    public void okAction(ActionEvent actionEvent) {
        Node n = (Node) actionEvent.getSource();
        Stage stage = (Stage) n.getScene().getWindow();
        stage.close();
    }

    public void cancelAction(ActionEvent actionEvent) {
        tekst.clear();
        Node n = (Node) actionEvent.getSource();
        Stage stage = (Stage) n.getScene().getWindow();
        stage.close();
    }
    public String dajOznaku(){
        return tekst.getText();
    }
}
