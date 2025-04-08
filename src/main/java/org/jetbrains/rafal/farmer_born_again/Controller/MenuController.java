package org.jetbrains.rafal.farmer_born_again.Controller;

import jakarta.servlet.http.HttpServletRequest;
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
    public String waiting(@RequestParam String playerName, HttpSession session, Model model, HttpServletRequest request) {

        String currentName = (String) session.getAttribute("playerName");

        if (currentName == null) {
            if (waitingPlayers.contains(playerName)) {
                return "redirect:/?error=loggedOut";
            }
            session.setAttribute("playerName", playerName);
            waitingPlayers.add(playerName);
            currentName = playerName;
        }

        model.addAttribute("_csrf", request.getAttribute("_csrf"));
        model.addAttribute("playerName", currentName);
        model.addAttribute("players", waitingPlayers);
        return "menu/waitingRoom";
    }

    @PostMapping("/leave")
    public String leave(HttpSession session) {
        String playerName = (String) session.getAttribute("playerName");
        if (playerName != null) {
            waitingPlayers.remove(playerName);
        }
        session.invalidate();
        return "redirect:/logout";
    }
}
