package org.jetbrains.rafal.farmer_born_again.Service;

import lombok.AllArgsConstructor;
import org.jetbrains.rafal.farmer_born_again.DTO.GameStartStatusDTO;
import org.jetbrains.rafal.farmer_born_again.DTO.PlayerStatusDTO;
import org.jetbrains.rafal.farmer_born_again.Model.Game;
import org.jetbrains.rafal.farmer_born_again.Model.Player;
import org.jetbrains.rafal.farmer_born_again.Repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@AllArgsConstructor
@Service
public class GameService {
    private final Map<String, Game> waitingGames = new ConcurrentHashMap<>();
    private final GameRepository gameRepository;
    private static final int MAX_PLAYERS = 2;
    private final Map<String, Player> allPlayers = new ConcurrentHashMap<>();


    public synchronized Game assignPlayerToGame(Player player) {
        Optional<Game> openGame = waitingGames.values().stream()
                .filter(g -> g.getPlayers().size() < MAX_PLAYERS && !g.isStarted())
                .findFirst();

        Player existing = allPlayers.get(player.getName());
        if (existing != null) {
            return existing.getGame();
        }
        allPlayers.put(player.getName(), player);


        Game game;
        if (openGame.isPresent()) {
            game = openGame.get();
        } else {
            game = new Game();
            game.setId(UUID.randomUUID().toString());
            game.setTureNumber(0);
            game.setCurrentPhase(Game.Phase.MORNING);
            game.setStarted(false);
            game.setCurrentEvent(null);
            game.setPlayers(new ArrayList<>());
            waitingGames.put(game.getId(), game);
        }

        boolean alreadyInGame = game.getPlayers().stream()
                .anyMatch(p -> p.getName().equals(player.getName()));

        if (!alreadyInGame) {
            game.getPlayers().add(player);
            broadcastPlayerStatus(player, "JOINED");

        }
        player.setGame(game);

        broadcastPlayerList(game);
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

    private void broadcastPlayerList(Game game) {
        List<PlayerStatusDTO> playerNames = game.getPlayers().stream()
                .map(p -> new PlayerStatusDTO(p.getName(), p.isReady() ? "READY" : "JOINED"))
                .toList();

        messagingTemplate.convertAndSend(
                "/topic/lobby/playerList",
                playerNames
        );
    }

    private void broadcastPlayerStatus(Player player, String status) {
        messagingTemplate.convertAndSend(
                "/topic/lobby/status",
                new PlayerStatusDTO(player.getName(), status)
        );
    }

    public void markPlayerReady(Player player) {
        if(!player.isReady()) {
            player.setReady(true);
            broadcastPlayerStatus(player, "READY");
        } else {
            player.setReady(false);
            broadcastPlayerStatus(player, "JOINED");
        }
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
            broadcastPlayerList(game);
            broadcastPlayerStatus(player, "LEFT");
            if (game.getPlayers().isEmpty()) {
                removeGame(game);
            }
        }
    }

    public synchronized void endTurn(Player player) {
        markPlayerFinishTurn(player);
        Game game = player.getGame();

        if (doesEveronefinishedTurn(game)) {
            game.changeCycle();

            messagingTemplate.convertAndSend(
                    "/topic/game/"+game.getId()+"/endTurn",
                    new GameStartStatusDTO("NIGHT_TIME", "Gracze zakończył ture!")
            );
        }
    }

    public void markPlayerFinishTurn(Player player) {
        player.setFinishedTurn(true);
    }

    public boolean doesEveronefinishedTurn(Game game){
        return game.getPlayers().stream().allMatch(Player::isFinishedTurn);
    }

}
