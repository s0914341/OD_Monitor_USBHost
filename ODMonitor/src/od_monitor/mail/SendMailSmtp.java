package od_monitor.mail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import org.apache.android.mail.GMailSender;
import org.apache.commons.validator.routines.EmailValidator;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.MimeTypeMap;

public class SendMailSmtp implements Html.ImageGetter {
    public static final String Tag = "com.example.access_mail";
    File sdcard = Environment.getExternalStorageDirectory();
    public final String iTracker_Data_Dir = sdcard.getPath() + "/iTracker"; 
    File Attachment_folder = new File(iTracker_Data_Dir);
    File Attachment = new File(iTracker_Data_Dir + "/log_20131230-144958.txt");
    File Email_Html_Body = new File(Environment.getExternalStorageDirectory() + "/Downloads/email_body.html");

	/*20140108 added by michael
	 * return a  encapsulated URI for attachment¡Aonly local file:/// scheme can be attached to mail */
	public Uri encapsulate_attachment_uri(String uri_string) {
		return Uri.parse(uri_string);
	}
	
	public Uri encapsulate_attachment_uri_from_file(File file) {
		return Uri.fromFile(file);
	}
	
	public ArrayList<Uri>  encapsulate_attachment_uri_from_folder(File directory) {
		int i = 0;
		File[] fileList = directory.listFiles();
		ArrayList<Uri> uris = new ArrayList<Uri>();
			
		if (fileList != null) {
		  for (i=0; i < fileList.length; i++) {
			  uris.add(Uri.fromFile(fileList[i]));
		  }
		}
		return uris;
	}
	
	public String read_text_file(File file) {
		StringBuilder text;
		String line;
		
	      text = new StringBuilder();
	      if (file != null) {
	    	  try {
				BufferedReader buf  = new BufferedReader(new FileReader(file));
	            try {
					while ((line = buf.readLine()) != null) {
						text.append(line);
						text.append('\n');
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	      }
	      
	      return text.toString();
	} 
	
	/*20140116 added by michael*/
	public static final String userid = "simex2001";
	public static final String password = "zfteshvkmcorttlw";
	public static final String from = "simex2001@gmail.com";
	public static final String to = "software@maestrogen.com";
	public static final String subject = "this is a test";
	public static final String body = "knight rider, hello android!";
	/*20140117 added by michael
	 * add multipart for attachment*/
	public MimeMultipart _multipart;
	public MimeBodyPart messageBodyPart;
	public DataSource attachment_source;
	
	public void SendMailSMTPMethod1() {
		/*20140117 added by michael
		 * from android API level 3.1 or later¡Ait will be forbidden to run the network operation in UI thread*/
		Thread send_mail_thread;
		final String email_html_body;
		
		if (Email_Html_Body.isFile() && Email_Html_Body.exists()) {
			email_html_body =  read_text_file(Email_Html_Body);
		} else
			email_html_body = null;
		    send_mail_thread = new Thread() {
			public void run() {
				GMailSender sender = new GMailSender(userid, password);
		        try {
		        	/*20140121 added by michael
		        	 * mailcap define MIME type v.s. data handler
		        	 * */
		        	
		        	MailcapCommandMap mc = (MailcapCommandMap)MailcapCommandMap.getDefaultCommandMap();
	                mc.addMailcap("text/html;; x-java-content-handler=mycom.sun.mail.handlers.text_html");
	                mc.addMailcap("text/xml;; x-java-content-handler=mycom.sun.mail.handlers.text_xml");
	                mc.addMailcap("text/plain;; x-java-content-handler=mycom.sun.mail.handlers.text_plain");
	                mc.addMailcap("multipart/*;; x-java-content-handler=mycom.sun.mail.handlers.multipart_mixed");
	                mc.addMailcap("message/rfc822;; x-java-content-handler=mycom.sun.mail.handlers.message_rfc822");
	                mc.addMailcap("image/jpeg;; x-java-content-handler=com.sun.mail.handlers.image_jpeg");
	                mc.addMailcap("image/jpg;; x-java-content-handler=com.sun.mail.handlers.image_jpg");
	                mc.addMailcap("image/gif;; x-java-content-handler=com.sun.mail.handlers.image_gif");
	                CommandMap.setDefaultCommandMap(mc);
	                
		            //sender.sendMail(subject, body, from, to);
		        	_multipart = new MimeMultipart();
		        	messageBodyPart = new MimeBodyPart(); 
		        	//attachment_source = new FileDataSource(new File("/mnt/sdcard/Downloads/20111007686.jpg"));
		            //messageBodyPart.setDataHandler(new DataHandler(attachment_source));
		            //messageBodyPart.setFileName("/mnt/sdcard/Downloads/20111007686.jpg");
		        	//messageBodyPart.attachFile("/mnt/sdcard/Downloads/20111007686.jpg", "image/jpg", null);
		        	
		        	messageBodyPart.setDataHandler(new DataHandler(email_html_body, "text/html"));
		            _multipart.addBodyPart(messageBodyPart);
		            
		        	messageBodyPart = new MimeBodyPart();
		        	messageBodyPart.attachFile("//mnt//sdcard//od_chart//chart20150203.png", "image/png", null);
		        	_multipart.addBodyPart(messageBodyPart);
		        	
		        	messageBodyPart = new MimeBodyPart();
		        	messageBodyPart.attachFile("//mnt//sdcard//myfile.csv", "text/csv", null);
		        	_multipart.addBodyPart(messageBodyPart);

		            //sender.sendMail_html(subject, email_html_body, from, to);
		            sender.sendMail_html_with_attachment(subject, body, from, to, _multipart);
		        } catch (Exception e) {
		            Log.e("SendMail failed", e.getMessage(), e);
		        }				
			}
	
		};
		
		send_mail_thread.start();
	}
		
	public static boolean isEmailAddr(String emailAddressString) {
		return EmailValidator.getInstance().isValid(emailAddressString);
	}

	public Drawable getDrawable(String source) {
		// TODO Auto-generated method stub
		return null;
	}
}
