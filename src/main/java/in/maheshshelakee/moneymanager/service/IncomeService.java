package in.maheshshelakee.moneymanager.service;

import in.maheshshelakee.moneymanager.dto.IncomeDTO;
import in.maheshshelakee.moneymanager.entity.IncomeEntity;
import in.maheshshelakee.moneymanager.entity.ProfileEntity;
import in.maheshshelakee.moneymanager.repository.IncomeRepository;
import in.maheshshelakee.moneymanager.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final CategoryRepository categoryRepository;
    private final ProfileService profileService;

    @Transactional(readOnly = true)
    public List<IncomeDTO> getAll(String email) {
        ProfileEntity profile = profileService.getProfileByEmail(email);
        return incomeRepository.findByProfileOrderByDateDesc(profile)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public IncomeDTO add(IncomeDTO dto, String email) {
        ProfileEntity profile = profileService.getProfileByEmail(email);

        String catName = dto.getCategory() != null ? dto.getCategory().trim() : "";
        if (!catName.isEmpty() && !categoryRepository.existsByNameAndTypeAndProfile(catName, "INCOME", profile)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid income category: " + catName);
        }

        IncomeEntity entity = IncomeEntity.builder()
                .source(dto.getSource().trim())
                .amount(dto.getAmount())
                .date(dto.getDate())
                .category(dto.getCategory())
                .icon(dto.getIcon() != null && !dto.getIcon().isBlank() ? dto.getIcon() : "💰")
                .profile(profile)
                .build();
        return toDTO(incomeRepository.save(entity));
    }

    @Transactional
    public IncomeDTO update(Long id, IncomeDTO dto, String email) {
        ProfileEntity profile = profileService.getProfileByEmail(email);
        IncomeEntity entity = incomeRepository.findByIdAndProfile(id, profile)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Income record not found"));
        
        String catName = dto.getCategory() != null ? dto.getCategory().trim() : "";
        if (!catName.isEmpty() && !catName.equalsIgnoreCase(entity.getCategory())) {
            if (!categoryRepository.existsByNameAndTypeAndProfile(catName, "INCOME", profile)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid income category: " + catName);
            }
        }

        entity.setSource(dto.getSource().trim());
        entity.setAmount(dto.getAmount());
        entity.setDate(dto.getDate());
        entity.setCategory(catName);
        if (dto.getIcon() != null && !dto.getIcon().isBlank()) {
            entity.setIcon(dto.getIcon());
        }
        return toDTO(incomeRepository.save(entity));
    }

    @Transactional
    public void delete(Long id, String email) {
        ProfileEntity profile = profileService.getProfileByEmail(email);
        IncomeEntity entity = incomeRepository.findByIdAndProfile(id, profile)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Income record not found"));
        incomeRepository.delete(entity);
    }

    private IncomeDTO toDTO(IncomeEntity entity) {
        return IncomeDTO.builder()
                .id(entity.getId())
                .source(entity.getSource())
                .amount(entity.getAmount())
                .date(entity.getDate())
                .category(entity.getCategory())
                .icon(entity.getIcon())
                .build();
    }
}
