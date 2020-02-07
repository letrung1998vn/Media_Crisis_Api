package fpt.capstone.betatest.entities;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the UserInfo database table.
 * 
 */
@Entity
@Table(name="[UserInfo]")
public class UserInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="UserId")
	private String userId;

	@Column(name="Email")
	private String email;

	@Column(name="Name")
	private String name;

	//bi-directional many-to-one association to Notification
	@OneToMany(mappedBy="userInfo")
	private List<Notification> notifications;

	//bi-directional one-to-one association to User
	@OneToOne
	@JoinColumn(name="UserId")
	private User user;

	public UserInfo() {
	}

	public String getUserId() {
		return this.userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Notification> getNotifications() {
		return this.notifications;
	}

	public void setNotifications(List<Notification> notifications) {
		this.notifications = notifications;
	}

	public Notification addNotification(Notification notification) {
		getNotifications().add(notification);
		notification.setUserInfo(this);

		return notification;
	}

	public Notification removeNotification(Notification notification) {
		getNotifications().remove(notification);
		notification.setUserInfo(null);

		return notification;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public String toString() {
		return "UserInfo [userId=" + userId + ", email=" + email + ", name=" + name + ", notifications=" + notifications
				+ ", user=" + user + "]";
	}

	
}