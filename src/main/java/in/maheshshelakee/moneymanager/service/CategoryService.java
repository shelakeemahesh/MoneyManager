package in.maheshshelakee.moneymanager.service;

import in.maheshshelakee.moneymanager.dto.CategoryRequest;
import in.maheshshelakee.moneymanager.dto.CategoryResponse;
import in.maheshshelakee.moneymanager.dto.SubCategoryResponse;
import in.maheshshelakee.moneymanager.entity.CategoryEntity;
import in.maheshshelakee.moneymanager.entity.ProfileEntity;
import in.maheshshelakee.moneymanager.repository.CategoryRepository;
import in.maheshshelakee.moneymanager.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    // Uses ProfileRepository directly (not ProfileService) to avoid circular dependency.
    // ProfileService uses CategoryRepository directly for the same reason.
    private final CategoryRepository categoryRepository;
    private final ProfileRepository profileRepository;

    // ─── Profile lookup helper ────────────────────────────────────────────────
    private ProfileEntity getProfile(String email) {
        return profileRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User not found: " + email));
    }

    // ─── READ ─────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll(String email) {
        ProfileEntity profile = getProfile(email);
        return categoryRepository.findByProfileWithSubcategories(profile)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── CREATE ───────────────────────────────────────────────────────────────
    @Transactional
    public CategoryResponse create(CategoryRequest request, String email) {
        ProfileEntity profile = getProfile(email);
        String normalizedType = request.getType().toUpperCase();

        if (categoryRepository.existsByNameAndTypeAndProfile(
                request.getName().trim(), normalizedType, profile)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Category '" + request.getName() + "' already exists for type " + normalizedType);
        }

        CategoryEntity entity = CategoryEntity.builder()
                .name(request.getName().trim())
                .type(normalizedType)
                .icon(request.getIcon() != null && !request.getIcon().isBlank() ? request.getIcon() : "📁")
                .color(request.getColor() != null && !request.getColor().isBlank() ? request.getColor() : "#6366f1")
                .profile(profile)
                .build();

        return toResponse(categoryRepository.save(entity));
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request, String email) {
        ProfileEntity profile = getProfile(email);

        CategoryEntity entity = categoryRepository.findByIdAndProfile(id, profile)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        String normalizedType = request.getType().toUpperCase();
        boolean nameChanged = !entity.getName().equalsIgnoreCase(request.getName().trim())
                || !entity.getType().equalsIgnoreCase(normalizedType);

        if (nameChanged && categoryRepository.existsByNameAndTypeAndProfile(
                request.getName().trim(), normalizedType, profile)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Category '" + request.getName() + "' already exists for type " + normalizedType);
        }

        entity.setName(request.getName().trim());
        entity.setType(normalizedType);
        if (request.getIcon() != null && !request.getIcon().isBlank()) entity.setIcon(request.getIcon());
        if (request.getColor() != null && !request.getColor().isBlank()) entity.setColor(request.getColor());

        return toResponse(categoryRepository.save(entity));
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────
    @Transactional
    public void delete(Long id, String email) {
        ProfileEntity profile = getProfile(email);
        CategoryEntity entity = categoryRepository.findByIdAndProfile(id, profile)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        categoryRepository.delete(entity);
    }

    // ─── GET BY ID + EMAIL ────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public CategoryEntity getCategoryByIdAndEmail(Long id, String email) {
        ProfileEntity profile = getProfile(email);
        return categoryRepository.findByIdAndProfile(id, profile)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
    }

    // ─── INTERNAL MAPPER ──────────────────────────────────────────────────────
    private CategoryResponse toResponse(CategoryEntity entity) {
        List<SubCategoryResponse> subs = entity.getSubcategories().stream()
                .map(s -> SubCategoryResponse.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .icon(s.getIcon())
                        .categoryId(entity.getId())
                        .categoryName(entity.getName())
                        .createdAt(s.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return CategoryResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .type(entity.getType())
                .icon(entity.getIcon())
                .color(entity.getColor())
                .subcategoryCount(subs.size())
                .subcategories(subs)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}