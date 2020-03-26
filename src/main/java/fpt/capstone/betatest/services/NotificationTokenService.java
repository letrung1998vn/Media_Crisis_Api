package fpt.capstone.betatest.services;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fpt.capstone.betatest.entities.NotificationToken;
import fpt.capstone.betatest.entities.Post;
import fpt.capstone.betatest.repositories.NotificationTokenRepository;
import fpt.capstone.betatest.repositories.PostRepository;

@Service
public class NotificationTokenService {
	@Autowired
	private NotificationTokenRepository notificationTokenRepository;

	@Transactional
	public List<NotificationToken> getNotiTokenByUserId(String userId) {
		return notificationTokenRepository.findByUserName(userId);
	}

	@Transactional
	public void saveToken(NotificationToken notiToken) {
		notificationTokenRepository.save(notiToken);
	}
	@Transactional
	public NotificationToken getNotiTokenByUserIdAndNotiToken(String userId, String notiToken) {
		return notificationTokenRepository.findByUserNameAndNotiToken(userId, notiToken);
	}
	@Transactional
	public void deleteToken(NotificationToken notiToken) {
		notificationTokenRepository.delete(notiToken);
	}
}	
