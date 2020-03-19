package fpt.capstone.betatest.entities;

import java.io.Serializable;
import java.util.Date;

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
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;

	@Column(name="email")
	private boolean email;
	
	@Column(name="webhook")
	private boolean webhook;
	//bi-directional many-to-one association to UserInfo
	@ManyToOne
	@JoinColumn(name="user_id", referencedColumnName="user_id")
	private User user;
	
	@Column(name="date")
	private Date date;

	public Notification() {
	}

	public Notification(boolean email, boolean webhook, User user, Date date) {
		super();
		this.email = email;
		this.webhook = webhook;
		this.user = user;
		this.date = date;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public String toString() {
		return "Notification [id=" + id + ", email=" + email + ", webhook=" + webhook + ", user=" + user + ", date="
				+ date + "]";
	}

	

}