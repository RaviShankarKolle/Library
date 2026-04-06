package com.library.userservice.service;

import com.library.userservice.domain.User;
import com.library.userservice.repository.UserRepository;
import com.library.userservice.web.error.BorrowNotAllowedException;
import com.library.userservice.web.error.UserNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class LibraryUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LibraryUserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User getById(long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }

    public void validateBorrowLimit(long userId) {
        User user = getById(userId);
        if (user.isBlocked()) {
            throw new BorrowNotAllowedException("User is blocked");
        }
        if (user.getCurrentCount() >= user.getMaxLimit()) {
            throw new BorrowNotAllowedException("Borrow limit reached");
        }
    }

    public User createUser(String email, String role, int maxLimit, String rawPassword) {
        String hash = StringUtils.hasText(rawPassword) ? passwordEncoder.encode(rawPassword) : null;
        return userRepository.insert(email, role, maxLimit, hash);
    }

    @Transactional
    public void incrementBorrowCount(long userId) {
        userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        int updated = userRepository.incrementBorrowCountIfAllowed(userId);
        if (updated == 0) {
            throw new BorrowNotAllowedException("Cannot increment borrow count (blocked or at limit)");
        }
    }

    @Transactional
    public void decrementBorrowCount(long userId) {
        userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        userRepository.decrementBorrowCount(userId);
    }
}
