package fpt.capstone.betatest.services;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fpt.capstone.betatest.entities.Notification_Content;
import fpt.capstone.betatest.repositories.NotificationContentRepository;

@Service
public class NotificationContentService {
	@Autowired
	private NotificationContentRepository notificationContentRepository;
	
	@Transactional
	public Notification_Content save(Notification_Content notification_Content) {
		return notificationContentRepository.save(notification_Content);
	}
	
	@Transactional
	public List<Notification_Content> getNotificationContent(int notificationId) {
		return notificationContentRepository.findByNotificationId(notificationId);
	}
	
}
