package fpt.capstone.betatest.services;

import java.sql.Date;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fpt.capstone.betatest.entities.Notification;
import fpt.capstone.betatest.entities.User;
import fpt.capstone.betatest.repositories.NotificationRepository;

@Service
public class NotificationService {
	@Autowired
	private NotificationRepository notificationRepository;
	
	@Transactional
	public Notification save(Notification notification) {
		return notificationRepository.save(notification);
	}
	
	@Transactional
	public int getId(User userName, boolean email, boolean webhook, Date date) {
		return notificationRepository.findByUserAndEmailAndWebhookAndDate(userName, email, webhook, date);
	}
}
