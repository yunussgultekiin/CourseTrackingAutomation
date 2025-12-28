package org.example.coursetrackingautomation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.coursetrackingautomation.dto.CreateUserRequest;
import org.example.coursetrackingautomation.dto.RoleDTO;
import org.example.coursetrackingautomation.dto.SelectOptionDTO;
import org.example.coursetrackingautomation.dto.UpdateUserRequest;
import org.example.coursetrackingautomation.dto.UserDetailsDTO;
import org.example.coursetrackingautomation.entity.Role;
import org.example.coursetrackingautomation.entity.User;
import org.example.coursetrackingautomation.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
/**
 * Provides user management operations.
 *
 * <p>This service handles creation and updates of {@link User} entities, password changes, and
 * convenience lookups used by the UI. Passwords are always stored encoded via Spring Security's
 * {@link PasswordEncoder}.</p>
 */
public class UserService {

	private static final String DEFAULT_ADMIN_USERNAME = "admin";
	private static final String DEFAULT_ADMIN_PASSWORD = "123";

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	/**
	 * Creates a new user from the provided request.
	 *
	 * @param request user creation payload
	 * @return the persisted {@link User}
	 * @throws IllegalArgumentException if validation fails or username is already taken
	 */
	public User createUser(CreateUserRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("Kullanıcı oluşturma isteği boş olamaz");
		}

		String username = request.username() == null ? "" : request.username().trim();
		String password = request.password() == null ? "" : request.password();

		if (username.isBlank()) {
			throw new IllegalArgumentException("Kullanıcı adı boş bırakılamaz");
		}
		if (password.isBlank()) {
			throw new IllegalArgumentException("Şifre boş bırakılamaz");
		}
		if (request.firstName() == null || request.firstName().trim().isBlank()) {
			throw new IllegalArgumentException("Ad boş bırakılamaz");
		}
		if (request.lastName() == null || request.lastName().trim().isBlank()) {
			throw new IllegalArgumentException("Soyad boş bırakılamaz");
		}
		if (request.role() == null) {
			throw new IllegalArgumentException("Rol boş olamaz");
		}

		userRepository.findByUsername(username).ifPresent(u -> {
			throw new IllegalArgumentException("Bu kullanıcı adı zaten kullanılıyor");
		});

		User user = User.builder()
			.username(username)
			.password(passwordEncoder.encode(password))
			.firstName(request.firstName().trim())
			.lastName(request.lastName().trim())
			.role(Role.valueOf(request.role().name()))
			.studentNumber(request.studentNumber())
			.email(request.email())
			.phone(request.phone())
			.active(request.active())
			.build();

