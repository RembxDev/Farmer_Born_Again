package org.jetbrains.rafal.farmer_born_again.Controller;

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
    public String StartGame(Model model) {

    return "farm/farm";
}

}
