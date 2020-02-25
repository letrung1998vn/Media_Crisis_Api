package fpt.capstone.betatest.entities;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the Notification database table.
 * 
 */
@Entity
@Table(name="[Notification]")
public class Notification implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="ID")
	private int id;

	@Column(name="CrisisId")
	private int crisisId;

	@Column(name="Email")
	private boolean email;
	
	@Column(name="Webhook")
	private boolean webhook;
	//bi-directional many-to-one association to UserInfo
	@ManyToOne
	@JoinColumn(name="UserId")
	private UserInfo userInfo;

	public Notification() {
	}

	public Notification(int id, int crisisId, boolean email, boolean webhook, UserInfo userInfo) {
		super();
		this.id = id;
		this.crisisId = crisisId;
		this.email = email;
		this.webhook = webhook;
		this.userInfo = userInfo;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCrisisId() {
		return crisisId;
	}

	public void setCrisisId(int crisisId) {
		this.crisisId = crisisId;
	}

	public boolean isEmail() {
		return email;
	}

	public void setEmail(boolean email) {
		this.email = email;
	}

	public boolean isWebhook() {
		return webhook;
	}

	public void setWebhook(boolean webhook) {
		this.webhook = webhook;
	}

	public UserInfo getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}

	@Override
	public String toString() {
		return "Notification [id=" + id + ", crisisId=" + crisisId + ", email=" + email + ", webhook=" + webhook
				+ ", userInfo=" + userInfo + "]";
	}

	

}