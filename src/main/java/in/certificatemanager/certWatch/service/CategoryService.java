package in.certificatemanager.certWatch.service;

import in.certificatemanager.certWatch.dto.CategoryDTO;
import in.certificatemanager.certWatch.entity.CategoryEntity;
import in.certificatemanager.certWatch.entity.CertificateEntity;
import in.certificatemanager.certWatch.entity.ProfileEntity;
import in.certificatemanager.certWatch.repository.CategoryRepository;
import in.certificatemanager.certWatch.repository.CertificateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final ProfileService profileService;
    private final CategoryRepository categoryRepository;
    private final CertificateRepository certificateRepository;

    // save category
    public CategoryDTO saveCategory(CategoryDTO categoryDTO){
        ProfileEntity profile = profileService.getCurrentProfile();
        if(categoryRepository.existsByNameAndProfileId(categoryDTO.getName(),profile.getId())){
            throw new RuntimeException("Category with this name already exists.");
        }

        CategoryEntity newCategory = toEntity(categoryDTO, profile);
        newCategory = categoryRepository.save(newCategory);

        return toDTO(newCategory);
    }

    // get categories for current user
    public List<CategoryDTO> getCategoriesForCurrentUser(){
        ProfileEntity profile = profileService.getCurrentProfile();
        List<CategoryEntity> categories = categoryRepository.findByProfileId(profile.getId());
        return categories.stream().map(this::toDTO).toList();
    }

    public CategoryDTO updateCategory(Long categoryId, CategoryDTO dto){
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity existingCategory = categoryRepository.findByIdAndProfileId(categoryId, profile.getId())
                .orElseThrow(() -> new RuntimeException("Category not found or is not accessible."));
        existingCategory.setName(dto.getName());
        existingCategory.setIcon(dto.getIcon());
        existingCategory = categoryRepository.save(existingCategory);
        return toDTO(existingCategory);
    }

    public boolean deleteCategory(Long categoryId){
        try {
            ProfileEntity profile = profileService.getCurrentProfile();
            CategoryEntity category = categoryRepository.findByIdAndProfileId(categoryId, profile.getId())
                    .orElseThrow(() -> new RuntimeException("No category found."));

            List<CertificateEntity> certsOfThisCategory = certificateRepository.findByProfileIdAndCategoryId(profile.getId(), categoryId)
                    .orElseThrow(() -> new RuntimeException("No certificates found for this category."));

            if (certsOfThisCategory.isEmpty()) {
                categoryRepository.delete(category);
                System.out.println(category.getName() + " is deleted.");
                return true;
            }
        }
        catch(Exception e){
            System.out.println("Cannot delete a property as certificates exists for this category.");
            System.err.println(e.getMessage());
            return false;
        }
        return false;
    }

    // helper methods
    private CategoryEntity toEntity(CategoryDTO categoryDTO, ProfileEntity profile){
        return CategoryEntity.builder()
                .name(categoryDTO.getName())
                .icon(categoryDTO.getIcon())
                .profile(profile)
                .build();
    }

    private CategoryDTO toDTO(CategoryEntity entity){
        return CategoryDTO.builder()
                .id(entity.getId())
                .profileId(entity.getProfile() != null ? entity.getProfile().getId() : null)
                .name(entity.getName())
                .icon(entity.getIcon())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }


}