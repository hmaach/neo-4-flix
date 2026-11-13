package com.neo4flix.user;

import com.neo4flix.common.error.ConflictException;
import com.neo4flix.common.error.NotFoundException;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User get(String userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
    }

    public User update(String userId, UpdateUserRequest request) {
        User user = get(userId);
        String email = request.email().toLowerCase(Locale.ROOT);
        if (!user.getUsername().equals(request.username()) && userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username already taken");
        }
        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already registered");
        }
        user.setUsername(request.username());
        user.setEmail(email);
        return userRepository.save(user);
    }

    public void delete(String userId) {
        get(userId);
        userRepository.deleteWithRelationships(userId);
    }
}
