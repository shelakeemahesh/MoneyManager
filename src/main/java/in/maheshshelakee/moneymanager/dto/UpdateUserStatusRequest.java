package in.maheshshelakee.moneymanager.dto;

import in.maheshshelakee.moneymanager.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserStatusRequest {
    private UserStatus status;
}
