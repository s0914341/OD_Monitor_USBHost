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
import java.util.List;

import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import od_monitor.app.ODMonitorActivity.mail_attach_file;
import od_monitor.app.file.FileOperateObject;

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
    public static final String Tag = "SendMailSmtp";
    File sdcard = Environment.getExternalStorageDirectory();
    public final String iTracker_Data_Dir = sdcard.getPath() + "/iTracker"; 
    File Attachment_folder = new File(iTracker_Data_Dir);
    File Attachment = new File(iTracker_Data_Dir + "/log_20131230-144958.txt");
    File Email_Html_Body = new File(Environment.getExternalStorageDirectory() + "/Downloads/email_body.html");
  /*  public static final String userid = "simex2001";
	public static final String password = "zfteshvkmcorttlw";
	public static final String from = "simex2001@gmail.com";
	public static final String subject = "OD experiment data";
	public static final String body = "1.OD chart file, 2.OD data csv file.";
	public static final String to = "software@maestrogen.com";*/
	/*20140117 added by michael
	 * add multipart for attachment*/
	public MimeMultipart _multipart;
	public MimeBodyPart messageBodyPart;
	public DataSource attachment_source;
    
	/*20140108 added by michael
	 * return a  encapsulated URI for attachment¡Aonly local file:/// scheme can be attached to mail */
	/*public Uri encapsulate_attachment_uri(String uri_string) {
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
	}*/
	
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
	
	public void SendMailUseSMTP(final List<mail_attach_file> list_mail_attach) {
		/*20140117 added by michael
		 * from android API level 3.1 or later¡Ait will be forbidden to run the network operation in UI thread*/
		Thread send_mail_thread;
		final String email_html_body;
		final EmailAlertData email_set;
		
		FileOperateObject read_file = new FileOperateObject(EmailAlertData.email_alert_folder_name, EmailAlertData.email_alert_file_name);
	    try {
			read_file.open_read_file(read_file.generate_filename_no_date());
			email_set = (EmailAlertData)read_file.read_file_object();
			if (null == email_set) {
		    	return;
			} else {
				if (email_set.get_fromEmail().equals("") || email_set.get_fromPassword().equals("") || email_set.get_toEmails().equals(""))
					return;
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
	    
		
		/*if (Email_Html_Body.isFile() && Email_Html_Body.exists()) {
			email_html_body =  read_text_file(Email_Html_Body);
		} else
			email_html_body = null;*/
		send_mail_thread = new Thread() {
			public void run() {
				GMailSender sender = new GMailSender(email_set.get_fromEmail(), email_set.get_fromPassword());
		        try {
		        	/*20140121 added by michael
		        	 * mailcap define MIME type v.s. data handler
		        	 * */
		        	
		        	MailcapCommandMap mc = (MailcapCommandMap)MailcapCommandMap.getDefaultCommandMap();
	                mc.addMailcap("text/html;; x-java-content-handler=mycom.sun.mail.handlers.TextHtml");
	                mc.addMailcap("text/xml;; x-java-content-handler=mycom.sun.mail.handlers.TextXml");
	                mc.addMailcap("text/plain;; x-java-content-handler=mycom.sun.mail.handlers.TextPlain");
	                mc.addMailcap("multipart/*;; x-java-content-handler=mycom.sun.mail.handlers.MultipartMixed");
	                mc.addMailcap("message/rfc822;; x-java-content-handler=mycom.sun.mail.handlers.MessageRfc822");
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
		        	
		        	//messageBodyPart.setDataHandler(new DataHandler(email_html_body, "text/html"));
		        	String body = email_set.get_emailBody();
		        	messageBodyPart.setDataHandler(new DataHandler(body, "text/html"));
		            _multipart.addBodyPart(messageBodyPart);
		            
		            for (int i = 0; i < list_mail_attach.size(); i++) {
		        	    messageBodyPart = new MimeBodyPart();
		        	    messageBodyPart.attachFile(list_mail_attach.get(i).file, list_mail_attach.get(i).content_type, null);
		        	    _multipart.addBodyPart(messageBodyPart);
		            }
		         
		            //sender.sendMail_html(subject, email_html_body, from, to);
		        	String from = email_set.get_fromEmail();
		        	String subject = "";
		        	if (list_mail_attach.size() > 0)
		        	    subject = email_set.get_emailSubject() +  list_mail_attach.get(0).mail_alert_type;
		        	else 
		        		subject = email_set.get_emailSubject();
		        	String to = email_set.get_toEmails();
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
