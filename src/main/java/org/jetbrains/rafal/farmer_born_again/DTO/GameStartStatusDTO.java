package org.jetbrains.rafal.farmer_born_again.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameStartStatusDTO {
    private String type; // np. "GAME_STARTED"
    private String message;
}
