package in.maheshshelakee.moneymanager.controller;

import in.maheshshelakee.moneymanager.dto.SubCategoryRequest;
import in.maheshshelakee.moneymanager.dto.SubCategoryResponse;
import in.maheshshelakee.moneymanager.service.SubCategoryService;
import in.maheshshelakee.moneymanager.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subcategories")
@RequiredArgsConstructor
public class SubCategoryController {

    private final SubCategoryService subCategoryService;

    @PostMapping
    public ResponseEntity<SubCategoryResponse> create(@Valid @RequestBody SubCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(subCategoryService.create(request, SecurityUtils.getCurrentUserEmail()));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<List<SubCategoryResponse>> getByCategoryId(@PathVariable Long categoryId) {
        return ResponseEntity.ok(subCategoryService.getByCategoryId(categoryId, SecurityUtils.getCurrentUserEmail()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        subCategoryService.delete(id, SecurityUtils.getCurrentUserEmail());
        return ResponseEntity.noContent().build();
    }
}
