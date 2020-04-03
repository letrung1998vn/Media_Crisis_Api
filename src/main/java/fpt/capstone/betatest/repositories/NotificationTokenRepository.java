package fpt.capstone.betatest.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fpt.capstone.betatest.entities.NotificationToken;

@Repository("notificationTokenRepository")
public interface NotificationTokenRepository extends JpaRepository<NotificationToken, Integer>{
	List<NotificationToken> findByUserName(String UserId);
	NotificationToken findByUserNameAndNotiToken(String userId, String notiToken);
}
