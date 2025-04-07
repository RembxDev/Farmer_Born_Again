package org.jetbrains.rafal.farmer_born_again.Service;

import lombok.AllArgsConstructor;
import org.jetbrains.rafal.farmer_born_again.Model.Player;
import org.jetbrains.rafal.farmer_born_again.Model.Product;
import org.jetbrains.rafal.farmer_born_again.Repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;

    public List<Product> getPlayerProducts(Player player) {
        return productRepository.getProductsByPlayer(player);
    }

    public List<Product> getPlayerProducts(int playerId) {
        return productRepository.getProductsByPlayer_Id(playerId);
    }

    public void addOrUpdateProduct(Product product) {
        productRepository.save(product);
    }

}
