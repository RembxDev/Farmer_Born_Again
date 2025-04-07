package org.jetbrains.rafal.farmer_born_again.Controller.WS;

import org.jetbrains.rafal.farmer_born_again.DTO.ChatMessageDTO;
import org.jetbrains.rafal.farmer_born_again.DTO.PlayerStatusDTO;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class LobbyWsController {

    @MessageMapping("/lobby/chat")
    @SendTo("/topic/lobby/chat")
    public ChatMessageDTO handleChat(ChatMessageDTO chatMessage) {
        System.out.println(">>> Czat: " + chatMessage.getSender() + ": " + chatMessage.getContent());
        chatMessage.setTimestamp(LocalDateTime.now());
        chatMessage.setType("CHAT");
        return chatMessage;
    }

    @MessageMapping("/lobby/status")
    @SendTo("/topic/lobby/status")
    public PlayerStatusDTO handlePlayerStatus(PlayerStatusDTO status) {
        return status;
    }
}
