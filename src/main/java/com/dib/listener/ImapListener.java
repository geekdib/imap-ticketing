package com.dib.listener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.AuthenticationFailedException;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dib.model.CC;
import com.dib.model.FileModel;
import com.dib.model.Status;
import com.dib.model.Ticket;
import com.dib.repository.FileRepository;
import com.dib.repository.StatusRepository;
import com.dib.repository.TicketRepository;
import com.dib.tools.SMTPManager;
import com.dib.tools.TicketManager;
import com.sun.mail.util.BASE64DecoderStream;
import com.sun.mail.imap.IMAPFolder;

@Service
public class ImapListener {

	@Autowired
	private TicketRepository ticketRepository;

	@Autowired
	private FileRepository fileRepository;

	@Autowired
	private StatusRepository statusRepository;

	@Autowired
	private TicketManager ticketManager;

	@Autowired
	private SMTPManager smtpManager;

	@Scheduled(fixedDelay = 10000)
	@Bean
	@Transactional
	public void readInboundEmails() {
		// for storing attachments
		List<BASE64DecoderStream> fileList = new ArrayList<BASE64DecoderStream>();
		// for storing attachment file names
		List<String> filenameA = new ArrayList<String>();
		// for storing cc emails
		Address[] allRecipients = null;

		// getting IMAP session
		Session session = getImapSession();
		session.setDebug(false);

		try {
			// Getting store object from session
			Store store = session.getStore("imap");

			// Connecting to IMAP store
			store.connect("imap.gmail.com", 993, "geekytest.poc", "Cropy@043");
			IMAPFolder inbox = (IMAPFolder) store.getFolder("INBOX");

			// Opening inbox folder with R&W permissions
			inbox.open(Folder.READ_WRITE);

			// Retriving all unread messages
			Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
			for (int i = 0; i < messages.length; i++) {
				Message msg = messages[i];
				msg.setFlag(Flags.Flag.SEEN, true);
				allRecipients = msg.getAllRecipients();

				String subject = msg.getSubject();

				String body = "";
				String from = InternetAddress.toString(msg.getFrom());

				// For emails containing attachments
				if (msg.isMimeType("multipart/*")) {
					Multipart mutli = (Multipart) msg.getContent();
					for (int o = 0; o < mutli.getCount(); o++) {
						Part part = mutli.getBodyPart(o);
						String disposition = part.getDisposition();
						if (disposition != null) {
							MimeBodyPart mimeBodyPart = (MimeBodyPart) part;
							fileList.add((BASE64DecoderStream) mimeBodyPart.getContent());
							String fileName = mimeBodyPart.getFileName();
							File fileToSave = new File(fileName);
							filenameA.add(fileToSave.getName());
						}
						body = this.getTextFromMessage(msg);
					}
				}

				Boolean flag = false;
				int tid = 0;
				String sb = "";

				// If it's a new email it will not have Re: but if it's a reply to existing thread it will contain Re:
				if (subject.contains("Re:")) {
					sb = subject.substring(4);
				} else {
					sb = subject;
				}

				String stre = InternetAddress.toString(msg.getFrom());

				if (ticketRepository.findByTitle(sb).size() > 0) {
					for (Ticket ticket : ticketRepository.findByTitle(sb)) {
						if (ticket.getTicketPusher()
								.equalsIgnoreCase(stre.substring(stre.indexOf('<') + 1, stre.indexOf('>')))) {
							if (!ticket.getStage().equals("CLOSED")) {
								tid = ticket.getId();
								flag = false;
							} else {
								tid = ticket.getId();
								ticketManager.reOpen(tid);
							}
						} else {
							flag = true;
						}
					}
				} else {
					flag = true;
				}

				if (flag) {

					String str = InternetAddress.toString(msg.getFrom());

					int id = 0;

					id = ticketManager.createNewTicket(subject, body, msg.getSentDate(), "OPEN",
							str.substring(str.indexOf('<') + 1, str.indexOf('>')), str.substring(0, str.indexOf('<')),
							true);

					List<CC> ccList = new ArrayList<CC>();
					Ticket ticket = ticketRepository.findById(id);

					for (String s : msg.getHeader("Message-ID")) {
						ticket.setMessageId(s);
					}

					Status status = new Status();
					status.setAuthor(str.substring(str.indexOf('<') + 1, str.indexOf('>')));
					status.setUpdateDate(new Date());
					status.setDescription(body);
					status.setType("user");

					for (Address address : allRecipients) {
						CC cc = new CC();
						cc.setTicketId(tid);
						if (address.toString().contains("<")) {
							cc.setCcEmail(address.toString().substring(address.toString().indexOf('<') + 1,
									address.toString().indexOf('>')));
						} else {
							cc.setCcEmail(address.toString());
						}
						ccList.add(cc);
					}

					ticket.setCcList(ccList);

					if (fileList.size() > 0) {

						String[] files = new String[10];

						for (int o = 0; o < fileList.size(); o++) {
							FileModel file = new FileModel();
							file.setTicketId(ticket.getId());
							file.setAttachmentName(filenameA.get(o));
							file.setAtttachments(IOUtils.toByteArray(fileList.get(o)));
							FileModel model = fileRepository.save(file);
							files[o] = String.valueOf(model.getId());
						}

						ticket.setFileList(files);
						status.setFileList(files);
					}

					List<Status> updates = null;

					if (ticket.getUpdates() != null) {
						updates = ticket.getUpdates();
					} else {
						updates = new ArrayList<Status>();
					}
					updates.add(status);
					ticket.setUpdates(updates);

					statusRepository.save(status);
					ticketRepository.save(ticket);

//					smtpManager.sendWelcomeMail(str.substring(0, str.indexOf('<')),
//							str.substring(str.indexOf('<') + 1, str.indexOf('>')), String.valueOf(id));
				} else {
					if (tid != 0) {
						Status status = new Status();
						status.setAuthor(from.substring(from.indexOf('<') + 1, from.indexOf('>')));
						status.setUpdateDate(new Date());
						status.setDescription(body);
						status.setType("user");
						Status saved = statusRepository.save(status);

						String[] files = new String[10];

						for (int o = 0; o < fileList.size(); o++) {
							FileModel file = new FileModel();
							file.setStatusId(saved.getId());
							file.setAttachmentName(filenameA.get(o));
							file.setAtttachments(IOUtils.toByteArray(fileList.get(o)));
							FileModel model = fileRepository.save(file);
							files[o] = String.valueOf(model.getId());
						}

						status.setFileList(files);
						statusRepository.save(status);

						Ticket activeTicket = ticketRepository.findById(tid);
						activeTicket.setDescription(body);
						activeTicket.setStage("OPEN");

						List<Status> updates = null;

						if (activeTicket.getUpdates() != null) {
							updates = activeTicket.getUpdates();
						} else {
							updates = new ArrayList<Status>();
						}
						updates.add(status);
						activeTicket.setUpdates(updates);

						ticketRepository.save(activeTicket);
					}
				}
			}
		} catch (AuthenticationFailedException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
		String result = "";
		int count = mimeMultipart.getCount();
		for (int i = 0; i < count; i++) {
			BodyPart bodyPart = mimeMultipart.getBodyPart(i);
			if (bodyPart.isMimeType("text/plain")) {
				result = result + "\n" + bodyPart.getContent();
				break; // without break same text appears twice in my tests
			} else if (bodyPart.getContent() instanceof MimeMultipart) {
				result = result + getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
			}
		}
		return result;
	}

	private String getTextFromMessage(Message message) throws MessagingException, IOException {
		String result = "";
		if (message.isMimeType("text/plain")) {
			result = message.getContent().toString();
		} else if (message.isMimeType("multipart/*")) {
			MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
			result = getTextFromMimeMultipart(mimeMultipart);
		}
		return result;
	}

	private Session getImapSession() {
		Properties props = new Properties();
		props.setProperty("mail.store.protocol", "imap");
		props.setProperty("mail.debug", "true");
		props.setProperty("mail.imap.host", "imap.gmail.com");
		props.setProperty("mail.imap.port", "587");
		props.setProperty("mail.imap.ssl.enable", "true");
		props.setProperty("mail.imap.starttls.enable", "true");
		Session session = Session.getDefaultInstance(props, null);
		session.setDebug(true);
		return session;
	}
}