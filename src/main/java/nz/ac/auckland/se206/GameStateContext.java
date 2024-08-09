package nz.ac.auckland.se206;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javafx.scene.input.MouseEvent;
import nz.ac.auckland.se206.states.GameOver;
import nz.ac.auckland.se206.states.GameStarted;
import nz.ac.auckland.se206.states.GameState;
import nz.ac.auckland.se206.states.Guessing;
import org.yaml.snakeyaml.Yaml;

/**
 * Context class for managing the state of the game. Handles transitions between different game
 * states and maintains game data such as the names and rectangle IDs.
 */
public class GameStateContext {

  private final String rectIdToGuess;
  private final Map<String, String> rectanglesToName;
  private final GameStarted gameStartedState;
  private final Guessing guessingState;
  private final GameOver gameOverState;
  private GameState gameState;

  /** Constructs a new GameStateContext and initializes the game states and names. */
  public GameStateContext() {
    gameStartedState = new GameStarted(this);
    guessingState = new Guessing(this);
    gameOverState = new GameOver(this);

    gameState = gameStartedState; // Initial state
    Map<String, Object> obj = null;
    Yaml yaml = new Yaml();
    try (InputStream inputStream =
        GameStateContext.class.getClassLoader().getResourceAsStream("data/names.yaml")) {
      if (inputStream == null) {
        throw new IllegalStateException("File not found!");
      }
      obj = yaml.load(inputStream);
    } catch (IOException e) {
      e.printStackTrace();
    }

    @SuppressWarnings("unchecked")
    List<String> names = (List<String>) obj.get("names");

    Random random = new Random();
    Set<String> randomNames = new HashSet<>();
    while (randomNames.size() < 3) {
      String name = names.get(random.nextInt(names.size()));
      randomNames.add(name);
    }

    String[] randomNamesArray = randomNames.toArray(new String[3]);
    rectanglesToName = new HashMap<>();
    rectanglesToName.put("rectPerson1", randomNamesArray[0]);
    rectanglesToName.put("rectPerson2", randomNamesArray[1]);
    rectanglesToName.put("rectPerson3", randomNamesArray[2]);

    rectIdToGuess = randomNamesArray[1];
  }

  /**
   * Sets the current state of the game.
   *
   * @param state the new state to set
   */
  public void setState(GameState state) {
    this.gameState = state;
  }

  /**
   * Gets the initial game started state.
   *
   * @return the game started state
   */
  public GameState getGameStartedState() {
    return gameStartedState;
  }

  /**
   * Gets the guessing state.
   *
   * @return the guessing state
   */
  public GameState getGuessingState() {
    return guessingState;
  }

  /**
   * Gets the game over state.
   *
   * @return the game over state
   */
  public GameState getGameOverState() {
    return gameOverState;
  }

  /**
   * Gets the ID of the rectangle to be guessed.
   *
   * @return the rectangle ID to guess
   */
  public String getRectIdToGuess() {
    return rectIdToGuess;
  }

  /**
   * Gets the name associated with a specific rectangle ID.
   *
   * @param rectangleId the rectangle ID
   * @return the name associated with the rectangle ID
   */
  public String getName(String rectangleId) {
    return rectanglesToName.get(rectangleId);
  }

  /**
   * Handles the event when a rectangle is clicked.
   *
   * @param event the mouse event triggered by clicking a rectangle
   * @param rectangleId the ID of the clicked rectangle
   * @throws IOException if there is an I/O error
   */
  public void handleRectangleClick(MouseEvent event, String rectangleId) throws IOException {
    gameState.handleRectangleClick(event, rectangleId);
  }

  /**
   * Handles the event when the guess button is clicked.
   *
   * @throws IOException if there is an I/O error
   */
  public void handleGuessClick() throws IOException {
    gameState.handleGuessClick();
  }
}
