package fpt.capstone.betatest.entities;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the User database table.
 * 
 */
@Entity
@Table(name="[User]")
public class User implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="UserName")
	private String userName;

	@Column(name="Password")
	private String password;

	@Column(name="Role")
	private String role;

	public User() {
	}

	public String getUserName() {
		return this.userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRole() {
		return this.role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	@Override
	public String toString() {
		return "User [userName=" + userName + ", password=" + password + ", role=" + role + "]";
	}

	

	
}