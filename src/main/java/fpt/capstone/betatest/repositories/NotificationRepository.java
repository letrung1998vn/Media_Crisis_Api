package fpt.capstone.betatest.repositories;

import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fpt.capstone.betatest.entities.Notification;
import fpt.capstone.betatest.entities.User;

@Repository("notificationRepository")
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
	int findByUserAndEmailAndWebhookAndDate(User user, boolean email, boolean webhook, Date date);
	List<Notification> findByUser(User user);
	
}
