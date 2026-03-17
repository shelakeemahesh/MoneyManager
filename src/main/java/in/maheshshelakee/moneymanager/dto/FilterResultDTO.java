package in.maheshshelakee.moneymanager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilterResultDTO {
    private Long id;
    private String label;        // source (income) or title (expense)
    private String type;         // "Income" or "Expense"
    private Double amount;
    private LocalDate date;
    private String icon;
    private String note;         // expense only
    private String paymentMethod; // expense only
}
