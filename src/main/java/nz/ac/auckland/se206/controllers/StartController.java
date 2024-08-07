package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;

public class StartController {
  @FXML
  private void startGame(ActionEvent event) throws ApiProxyException, IOException {
    App.setRoot("room");
  }
}
