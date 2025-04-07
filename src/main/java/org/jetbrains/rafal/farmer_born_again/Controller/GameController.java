package org.jetbrains.rafal.farmer_born_again.Controller;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Controller
@AllArgsConstructor
@RequestMapping("/farm")
public class GameController {

    @GetMapping("/")
    public String startGame(Model model, HttpSession session) {
        String playerName = (String) session.getAttribute("playerName");

        if (playerName == null) {
            return "redirect:/?error=loggedOut";
        }

        String gameId = (String) session.getAttribute("gameId");
        if (gameId == null) {
            gameId = UUID.randomUUID().toString();
            session.setAttribute("gameId", gameId);
        }

        model.addAttribute("playerName", playerName);
        model.addAttribute("gameId", gameId);
        return "game/farm";
    }
}
