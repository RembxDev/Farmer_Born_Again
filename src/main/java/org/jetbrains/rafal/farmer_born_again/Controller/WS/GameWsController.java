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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class GameWsController {
    private final GameService gameService;
    private final AnimalService animalService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/game/{gameId}/action")
    public void handleAction(@DestinationVariable String gameId,
                             GameActionEventDTO action) {

        if ("FEED_ANIMAL".equals(action.getAction())) {

            Game game = gameService.getGameById(gameId);
            Player player = game.getPlayers().stream()
                    .filter(p -> p.getName().equals(action.getPlayer()))
                    .findFirst()
                    .orElse(null);

            if (player != null) {
                Optional<Animal> opt = animalService.feedAnimalById(player, action.getTargetId());
                if (opt.isPresent()) {
                    Animal fed = opt.get();
                    String siloKey;
                    switch (fed.getFoodRequirement()) {
                        case 0 -> siloKey = null;
                        case 1 -> siloKey = "low_quality";
                        case 2 -> siloKey = "medium_quality";
                        default -> siloKey = "high_quality";
                    }
                    System.out.printf("%s: %s\n", fed.getFeedLevel(), player.getSilo().get(siloKey));
                    action.setDescription(fed.isFed() && player.getSilo().get(siloKey) > 0
                            ? "✅ " + fed.getName() + " nakarmiony (" + (fed.getFeedLevel() - 1) + " → " + fed.getFeedLevel() + ")"
                            : "❌ Brak paszy na karmienie " + fed.getName());
                    action.setFeedLevel(fed.getFeedLevel());
                    fed.setFed(false);
                } else {
                    action.setDescription("❌ Nie znaleziono zwierzęcia o ID " + action.getTargetId());
                }
            }
        }


        messagingTemplate.convertAndSend(
                "/topic/game/" + gameId,
                action
        );
    }
}