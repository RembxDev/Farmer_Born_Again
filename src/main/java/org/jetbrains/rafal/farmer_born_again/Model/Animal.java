package org.jetbrains.rafal.farmer_born_again.Model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Animal {
    private int id;
    private String name;
    private int reproductionChance;
    private int foodRequirement;
    private boolean isSick;

}
