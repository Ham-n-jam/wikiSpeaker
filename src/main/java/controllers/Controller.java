package controllers;


import app.WikiSpeaker;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Controller {

    /**
     * Change scenes by providing the fxml filename
     * @param fxml the fxml filename
     */
    public void changeScene(String fxml) {

        try {
            //Change the fxml file
            Parent layout = FXMLLoader.load(getClass().getResource(fxml));

            //Change the scene
            Scene scene = new Scene(layout);
            Stage mainStage = WikiSpeaker.getMainStage();
            mainStage.setScene(scene);
            mainStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
