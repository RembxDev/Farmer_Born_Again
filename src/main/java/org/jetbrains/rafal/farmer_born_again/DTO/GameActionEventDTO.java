package org.jetbrains.rafal.farmer_born_again.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameActionEventDTO {

    private String action;
    private String player;
    private Long targetId;
    private String description;
    private Integer feedLevel;
}
