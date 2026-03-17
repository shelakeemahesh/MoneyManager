package in.maheshshelakee.moneymanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Incoming request payload for creating or updating an expense.
 * All required fields are validated with Bean Validation annotations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be a positive number")
    private Double amount;

    @NotBlank(message = "Category is required")
    private String category;

    private String note;

    @NotNull(message = "Expense date is required")
    private LocalDate expenseDate;

    /**
     * Accepted values: CASH, CARD, UPI, BANK_TRANSFER, OTHER
     */
    @NotBlank(message = "Payment method is required")
    @Pattern(
        regexp = "^(CASH|CARD|UPI|BANK_TRANSFER|OTHER)$",
        message = "Payment method must be one of: CASH, CARD, UPI, BANK_TRANSFER, OTHER"
    )
    private String paymentMethod;

    /**
     * Optional emoji icon. Defaults to 🛒 in service layer if not provided.
     */
    private String icon;
}
