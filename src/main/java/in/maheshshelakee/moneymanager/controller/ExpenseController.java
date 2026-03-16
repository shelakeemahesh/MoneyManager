package in.maheshshelakee.moneymanager.controller;

import in.maheshshelakee.moneymanager.dto.ExpenseRequest;
import in.maheshshelakee.moneymanager.dto.ExpenseResponse;
import in.maheshshelakee.moneymanager.service.ExpenseService;
import in.maheshshelakee.moneymanager.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/expense")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getAll() {
        return ResponseEntity.ok(expenseService.getAll(SecurityUtils.getCurrentUserEmail()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.getById(id, SecurityUtils.getCurrentUserEmail()));
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> create(@Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(expenseService.add(request, SecurityUtils.getCurrentUserEmail()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> update(@PathVariable Long id, @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.ok(expenseService.update(id, request, SecurityUtils.getCurrentUserEmail()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        expenseService.delete(id, SecurityUtils.getCurrentUserEmail());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/category")
    public ResponseEntity<List<ExpenseResponse>> getByCategory(@RequestParam String category) {
        return ResponseEntity.ok(expenseService.getByCategory(category, SecurityUtils.getCurrentUserEmail()));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<ExpenseResponse>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(expenseService.getByDateRange(startDate, endDate, SecurityUtils.getCurrentUserEmail()));
    }

    @GetMapping("/total")
    public ResponseEntity<Map<String, Double>> getTotal() {
        Double total = expenseService.getTotalAmount(SecurityUtils.getCurrentUserEmail());
        return ResponseEntity.ok(Map.of("totalExpense", total));
    }
}
