package org.jetbrains.rafal.farmer_born_again.Repository;

import org.jetbrains.rafal.farmer_born_again.Model.Animal;
import org.jetbrains.rafal.farmer_born_again.Model.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnimalRepository extends JpaRepository<Animal, Integer> {

    List<Animal> getAnimalByPlayer(Player player);

    List<Animal> getAnimalByPlayer_Id(int playerId);

    List<Animal> getAnimalByName(String name);
}
