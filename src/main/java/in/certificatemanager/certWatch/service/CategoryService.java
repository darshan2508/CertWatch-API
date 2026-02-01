package in.certificatemanager.certWatch.service;

import in.certificatemanager.certWatch.customExceptions.ResourceInUseException;
import in.certificatemanager.certWatch.customExceptions.ResourceNotFoundException;
import in.certificatemanager.certWatch.dto.CategoryDTO;
import in.certificatemanager.certWatch.entity.CategoryEntity;
import in.certificatemanager.certWatch.entity.ProfileEntity;
import in.certificatemanager.certWatch.repository.CategoryRepository;
import in.certificatemanager.certWatch.repository.CertificateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.util.List;

@Slf4j
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
            log.info("Category with the name [" + categoryDTO.getName() + "] already exists.");
            throw new ResourceInUseException("Category with the name [" + categoryDTO.getName() + "] already exists.");
        }else{
            CategoryEntity newCategory = toEntity(categoryDTO, profile);
            newCategory = categoryRepository.save(newCategory);
            return toDTO(newCategory);
        }
    }

    // get categories for current user
    public List<CategoryDTO> getCategoriesForCurrentUser(){
        ProfileEntity profile = profileService.getCurrentProfile();
        List<CategoryEntity> categories = categoryRepository.findByProfileId(profile.getId());
        if(categories.isEmpty()) log.info("No categories found for the user - " + profile.getId());
        return categories.stream().map(this::toDTO).toList();
    }

    public CategoryDTO updateCategory(Long categoryId, CategoryDTO dto){
        ProfileEntity profile = profileService.getCurrentProfile();

        // Find the category with the id mentioned in the request
        CategoryEntity existingCategory = categoryRepository
                .findByIdAndProfileId(categoryId, profile.getId())
                .orElseThrow(() -> {
                    log.info("No category is found for category Id - " + categoryId);
                    return new ResourceNotFoundException("Category not found");
                });

        // Check if any category already exists with the name provided in the request
        if(categoryRepository.existsByNameAndProfileId(dto.getName(),profile.getId())){
            log.info("Category with the name [" + dto.getName() + "] already exists.");
            throw new ResourceInUseException("Category with the name [" + dto.getName() + "] already exists.");
        }else{
            existingCategory.setName(dto.getName());
            existingCategory.setIcon(dto.getIcon());
            existingCategory = categoryRepository.save(existingCategory);
            log.info("Category details updated for category Id - " + categoryId);
            return toDTO(existingCategory);
        }
    }

    public void deleteCategory(Long categoryId){

        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity category = categoryRepository
                .findByIdAndProfileId(categoryId, profile.getId())
                .orElseThrow(() -> {
                    log.info("No category is found for category Id - " + categoryId);
                    return new ResourceNotFoundException("Category not found");
                });

        boolean hasCertificates = certificateRepository
                .existsByProfileIdAndCategoryId(profile.getId(), categoryId);

        if (!hasCertificates) {
            categoryRepository.delete(category);
            log.info("Category => "+category.getName() + " is deleted.");
        }else{
            throw new ResourceInUseException("Category contains certificates. Delete them first.");
        }
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