package org.jetbrains.rafal.farmer_born_again.Controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.jetbrains.rafal.farmer_born_again.Model.Animal;
import org.jetbrains.rafal.farmer_born_again.Model.Player;
import org.jetbrains.rafal.farmer_born_again.Model.Product;
import org.jetbrains.rafal.farmer_born_again.Service.AnimalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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

        }

        Product product = optional.get();
        if (product.getQuantity() < quantity) {
            model.addAttribute("message", "Za mało produktu.");
            model.addAttribute("player", player);
            return "game/shop";
        }

        Map<String, Integer> priceMap = Map.of(
                "egg", 2,
                "wool", 4,
                "milk", 6
        );

        int basePrice = priceMap.getOrDefault(productName.toLowerCase(), 1);
        int bonus = player.getGame().getPriceBonus();
        int unitPrice = applyPriceBonus(basePrice, bonus);
        int earned = unitPrice * quantity;

        product.setQuantity(product.getQuantity() - quantity);
        player.getSilo().merge("low_quality", earned, Integer::sum);

        model.addAttribute("message", "Sprzedano za " + earned + " paszy niskiej jakości.");
        model.addAttribute("player", player);
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
            model.addAttribute("player", player);
            return "game/shop";
        }

        int available = player.getSilo().getOrDefault(currency, 0);
        if (available < quantity * 2) {
            model.addAttribute("message", "Za mało paszy: " + currency);
            model.addAttribute("player", player);
            return "game/shop";
        }

        player.getSilo().put(currency, available - quantity * 2);
        player.getSilo().merge(type, quantity, Integer::sum);

        model.addAttribute("message", "Kupiono " + quantity + "x " + type + " za " + (quantity * 2) + " " + currency);
        model.addAttribute("player", player);
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
            model.addAttribute("player", player);
            return "game/shop";
        }

        long count = player.getAnimals().stream()
                .filter(a -> a.getName().equalsIgnoreCase(rule.source))
                .count();

        if (count < rule.required) {
            model.addAttribute("message", "Za mało " + rule.source + " do wymiany.");
            model.addAttribute("player", player);
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
        model.addAttribute("player", player);
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
            model.addAttribute("player", player);
            return "game/shop";
        }

        Optional<Animal> optional = player.getAnimals().stream()
                .filter(a -> a.getName().equalsIgnoreCase(type))
                .findFirst();

        if (optional.isEmpty()) {
            model.addAttribute("message", "Nie masz takiego zwierzęcia.");
            model.addAttribute("player", player);
            return "game/shop";
        }

        player.getAnimals().remove(optional.get());

        String feedType = switch (type.toLowerCase()) {
            case "rabbit", "chicken" -> "low_quality";
            case "sheep" -> "medium_quality";
            case "cow", "horse" -> "high_quality";
            default -> "low_quality";
        };

        int bonus = player.getGame().getPriceBonus();
        int price = applyPriceBonus(data.sellPrice(), bonus);
        player.getSilo().merge(feedType, price, Integer::sum);

        model.addAttribute("message", "Sprzedano " + type + " za " + price + " (" + feedType + ")");
        model.addAttribute("player", player);
        return "game/shop";
    }

    @PostMapping("/sell-product")
    public String sellProduct(@RequestParam String type, HttpSession session, Model model) {
        Player player = (Player) session.getAttribute("player");

        Map<String, Integer> productToFeedMap = Map.of(
                "egg", 2,
                "wool", 4,
                "milk", 6
        );

        Map<String, String> feedTypeMap = Map.of(
                "egg", "low_quality",
                "wool", "medium_quality",
                "milk", "high_quality"
        );

        Optional<Product> productOpt = player.getProducts().stream()
                .filter(p -> p.getName().equalsIgnoreCase(type) && p.getQuantity() > 0)
                .findFirst();

        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setQuantity(product.getQuantity() - 1);
            if (product.getQuantity() <= 0) {
                player.getProducts().remove(product);
            }

            int base = productToFeedMap.get(type);
            int bonus = player.getGame().getPriceBonus();
            int modifiedAmount = applyPriceBonus(base, bonus);

            String feedType = feedTypeMap.get(type);
            player.getSilo().merge(feedType, modifiedAmount, Integer::sum);

            model.addAttribute("message", "✅ Sprzedano " + type + " za " + modifiedAmount + " " + feedType.replace("_", " ") + " paszy.");
        } else {
            model.addAttribute("message", "❌ Brak dostępnego produktu: " + type);
        }

        model.addAttribute("player", player);
        return "game/shop";
    }

    private int applyPriceBonus(int basePrice, int bonusPercent) {
        int modifiedPrice = (int) Math.round(basePrice * (1 + bonusPercent / 100.0));
        return Math.max(1, modifiedPrice);
    }

    private record ExchangeRule(String source, int required) {}
    private record AnimalData(int reproductionChance, int foodRequirement, int sellPrice) {}
}
