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
        String gameId = game.getId();

        session.setAttribute("player", player);
        model.addAttribute("player", player);
        model.addAttribute("gameId", gameId);
        return "game/farm";
    }

}
