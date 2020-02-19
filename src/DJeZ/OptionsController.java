package DJeZ;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class OptionsController {
    long brzina;
    public TextField speed;

    public OptionsController(long wait) {
        brzina = wait;
    }

    @FXML
    public void initialize(){
        speed.setText(Long.toString(brzina));
    }

    public void okAction(ActionEvent actionEvent){
        Node n = (Node) actionEvent.getSource();
        Stage stage = (Stage) n.getScene().getWindow();
        stage.close();
    }

    public void cancelAction(ActionEvent actionEvent){
        speed.setText(String.valueOf(-1));
        Node n = (Node) actionEvent.getSource();
        Stage stage = (Stage) n.getScene().getWindow();
        stage.close();
    }

    public long dajBrzinu(){
        return Long.parseLong(speed.getText());
    }
}
