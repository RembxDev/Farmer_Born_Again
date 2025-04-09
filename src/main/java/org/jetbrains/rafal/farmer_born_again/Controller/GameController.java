package org.jetbrains.rafal.farmer_born_again.Controller;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/farm")
public class GameController {


    private final GameService gameService;

    GameController( GameService gameservice) {

        this.gameService = gameservice;
    }

    @GetMapping("/")
    public String startGame(Model model, HttpSession session) {
        Player player = (Player) session.getAttribute("player");

        if (player == null || player.getGame() == null) {
            return "redirect:/?error=loggedOut";
        }

        Game game = player.getGame();

        model.addAttribute("player", player);
        model.addAttribute("game", game);
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
        return "game/night";
    }

    @GetMapping("/morning")
    public String morningPhase(HttpSession session, Model model) {
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




}
