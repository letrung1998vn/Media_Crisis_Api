package fpt.capstone.betatest.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fpt.capstone.betatest.entities.Notification_Content;

@Repository("notificationContentRepository")
public interface NotificationContentRepository extends JpaRepository<Notification_Content, Integer> {

}
