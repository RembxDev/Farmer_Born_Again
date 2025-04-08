package org.jetbrains.rafal.farmer_born_again.Repository;


import org.jetbrains.rafal.farmer_born_again.Model.Player;
import org.jetbrains.rafal.farmer_born_again.Model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    List<Product> getProductsByPlayer(Player player);

    List<Product> getProductsByPlayer_Id(int playerId);
}
