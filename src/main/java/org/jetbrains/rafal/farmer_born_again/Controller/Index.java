package org.jetbrains.rafal.farmer_born_again.Controller;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Controller
@AllArgsConstructor
public class Index {

    private static final List<String> waitingPlayers = new CopyOnWriteArrayList<>();

    @GetMapping("/")
    public String Play(Model model) {

        return "menu/mainMenu";
    }

    @GetMapping("/weiting")
    public String Waiting(@RequestParam String playerName, HttpSession session, Model model) {

        session.setAttribute("playerName", playerName);

        if (!waitingPlayers.contains(playerName)) {
            waitingPlayers.add(playerName);
        }

        model.addAttribute("players", waitingPlayers);
        return "menu/waitingRoom";
    }

    @GetMapping("/leave")
    public String leave(HttpSession session) {
        String name = (String) session.getAttribute("playerName");
        if (name != null) {
            waitingPlayers.remove(name);
        }
        session.invalidate();
        return "redirect:/";
    }

}
