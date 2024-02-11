package com.example.htmxchatgpt;

import com.example.htmxchatgpt.model.Chat;
import com.example.htmxchatgpt.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@SpringBootApplication
public class HtmxChatGptApplication {

  public static void main(String[] args) {
    SpringApplication.run(HtmxChatGptApplication.class, args);
  }
}

@Slf4j
@Controller
class WebController {
  private final ChatRepository chatRepository;

  public WebController(ChatRepository chatRepository) {
    this.chatRepository = chatRepository;
  }

  @GetMapping("/index")
  public String index(Model model) {
    var chatId = "single_chat";
    var chat = this.chatRepository.getChat(chatId);
    if (chat == null) {
      this.chatRepository.createChat(chatId);
    }
    model.addAttribute("messages", chat.getMessages());
    return "index";
  }

  @PostMapping("/user/message")
  public String message(@RequestParam String message, Model model) {
    var chatId = "single_chat";

    var chat = chatRepository.getChat(chatId);
    chat.getMessages().add(Message.builder().role("user").content(message).build());

    this.chatRepository.writeChat(chat);

    model.addAttribute("message", message);
    model.addAttribute("role", "user");
    model.addAttribute("id", chatId);
    return "UserMessage";
  }

  @GetMapping("/agent/message")
  public String agentMessage(@RequestParam("id") String id, Model model) {
    log.info("entering agent message");
    var chat = this.chatRepository.getChat(id);
    var messages = chat.getMessages();
    var lastMessage = messages.get(messages.size() - 1);
    chat.getMessages().add(lastMessage);
    model.addAttribute("message", lastMessage.getContent());
    model.addAttribute("role", "assistant");
    model.addAttribute("id", chat.getId());
    return "AgentMessage";
  }
}

@Slf4j
@Service
class ChatRepository {
  private static final String CHAT_DIR = "./chats";

  private final ObjectMapper mapper;

  public ChatRepository(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  private File getChatFile(String id) {
    var chatFilePath = id + ".json";
    return Paths.get(CHAT_DIR, chatFilePath).toFile();
  }

  public Chat createChat(String id) {
    var chat = Chat.builder().id(id).messages(new ArrayList<Message>()).build();
    writeChat(chat);
    return chat;
  }

  public void writeChat(Chat chat) {
    log.info("creating chat: {}", chat.getId());
    var chatFile = getChatFile(chat.getId());
    try (var fileWriter = new FileWriter(chatFile);
        var bufWriter = new BufferedWriter(fileWriter)) {
      bufWriter.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(chat));
    } catch (IOException e) {
      log.error("unable to writeChat: {}", chat.getId(), e);
    }
  }

  public Chat getChat(String id) {
    var chatFile = getChatFile(id);
    try (var fileReader = new FileReader(chatFile);
        var bufReader = new BufferedReader(fileReader)) {
      return mapper.readValue(bufReader, Chat.class);
    } catch (Exception e) {
      log.error("unable to getChat: {}", id, e);
      return createChat(id);
    }
  }
}
