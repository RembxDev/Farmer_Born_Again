package org.jetbrains.rafal.farmer_born_again.DTO;




import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDTO {

    private String type;
    private String sender;
    private String content;
    private LocalDateTime timestamp;

}