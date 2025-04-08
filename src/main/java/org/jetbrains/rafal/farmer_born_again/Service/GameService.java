package org.jetbrains.rafal.farmer_born_again.Service;

import lombok.AllArgsConstructor;
import org.jetbrains.rafal.farmer_born_again.DTO.GameStartStatusDTO;
import org.jetbrains.rafal.farmer_born_again.Model.Game;
import org.jetbrains.rafal.farmer_born_again.Model.Player;
import org.jetbrains.rafal.farmer_born_again.Repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@AllArgsConstructor
@Service
public class GameService {
    private final Map<String, Game> waitingGames = new ConcurrentHashMap<>();
    private final GameRepository gameRepository;
    private static final int MAX_PLAYERS = 2;

    public synchronized Game assignPlayerToGame(Player player) {
        Optional<Game> openGame = waitingGames.values().stream()
                .filter(g -> g.getPlayers().size() < MAX_PLAYERS && !g.isStarted())
                .findFirst();

        Game game;
        if (openGame.isPresent()) {
            game = openGame.get();
        } else {
            game = new Game();
            game.setTureNumber(0);
            game.setCurrentPhase(Game.Phase.MORNING);
            game.setStarted(false);
            game.setCurrentEvent(null);
            game.setPlayers(new ArrayList<>());
            waitingGames.put(UUID.randomUUID().toString(), game);
        }

        boolean alreadyInGame = game.getPlayers().stream()
                .anyMatch(p -> p.getName().equals(player.getName()));

        if (!alreadyInGame) {
            game.getPlayers().add(player);
        }
        player.setGame(game);

        return game;
    }

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public synchronized void markPlayerReadyAndStartIfPossible(Player player) {
        markPlayerReady(player);
        Game game = player.getGame();

        if (isGameReady(game)) {
            game.setStarted(true);

            messagingTemplate.convertAndSend(
                    "/topic/lobby/gameStartStatus",
                    new GameStartStatusDTO("GAME_STARTED", "Gra się rozpoczyna!")
            );
        }
    }


    public void markPlayerReady(Player player) {
        player.setReady(true);
    }

    public boolean isGameReady(Game game) {
        return game.getPlayers().size() == MAX_PLAYERS &&
                game.getPlayers().stream().allMatch(Player::isReady);
    }

    public void removeGame(Game game) {
        waitingGames.values().removeIf(g -> g == game);
    }

    public void removePlayerFromGame(Player player) {
        Game game = player.getGame();
        if (game != null) {
            game.getPlayers().remove(player);
            player.setGame(null);

            if (game.getPlayers().isEmpty()) {
                removeGame(game);
            }
        }
    }

}
