package org.jetbrains.rafal.farmer_born_again.Service;

import lombok.AllArgsConstructor;
import org.jetbrains.rafal.farmer_born_again.Model.Animal;
import org.jetbrains.rafal.farmer_born_again.Model.Player;
import org.jetbrains.rafal.farmer_born_again.Repository.AnimalRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class AnimalService {
    private final AnimalRepository animalRepository;

    public List<Animal> getPlayerAnimals(Player player) {
        return animalRepository.getAnimalByPlayer(player);
    }

    public List<Animal> getPlayerAnimals(int playerId) {
        return animalRepository.getAnimalByPlayer_Id(playerId);
    }

    public void addOrUpdateAnimal(Animal animal) {
        animalRepository.save(animal);
    }

    public void removeAnimal(Animal animal) {
        animalRepository.delete(animal);
    }

    public void removeAnimalsByName(String name) {
        List<Animal> animals = animalRepository.getAnimalByName(name);

        for (Animal animal : animals) {
            removeAnimal(animal);
        }

    }
}
