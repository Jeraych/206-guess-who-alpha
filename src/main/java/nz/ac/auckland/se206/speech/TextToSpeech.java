package nz.ac.auckland.se206.speech;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import javafx.concurrent.Task;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import nz.ac.auckland.apiproxy.config.ApiProxyConfig;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.apiproxy.tts.TextToSpeechRequest;
import nz.ac.auckland.apiproxy.tts.TextToSpeechRequest.Provider;
import nz.ac.auckland.apiproxy.tts.TextToSpeechRequest.Voice;
import nz.ac.auckland.apiproxy.tts.TextToSpeechResult;

/** A utility class for converting text to speech using the specified API proxy. */
public class TextToSpeech {

  /**
   * Converts the given text to speech and plays the audio.
   *
   * @param text the text to be converted to speech
   * @throws IllegalArgumentException if the text is null or empty
   */
  public static void speak(String text) {
    if (text == null || text.isEmpty()) {
      throw new IllegalArgumentException("Text should not be null or empty");
    }

    Task<Void> backgroundTask =
        new Task<>() {
          @Override
          protected Void call() throws IOException {
            try {
              ApiProxyConfig config = ApiProxyConfig.readConfig();
              Provider provider = Provider.GOOGLE;
              Voice voice = Voice.GOOGLE_EN_US_STANDARD_H;

              TextToSpeechRequest ttsRequest = new TextToSpeechRequest(config);
              ttsRequest.setText(text).setProvider(provider).setVoice(voice);

              TextToSpeechResult ttsResult = ttsRequest.execute();
              String audioUrl = ttsResult.getAudioUrl();

              FileWriter write = new FileWriter("src/main/resources/sounds/audio.txt");
              write.write(audioUrl);
              write.close();

              File file = new File("src/main/resources/sounds/audio.txt");
              Scanner urlReader = new Scanner(file);
              String url = urlReader.nextLine();
              urlReader.close();

              try (InputStream inputStream = new BufferedInputStream(new URL(url).openStream())) {
                Player player = new Player(inputStream);
                player.play();
              } catch (JavaLayerException | IOException e) {
                e.printStackTrace();
              }

            } catch (ApiProxyException e) {
              e.printStackTrace();
            }
            return null;
          }
        };

    Thread backgroundThread = new Thread(backgroundTask);
    backgroundThread.setDaemon(true); // Ensure the thread does not prevent JVM shutdown
    backgroundThread.start();
  }
}
