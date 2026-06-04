package com.wip.workipedia.user.domain;

import com.wip.workipedia.department.domain.Department;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long userId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "department_id", nullable = false)
	private Department department;

	@Column(name = "employee_id", nullable = false, length = 100)
	private String employeeId;

	@Column(nullable = false, length = 255)
	private String email;

	@Column(nullable = false, length = 255)
	private String password;

	@Column(nullable = false, length = 20)
	private String nickname;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private UserRole role;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private UserStatus status;

	@Column(name = "last_login_at")
	private LocalDateTime lastLoginAt;

	protected User() {
	}

	private User(
		Department department,
		String employeeId,
		String email,
		String password,
		String nickname,
		UserRole role,
		UserStatus status
	) {
		this.department = department;
		this.employeeId = employeeId;
		this.email = email;
		this.password = password;
		this.nickname = nickname;
		this.role = role;
		this.status = status;
	}

	public static User signup(
		Department department,
		String employeeId,
		String email,
		String password,
		String nickname
	) {
		return new User(
			department,
			employeeId,
			email,
			password,
			nickname,
			UserRole.USER,
			UserStatus.ACTIVE
		);
	}

	public Long getUserId() {
		return userId;
	}

	public Department getDepartment() {
		return department;
	}

	public String getEmployeeId() {
		return employeeId;
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}

	public String getNickname() {
		return nickname;
	}

	public UserRole getRole() {
		return role;
	}

	public UserStatus getStatus() {
		return status;
	}

	public LocalDateTime getLastLoginAt() {
		return lastLoginAt;
	}
}
