package org.jetbrains.rafal.farmer_born_again.Controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.jetbrains.rafal.farmer_born_again.Model.Animal;
import org.jetbrains.rafal.farmer_born_again.Model.Player;
import org.jetbrains.rafal.farmer_born_again.Model.Product;
import org.jetbrains.rafal.farmer_born_again.Service.AnimalService;
import org.jetbrains.rafal.farmer_born_again.Service.GameService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/market")
public class MarketController {

    public final AnimalService animalService;

    public MarketController(final AnimalService animalService) {
        this.animalService = animalService;
    }

    @GetMapping
    public String showMarket(HttpSession session, Model model) {
        Player player = (Player) session.getAttribute("player");
        if (player == null) return "redirect:/";
        model.addAttribute("player", player);
        return "game/shop";
    }

    @PostMapping("/sell")
    public String sellProduct(@RequestParam String productName,
                              @RequestParam int quantity,
                              HttpSession session,
                              Model model) {

        Player player = (Player) session.getAttribute("player");

        Optional<Product> optional = player.getProducts().stream()
                .filter(p -> p.getName().equalsIgnoreCase(productName))
                .findFirst();

        if (optional.isEmpty()) {
            model.addAttribute("message", "Nie masz tego produktu.");
            return "game/shop";
        }

        Product product = optional.get();
        if (product.getQuantity() < quantity) {
            model.addAttribute("message", "Za mało produktu.");
            return "game/shop";
        }

        Map<String, Integer> priceMap = Map.of(
                "egg", 2,
                "wool", 4,
                "milk", 6
        );

        int earned = priceMap.getOrDefault(productName.toLowerCase(), 1) * quantity;
        product.setQuantity(product.getQuantity() - quantity);
        player.getSilo().merge("low_quality", earned, Integer::sum);

        model.addAttribute("message", "Sprzedano za " + earned + " paszy niskiej jakości.");
        return "game/shop";
    }

    @PostMapping("/buy-feed")
    public String buyFeed(@RequestParam String type,
                          @RequestParam int quantity,
                          HttpSession session,
                          Model model) {

        Player player = (Player) session.getAttribute("player");

        Map<String, String> costMap = Map.of(
                "medium_quality", "low_quality",
                "high_quality", "medium_quality"
        );

        String currency = costMap.get(type);
        if (currency == null) {
            model.addAttribute("message", "Nieznany typ paszy.");
            return "game/shop";
        }

        int available = player.getSilo().getOrDefault(currency, 0);
        if (available < quantity * 2) {
            model.addAttribute("message", "Za mało paszy: " + currency);
            return "game/shop";
        }

        player.getSilo().put(currency, available - quantity * 2);
        player.getSilo().merge(type, quantity, Integer::sum);

        model.addAttribute("message", "Kupiono " + quantity + "x " + type + " za " + (quantity * 2) + " " + currency);
        return "game/shop";
    }

    @PostMapping("/exchange")
    public String exchangeAnimal(@RequestParam String type,
                                 HttpSession session,
                                 Model model) {
        Player player = (Player) session.getAttribute("player");

        Map<String, ExchangeRule> exchangeRules = Map.of(
                "chicken", new ExchangeRule("rabbit", 5),
                "sheep", new ExchangeRule("chicken", 4),
                "cow", new ExchangeRule("sheep", 4),
                "horse", new ExchangeRule("cow", 3)
        );

        Map<String, AnimalData> animalData = Map.of(
                "rabbit", new AnimalData(80, 0, 1),
                "chicken", new AnimalData(60, 1, 4),
                "sheep", new AnimalData(50, 2, 8),
                "cow", new AnimalData(40, 3, 25),
                "horse", new AnimalData(10, 4, 40)
        );

        ExchangeRule rule = exchangeRules.get(type);
        AnimalData data = animalData.get(type);

        if (rule == null || data == null) {
            model.addAttribute("message", "Nieznana wymiana.");
            return "game/shop";
        }

        long count = player.getAnimals().stream()
                .filter(a -> a.getName().equalsIgnoreCase(rule.source))
                .count();

        if (count < rule.required) {
            model.addAttribute("message", "Za mało " + rule.source + " do wymiany.");
            return "game/shop";
        }

        int removed = 0;
        Iterator<Animal> it = player.getAnimals().iterator();
        while (it.hasNext() && removed < rule.required) {
            Animal a = it.next();
            if (a.getName().equalsIgnoreCase(rule.source)) {
                it.remove();
                removed++;
            }
        }

        Animal newAnimal = animalService.createAnimal(
                type,
                data.reproductionChance(),
                data.foodRequirement(),
                data.sellPrice(),
                player
        );

        player.getAnimals().add(newAnimal);

        model.addAttribute("message", "Wymieniono na nowe zwierzę: " + type);
        return "game/shop";
    }

    @PostMapping("/sell-animal")
    public String sellAnimal(@RequestParam String type,
                             HttpSession session,
                             Model model) {
        Player player = (Player) session.getAttribute("player");

        Map<String, AnimalData> animalData = Map.of(
                "rabbit", new AnimalData(80, 0, 1),
                "chicken", new AnimalData(60, 1, 4),
                "sheep", new AnimalData(50, 2, 8),
                "cow", new AnimalData(40, 3, 25),
                "horse", new AnimalData(10, 4, 40)
        );

        AnimalData data = animalData.get(type);
        if (data == null) {
            model.addAttribute("message", "Nieznane zwierzę.");
            return "game/shop";
        }

        Optional<Animal> optional = player.getAnimals().stream()
                .filter(a -> a.getName().equalsIgnoreCase(type))
                .findFirst();

        if (optional.isEmpty()) {
            model.addAttribute("message", "Nie masz takiego zwierzęcia.");
            return "game/shop";
        }

        player.getAnimals().remove(optional.get());

        String feedType = switch (type.toLowerCase()) {
            case "rabbit", "chicken" -> "low_quality";
            case "sheep" -> "medium_quality";
            case "cow", "horse" -> "high_quality";
            default -> "low_quality";
        };

        player.getSilo().merge(feedType, data.sellPrice(), Integer::sum);

        model.addAttribute("message", "Sprzedano " + type + " za " + data.sellPrice() + " (" + feedType + ")");
        return "game/shop";
    }


    private record ExchangeRule(String source, int required) {}
    private record AnimalData(int reproductionChance, int foodRequirement, int sellPrice) {}


}
