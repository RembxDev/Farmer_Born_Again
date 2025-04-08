package org.jetbrains.rafal.farmer_born_again.Repository;

import org.jetbrains.rafal.farmer_born_again.Model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Integer> {

}
