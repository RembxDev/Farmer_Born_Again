package org.jetbrains.rafal.farmer_born_again.Repository;


import org.jetbrains.rafal.farmer_born_again.Model.Player;
import org.jetbrains.rafal.farmer_born_again.Model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    List<Product> getProductsByPlayer(Player player);

    List<Product> getProductsByPlayer_Id(int playerId);
}
