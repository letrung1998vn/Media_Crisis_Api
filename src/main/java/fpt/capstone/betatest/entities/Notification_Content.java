package fpt.capstone.betatest.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Notification_Content")
public class Notification_Content {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;
	
	@Column(name = "crisis_id")
	private int crisisId;
	
	@Column(name = "notification_id")
	private int notificationId;

	public Notification_Content() {
	}

	public Notification_Content(int crisisId, int notificationId) {
		super();
		this.crisisId = crisisId;
		this.notificationId = notificationId;
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

	public int getNotificationId() {
		return notificationId;
	}

	public void setNotificationId(int notificationId) {
		this.notificationId = notificationId;
	}

	
	
	
	

}
