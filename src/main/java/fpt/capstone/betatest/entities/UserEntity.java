package fpt.capstone.betatest.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="[User]")
public class UserEntity {
	@Id
//	@GeneratedValue
//	@Column(name="ID", nullable=false)
//	private Long id;
	
	@Column(name="UserName", nullable=false)
	private String username;
	
	@Column(name="Password", nullable=false)
	private String password;
	
	@Column(name="Role", nullable=false)
	private String role;

//	public Long getId() {
//		return id;
//	}
//
//	public void setId(Long id) {
//		this.id = id;
//	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	@Override
	public String toString() {
//		return "UserEntity [id=" + id + ", username=" + username + ", password=" + password + ", role=" + role + "]";
		return "UserEntity [username=" + username + ", password=" + password + ", role=" + role + "]";
	}
	
	
}