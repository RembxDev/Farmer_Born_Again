package org.jetbrains.rafal.farmer_born_again.Controller;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Controller
@AllArgsConstructor
public class MenuController {

    private static final List<String> waitingPlayers = new CopyOnWriteArrayList<>();

    @GetMapping("/")
    public String play(Model model) {
        return "menu/mainMenu";
    }

    @PostMapping("/")
    public String logout() {
        return "redirect:/";
    }

    @GetMapping("/waiting")
    public String waiting(@RequestParam String playerName, HttpSession session, Model model) {

        String currentName = (String) session.getAttribute("playerName");

        if (currentName == null && session.isNew()) {
            return "redirect:/?error=loggedOut";
        }

        if (currentName != null && currentName.equals(playerName)) {
            model.addAttribute("playerName", playerName);
            model.addAttribute("players", waitingPlayers);
            return "menu/waitingRoom";
        }

        if (waitingPlayers.contains(playerName)) {
            return "redirect:/?error=loggedOut";
        }

        session.setAttribute("playerName", playerName);
        waitingPlayers.add(playerName);
        model.addAttribute("players", waitingPlayers);
        model.addAttribute("playerName", playerName);
        return "menu/waitingRoom";
    }

}
