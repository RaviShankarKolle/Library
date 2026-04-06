package com.library.fineservice.web;

import com.library.fineservice.service.LibraryFineService;
import com.library.fineservice.web.dto.FineResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class FineController {

    private final LibraryFineService libraryFineService;

    public FineController(LibraryFineService libraryFineService) {
        this.libraryFineService = libraryFineService;
    }

    @GetMapping("/users/{userId}/fines")
    public List<FineResponse> listByUser(@PathVariable long userId) {
        return libraryFineService.listByUser(userId).stream().map(FineResponse::from).toList();
    }

    @PostMapping("/fines/{id}/pay")
    public FineResponse pay(@PathVariable long id) {
        return FineResponse.from(libraryFineService.payFine(id));
    }
}
