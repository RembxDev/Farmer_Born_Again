package org.jetbrains.rafal.farmer_born_again.Service;

import lombok.AllArgsConstructor;
import org.jetbrains.rafal.farmer_born_again.Model.Animal;
import org.jetbrains.rafal.farmer_born_again.Model.Player;
import org.jetbrains.rafal.farmer_born_again.Repository.AnimalRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@AllArgsConstructor
@Service
public class AnimalService {
    private final AnimalRepository animalRepository;
    private static int animalIdSequence = 1;

    public synchronized int generateUniqueAnimalId() {
        return animalIdSequence++;
    }

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

    public List<String> updateFeedingLevels(Player player) {
        List<String> logs = new ArrayList<>();
        List<Animal> animals = player.getAnimals();

        for (Animal animal : animals) {
            int loss = 1;
            int before = animal.getFeedLevel();
            int after = Math.max(0, before - loss);

            animal.setFeedLevel(after);

            if (after <= 1) {
                logs.add("⚠️ " + animal.getName() + " jest bardzo głodny!");
            } else {
                logs.add("🍽️ " + animal.getName() + " stracił " + loss + " poziom(y) sytości (" + before + " -> " + after + ")");
            }
        }
        return logs;
    }

    public String feedAnimalById(Player player, Long animalId) {
        List<Animal> animals = player.getAnimals();
        Animal target = null;

        for (Animal animal : animals) {
            if (animal.getId() != null && animal.getId().equals(animalId)) {
                target = animal;
                break;
            }
        }

        if (target == null) {
            return "❌ Zwierzę o ID " + animalId + " nie znalezione u gracza.";
        }

        int before = target.getFeedLevel();
        if (before >= 5) {
            return "ℹ️ " + target.getName() + " jest już najedzony (poziom 5)";
        }

        target.setFeedLevel(before + 1);
        return "✅ " + target.getName() + " został nakarmiony (" + before + " → " + (before + 1) + ")";
    }

    public Animal createAnimal(String name, int reproductionChance, int foodRequirement, int sellPrice, Player player) {
        Animal animal = new Animal();
        animal.setId(generateUniqueAnimalId());
        animal.setName(name);
        animal.setReproductionChance(reproductionChance);
        animal.setFoodRequirement(foodRequirement);
        animal.setSellPrice(sellPrice);
        animal.setSick(false);
        animal.setFed(false);
        animal.setFeedLevel(3);
        animal.setPlayer(player);
        return animal;
    }


}


