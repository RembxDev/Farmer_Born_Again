package org.jetbrains.rafal.farmer_born_again.Controller.WS;

import lombok.RequiredArgsConstructor;
import org.jetbrains.rafal.farmer_born_again.DTO.GameActionEventDTO;
import org.jetbrains.rafal.farmer_born_again.Model.Animal;
import org.jetbrains.rafal.farmer_born_again.Model.Game;
import org.jetbrains.rafal.farmer_born_again.Model.Player;
import org.jetbrains.rafal.farmer_born_again.Service.AnimalService;
import org.jetbrains.rafal.farmer_born_again.Service.GameService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.Objects;

@Controller
@RequiredArgsConstructor
public class GameWsController {

    private final GameService gameService;
    private final AnimalService animalService;

    @MessageMapping("/game/{gameId}/action")
    @SendTo("/topic/game/{gameId}")
    public GameActionEventDTO handleAction(@DestinationVariable String gameId, GameActionEventDTO action) {
        System.out.println(">>> Gra " + gameId + ": " + action.getPlayer() + " -> " + action.getAction());

        if ("FEED_ANIMAL".equals(action.getAction())) {

            Game game = gameService.getGameById(gameId);
            Player player = game.getPlayers().stream()
                    .filter(p -> p.getName().equals(action.getPlayer()))
                    .findFirst().orElse(null);

            if (player != null) {
                Animal fedAnimal = player.getAnimals().stream()
                        .filter(a -> a.getId() != null && Objects.equals(a.getId().longValue(), action.getTargetId()))
                        .findFirst()
                        .orElse(null);

                if (fedAnimal != null) {
                    int before = fedAnimal.getFeedLevel();
                    int newLevel = Math.min(5, before + 1);
                    fedAnimal.setFeedLevel(newLevel);
                    action.setFeedLevel(newLevel);
                }
            }
        }

        return action;
    }
}
