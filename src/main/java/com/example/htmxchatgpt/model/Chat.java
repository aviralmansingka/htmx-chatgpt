package com.example.htmxchatgpt.model;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Chat {
  private String id;
  private List<Message> messages;
}
