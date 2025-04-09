package org.jetbrains.rafal.farmer_born_again.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private int tureNumber;
    @Enumerated(EnumType.STRING)
    private Phase currentPhase;
    private String currentEvent;
    private boolean started;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
    private List<Player> players;


    public enum Phase {
        MORNING, DAY, NIGHT
    }

    public void changeCycle() {
        if(currentPhase.equals(Phase.MORNING)) {
            currentPhase = Phase.DAY;
        } else  if(currentPhase.equals(Phase.DAY)) {
            currentPhase = Phase.NIGHT;
        } else {
            currentPhase = Phase.MORNING;
        }
    }
}