		User saved = userRepository.save(user);
		log.info("User created: id={}, username={}, role={}", saved.getId(), saved.getUsername(), saved.getRole());
		return saved;
	}

	@Transactional
	/**
	 * Updates mutable attributes of an existing user.
	 *
	 * <p>This method applies partial updates for name fields and password (when provided). Email and
	 * phone are replaced with the values supplied in {@code request}.</p>
	 *
	 * @param userId the user identifier
	 * @param request update payload
	 * @return the persisted {@link User}
	 * @throws IllegalArgumentException if the user does not exist or input is invalid
	 */
	public User updateUser(Long userId, UpdateUserRequest request) {
		if (userId == null) {
			throw new IllegalArgumentException("Kullanıcı id boş olamaz");
		}
		if (request == null) {
			throw new IllegalArgumentException("Güncelleme isteği boş olamaz");
		}

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı"));

		if (request.firstName() != null && !request.firstName().trim().isBlank()) {
			user.setFirstName(request.firstName().trim());
		}
		if (request.lastName() != null && !request.lastName().trim().isBlank()) {
			user.setLastName(request.lastName().trim());
		}

		user.setEmail(request.email());
		user.setPhone(request.phone());

		String maybePassword = request.password();
		if (maybePassword != null && !maybePassword.isBlank()) {
			user.setPassword(passwordEncoder.encode(maybePassword));
		}

		User saved = userRepository.save(user);
		log.info("User updated: id={}, username={}", saved.getId(), saved.getUsername());
		return saved;
	}

	@Transactional
	/**
	 * Ensures the default administrator account exists.
	 *
	 * <p>If the account does not exist, it is created with configured default credentials.
	 * Intended for local/demo setups and initial bootstrap flows.</p>
	 */
	public void ensureDefaultAdminUserExists() {
		userRepository.findByUsername(DEFAULT_ADMIN_USERNAME).ifPresentOrElse(
			u -> log.info("Default admin user already exists: username={}", DEFAULT_ADMIN_USERNAME),
			() -> {
				CreateUserRequest request = new CreateUserRequest(
					DEFAULT_ADMIN_USERNAME,
					DEFAULT_ADMIN_PASSWORD,
					"Yönetici",
					"Hesabı",
					RoleDTO.ADMIN,
					null,
					null,
					null,
					true
				);
				createUser(request);
				log.info("Default admin user created: username={}", DEFAULT_ADMIN_USERNAME);
			}
		);
	}

	@Transactional
	/**
	 * Changes the password for the given user.
	 *
	 * @param userId the user identifier
	 * @param currentPassword the current raw password used for verification
	 * @param newPassword the new raw password to persist (will be encoded)
	 * @throws IllegalArgumentException if validation fails, user is missing, or current password is incorrect
	 */
	public void changePassword(Long userId, String currentPassword, String newPassword) {
		if (userId == null) {
			throw new IllegalArgumentException("Kullanıcı id boş olamaz");
		}
		if (currentPassword == null || currentPassword.isBlank()) {
			throw new IllegalArgumentException("Mevcut şifre boş bırakılamaz");
		}
		if (newPassword == null || newPassword.isBlank()) {
			throw new IllegalArgumentException("Yeni şifre boş bırakılamaz");
		}

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı"));

		if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
			throw new IllegalArgumentException("Mevcut şifre yanlış");
		}

		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);
		log.info("Password changed for user: id={}, username={}", user.getId(), user.getUsername());
	}

	@Transactional(readOnly = true)
	/**
	 * Retrieves a user by id.
	 *
	 * @param userId the user identifier
	 * @return the persisted {@link User}
	 * @throws IllegalArgumentException if {@code userId} is null or the user cannot be found
	 */
	public User getUserById(Long userId) {
		if (userId == null) {
			throw new IllegalArgumentException("Kullanıcı id boş olamaz");
		}
		return userRepository.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı"));
	}

	@Transactional(readOnly = true)
	/**
	 * Retrieves all active users for the specified role.
	 *
	 * @param role the role to filter by
	 * @return active users having the provided role
	 * @throws IllegalArgumentException if {@code role} is null
	 */
	public List<User> getActiveUsersByRole(Role role) {
		if (role == null) {
			throw new IllegalArgumentException("Rol boş olamaz");
		}
		return userRepository.findByRoleAndActiveTrue(role);
	}

	@Transactional(readOnly = true)
	/**
	 * Retrieves all active users for the specified role.
	 *
	 * @param role the role DTO to filter by
	 * @return active users having the provided role
	 * @throws IllegalArgumentException if {@code role} is null
	 */
	public List<User> getActiveUsersByRole(RoleDTO role) {
		if (role == null) {
			throw new IllegalArgumentException("Rol boş olamaz");
		}
		return getActiveUsersByRole(Role.valueOf(role.name()));
	}

	@Transactional(readOnly = true)
	/**
	 * Returns UI-friendly selection options for active users of the specified role.
	 *
	 * @param role the role to filter by
	 * @return option list containing user ids and display labels
	 */
	public List<SelectOptionDTO> getActiveUserOptionsByRole(Role role) {
		return getActiveUsersByRole(role).stream()
			.map(user -> {
				String firstName = user.getFirstName() == null ? "" : user.getFirstName();
				String lastName = user.getLastName() == null ? "" : user.getLastName();
				String username = user.getUsername() == null ? "" : user.getUsername();
				String label = (firstName + " " + lastName).trim();
				if (!username.isBlank()) {
					label = label.isBlank() ? ("(" + username + ")") : (label + " (" + username + ")");
				}
				return new SelectOptionDTO(user.getId(), label);
			})
			.toList();
	}

	@Transactional(readOnly = true)
	/**
	 * Returns UI-friendly selection options for active users of the specified role.
	 *
	 * @param role the role DTO to filter by
	 * @return option list containing user ids and display labels
	 */
	public List<SelectOptionDTO> getActiveUserOptionsByRole(RoleDTO role) {
		return getActiveUsersByRole(role).stream()
			.map(user -> {
				String firstName = user.getFirstName() == null ? "" : user.getFirstName();
				String lastName = user.getLastName() == null ? "" : user.getLastName();
				String username = user.getUsername() == null ? "" : user.getUsername();
				String label = (firstName + " " + lastName).trim();
				if (!username.isBlank()) {
					label = label.isBlank() ? ("(" + username + ")") : (label + " (" + username + ")");
				}
				return new SelectOptionDTO(user.getId(), label);
			})
			.toList();
	}

	@Transactional(readOnly = true)
	/**
	 * Retrieves a lightweight user details projection.
	 *
	 * @param userId the user identifier
	 * @return user details DTO
	 * @throws IllegalArgumentException if the user cannot be found
	 */
	public UserDetailsDTO getUserDetailsById(Long userId) {
		User user = getUserById(userId);
		return new UserDetailsDTO(
			user.getId(),
			user.getUsername(),
			user.getFirstName(),
			user.getLastName(),
			user.getEmail(),
			user.getPhone()
		);
	}
}