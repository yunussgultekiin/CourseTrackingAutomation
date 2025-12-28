package org.example.coursetrackingautomation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.coursetrackingautomation.config.UserSession;
import org.example.coursetrackingautomation.dto.RoleDTO;
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
/**
 * Provides authentication operations for the desktop application.
 *
 * <p>This service validates credentials, enforces user activation rules, and publishes the
 * authenticated principal into the {@link UserSession}. It is intentionally state-free aside
 * from writing to the session component.</p>
 */
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserSession userSession;

	@Transactional(readOnly = true)
	/**
	 * Authenticates a user and stores the authenticated principal in the current session.
	 *
	 * <p>Usernames are trimmed; null inputs are treated as blank. Authentication fails for
	 * unknown users, inactive users, or password mismatches.</p>
	 *
	 * @param username the username provided by the user
	 * @param rawPassword the raw (unencoded) password provided by the user
	 * @return the authenticated session user representation
	 * @throws InvalidCredentialsException if the credentials are missing, unknown, or invalid
	 * @throws InactiveUserException if the user exists but is not active
	 */
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
			RoleDTO.valueOf(user.getRole().name())
		);

		userSession.setCurrentUser(sessionUser);
		log.info("User logged in: id={}, username={}, role={}", user.getId(), user.getUsername(), user.getRole());
		return sessionUser;
	}

	/**
	 * Clears the current authentication session.
	 *
	 * <p>This operation is idempotent; calling it without an active user has no adverse effect.</p>
	 */
	public void logout() {
		userSession.getCurrentUser().ifPresent(u -> log.info("User logged out: id={}, username={}", u.id(), u.username()));
		userSession.cleanUserSession();
	}
}