package ru.practicum.ewm.controller.pub;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.service.CategoryService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;


@Slf4j
@AllArgsConstructor
@Controller
@RequestMapping("/categories")
public class CategoryPublicController {
    private final CategoryService service;

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getCategories(@PositiveOrZero  @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                           @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Получение категорий с позиции={}, размер={}", from, size);
        Page<CategoryDto> categories = service.getCategories(from, size);
        return new ResponseEntity<>(categories.getContent(), HttpStatus.OK);
    }

    @GetMapping("/{catId}")
    public ResponseEntity<CategoryDto> getCategory(@PathVariable(name = "catId") Integer catId) {
        log.info("Получение категорий id={}", catId);
        return new ResponseEntity<>(service.getCategory(catId), HttpStatus.OK);
    }
}
