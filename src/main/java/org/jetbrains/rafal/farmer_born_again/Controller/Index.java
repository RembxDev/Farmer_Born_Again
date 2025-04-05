package org.jetbrains.rafal.farmer_born_again.Controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@AllArgsConstructor
public class Index {

    @GetMapping("/")
    public String Play(Model model) {

        return "menu/mainMenu";
    }
}
