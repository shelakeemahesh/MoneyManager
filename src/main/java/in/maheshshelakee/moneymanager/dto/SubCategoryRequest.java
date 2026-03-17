package in.maheshshelakee.moneymanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubCategoryRequest {

    @NotBlank(message = "Subcategory name is required")
    private String name;

    @Builder.Default
    private String icon = "📌";

    @NotNull(message = "Category ID is required")
    private Long categoryId;
}
