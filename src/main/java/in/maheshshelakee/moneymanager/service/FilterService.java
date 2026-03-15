package in.maheshshelakee.moneymanager.service;

import in.maheshshelakee.moneymanager.dto.FilterResultDTO;
import in.maheshshelakee.moneymanager.entity.ProfileEntity;
import in.maheshshelakee.moneymanager.repository.ExpenseRepository;
import in.maheshshelakee.moneymanager.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilterService {

    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final ProfileService profileService;

    @Transactional(readOnly = true)
    public List<FilterResultDTO> filter(String email, String type, LocalDate startDate, LocalDate endDate,
            String sortField, String sortOrder, String search) {

        ProfileEntity profile = profileService.getProfileByEmail(email);
        List<FilterResultDTO> results;

        if ("Income".equalsIgnoreCase(type)) {
            var incomes = (startDate != null && endDate != null)
                    ? incomeRepository.findByProfileAndDateBetweenOrderByDateDesc(profile, startDate, endDate)
                    : incomeRepository.findByProfileOrderByDateDesc(profile);

            results = incomes.stream()
                    .map(i -> FilterResultDTO.builder()
                            .id(i.getId())
                            .label(i.getSource())
                            .type("Income")
                            .amount(i.getAmount())
                            .date(i.getDate())
                            .icon(i.getIcon())
                            .build())
                    .collect(Collectors.toList());
        } else {
            // Updated to use renamed field: date → expenseDate (ExpenseEntity v2)
            var expenses = (startDate != null && endDate != null)
                    ? expenseRepository.findByProfileAndExpenseDateBetweenOrderByExpenseDateDesc(
                            profile, startDate, endDate)
                    : expenseRepository.findByProfileOrderByExpenseDateDesc(profile);

            results = expenses.stream()
                    .map(e -> FilterResultDTO.builder()
                            .id(e.getId())
                            .label(e.getTitle() != null ? e.getTitle() : e.getCategory())
                            .type("Expense")
                            .amount(e.getAmount())
                            .date(e.getExpenseDate())
                            .icon(e.getIcon())
                            .note(e.getNote())
                            .paymentMethod(e.getPaymentMethod())
                            .build())
                    .collect(Collectors.toList());
        }

        // Keyword search
        if (search != null && !search.isBlank()) {
            String q = search.toLowerCase();
            results = results.stream()
                    .filter(r -> r.getLabel() != null && r.getLabel().toLowerCase().contains(q))
                    .collect(Collectors.toList());
        }

        // Sorting
        Comparator<FilterResultDTO> comparator = "Amount".equalsIgnoreCase(sortField)
                ? Comparator.comparingDouble(r -> r.getAmount() != null ? r.getAmount() : 0)
                : Comparator.comparing(r -> r.getDate() != null ? r.getDate() : LocalDate.MIN);

        if ("Descending".equalsIgnoreCase(sortOrder)) {
            comparator = comparator.reversed();
        }

        return results.stream().sorted(comparator).collect(Collectors.toList());
    }
}
