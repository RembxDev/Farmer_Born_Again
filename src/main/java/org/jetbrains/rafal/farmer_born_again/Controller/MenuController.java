package org.jetbrains.rafal.farmer_born_again.Controller;

import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Controller
public class MenuController {

    private final PlayerService playerService;
    private final GameService gameService;

    MenuController(PlayerService playerservice, GameService gameservice) {
        this.playerService = playerservice;
        this.gameService = gameservice;
    }

    @GetMapping("/")
    public String play() {
        return "menu/mainMenu";
    }

    @PostMapping("/")
    public String logout() {
        return "redirect:/";
    }

    @GetMapping("/waiting")
    public String waiting(@RequestParam String playerName, HttpSession session) {
        if (session.getAttribute("player") == null) {
            Player player = new Player(playerName);
            session.setAttribute("player", player);
        }
        return "redirect:/waitingRoom";
    }

    @GetMapping("/waitingRoom")
    public String waitingRoom(HttpSession session, Model model) {
        Player player = (Player) session.getAttribute("player");
        if (player == null) return "redirect:/";

        Game game = gameService.assignPlayerToGame(player);

        model.addAttribute("playerName", player.getName());
        model.addAttribute("players", game.getPlayers());
        return "menu/waitingRoom";
    }

    @PostMapping("/ready")
    public ResponseEntity<Void> playerReady(HttpSession session) {
        Player player = (Player) session.getAttribute("player");
        if (player == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        gameService.markPlayerReadyAndStartIfPossible(player);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/leave")
    public String leave(HttpSession session) {
        Player player = (Player) session.getAttribute("player");

        if (player != null) {
            gameService.removePlayerFromGame(player);
        }
        session.invalidate();
        return "redirect:/logout";
    }
}
