package com.example.htmxchatgpt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@SpringBootApplication
public class HtmxChatGptApplication {

  public static void main(String[] args) {
    SpringApplication.run(HtmxChatGptApplication.class, args);
  }
}

@Controller
class WebController {
  @GetMapping("/index")
  public String helloWorld() {
    return "index";
  }

  @PostMapping("/new_chat")
  public String newChat(Model model) {
    model.addAttribute("name", "Sample chat name");
    return "new_chat";
  }
}
