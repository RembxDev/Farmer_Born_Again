package org.jetbrains.rafal.farmer_born_again.Service;

import lombok.AllArgsConstructor;
import org.jetbrains.rafal.farmer_born_again.Repository.PlayerRepository;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class PlayerService {
    private final PlayerRepository playerRepository;

}
