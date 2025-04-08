package org.jetbrains.rafal.farmer_born_again.Controller;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
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
        String playerName = player.getName();

        if (playerName == null) {
            return "redirect:/?error=loggedOut";
        }

        String gameId = (String) session.getAttribute("gameId");
        if (gameId == null) {
            gameId = UUID.randomUUID().toString();
            session.setAttribute("gameId", gameId);
        }

        session.setAttribute("player", player);
        model.addAttribute("player", player);
        model.addAttribute("gameId", gameId);
        return "game/farm";
    }

}
