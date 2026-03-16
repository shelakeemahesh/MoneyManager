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
    private String label;
    private String type;
    private Double amount;
    private LocalDate date;
    private String icon;
    private String note;
    private String paymentMethod;
}
