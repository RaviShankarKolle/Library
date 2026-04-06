package com.library.userservice.web;

import com.library.userservice.service.LibraryUserService;
import com.library.userservice.web.dto.CreateUserRequest;
import com.library.userservice.web.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final LibraryUserService libraryUserService;

    public UserController(LibraryUserService libraryUserService) {
        this.libraryUserService = libraryUserService;
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable long id) {
        return UserResponse.from(libraryUserService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUserRequest body) {
        return UserResponse.from(
                libraryUserService.createUser(body.email(), body.role(), body.maxLimit(), body.password()));
    }

    @PostMapping("/{id}/borrow/validate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void validateBorrow(@PathVariable long id) {
        libraryUserService.validateBorrowLimit(id);
    }
}
