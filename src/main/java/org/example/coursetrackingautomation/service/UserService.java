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
public class UserService {

	private static final String DEFAULT_ADMIN_USERNAME = "admin";
	private static final String DEFAULT_ADMIN_PASSWORD = "123";

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
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
	public User getUserById(Long userId) {
		if (userId == null) {
			throw new IllegalArgumentException("Kullanıcı id boş olamaz");
		}
		return userRepository.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı"));
	}

	@Transactional(readOnly = true)
	public List<User> getActiveUsersByRole(Role role) {
		if (role == null) {
			throw new IllegalArgumentException("Rol boş olamaz");
		}
		return userRepository.findByRoleAndActiveTrue(role);
	}

	@Transactional(readOnly = true)
	public List<User> getActiveUsersByRole(RoleDTO role) {
		if (role == null) {
			throw new IllegalArgumentException("Rol boş olamaz");
		}
		return getActiveUsersByRole(Role.valueOf(role.name()));
	}

	@Transactional(readOnly = true)
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