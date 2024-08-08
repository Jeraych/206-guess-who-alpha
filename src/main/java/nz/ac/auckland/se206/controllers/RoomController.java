package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionRequest;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionResult;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.chat.openai.Choice;
import nz.ac.auckland.apiproxy.config.ApiProxyConfig;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.GameStateContext;
import nz.ac.auckland.se206.prompts.PromptEngineering;
import nz.ac.auckland.se206.speech.TextToSpeech;

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
  @FXML private Text finalCountdown;

  @FXML private TextArea txtChat;
  @FXML private TextField txtInput;
  @FXML private Button btnSend;

  private static boolean isFirstTimeInit = true;
  private static GameStateContext context = new GameStateContext();
  private int milliSecond;
  private int second;
  private int minute;
  private Timeline clock;
  private String timerString;
  private Timeline countdown;
  private boolean timerEnd;
  private boolean countdownEnd;
  private int finalSecond;
  private boolean clueFound = false;

  private ChatCompletionRequest chatCompletionRequest;
  private String profession;

  /**
   * Initializes the room view. If it's the first time initialization, it will provide instructions
   * via text-to-speech.
   */
  @FXML
  public void initialize() {
    timerEnd = false;
    milliSecond = 0;
    second = 0;
    minute = 2;
    timerString = "%d:%02d:%02d";
    timer.setText(String.format(timerString, minute, second, milliSecond));
    clock =
        new Timeline(
            new KeyFrame(
                Duration.millis(10),
                e -> {
                  milliSecond--;
                  if (milliSecond < 0) {
                    milliSecond = 99;
                    second--;
                    if (second < 0) {
                      second = 59;
                      minute--;
                      if (minute < 0) {
                        milliSecond = 0;
                        second = 0;
                        minute = 0;
                        timerEnd = true;
                        clock.stop();

                        countdown.play();
                      }
                    }
                  }
                  timer.setText(String.format(timerString, minute, second, milliSecond));
                }));
    clock.setCycleCount(Animation.INDEFINITE);
    clock.play();

    countdownEnd = false;
    finalSecond = 10;

    countdown =
        new Timeline(
            new KeyFrame(
                Duration.seconds(1),
                event -> {
                  finalCountdown.setText(Integer.toString(finalSecond));
                  finalSecond--;
                  if (finalSecond < 0) {
                    finalSecond = 0;
                    countdownEnd = true;
                    countdown.stop();
                  }
                  finalCountdown.setText(Integer.toString(finalSecond));
                }));
    countdown.setCycleCount(Animation.INDEFINITE);

    Task<Void> timerCountdown =
        new Task<Void>() {

          @Override
          protected Void call() throws Exception {
            return null;
          }
        };
    Thread timerThread = new Thread(timerCountdown);
    timerThread.setDaemon(true);
    timerThread.start();
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
    clickedRectangle.setStyle("-fx-opacity: 0.2");
    rectPerson2.setStyle("-fx-opacity: 0");
    rectPerson3.setStyle("-fx-opacity: 0");
    rectStatue.setStyle("-fx-opacity: 0");
    txtChat.appendText(
        "A man is staring at the description card mindlessly. You decide to name him Suspect"
            + " 2.\n\n");
    if (clueFound) {
      txtChat.appendText("You notice Suspect 1 has got some white dust on his shirt.\n\n");
    }
  }

  @FXML
  private void handleRectangleClickPerson2(MouseEvent event) throws IOException {
    Rectangle clickedRectangle = (Rectangle) event.getSource();
    clickedRectangle.setStyle("-fx-opacity: 0.2");
    rectPerson1.setStyle("-fx-opacity: 0");
    rectPerson3.setStyle("-fx-opacity: 0");
    rectStatue.setStyle("-fx-opacity: 0");
    txtChat.appendText(
        "You notice a man wondering around looking for something. You noted him as Suspect 1\n\n");
  }

  @FXML
  private void handleRectangleClickPerson3(MouseEvent event) throws IOException {
    Rectangle clickedRectangle = (Rectangle) event.getSource();
    clickedRectangle.setStyle("-fx-opacity: 0.2");
    rectPerson2.setStyle("-fx-opacity: 0");
    rectPerson1.setStyle("-fx-opacity: 0");
    rectStatue.setStyle("-fx-opacity: 0");
    txtChat.appendText(
        "A beautiful woman works around the museum, she seems interested in those statues. You"
            + " called her Suspect 3 anyways\n\n");
  }

  @FXML
  private void handleRectangleClickStatue(MouseEvent event) throws IOException {
    Rectangle clickedRectangle = (Rectangle) event.getSource();
    clickedRectangle.setStyle("-fx-opacity: 0.2");
    rectPerson2.setStyle("-fx-opacity: 0");
    rectPerson3.setStyle("-fx-opacity: 0");
    rectPerson1.setStyle("-fx-opacity: 0");
    txtChat.appendText(
        new String(Files.readAllBytes(Paths.get("src\\main\\resources\\prompts\\chatClue.txt"))));
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

  /**
   * Generates the system prompt based on the profession.
   *
   * @return the system prompt string
   */
  private String getSystemPrompt() {
    Map<String, String> map = new HashMap<>();
    map.put("profession", profession);
    return PromptEngineering.getPrompt("chat.txt", map);
  }

  /**
   * Sets the profession for the chat context and initializes the ChatCompletionRequest.
   *
   * @param profession the profession to set
   */
  public void setProfession(String profession) {
    this.profession = profession;
    try {
      ApiProxyConfig config = ApiProxyConfig.readConfig();
      chatCompletionRequest =
          new ChatCompletionRequest(config)
              .setN(1)
              .setTemperature(0.2)
              .setTopP(0.5)
              .setMaxTokens(100);
      runGpt(new ChatMessage("system", getSystemPrompt()));
    } catch (ApiProxyException e) {
      e.printStackTrace();
    }
  }

  /**
   * Appends a chat message to the chat text area.
   *
   * @param msg the chat message to append
   */
  private void appendChatMessage(ChatMessage msg) {
    txtChat.appendText(msg.getRole() + ": " + msg.getContent() + "\n\n");
  }

  /**
   * Runs the GPT model with a given chat message.
   *
   * @param msg the chat message to process
   * @return the response chat message
   * @throws ApiProxyException if there is an error communicating with the API proxy
   */
  private ChatMessage runGpt(ChatMessage msg) throws ApiProxyException {
    chatCompletionRequest.addMessage(msg);
    try {
      ChatCompletionResult chatCompletionResult = chatCompletionRequest.execute();
      Choice result = chatCompletionResult.getChoices().iterator().next();
      chatCompletionRequest.addMessage(result.getChatMessage());
      appendChatMessage(result.getChatMessage());
      TextToSpeech.speak(result.getChatMessage().getContent());
      return result.getChatMessage();
    } catch (ApiProxyException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Sends a message to the GPT model.
   *
   * @param event the action event triggered by the send button
   * @throws ApiProxyException if there is an error communicating with the API proxy
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void onSendMessage(ActionEvent event) throws ApiProxyException, IOException {
    String message = txtInput.getText().trim();
    if (message.isEmpty()) {
      return;
    }
    txtInput.clear();
    ChatMessage msg = new ChatMessage("user", message);
    appendChatMessage(msg);
    runGpt(msg);
  }
}
