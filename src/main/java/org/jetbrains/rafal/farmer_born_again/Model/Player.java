package org.jetbrains.rafal.farmer_born_again.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player {
    private Map<String, Integer> silo;
    private String name;
    private List<Animal> animals;

}
