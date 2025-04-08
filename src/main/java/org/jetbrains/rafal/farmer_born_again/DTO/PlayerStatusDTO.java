    package org.jetbrains.rafal.farmer_born_again.DTO;

    import lombok.AllArgsConstructor;
    import lombok.Data;
    import lombok.NoArgsConstructor;

    import java.io.Serializable;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class PlayerStatusDTO implements Serializable {

        private String playerName;
        private String status;
    }
