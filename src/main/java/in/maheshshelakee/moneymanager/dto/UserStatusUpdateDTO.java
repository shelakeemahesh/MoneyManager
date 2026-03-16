package in.maheshshelakee.moneymanager.dto;

import in.maheshshelakee.moneymanager.entity.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// FIX: This is the single DTO for status updates — includes both status and reason.
//      The duplicate UpdateUserStatusRequest (status only, no reason) has been deleted.
@Data
public class UserStatusUpdateDTO {

    @NotNull(message = "Status cannot be null")
    private UserStatus status;

    @NotBlank(message = "Reason is required for status change")
    private String reason;
}
