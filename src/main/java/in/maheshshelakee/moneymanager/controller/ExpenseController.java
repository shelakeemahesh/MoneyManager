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

/**
 * REST Controller for the Expense Management module.
 *
 * Base path: /api/v1.0/expense
 *
 * All endpoints require JWT authentication (handled by Spring Security filter).
 * The authenticated user's email is resolved via SecurityUtils.getCurrentUserEmail().
 */
@RestController
@RequestMapping("/expense")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    // ─── GET /expense ──────────────────────────────────────────────────────────
    // Returns all expenses for the authenticated user, sorted by date descending.

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getAll() {
        return ResponseEntity.ok(
                expenseService.getAll(SecurityUtils.getCurrentUserEmail())
        );
    }

    // ─── GET /expense/{id} ─────────────────────────────────────────────────────
    // Returns a single expense record by ID. Returns 404 if not found or not owned.

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                expenseService.getById(id, SecurityUtils.getCurrentUserEmail())
        );
    }

    // ─── POST /expense ─────────────────────────────────────────────────────────
    // Creates a new expense record. Returns 201 Created with the saved record.

    @PostMapping
    public ResponseEntity<ExpenseResponse> create(@Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(expenseService.add(request, SecurityUtils.getCurrentUserEmail()));
    }

    // ─── PUT /expense/{id} ─────────────────────────────────────────────────────
    // Updates an existing expense by ID. Returns 200 with the updated record.

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest request
    ) {
        return ResponseEntity.ok(
                expenseService.update(id, request, SecurityUtils.getCurrentUserEmail())
        );
    }

    // ─── DELETE /expense/{id} ──────────────────────────────────────────────────
    // Deletes an expense by ID. Returns 204 No Content on success.

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        expenseService.delete(id, SecurityUtils.getCurrentUserEmail());
        return ResponseEntity.noContent().build();
    }

    // ─── GET /expense/category?category=Food ──────────────────────────────────
    // Returns expenses filtered by a specific category (case-insensitive).

    @GetMapping("/category")
    public ResponseEntity<List<ExpenseResponse>> getByCategory(
            @RequestParam String category
    ) {
        return ResponseEntity.ok(
                expenseService.getByCategory(category, SecurityUtils.getCurrentUserEmail())
        );
    }

    // ─── GET /expense/date-range?startDate=2024-01-01&endDate=2024-12-31 ───────
    // Returns expenses within the given date range (inclusive), sorted by date desc.

    @GetMapping("/date-range")
    public ResponseEntity<List<ExpenseResponse>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(
                expenseService.getByDateRange(startDate, endDate, SecurityUtils.getCurrentUserEmail())
        );
    }

    // ─── GET /expense/total ────────────────────────────────────────────────────
    // Returns the sum of all expense amounts for the authenticated user.

    @GetMapping("/total")
    public ResponseEntity<Map<String, Double>> getTotal() {
        Double total = expenseService.getTotalAmount(SecurityUtils.getCurrentUserEmail());
        return ResponseEntity.ok(Map.of("totalExpense", total));
    }
}
