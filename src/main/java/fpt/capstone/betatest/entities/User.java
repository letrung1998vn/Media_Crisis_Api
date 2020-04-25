package fpt.capstone.betatest.entities;

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
//import com.fasterxml.jackson.annotation.JsonManagedReference;

/**
 * The persistent class for the User database table.
 * 
 */
@Entity
@Table(name = "[User]")
public class User implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "user_id")
	private String userName;

	@Column(name = "password")
	private String password;

	@Column(name = "role")
	private String role;

	@Column(name = "available")
	private boolean available;

	// bi-directional one-to-one association to User
	@OneToOne
	@JoinColumn(name = "user_id")
	private UserInfo user;

	// bi-directional many-to-one association to Notification
	@JsonIgnore
	@OneToMany(mappedBy = "user")
	private List<Notification> notifications;
		
	// bi-directional many-to-one association to Notification
	@JsonIgnore
	@OneToMany(mappedBy = "user")
	private List<Keyword> keyword;
	
	public User(String userName, String password, String role, boolean available, UserInfo user,
			List<Notification> notifications, List<Keyword> keyword) {
		super();
		this.userName = userName;
		this.password = password;
		this.role = role;
		this.available = available;
		this.user = user;
		this.notifications = notifications;
		this.keyword = keyword;
	}

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

	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public UserInfo getUser() {
		return user;
	}

	public void setUser(UserInfo user) {
		this.user = user;
	}

	public List<Notification> getNotifications() {
		return notifications;
	}

	public void setNotifications(List<Notification> notifications) {
		this.notifications = notifications;
	}

	public List<Keyword> getKeyword() {
		return keyword;
	}

	public void setKeyword(List<Keyword> keyword) {
		this.keyword = keyword;
	}


	
}