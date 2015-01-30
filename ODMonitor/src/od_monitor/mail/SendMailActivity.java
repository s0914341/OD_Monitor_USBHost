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

//import org.apache.android.mail.GMailSender;
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

public class SendMailActivity extends Activity implements Html.ImageGetter {
    public static final String Tag = "com.example.access_mail";
    File sdcard = Environment.getExternalStorageDirectory();
    public final String iTracker_Data_Dir = sdcard.getPath() + "/iTracker"; 
    File Attachment_folder = new File(iTracker_Data_Dir);
    File Attachment = new File(iTracker_Data_Dir + "/log_20131230-144958.txt");
    File Email_Html_Body = new File(Environment.getExternalStorageDirectory() + "/Downloads/email_body.html");
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_send_mail);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
	//	getMenuInflater().inflate(R.menu.send_mail, menu);
		return true;
	}

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
	
	public void SendMailMethod1() {
		String emailAddress;
		Uri uri;
		Intent intent_send_mail;
		
		/*20140107 added by michael
		 * validation a email address */
		emailAddress = "s0914341@gmail.com";
		Log.d(Tag, Boolean.toString(isEmailAddr(emailAddress)));
		
		if (isEmailAddr(emailAddress))
			uri = Uri.parse("mailto:");
		else
			uri = null;
		
		if (uri != null) {
			//intent_send_mail = new Intent(Intent.ACTION_SENDTO, uri);
			intent_send_mail = new Intent(Intent.ACTION_VIEW, uri);
			intent_send_mail.putExtra(Intent.EXTRA_SUBJECT, "Set object via intent");
			intent_send_mail.putExtra(Intent.EXTRA_TEXT, "MIME type is text/plain");
			//intent_send_mail.setType("text/html");
			//intent_send_mail.setType("text/plain");
			//intent_send_mail.setType("message/rfc822");
			intent_send_mail.putExtra(Intent.EXTRA_EMAIL, new String [] { emailAddress });
			//intent_send_mail.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///mnt/sdcard/Downloads/20111007686.jpg"));
			//intent_send_mail.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///mnt/sdcard/iTracker/log_20131230-144958.txt"));
			//intent_send_mail.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(Attachment));
			SendMailActivity.this.startActivity(intent_send_mail);
		}
	}
	
	public void SendMailMethod2(View v) {
		String emailAddress, cc_emailAddress, hyperlink, img_hyperlink;
		String select_client = "Select a email client";
		Intent intent_send_mail;
		
		/*20140107 added by michael
		 * validation a email address */
		emailAddress = "simex2001@gmail.com";
		cc_emailAddress = "allen@maestrogen.com";
		hyperlink = "<a href=http://www.google.com>google search</a>";
		//hyperlink = "<a href=\"http://www.google.com\">google search</a>";
		//hyperlink = "<a href=\'http://www.google.com\'>google search</a>";
		//img_hyperlink = "<img src=\"http://ppt.cc/3Kz5\" alt=\"1993 camry\">";
		Log.d(Tag, Boolean.toString(isEmailAddr(emailAddress)));
		
		if (isEmailAddr(emailAddress) && isEmailAddr(cc_emailAddress)) {
			//intent_send_mail = new Intent(Intent.ACTION_SEND);
			/*20140108 added by michael
			 * use ACTION_SEND_MULTIPLE to send multiple attachments*/
			intent_send_mail = new Intent(Intent.ACTION_SEND_MULTIPLE);
			intent_send_mail.putExtra(Intent.EXTRA_EMAIL, new String[] { emailAddress });
			//intent_send_mail.putExtra(Intent.EXTRA_CC, new String[] { cc_emailAddress });
			intent_send_mail.putExtra(Intent.EXTRA_SUBJECT, "Set object via intent");
			//intent_send_mail.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(img_hyperlink));
			intent_send_mail.putExtra(Intent.EXTRA_TEXT, Html.fromHtml("MIME type is text/html\r\n message throgh android gmail!\r\n" + hyperlink));
			//intent_send_mail.putExtra(Intent.EXTRA_TEXT, "MIME type is text/plain\n message throgh android gmail!\n" + hyperlink);
			//intent_send_mail.putExtra(Intent.EXTRA_STREAM, Html.fromHtml("MIME type is text/plain\r\n message throgh android gmail!\r\n" + hyperlink));
			//intent_send_mail.setType("text/plain");
			intent_send_mail.setType("text/html");
			//intent_send_mail.setType("message/rfc822");
			//intent_send_mail.setType("police/911");
			
			/*20140108 added by michael
			 * add attachment(s)*/
			//if (Attachment.exists() && Attachment.isFile())
				//intent_send_mail.putExtra(Intent.EXTRA_STREAM, encapsulate_attachment_uri_from_file(Attachment));
			if (Attachment_folder.exists() && Attachment_folder.isDirectory())
				intent_send_mail.putParcelableArrayListExtra(Intent.EXTRA_STREAM, encapsulate_attachment_uri_from_folder(Attachment_folder));
				
			SendMailActivity.this.startActivity(Intent.createChooser(intent_send_mail, select_client));
		}
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
	
	public void SendMailMethod3(View v) {
		String emailAddress, cc_emailAddress, hyperlink, img_hyperlink, email_html_body;
		String select_client = "Select a email client";
		Intent intent_send_mail;
		
		/*20140107 added by michael
		 * validation a email address */
		emailAddress = "simex2001@gmail.com";
		cc_emailAddress = "allen@maestrogen.com";
		hyperlink = "<a href=http://www.google.com>google search</a>";
		Log.d(Tag, Boolean.toString(isEmailAddr(emailAddress)));
		
		if (Email_Html_Body.isFile() && Email_Html_Body.exists()) {
			email_html_body =  read_text_file(Email_Html_Body);
			//email_html_body = "<h1>Mime body 4, This is a html with image test</h1>" + "<img src=\"https://lh5.googleusercontent.com/-Vq8K9cfXofM/T8HGjKvJh8I/AAAAAAAABNI/ivceVe-LCdU/w1548-h870-no/20111007687.jpg\">";
		}
		else
			email_html_body = null;
		if (isEmailAddr(emailAddress) && isEmailAddr(cc_emailAddress)) {
			intent_send_mail = new Intent(Intent.ACTION_SEND);
			intent_send_mail.putExtra(Intent.EXTRA_EMAIL, new String[] { emailAddress });
			//intent_send_mail.putExtra(Intent.EXTRA_CC, new String[] { cc_emailAddress });
			intent_send_mail.putExtra(Intent.EXTRA_SUBJECT, "Set object via intent");
			//intent_send_mail.putExtra(Intent.EXTRA_TEXT, Html.fromHtml("MIME type is text/plain\r\n message throgh android gmail!\r\n" + hyperlink));
			intent_send_mail.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(email_html_body));
			//intent_send_mail.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///mnt/sdcard/Downloads/20111007686.jpg"));
			//intent_send_mail.setType("application/image");
			intent_send_mail.setType("text/html");
			//intent_send_mail.setType("police/911");
			SendMailActivity.this.startActivity(Intent.createChooser(intent_send_mail, select_client));
		}
		
		/*URI uri = null;
		URL url;
		try {
			uri = new URI("mailto:simex2001@gmail.com?subject=" + Uri.encode("test is test"));
			uri = new URI("mailto://simex2001@gmail.com?subject=" + Uri.encode("test is test"));
			uri = new URI("http://android.com/knight");
			uri = new URI("http://android.com/police/");
			//uri = new URI("robots.txt");
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			url = uri.toURL();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
		
	public static boolean isEmailAddr(String emailAddressString) {
		return EmailValidator.getInstance().isValid(emailAddressString);
	}

	//@Override
	public Drawable getDrawable(String source) {
		// TODO Auto-generated method stub
		return null;
	}
}
