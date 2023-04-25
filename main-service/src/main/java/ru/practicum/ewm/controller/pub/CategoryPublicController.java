package ru.practicum.ewm.controller.pub;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.service.CategoryService;


@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/categories")
public class CategoryPublicController {
    private final CategoryService service;

    @GetMapping
    public ResponseEntity<Page<CategoryDto>> getCategories(@RequestParam(name = "from", defaultValue = "0") Integer from,
                                                           @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Получение категорий с позиции={}, размер={}", from, size);
        return new ResponseEntity<>(service.getCategories(from, size), HttpStatus.OK);
    }

    @GetMapping("/{catId}")
    public ResponseEntity<CategoryDto> getCategory(@PathVariable(name = "catId") Integer catId) {
        log.info("Получение категорий id={}", catId);
        return new ResponseEntity<>(service.getCategory(catId), HttpStatus.OK);
    }
}
