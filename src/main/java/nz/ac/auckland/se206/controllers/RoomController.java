package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import nz.ac.auckland.se206.GameStateContext;

/**
 * Controller class for the room view. Handles user interactions within the room where the user can
 * chat with customers and guess their profession.
 */
public class RoomController {

  @FXML private Rectangle rectPerson1;
  @FXML private Rectangle rectPerson2;
  @FXML private Rectangle rectPerson3;
  @FXML private Rectangle rectStatue;
  @FXML private Text timer;

  private static boolean isFirstTimeInit = true;
  private static GameStateContext context = new GameStateContext();
  private int milliSecond;
  private int second;
  private int minute;
  private Timeline clock;
  private String timerString;

  /**
   * Initializes the room view. If it's the first time initialization, it will provide instructions
   * via text-to-speech.
   */
  @FXML
  public void initialize() {

    milliSecond = 0;
    second = 0;
    minute = 0;
    timerString = "%d:%02d:%02d";
    timer.setText(
        Integer.toString(minute)
            + ":"
            + Integer.toString(second)
            + ":"
            + Integer.toString(milliSecond));
    clock =
        new Timeline(
            new KeyFrame(
                Duration.millis(10),
                e -> {
                  milliSecond++;
                  if (milliSecond == 100) {
                    milliSecond = 0;
                    second++;
                    if (second == 60) {
                      second = 0;
                      minute++;
                    }
                  }
                  timer.setText(String.format(timerString, minute, second, milliSecond));
                }));
    clock.setCycleCount(Animation.INDEFINITE);
    clock.play();
  }

  /**
   * Handles the key pressed event.
   *
   * @param event the key event
   */
  @FXML
  public void onKeyPressed(KeyEvent event) {
    System.out.println("Key " + event.getCode() + " pressed");
  }

  /**
   * Handles the key released event.
   *
   * @param event the key event
   */
  @FXML
  public void onKeyReleased(KeyEvent event) {
    System.out.println("Key " + event.getCode() + " released");
  }

  /**
   * Handles mouse clicks on rectangles representing people in the room.
   *
   * @param event the mouse event triggered by clicking a rectangle
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void handleRectangleClickPerson1(MouseEvent event) throws IOException {
    Rectangle clickedRectangle = (Rectangle) event.getSource();
  }

  @FXML
  private void handleRectangleClickPerson2(MouseEvent event) throws IOException {
    Rectangle clickedRectangle = (Rectangle) event.getSource();
  }

  @FXML
  private void handleRectangleClickPerson3(MouseEvent event) throws IOException {
    Rectangle clickedRectangle = (Rectangle) event.getSource();
  }

  @FXML
  private void handleRectangleClickStatue(MouseEvent event) throws IOException {
    Rectangle clickedRectangle = (Rectangle) event.getSource();
  }

  /**
   * Handles the guess button click event.
   *
   * @param event the action event triggered by clicking the guess button
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void handleGuessClick(ActionEvent event) throws IOException {
    context.handleGuessClick();
  }
}
