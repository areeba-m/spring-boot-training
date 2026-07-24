package com.redmath.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    ChatClient chatClient;

    public ChatController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping
    public String chat(@RequestParam(name = "message", defaultValue = "Hi") String message) {
        // return chatModel.call(message);
        return chatClient.prompt(message)
                .advisors(context -> context.param(ChatMemory.CONVERSATION_ID, "default"))
                .call().content();
    }
}
