package org.jetbrains.rafal.farmer_born_again.Repository;

import org.jetbrains.rafal.farmer_born_again.Model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<Game, Integer> {

}
