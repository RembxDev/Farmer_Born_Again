package org.jetbrains.rafal.farmer_born_again.Controller;

import jakarta.servlet.http.HttpSession;
import org.jetbrains.rafal.farmer_born_again.Model.Animal;
import org.jetbrains.rafal.farmer_born_again.Model.Game;
import org.jetbrains.rafal.farmer_born_again.Model.Player;
import org.jetbrains.rafal.farmer_born_again.Service.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/farm")
public class GameController {


    private final GameService gameService;

    public GameController(final GameService gameService) {
        this.gameService = gameService;
    }


    @GetMapping("/")
    public String startGame(Model model, HttpSession session) {
        Player player = (Player) session.getAttribute("player");

        if (player == null || player.getGame() == null) {
            return "redirect:/?error=loggedOut";
        }

        Game game = player.getGame();
        game.setCurrentPhase(Game.Phase.DAY);

        List<Animal> animals = player.getAnimals();
        if (animals == null) {
            animals = List.of();
        }


        Map<String, List<Animal>> groupedAnimals = animals.stream()
                .collect(Collectors.groupingBy(Animal::getName));


        Map<String, List<Long>> groupedAnimalIds = groupedAnimals.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream()
                                .map(animal -> animal.getId().longValue())
                                .toList()
                ));

        long sickCount = animals.stream().filter(Animal::isSick).count();
        int percentage = animals.isEmpty() ? 0 : (int) ((double) sickCount * 100 / animals.size());

        model.addAttribute("eventName", formatEventName(game.getCurrentEvent()));
        model.addAttribute("player", player);
        model.addAttribute("game", game);
        model.addAttribute("silo", player.getSilo());
        model.addAttribute("groupedAnimals", groupedAnimals);
        model.addAttribute("groupedAnimalIds", groupedAnimalIds);
        model.addAttribute("sickPercentage", percentage);
        return "game/farm";
    }

    @PostMapping("/ready")
    public ResponseEntity<Void> endTurn(HttpSession session) {
        Player player = (Player) session.getAttribute("player");
        if (player == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        gameService.endTurn(player);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/night")
    public String nightPhase(HttpSession session, Model model) {
        Player player = (Player) session.getAttribute("player");
        if (player == null) {
            return "redirect:/?error=loggedOut";
        }

        Game game = player.getGame();
        Game.NightEventType currentEvent = game.getCurrentEvent();

        model.addAttribute("eventName", formatEventName(currentEvent));
        model.addAttribute("eventDescription", getEventDescription(currentEvent));

        return "game/night";
    }

    @GetMapping("/morning")
    public String morningPhase(HttpSession session, Model model) {
        Player player = (Player) session.getAttribute("player");
        if (player == null) {
            return "redirect:/?error=loggedOut";
        }

        Game game = player.getGame();
        model.addAttribute("game", game);

        return "game/morning";
    }

    @GetMapping("/morning/process")
    @ResponseBody
    public Map<String, Object> processMorning(HttpSession session) {
        Player player = (Player) session.getAttribute("player");

        if (player == null) {
            return Map.of(
                    "log", List.of("❌ Błąd: Nie znaleziono gracza."),
                    "diceResults", List.of()
            );
        }

        return gameService.animalsRoll(player);
    }

    public String formatEventName(Game.NightEventType type) {
        if (type == null) return "Brak";
        return switch (type) {
            case MILA_POGODA -> "🌤️ Miła pogoda – zwierzęta łatwiej się rozmnażają!";
            case DOBRE_ZBIORY -> "\uD83C\uDF3E Dobre zbiory – pasza urosła szybciej niż zwykle";
            case CHOROBA -> "🤒 Choroba – część zwierząt zachorowała.";
            case JARMARK -> "\uD83C\uDFEA Jarmark – Sklepy mają przeceny.";
            case SPOKOJNA_NOC -> "😴 Spokojna noc – nic się nie wydarzyło.";
            case INTENSYWNA_BURZA -> "\uD83C\uDF29\uFE0F Burza Zaczęło grzmić";
            default -> type.toString();
        };
    }

    public String getEventDescription(Game.NightEventType event) {
        return switch (event) {
            case MILA_POGODA -> "Zwierzęta mają większą szansę na rozmnożenie.";
            case DOBRE_ZBIORY -> "Każdy gracz otrzymuje dodatkową paszę.";
            case CHOROBA -> "Część zwierząt może zachorować.";
            case JARMARK -> "Produkty zyskują na wartości – idealny czas by je sprzedać!";
            case SPOKOJNA_NOC -> "To była spokojna noc – nic się nie wydarzyło.";
            case INTENSYWNA_BURZA -> "Brza tak mocna, że nie przejść na rynek";
            default -> "Brak opisu.";
        };
    }





}