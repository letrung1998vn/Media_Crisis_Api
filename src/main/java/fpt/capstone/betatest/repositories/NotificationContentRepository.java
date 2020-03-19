package fpt.capstone.betatest.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fpt.capstone.betatest.entities.Notification_Content;

@Repository("notificationContentRepository")
public interface NotificationContentRepository extends JpaRepository<Notification_Content, Integer> {

	List<Notification_Content> findByNotificationId(int notificationId);
}
