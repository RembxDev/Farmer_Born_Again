package org.jetbrains.rafal.farmer_born_again.Model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
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


    Animal(String name, int RC, int FR, int sellPrice)
    {
        this.name = name;
        this.reproductionChance = RC;
        this.foodRequirement = FR;
        this.sellPrice = sellPrice;
        this.isFed = true;
        this.isSick = false;

    }
}
