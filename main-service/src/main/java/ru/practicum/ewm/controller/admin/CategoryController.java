package ru.practicum.ewm.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.service.CategoryService;

import javax.validation.Valid;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
public class CategoryController {
    private final CategoryService service;

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@RequestBody @Valid CategoryDto dto) {
        log.info("Добавление категории: {}", dto);
        return new ResponseEntity<>(service.createCategory(dto), HttpStatus.CREATED);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable(name = "catId") Integer catId) {
        log.info("Дестрой категории id={}", catId);
        service.deleteCategory(catId);
    }

    @PatchMapping("{catId}")
    public ResponseEntity<CategoryDto> patchCategory(@RequestBody @Valid CategoryDto dto,
                                                     @PathVariable(name = "catId") Integer catId) {
        dto.setId(catId);
        log.info("Обновление категории: {}", dto);
        return new ResponseEntity<>(service.updateCategory(dto), HttpStatus.OK);
    }
}
