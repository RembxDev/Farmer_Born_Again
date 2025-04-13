package org.jetbrains.rafal.farmer_born_again.Model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.function.Consumer;

@AllArgsConstructor
@Data
public class Event {

    public static void applyEventEffect(Game game) {
        if (game.getCurrentEvent() == null) return;

        switch (game.getCurrentEvent()) {
            case DOBRE_ZBIORY -> {}
            case POPYT_NA_PRODUKT -> {}
            case WYPRZEDAZ -> {}
            case INTENSYWNA_BURZA -> {}
            case SUSZA -> {}
            case MILA_POGODA -> game.setBreedingBonus(20);
            case ZLA_POGODA -> game.setBreedingBonus(-20);
            case CHOROBA -> {
                for (Player p : game.getPlayers()) {
                    p.getAnimals().stream()
                            .filter(a -> !a.isSick())
                            .limit(2)
                            .forEach(a -> a.setSick(true));
                }
            }
            case JARMARK -> game.setPriceBonus(30);

            case SPOKOJNA_NOC -> {}
        }
    }

}
