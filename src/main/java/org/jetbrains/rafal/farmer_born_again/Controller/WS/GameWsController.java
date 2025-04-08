package org.jetbrains.rafal.farmer_born_again.Controller.WS;

import org.jetbrains.rafal.farmer_born_again.DTO.GameActionEventDTO;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class GameWsController {

    @MessageMapping("/game/{gameId}/action")
    @SendTo("/topic/game/{gameId}")
    public GameActionEventDTO handleAction(@DestinationVariable String gameId, GameActionEventDTO action) {
        System.out.println(">>> Gra " + gameId + ": " + action.getPlayer() + " -> " + action.getAction());
        return action;
    }
}
