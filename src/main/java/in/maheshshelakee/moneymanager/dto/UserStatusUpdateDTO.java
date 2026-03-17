package in.maheshshelakee.moneymanager.dto;

import in.maheshshelakee.moneymanager.entity.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserStatusUpdateDTO {

    @NotNull(message = "Status cannot be null")
    private UserStatus status;

    @NotBlank(message = "Reason is required for status change")
    private String reason;
}
