package ODMonitor.App;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.activation.CommandInfo;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import android.util.Log;
import android.webkit.MimeTypeMap;


public class SendMailSSL {
	/*20140117 added by michael
	 * add multipart for attachment*/
	public static String Tag = "SendMailSSL";
	public static MimeMultipart _multipart;
	public static MimeBodyPart messageBodyPart;
	public static FileDataSource attachment_source;
	
	public static void send_mail() {
		Properties props = new Properties();
		/*props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");*/
        props.put("mail.host", "smtp.gmail.com");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.quitwait", "false");
		props.put("mail.transport.protocol", "smtp");
 
		Session session = Session.getDefaultInstance(props,
			new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication("s0914341","david2507");
				}
			});
 
		try {
 
			MimeMessage message = new MimeMessage(session);
			System.out.println("main message MIME type: " + message.getContentType());
			message.setFrom(new InternetAddress("s0914341@gmail.com"));
			//message.setSender(new InternetAddress("simex2001@gmail.com"));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("s0914341@gmail.com"));
			message.setSubject("Testing Subject");
			message.setText("Dear Mail Crawler," + "\n\n No spam to my email, please!");
			System.out.println("main message MIME type: " + message.getContentType());
 
			/*20140119 added by michael
			 * use MailcapCommandMap class to explore the MIME type v.s. data handler*/
			MailcapCommandMap mc = (MailcapCommandMap) MailcapCommandMap.getDefaultCommandMap();
			for (String mime : mc.getMimeTypes()) {
				for (CommandInfo cmdinfo : mc.getAllCommands(mime)) {
					Log.d(Tag, cmdinfo.getCommandClass());
				}
				
				Log.d(Tag, mime);
			}
			
					
			/*20140114 added by michael
			 * add attachment*/
			_multipart = new MimeMultipart();
        	messageBodyPart = new MimeBodyPart();
        	messageBodyPart.setText("Mime body 1");
        	System.out.println("attachment MIME type: " + messageBodyPart.getContentType());
        	/*attachment_source = new FileDataSource(new File("D:\\temp\\android\\android_study\\access_mail\\tmp\\20111007686.jpg"));
        	System.out.println("attachment MIME type: " + attachment_source.getContentType());
            messageBodyPart.setDataHandler(new DataHandler(attachment_source));
            messageBodyPart.setFileName("D:\\temp\\android\\android_study\\access_mail\\tmp\\20111007686.jpg");*/
            _multipart.addBodyPart(messageBodyPart);
            System.out.println("mutiple part MIME type: " + _multipart.getContentType());           
            
        	messageBodyPart = new MimeBodyPart();
        	messageBodyPart.setText("Mime body 2");
        	_multipart.addBodyPart(messageBodyPart);

        	messageBodyPart = new MimeBodyPart();
        	messageBodyPart.setText("Mime body 3");
        	_multipart.addBodyPart(messageBodyPart);

        	messageBodyPart = new MimeBodyPart();
        	messageBodyPart.setDataHandler(new DataHandler("<h1>Mime body 4, This is a html with image test</h1>" + "<img src=\"https://lh5.googleusercontent.com/-Vq8K9cfXofM/T8HGjKvJh8I/AAAAAAAABNI/ivceVe-LCdU/w1548-h870-no/20111007687.jpg\">", "text/html"));
        	_multipart.addBodyPart(messageBodyPart);
        	
        	//System.out.println(MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getSingleton().getFileExtensionFromUrl("file://1.jpg")));
        //	messageBodyPart = new MimeBodyPart();
        //	messageBodyPart.attachFile("D:\\FTDI\\android_workspace\\android_acc_appl\\ODMonitor\\res\\drawable-mdpi\\ok.png", "image/jpg", null);
        //	_multipart.addBodyPart(messageBodyPart);
        	
            message.setContent(_multipart);
			Transport.send(message);
			
			Log.d(Tag, "Done");
 
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}
}
