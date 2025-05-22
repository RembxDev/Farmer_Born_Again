package org.jetbrains.rafal.farmer_born_again.Controller;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.jetbrains.rafal.farmer_born_again.Model.Event;
import org.jetbrains.rafal.farmer_born_again.Model.Game;
import org.jetbrains.rafal.farmer_born_again.Model.Player;
import org.jetbrains.rafal.farmer_born_again.Service.GameService;
import org.jetbrains.rafal.farmer_born_again.Service.PlayerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.jetbrains.rafal.farmer_born_again.Model.Game.NightEventType.*;

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
        model.addAttribute("eventName", formatEventName(game.getCurrentEvent()));
        model.addAttribute("player", player);
        model.addAttribute("game", game);
        model.addAttribute("silo", player.getSilo());
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
    public String morningPhase(HttpSession session) {
        Player player = (Player) session.getAttribute("player");
        if (player == null) {
            return "redirect:/?error=loggedOut";
        }
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

        return gameService.handleMorningPhase(player);
    }

    public String formatEventName(Game.NightEventType type) {
        if (type == null) return "Brak";
        return switch (type) {
            case MILA_POGODA -> "🌤️ Miła pogoda – zwierzęta łatwiej się rozmnażają!";
            case CHOROBA -> "🤒 Choroba – część zwierząt zachorowała.";
            case SPOKOJNA_NOC -> "😴 Spokojna noc – nic się nie wydarzyło.";

            default -> type.toString();
        };
    }

    public String getEventDescription(Game.NightEventType event) {
        return switch (event) {
            case MILA_POGODA -> "Zwierzęta mają większą szansę na rozmnożenie.";
            case DOBRE_ZBIORY -> "Każdy gracz otrzymuje dodatkową paszę.";
            case CHOROBA -> "Część zwierząt może zachorować.";
            case JARMARK -> "Produkty zyskują na wartości – idealny czas na sprzedaż!";
            case SPOKOJNA_NOC -> "To była spokojna noc – nic się nie wydarzyło.";

            default -> "Brak opisu.";
        };
    }





}