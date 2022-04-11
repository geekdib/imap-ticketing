package com.dib.tools;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dib.model.CC;
import com.dib.model.Ticket;
import com.dib.repository.TicketRepository;

@Service
public class SMTPManager {

	@Autowired
	private TicketRepository ticketRepository;


	public void sendWelcomeMail(String name, String email, String TID) throws AddressException, MessagingException {
		Properties props = new Properties();
		Ticket ticket = ticketRepository.findById(Integer.parseInt(TID));
		Session session = Session.getInstance(props,
				new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication("geekytest.poc", "Geek@123");
			}
		});

		Message mailMessage = new MimeMessage(session);
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", 587);
		mailMessage.addRecipients(Message.RecipientType.TO,
				InternetAddress.parse(email));

		if(ticket.getMessageId()!=null) {
			mailMessage.setHeader("In-Reply-To", ticket.getMessageId());
			mailMessage.setHeader("References", ticket.getMessageId());
		}

		mailMessage.saveChanges();
		
		if(ticketRepository.findById(Integer.parseInt(TID)).getCcList().size()>0) {
			for(CC cc : ticketRepository.findById(Integer.parseInt(TID)).getCcList()) {
				mailMessage.addRecipients(Message.RecipientType.CC, InternetAddress.parse(cc.getCcEmail()));
			}
		}

		mailMessage.setSubject(ticket.getTitle());
		
		mailMessage.setContent("Your ticket has been created", "text/html");
		mailMessage.setFrom(new InternetAddress("geekytest.poc@gmail.com"));

		Transport.send(mailMessage);
	}

}