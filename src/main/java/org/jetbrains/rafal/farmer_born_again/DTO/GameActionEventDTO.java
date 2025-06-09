package org.jetbrains.rafal.farmer_born_again.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameActionEventDTO {

    private String action;
    private String player;
    private Integer targetId;
    private String description;
    private Integer feedLevel;
    private Map<String, Object> extra;

}