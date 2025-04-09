package org.jetbrains.rafal.farmer_born_again.Model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Animal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private int reproductionChance;
    private int foodRequirement;
    private int sellPrice;
    private boolean isSick;
    private boolean isFed;

    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player;

    Animal(String name, int reproductionChance, int foodRequirement, int sellPrice, Player player) {
        this.name = name;
        this.reproductionChance = reproductionChance;
        this.foodRequirement = foodRequirement;
        this.sellPrice = sellPrice;
        this.isFed = true;
        this.isSick = false;
        this.player = player;
    }
}
