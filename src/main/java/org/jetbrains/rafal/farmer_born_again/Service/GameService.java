package org.jetbrains.rafal.farmer_born_again.Service;

import lombok.AllArgsConstructor;
import org.jetbrains.rafal.farmer_born_again.DTO.GameActionEventDTO;
import org.jetbrains.rafal.farmer_born_again.DTO.GameStartStatusDTO;
import org.jetbrains.rafal.farmer_born_again.DTO.PlayerStatusDTO;
import org.jetbrains.rafal.farmer_born_again.Model.*;
import org.jetbrains.rafal.farmer_born_again.Repository.GameRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.jetbrains.rafal.farmer_born_again.Model.Game.NightEventType.SPOKOJNA_NOC;

@AllArgsConstructor
@Service
public class GameService {
    private final Map<String, Game> waitingGames = new ConcurrentHashMap<>();
    private final GameRepository gameRepository;
    private final AnimalService animalService;
    private static final int MAX_PLAYERS = 2;
    private final Map<String, Player> allPlayers = new ConcurrentHashMap<>();

    public Game getGameById(String gameId) {
        return waitingGames.get(gameId);
    }

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
            game.setCurrentEvent(SPOKOJNA_NOC);
            game.setPlayers(new ArrayList<>());
            waitingGames.put(game.getId(), game);
        }

        boolean alreadyInGame = game.getPlayers().stream()
                .anyMatch(p -> p.getName().equals(player.getName()));

        if (!alreadyInGame) {
            game.getPlayers().add(player);
            broadcastPlayerStatus(player, "JOINED");

            Animal rabbit1 = animalService.createAnimal("rabbit", 80, 0, 1, player);
            Animal rabbit2 = animalService.createAnimal("rabbit", 80, 0, 1, player);
            //Animal chicken = animalService.createAnimal("chicken", 80, 1, 1, player);
            player.setAnimals(new ArrayList<>());
            player.getAnimals().add(rabbit1);
            player.getAnimals().add(rabbit2);
            //player.getAnimals().add(chicken);
        } else {
            player.setGame(game);
        }
        player.setGame(game);

        broadcastPlayerList(game);
        return game;
    }


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


        if (game.getPlayers() != null) {
            for (Player p : game.getPlayers()) {
                allPlayers.remove(p.getName());
            }
        }
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

        long finished = game.getPlayers().stream().filter(Player::isFinishedTurn).count();
        int total = game.getPlayers().size();

        GameActionEventDTO turnProgress = new GameActionEventDTO();
        turnProgress.setAction("TURN_PROGRESS");
        turnProgress.setPlayer(player.getName());
        turnProgress.setDescription("🔄 " + finished + "/" + total + " graczy zakończyło turę");

        messagingTemplate.convertAndSend("/topic/game/" + game.getId(), turnProgress);

        if (doesEveronefinishedTurn(game)) {
            game.changeCycle();


            for (Player p : game.getPlayers()) {
                p.setFinishedTurn(false);
            }

            game.setBreedingBonus(0);
            game.setPriceBonus(0);
            game.setMarketLock(false);

            triggerNightEvent(game);

            messagingTemplate.convertAndSend(
                    "/topic/game/" + game.getId() + "/endTurn",
                    new GameStartStatusDTO("NIGHT_TIME", "Gracze zakończyli turę!")
            );
        }
    }

    public void markPlayerFinishTurn(Player player) {
        player.setFinishedTurn(!player.isFinishedTurn());
    }

    public boolean doesEveronefinishedTurn(Game game){
        return game.getPlayers().stream().allMatch(Player::isFinishedTurn);
    }

    public void generateAnimalProducts(Player player, List<String> log) {
        List<Animal> animals = player.getAnimals();

        for (Animal animal : animals) {
            animal.setTurnCounter(animal.getTurnCounter() + 1);

            if (animal.isSick() || animal.getFeedLevel() < 3) continue;

            String productName = switch (animal.getName()) {
                case "cow" -> "milk";
                case "sheep" -> "wool";
                case "chicken" -> "egg";
                default -> null;
            };

            if (productName != null) {
                Optional<Product> existing = player.getProducts().stream()
                        .filter(p -> p.getName().equals(productName))
                        .findFirst();

                if (existing.isPresent()) {
                    existing.get().setQuantity(existing.get().getQuantity() + 1);
                } else {
                    player.getProducts().add(new Product(null, productName, 1, player));
                }

                log.add("🧺 " + productName + " wyprodukowany przez: " + animal.getName() + " (ID: " + animal.getId() + ")");
                animal.setTurnCounter(0);
            }
        }
    }

    public Map<String, Object> animalsRoll(Player player) {
        List<String> log = new ArrayList<>();
        List<String> diceResults = new ArrayList<>();

        generateAnimalProducts(player, log);
        generateGrass(player);

        List<Animal> playerAnimals = player.getAnimals();

        Map<String, Integer> weightedTypes = Map.of(
                "rabbit", 35,
                "chicken", 25,
                "sheep", 15,
                "cow", 15,
                "fox", 5,
                "wolf", 5
        );

        List<String> possibleTypes = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : weightedTypes.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                possibleTypes.add(entry.getKey());
            }
        }

        Random rand = new Random();
        Map<String, Integer> diceCounts = new HashMap<>();


        for (int i = 0; i < 3; i++) {
            String rolledType = possibleTypes.get(rand.nextInt(possibleTypes.size()));
            diceResults.add(rolledType);
            log.add("🎲 Rzut #" + (i + 1) + ": " + rolledType);
            diceCounts.put(rolledType, diceCounts.getOrDefault(rolledType, 0) + 1);
        }

        if (diceCounts.containsKey("fox") && player.getGame().getBreedingBonus()==0) {
            log.add("🦊 Lis zakradł się na farmę!");
            double foxChance = player.isSmallDog() ? 5 : 90;
            log.add("🛡️ Szansa na zjedzenie królika i kury: " + foxChance + "%");

            Iterator<Animal> it = playerAnimals.iterator();
            while (it.hasNext()) {
                Animal a = it.next();
                if (a.getName().equals("rabbit") || a.getName().equals("chicken")) {
                    if (rand.nextInt(100) < foxChance) {
                        log.add("❌ Lis pożarł: " + a.getName() + " (ID: " + a.getId() + ")");
                        it.remove();
                    }
                }
            }
        }

        if (diceCounts.containsKey("wolf") && player.getGame().getBreedingBonus()==0) {
            log.add("🐺 Wilk zakradł się na farmę!");
            double wolfChance = player.isBigDog() ? 5 : 90;
            log.add("🛡️ Szansa na zjedzenie owcy i krowy: " + wolfChance + "%");

            Iterator<Animal> it = playerAnimals.iterator();
            while (it.hasNext()) {
                Animal a = it.next();
                if (a.getName().equals("sheep") || a.getName().equals("cow")) {
                    if (rand.nextInt(100) < wolfChance) {
                        log.add("❌ Wilk pożarł: " + a.getName() + " (ID: " + a.getId() + ")");
                        it.remove();
                    }
                }
            }
        }

        for (String type : diceCounts.keySet()) {

            long validCount = playerAnimals.stream()
                    .filter(a -> a.getName().equals(type) && !a.isSick() && a.getFeedLevel() >= 3)
                    .count();

            int total = (int) validCount + diceCounts.get(type);
            int pairs = total / 2;

            if(type.equals("fox") || type.equals("wolf")){
                continue;
            }

            if (pairs == 0) {
                log.add("⚠️ Za mało zdrowych i najedzonych " + type + " do rozmnażania.");
                continue;
            }

            Animal template = playerAnimals.stream()
                    .filter(a -> a.getName().equals(type))
                    .findFirst()
                    .orElseGet(() -> animalService.getBaseAnimal(type));

            if (template == null) {
                continue;
            }
            for (int i = 0; i < pairs; i++) {
                if (rand.nextInt(100) < (template.getReproductionChance()+player.getGame().getBreedingBonus())) {
                    Animal baby = animalService.createAnimal(
                            template.getName(),
                            template.getReproductionChance(),
                            template.getFoodRequirement(),
                            template.getSellPrice(),
                            player
                    );

                    player.getAnimals().add(baby);
                    log.add("✨ Nowe zwierzę urodziło się: " + baby.getName() + " (ID: " + baby.getId() + ")");
                } else {
                    log.add("❌ Nie udało się rozmnożyć " + type);
                }
            }
        }

        return Map.of(
                "log", log,
                "diceResults", diceResults
        );
    }

    private void generateGrass(Player player) {
        Map<String, Integer> Silo = player.getSilo();
        Silo.compute("grass", (k, GrassCount) -> (int) (GrassCount * 1.5));

    }

    public void triggerNightEvent(Game game) {
        Game.NightEventType[] all = Game.NightEventType.values();
        Game.NightEventType drawn = all[new Random().nextInt(all.length)];
        game.setCurrentEvent(drawn);

        Event.applyEventEffect(game);


        for (Player player : game.getPlayers()) {
            List<String> feedingLogs = animalService.updateFeedingLevels(player);
            for (String msg : feedingLogs) {
                GameActionEventDTO logMsg = new GameActionEventDTO();
                logMsg.setAction("HUNGER_UPDATE");
                logMsg.setPlayer(player.getName());
                logMsg.setDescription(msg);
                messagingTemplate.convertAndSend("/topic/game/" + game.getId(), logMsg);
            }
        }
    }




}