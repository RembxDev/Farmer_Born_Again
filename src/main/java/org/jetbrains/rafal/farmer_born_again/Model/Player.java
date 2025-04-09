package org.jetbrains.rafal.farmer_born_again.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Player implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;

    @ElementCollection
    @CollectionTable(name = "silo", joinColumns = @JoinColumn(name = "player_id"))
    @MapKeyColumn(name = "type")
    @Column(name = "quantity")
    private Map<String, Integer> silo;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Animal> animals;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;

    @Transient
    private boolean ready;

    public Player(String name){
        this.name = name;
        this.silo = new HashMap<>();
        this.silo.put("low_quality", 5);
        this.silo.put("medium_quality", 0);
        this.silo.put("high_quality", 0);
        this.animals = new ArrayList<>();
        this.products = new ArrayList<>();
        this.ready = false;
    }
}



