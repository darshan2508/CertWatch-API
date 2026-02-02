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
@RequestMapping("/category")
public class CategoryController {

    private final CategoryService categoryService;

    // Saving a new category
    @PostMapping
    public ResponseEntity<CategoryDTO> saveCategory(@RequestBody CategoryDTO category){
            CategoryDTO savedCategory = categoryService.saveCategory(category);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedCategory);
    }

    // To get all the categories for the current user,
    // if no categories exists then empty array will be returned
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getCategories(){
        List<CategoryDTO> categories = categoryService.getCategoriesForCurrentUser();
        return ResponseEntity.ok(categories);
    }

    @PutMapping("/id/{categoryId}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long categoryId, @RequestBody CategoryDTO categoryDTO){
        CategoryDTO updatedCategory = categoryService.updateCategory(categoryId, categoryDTO);
        return ResponseEntity.ok(updatedCategory);
    }


//    @DeleteMapping("/id/{categoryId}")
//    public ResponseEntity<String> deleteCategory(@PathVariable Long categoryId){
//        categoryService.deleteCategory(categoryId);
//        return ResponseEntity.ok("Category deleted successfully.");
//    }

      @DeleteMapping("/id/{categoryId}")
      public ResponseEntity<Void> deleteCategory(@PathVariable Long categoryId){
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
      }

}
