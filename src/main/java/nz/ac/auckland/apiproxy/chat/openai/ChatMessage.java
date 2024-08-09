package nz.ac.auckland.apiproxy.chat.openai;

public class ChatMessage {

  private String role;
  private String content;
  private String name;

  public ChatMessage(String role, String content) {
    this.role = role;
    this.content = content;
  }

  public ChatMessage(String role, String content, String name) {
    this.role = role;
    this.content = content;
    this.name = name;
  }

  public String getRole() {
    return role;
  }

  public String getContent() {
    return content;
  }

  public String getName() {
    return name;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public void setName(String name) {
    this.name = name;
  }
}
