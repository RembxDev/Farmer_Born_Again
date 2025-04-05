package org.jetbrains.rafal.farmer_born_again.Controller;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@AllArgsConstructor
@RequestMapping("/farm")
public class GameController {

    @GetMapping("/")
    public String startGame(HttpSession session, Model model) {
        String playerName = (String) session.getAttribute("playerName");
        if (playerName == null) {
            return "redirect:/";
        }

        model.addAttribute("playerName", playerName);
        return "game/farm";
    }

}
