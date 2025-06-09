package org.jetbrains.rafal.farmer_born_again.Model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

@AllArgsConstructor
@Data
public class Event {

    public static void applyEventEffect(Game game) {
        if (game.getCurrentEvent() == null) return;

        switch (game.getCurrentEvent()) {
            case DOBRE_ZBIORY -> {
                Random random = new Random();
                for (Player p : game.getPlayers()) {
                    Map<String, Integer> silo = p.getSilo();

                    silo.put("grass", silo.getOrDefault("grass", 0) + random.nextInt(6));
                    silo.put("low_quality", silo.getOrDefault("low_quality", 0) + random.nextInt(6));
                    silo.put("medium_quality", silo.getOrDefault("medium_quality", 0) + random.nextInt(6));
                    silo.put("high_quality", silo.getOrDefault("high_quality", 0) + random.nextInt(6));
                }

            }
            case INTENSYWNA_BURZA -> game.setMarketLock(true);
            case SUSZA -> game.setPriceBonus(-30);
            case MILA_POGODA -> game.setBreedingBonus(20);
            case ZLA_POGODA -> game.setBreedingBonus(-20);
            case CHOROBA -> {
                for (Player p : game.getPlayers()) {
                    p.getAnimals().stream()
                            .filter(a -> !a.isSick())
                            .limit(p.getAnimals().size()/2)
                            .forEach(a -> a.setSick(true));
                }
            }

            case JARMARK -> game.setPriceBonus(30);

            case SPOKOJNA_NOC -> {}
        }
    }

}