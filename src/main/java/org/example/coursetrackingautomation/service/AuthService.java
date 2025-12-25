package org.example.coursetrackingautomation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.coursetrackingautomation.config.UserSession;
import org.example.coursetrackingautomation.dto.SessionUser;
import org.example.coursetrackingautomation.entity.User;
import org.example.coursetrackingautomation.exception.InactiveUserException;
import org.example.coursetrackingautomation.exception.InvalidCredentialsException;
import org.example.coursetrackingautomation.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserSession userSession;

	@Transactional(readOnly = true)
	public SessionUser login(String username, String rawPassword) {
		String safeUsername = username == null ? "" : username.trim();
		String safePassword = rawPassword == null ? "" : rawPassword;

		if (safeUsername.isBlank() || safePassword.isBlank()) {
			throw new InvalidCredentialsException();
		}

		User user = userRepository.findByUsername(safeUsername)
			.orElseThrow(InvalidCredentialsException::new);

		if (!user.isActive()) {
			throw new InactiveUserException();
		}

		if (!passwordEncoder.matches(safePassword, user.getPassword())) {
			throw new InvalidCredentialsException();
		}

		SessionUser sessionUser = new SessionUser(
			user.getId(),
			user.getUsername(),
			user.getFirstName(),
			user.getLastName(),
			user.getRole()
		);

		userSession.setCurrentUser(sessionUser);
		log.info("User logged in: id={}, username={}, role={}", user.getId(), user.getUsername(), user.getRole());
		return sessionUser;
	}

	public void logout() {
		userSession.getCurrentUser().ifPresent(u -> log.info("User logged out: id={}, username={}", u.id(), u.username()));
		userSession.cleanUserSession();
	}
}