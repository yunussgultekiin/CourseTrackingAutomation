package org.example.coursetrackingautomation.service;

import org.example.coursetrackingautomation.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Basit bir yer tutucu doğrulama. Gerçek doğrulama için kullanıcı adı/şifre
     * alanlarını User tablosuna ekleyip burada kontrol etmeliyiz.
     */
    public boolean authenticate(String username, String password) {
        // TODO: Gerçek veritabanı doğrulaması eklenmeli.
        return username != null && !username.isBlank()
                && password != null && !password.isBlank();
    }
}