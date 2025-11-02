package in.certificatemanager.certWatch.controller;

import in.certificatemanager.certWatch.dto.CategoryDTO;
import in.certificatemanager.certWatch.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryDTO> saveCategory(@RequestBody CategoryDTO category){
        CategoryDTO savedCategory = categoryService.saveCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED ).body(savedCategory);
    }

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getCategories(){
        List<CategoryDTO> categories = categoryService.getCategoriesForCurrentUser();
        return ResponseEntity.ok(categories);
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long categoryId, @RequestBody CategoryDTO categoryDTO){
        CategoryDTO updatedCategory = categoryService.updateCategory(categoryId, categoryDTO);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long categoryId){
        boolean verdict = categoryService.deleteCategory(categoryId);
        if(verdict) return ResponseEntity.ok("Category deleted successfully.");
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("One or more certificates in this category exists. Please delete them first.");
    }

}
