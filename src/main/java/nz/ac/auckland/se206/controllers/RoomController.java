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

/**
 * Controller class for the room view. Handles user interactions within the room where the user can
 * chat with customers and guess their name.
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
  @FXML private Button btnFlaw;

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
  private boolean greeting1 = true;
  private boolean greeting2 = true;
  private boolean greeting3 = true;
  private boolean greeting4 = true;
  private String me;
  private boolean setPerson1 = false;
  private boolean setPerson2 = false;
  private boolean setPerson3 = false;
  private boolean setStatue = false;
  private Rectangle clickedRectangle;
  private boolean gameEnd;

  private ChatCompletionRequest chatCompletionRequest1;
  private ChatCompletionRequest chatCompletionRequest2;
  private ChatCompletionRequest chatCompletionRequest3;
  private String name;

  /**
   * Initializes the room view. If it's the first time initialization, it will provide instructions
   * via text-to-speech.
   *
   * @throws ApiProxyException
   */
  @FXML
  public void initialize() throws ApiProxyException {
    me = "You";
    gameEnd = false;
    ApiProxyConfig config = ApiProxyConfig.readConfig();
    chatCompletionRequest1 =
        new ChatCompletionRequest(config)
            .setN(1)
            .setTemperature(0.2)
            .setTopP(0.5)
            .setMaxTokens(100);
    chatCompletionRequest2 =
        new ChatCompletionRequest(config)
            .setN(1)
            .setTemperature(0.2)
            .setTopP(0.5)
            .setMaxTokens(100);
    chatCompletionRequest3 =
        new ChatCompletionRequest(config)
            .setN(1)
            .setTemperature(0.2)
            .setTopP(0.5)
            .setMaxTokens(100);
    ChatMessage start =
        new ChatMessage("user", "Hurry, we have two minutes till suspects escape.", me);
    appendChatMessage(start);
    // TextToSpeech.speak(start.getContent());
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
                  if (gameEnd) {
                    clock.stop();
                  }
                  milliSecond--;
                  if (milliSecond < 0) {
                    milliSecond = 99;
                    second--;
                    if (second < 0) {
                      second = 1;
                      minute--;
                      if (minute < 0) {
                        milliSecond = 0;
                        second = 0;
                        minute = 0;
                        timerEnd = true;
                        clock.stop();
                        ChatMessage msg =
                            new ChatMessage("user", "Arrest a suspect, quick!", "You");
                        btnFlaw.setStyle("-fx-font-size: 28;" + "-fx-color: BLACK;");
                        btnFlaw.setText("Arrest!");
                        // TextToSpeech.speak(msg.getContent());
                        appendChatMessage(msg);
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
                  if (gameEnd) {
                    clock.stop();
                  }

                  finalSecond--;
                  if (finalSecond < 0) {
                    finalSecond = 0;
                    countdownEnd = true;
                    countdown.stop();
                    ChatMessage msgWin = new ChatMessage("user", "Suspects escaped. (Sigh)", "You");
                    // TextToSpeech.speak(msgWin.getContent());
                    appendChatMessage(msgWin);
                    appendSystemMessage("You lost the game.");
                    gameEnd = true;
                  }
                  finalCountdown.setText(Integer.toString(finalSecond));
                }));
    countdown.setCycleCount(Animation.INDEFINITE);
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
  private void handleRectangleClick(MouseEvent event) throws IOException {
    if (gameEnd) {
      return;
    }
    clickedRectangle = (Rectangle) event.getSource();
    if (clickedRectangle == rectPerson1) {
      setPerson1 = true;
      name = context.getName("rectPerson1");
      clickedRectangle.setStyle("-fx-opacity: 0.2");
      rectPerson2.setStyle("-fx-opacity: 0");
      rectPerson3.setStyle("-fx-opacity: 0");
      rectStatue.setStyle("-fx-opacity: 0");
      setPerson1 = true;
      setPerson2 = false;
      setPerson3 = false;
      setStatue = false;

      Task<Void> generateResponse =
          new Task<Void>() {

            @Override
            protected Void call() throws Exception {
              setName(name, "chat1.txt", chatCompletionRequest1);
              enableChat();
              return null;
            }
          };
      Thread responseThread = new Thread(generateResponse);

      if (greeting1) {
        appendSystemMessage(
            "A man is staring at the description card mindlessly. You decide to name him Suspect"
                + " 2");
        greeting1 = false;
        responseThread.setDaemon(true);
        responseThread.start();
      }

      if (clueFound) {
        appendSystemMessage("You notice Suspect 1 has got some white dust on his shirt.");
      }
    }

    if (clickedRectangle == rectPerson2) {
      name = context.getName("rectPerson2");
      clickedRectangle.setStyle("-fx-opacity: 0.2");
      rectPerson1.setStyle("-fx-opacity: 0");
      rectPerson3.setStyle("-fx-opacity: 0");
      rectStatue.setStyle("-fx-opacity: 0");
      setPerson2 = true;
      setPerson1 = false;
      setPerson3 = false;
      setStatue = false;

      Task<Void> generateResponse =
          new Task<Void>() {

            @Override
            protected Void call() throws Exception {
              setName(name, "chat2.txt", chatCompletionRequest2);
              enableChat();
              return null;
            }
          };
      Thread responseThread = new Thread(generateResponse);
      if (greeting2) {
        appendSystemMessage(
            "You notice a man wondering around looking for something. You noted him as Suspect 1");
        greeting2 = false;
        responseThread.setDaemon(true);
        responseThread.start();
      }
    }

    if (clickedRectangle == rectPerson3) {
      name = context.getName("rectPerson3");
      clickedRectangle.setStyle("-fx-opacity: 0.2");
      rectPerson2.setStyle("-fx-opacity: 0");
      rectPerson1.setStyle("-fx-opacity: 0");
      rectStatue.setStyle("-fx-opacity: 0");
      setPerson3 = true;
      setPerson2 = false;
      setPerson1 = false;
      setStatue = false;

      Task<Void> generateResponse =
          new Task<Void>() {

            @Override
            protected Void call() throws Exception {
              setName(name, "chat3.txt", chatCompletionRequest3);
              enableChat();
              return null;
            }
          };
      Thread responseThread = new Thread(generateResponse);

      if (greeting3) {
        appendSystemMessage(
            "A beautiful woman works around the museum, she seems interested in those statues. You"
                + " called her Suspect 3 anyways");
        greeting3 = false;
        responseThread.setDaemon(true);
        responseThread.start();
      }
    }

    if (clickedRectangle == rectStatue) {
      clickedRectangle.setStyle("-fx-opacity: 0.2");
      rectPerson2.setStyle("-fx-opacity: 0");
      rectPerson3.setStyle("-fx-opacity: 0");
      rectPerson1.setStyle("-fx-opacity: 0");
      setPerson1 = false;
      setPerson2 = false;
      setPerson3 = false;
      setStatue = true;
      if (greeting4) {
        appendSystemMessage(
            new String(
                Files.readAllBytes(Paths.get("src\\main\\resources\\prompts\\chatClue.txt"))));
        greeting4 = false;
      } else {
        appendSystemMessage("You got nothing more than white dusts on your hands.");
      }
      disableChat();
    }
  }

  @FXML
  private void enableChat() {
    txtInput.setDisable(false);
    btnSend.setDisable(false);
  }

  @FXML
  private void disableChat() {
    txtInput.setDisable(true);
    btnSend.setDisable(true);
  }

  /**
   * Handles the guess button click event.
   *
   * @param event the action event triggered by clicking the guess button
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void handleGuessClick(ActionEvent event) throws IOException {
    if (gameEnd) {
      return;
    }
    if (!setPerson1 & !setPerson2 & !setPerson3) {
      ChatMessage msg = new ChatMessage("user", "I need to choose someone.", "You");
      // TextToSpeech.speak(msg.getContent());
      appendChatMessage(msg);
      return;
    }
    btnFlaw.setStyle("-fx-background-color: RED;" + "-fx-font-size: 28;");
    btnFlaw.setText("Caught");
    disableChat();
    if (name == context.getRectIdToGuess()) {
      ChatMessage msgWin = new ChatMessage("user", name + "is guilty! Send to jail!", "You");
      // TextToSpeech.speak(msgWin.getContent());
      appendChatMessage(msgWin);
      appendSystemMessage("You won the game.");
    } else {
      ChatMessage msgLose = new ChatMessage("user", name + "is not guilty! I am wrong!", "You");
      // TextToSpeech.speak(msgLose.getContent());
      appendChatMessage(msgLose);
      appendSystemMessage("You Lost the game.");
    }

    gameEnd = true;
  }

  /**
   * Generates the system prompt based on the name.
   *
   * @return the system prompt string
   */
  private String getSystemPrompt(String id) {
    Map<String, String> map = new HashMap<>();
    map.put("name", name);
    return PromptEngineering.getPrompt(id, map);
  }

  /**
   * Sets the name for the chat context and initializes the ChatCompletionRequest.
   *
   * @param name the name to set
   */
  public void setName(String name, String id, ChatCompletionRequest chatCompletionRequest) {
    this.name = name;
    try {

      runGpt(new ChatMessage("system", getSystemPrompt(id), name), chatCompletionRequest);
    } catch (ApiProxyException e) {
      e.printStackTrace();
    }
  }

  /**
   * Appends a chat message to the chat text area.
   *
   * @param msg the chat message to append
   */
  public void appendChatMessage(ChatMessage msg) {
    txtChat.appendText(msg.getName() + ": " + msg.getContent() + "\n\n");
  }

  /**
   * Appends a chat message to the chat text area.
   *
   * @param msg the chat message to append
   */
  public void appendSystemMessage(String msg) {
    txtChat.appendText("[" + msg + "]\n\n");
  }

  /**
   * Runs the GPT model with a given chat message.
   *
   * @param msg the chat message to process
   * @return the response chat message
   * @throws ApiProxyException if there is an error communicating with the API proxy
   */
  private ChatMessage runGpt(ChatMessage msg, ChatCompletionRequest chatCompletionRequest)
      throws ApiProxyException {
    chatCompletionRequest.addMessage(msg);
    try {
      ChatCompletionResult chatCompletionResult = chatCompletionRequest.execute();
      Choice result = chatCompletionResult.getChoices().iterator().next();
      result.getChatMessage().setName(name);
      chatCompletionRequest.addMessage(result.getChatMessage());
      appendChatMessage(result.getChatMessage());
      // TextToSpeech.speak(result.getChatMessage().getContent());
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
    ChatCompletionRequest chatCompletionRequest;
    if (setPerson1) {
      chatCompletionRequest = chatCompletionRequest1;
    } else if (setPerson2) {
      chatCompletionRequest = chatCompletionRequest2;
    } else {
      chatCompletionRequest = chatCompletionRequest3;
    }
    txtInput.clear();
    ChatMessage msg = new ChatMessage("user", message, me);
    appendChatMessage(msg);
    runGpt(msg, chatCompletionRequest);
  }
}
