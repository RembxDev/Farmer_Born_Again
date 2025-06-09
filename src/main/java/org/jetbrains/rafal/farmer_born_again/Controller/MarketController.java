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
        HealingCostResult healingCostResult = calculateHealingCosts(player);
        model.addAttribute("player", player);
        model.addAttribute("sickAnimalCount", healingCostResult.sickAnimalCount());
        model.addAttribute("healingCosts", healingCostResult.costs());
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
            HealingCostResult healingCostResult = calculateHealingCosts(player);
            model.addAttribute("player", player);
            model.addAttribute("sickAnimalCount", healingCostResult.sickAnimalCount());
            model.addAttribute("healingCosts", healingCostResult.costs());
            return "game/shop";
        }

        Product product = optional.get();
        if (product.getQuantity() < quantity) {
            HealingCostResult healingCostResult = calculateHealingCosts(player);
            model.addAttribute("player", player);
            model.addAttribute("sickAnimalCount", healingCostResult.sickAnimalCount());
            model.addAttribute("healingCosts", healingCostResult.costs());
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
        HealingCostResult healingCostResult = calculateHealingCosts(player);
        model.addAttribute("player", player);
        model.addAttribute("sickAnimalCount", healingCostResult.sickAnimalCount());
        model.addAttribute("healingCosts", healingCostResult.costs());
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
            HealingCostResult healingCostResult = calculateHealingCosts(player);
            model.addAttribute("player", player);
            model.addAttribute("sickAnimalCount", healingCostResult.sickAnimalCount());
            model.addAttribute("healingCosts", healingCostResult.costs());
            return "game/shop";
        }

        int available = player.getSilo().getOrDefault(currency, 0);
        if (available < quantity * 2) {
            model.addAttribute("message", "Za mało paszy: " + currency);
            HealingCostResult healingCostResult = calculateHealingCosts(player);
            model.addAttribute("player", player);
            model.addAttribute("sickAnimalCount", healingCostResult.sickAnimalCount());
            model.addAttribute("healingCosts", healingCostResult.costs());
            return "game/shop";
        }

        player.getSilo().put(currency, available - quantity * 2);
        player.getSilo().merge(type, quantity, Integer::sum);

        model.addAttribute("message", "Kupiono " + quantity + "x " + type + " za " + (quantity * 2) + " " + currency);
        HealingCostResult healingCostResult = calculateHealingCosts(player);
        model.addAttribute("player", player);
        model.addAttribute("sickAnimalCount", healingCostResult.sickAnimalCount());
        model.addAttribute("healingCosts", healingCostResult.costs());
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
                "rabbit", new AnimalData(80, 0, 1, 1, "low_quality"),
                "chicken", new AnimalData(60, 1, 4, 2, "low_quality"),
                "sheep", new AnimalData(50, 2, 8, 4, "medium_quality"),
                "cow", new AnimalData(40, 3, 25, 13, "high_quality"),
                "horse", new AnimalData(10, 3, 40, 20, "high_quality")
        );

        ExchangeRule rule = exchangeRules.get(type);
        AnimalData data = animalData.get(type);

        if (rule == null || data == null) {
            model.addAttribute("message", "Nieznana wymiana.");
            HealingCostResult healingCostResult = calculateHealingCosts(player);
            model.addAttribute("player", player);
            model.addAttribute("sickAnimalCount", healingCostResult.sickAnimalCount());
            model.addAttribute("healingCosts", healingCostResult.costs());
            return "game/shop";
        }

        long count = player.getAnimals().stream()
                .filter(a -> a.getName().equalsIgnoreCase(rule.source))
                .count();

        if (count < rule.required) {
            model.addAttribute("message", "Za mało " + rule.source + " do wymiany.");
            HealingCostResult healingCostResult = calculateHealingCosts(player);
            model.addAttribute("player", player);
            model.addAttribute("sickAnimalCount", healingCostResult.sickAnimalCount());
            model.addAttribute("healingCosts", healingCostResult.costs());
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
        HealingCostResult healingCostResult = calculateHealingCosts(player);
        model.addAttribute("player", player);
        model.addAttribute("sickAnimalCount", healingCostResult.sickAnimalCount());
        model.addAttribute("healingCosts", healingCostResult.costs());
        return "game/shop";
    }

    @PostMapping("/sell-animal")
    public String sellAnimal(@RequestParam String type,
                             HttpSession session,
                             Model model) {
        Player player = (Player) session.getAttribute("player");

        Map<String, AnimalData> animalData = Map.of(
                "rabbit", new AnimalData(80, 0, 1, 1, "low_quality"),
                "chicken", new AnimalData(60, 1, 4, 2, "low_quality"),
                "sheep", new AnimalData(50, 2, 8, 4, "medium_quality"),
                "cow", new AnimalData(40, 3, 25, 13, "high_quality"),
                "horse", new AnimalData(10, 3, 40, 20, "high_quality")
        );

        AnimalData data = animalData.get(type);
        if (data == null) {
            model.addAttribute("message", "Nieznane zwierzę.");
            model.addAttribute("player", player);
            model.addAttribute("healingCosts", calculateHealingCosts(player));
            return "game/shop";
        }

        Optional<Animal> optional = player.getAnimals().stream()
                .filter(a -> a.getName().equalsIgnoreCase(type))
                .findFirst();

        if (optional.isEmpty()) {
            model.addAttribute("message", "Nie masz takiego zwierzęcia.");
            HealingCostResult healingCostResult = calculateHealingCosts(player);
            model.addAttribute("player", player);
            model.addAttribute("sickAnimalCount", healingCostResult.sickAnimalCount());
            model.addAttribute("healingCosts", healingCostResult.costs());
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
        HealingCostResult healingCostResult = calculateHealingCosts(player);
        model.addAttribute("player", player);
        model.addAttribute("sickAnimalCount", healingCostResult.sickAnimalCount());
        model.addAttribute("healingCosts", healingCostResult.costs());
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

        HealingCostResult healingCostResult = calculateHealingCosts(player);
        model.addAttribute("player", player);
        model.addAttribute("sickAnimalCount", healingCostResult.sickAnimalCount());
        model.addAttribute("healingCosts", healingCostResult.costs());
        return "game/shop";
    }

    @PostMapping("/buy-dog")
    public String buyDog(@RequestParam String type, HttpSession session, Model model) {
        Player player = (Player) session.getAttribute("player");

        Map<String, DogData> dogData = Map.of(
                "small_dog", new DogData(10, "Mały pies"),
                "big_dog", new DogData(20, "Duży pies")
        );

        DogData data = dogData.get(type);
        if (data == null) {
            model.addAttribute("message", "❌ Nieznany typ psa.");
            HealingCostResult healingCostResult = calculateHealingCosts(player);
            model.addAttribute("player", player);
            model.addAttribute("sickAnimalCount", healingCostResult.sickAnimalCount());
            model.addAttribute("healingCosts", healingCostResult.costs());
            return "game/shop";
        }

        boolean alreadyOwned = ("small_dog".equals(type) && player.isSmallDog()) || ("big_dog".equals(type) && player.isBigDog());
        if (alreadyOwned) {
            model.addAttribute("message", "❌ Już posiadasz " + data.name + "!");
            HealingCostResult healingCostResult = calculateHealingCosts(player);
            model.addAttribute("player", player);
            model.addAttribute("sickAnimalCount", healingCostResult.sickAnimalCount());
            model.addAttribute("healingCosts", healingCostResult.costs());
            return "game/shop";
        }

        int highQualityFeed = player.getSilo().getOrDefault("high_quality", 0);
        if (highQualityFeed < data.cost) {
            model.addAttribute("message", "❌ Za mało paszy wysokiej jakości.");
            HealingCostResult healingCostResult = calculateHealingCosts(player);
            model.addAttribute("player", player);
            model.addAttribute("sickAnimalCount", healingCostResult.sickAnimalCount());
            model.addAttribute("healingCosts", healingCostResult.costs());
            return "game/shop";
        }

        player.getSilo().put("high_quality", highQualityFeed - data.cost);
        if ("small_dog".equals(type)) {
            player.setSmallDog(true);
        } else {
            player.setBigDog(true);
        }

        model.addAttribute("message", "✅ Zakupiono " + data.name + " za " + data.cost + " paszy wysokiej jakości!");
        HealingCostResult healingCostResult = calculateHealingCosts(player);
        model.addAttribute("player", player);
        model.addAttribute("sickAnimalCount", healingCostResult.sickAnimalCount());
        model.addAttribute("healingCosts", healingCostResult.costs());
        return "game/shop";
    }

    @PostMapping("/heal-animals")
    public String healAnimals(HttpSession session, Model model) {
        Player player = (Player) session.getAttribute("player");

        Map<String, AnimalData> animalData = Map.of(
                "rabbit", new AnimalData(80, 0, 1, 1, "low_quality"),
                "chicken", new AnimalData(60, 1, 4, 2, "low_quality"),
                "sheep", new AnimalData(50, 2, 8, 4, "medium_quality"),
                "cow", new AnimalData(40, 3, 25, 13, "high_quality"),
                "horse", new AnimalData(10, 3, 40, 20, "high_quality")
        );

        Map<String, Integer> healingCosts = new HashMap<>();
        long sickAnimalCount = player.getAnimals().stream()
                .filter(Animal::isSick)
                .peek(animal -> {
                    AnimalData data = animalData.get(animal.getName().toLowerCase());
                    if (data != null) {
                        int cost = applyPriceBonus(data.healingCost(), player.getGame().getPriceBonus());
                        healingCosts.merge(data.feedType(), cost, Integer::sum);
                    }
                })
                .count();

        if (sickAnimalCount == 0) {
            model.addAttribute("message", "❌ Brak chorych zwierząt do wyleczenia.");
            model.addAttribute("player", player);
            model.addAttribute("sickAnimalCount", 0);
            model.addAttribute("healingCosts", new HashMap<String, Integer>());
            return "game/shop";
        }

        for (Map.Entry<String, Integer> entry : healingCosts.entrySet()) {
            String feedType = entry.getKey();
            int required = entry.getValue();
            int available = player.getSilo().getOrDefault(feedType, 0);
            if (available < required) {
                model.addAttribute("message", "❌ Za mało paszy: " + feedType.replace("_", " ") + ". Potrzeba: " + required);
                model.addAttribute("player", player);
                model.addAttribute("sickAnimalCount", sickAnimalCount);
                model.addAttribute("healingCosts", healingCosts);
                return "game/shop";
            }
        }

        for (Map.Entry<String, Integer> entry : healingCosts.entrySet()) {
            String feedType = entry.getKey();
            int required = entry.getValue();
            player.getSilo().merge(feedType, -required, Integer::sum);
        }
        player.getAnimals().stream()
                .filter(Animal::isSick)
                .forEach(animal -> animal.setSick(false));

        StringBuilder costMessage = new StringBuilder();
        healingCosts.forEach((feedType, amount) ->
                costMessage.append(amount).append(" ").append(feedType.replace("_", " ")).append(", "));
        if (costMessage.length() > 0) {
            costMessage.setLength(costMessage.length() - 2);
        }

        model.addAttribute("message", "✅ Wyleczono " + sickAnimalCount + " zwierząt za " + costMessage + " paszy!");
        model.addAttribute("player", player);
        model.addAttribute("sickAnimalCount", 0);
        model.addAttribute("healingCosts", new HashMap<String, Integer>());
        return "game/shop";
    }


    private HealingCostResult  calculateHealingCosts(Player player) {
        Map<String, AnimalData> animalData = Map.of(
                "rabbit", new AnimalData(80, 0, 1, 1, "low_quality"),
                "chicken", new AnimalData(60, 1, 4, 2, "low_quality"),
                "sheep", new AnimalData(50, 2, 8, 4, "medium_quality"),
                "cow", new AnimalData(40, 3, 25, 13, "high_quality"),
                "horse", new AnimalData(10, 3, 40, 20, "high_quality")
        );

        Map<String, Integer> healingCosts = new HashMap<>();
        long sickAnimalCount = player.getAnimals().stream()
                .filter(Animal::isSick)
                .peek(animal -> {
                    AnimalData data = animalData.get(animal.getName().toLowerCase());
                    if (data != null) {
                        int cost = applyPriceBonus(data.healingCost(), player.getGame().getPriceBonus());
                        healingCosts.merge(data.feedType(), cost, Integer::sum);
                    }
                })
                .count();

        return new HealingCostResult(sickAnimalCount, healingCosts);
    }

    private int applyPriceBonus(int basePrice, int bonusPercent) {
        int modifiedPrice = (int) Math.round(basePrice * (1 + bonusPercent / 100.0));
        return Math.max(1, modifiedPrice);
    }

    private record ExchangeRule(String source, int required) {}
    private record AnimalData(int reproductionChance, int foodRequirement, int sellPrice, int healingCost, String feedType) {}
    private record DogData(int cost, String name) {}
    private record HealingCostResult(long sickAnimalCount, Map<String, Integer> costs) {}
}
