package com.imac.FlashMe;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.imac.FlashMe.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class SendMailToUser {
	
    private final static String applimail = "flashme.ifyoucan@gmail.com";
    private final static String applipass = "flashmetamere";
    private Context context;
    private int exception = 0;
    private View alertDialogView;
    private LayoutInflater inflater;

    public SendMailToUser(Context ctx){
    	this.context = ctx;
    	inflater = LayoutInflater.from(context);
    }
    
    private Session createSessionObject() {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
     
        return Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(applimail, applipass);
            }
        });
    }
    
    private Message createMessage(String email, String subject, String messageBody, Session session) throws MessagingException, UnsupportedEncodingException {
        Message message = new MimeMessage(session);
        message.setContent(messageBody, "text/html");
        message.setFrom(new InternetAddress("flashme.app@gmail.com", "Flash Me"));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(email, email));
        message.setSubject(subject);
        message.setText(messageBody);
        return message;
    }
    
    private class SendMailTask extends AsyncTask<Message, Void, Void> {
     
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
     
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(exception < 0){
            	// Create an alert box
				AlertDialog.Builder adb = new AlertDialog.Builder(context);
				MessageAlert msg_a;
				
				if (alertDialogView == null) {
					msg_a = new MessageAlert();
					alertDialogView = inflater.inflate(R.layout.alert_dialog, null);
					msg_a.msg = (TextView)alertDialogView.findViewById(R.id.text_alert);
					alertDialogView.setTag(msg_a);
				} else {
					msg_a = (MessageAlert) alertDialogView.getTag();
	            	ViewGroup adbParent = (ViewGroup) alertDialogView.getParent();
					adbParent.removeView(alertDialogView);
				}
				
				// Choosing the type of message alert
				msg_a.msg.setText(context.getResources().getString(R.string.error_mail));				
				
				// Filling the alert box
				adb.setView(alertDialogView);
				adb.setTitle("Error !");
				adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int which) {
		        } });
				
				// Showing the alert box
		        adb.create();
				adb.show();
            }
        }
     
        @Override
        protected Void doInBackground(Message... messages) {
            try {
                Transport.send(messages[0]);
            } catch (MessagingException e) {
                e.printStackTrace();
            	exception = -1;
                //Toast.makeText(context, "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            return null;
        }
    }
    
    public void sendMail(String email, String subject, String messageBody) {
        Session session = createSessionObject();
     
        try {
            Message message = createMessage(email, subject, messageBody, session);
            new SendMailTask().execute(message);
        } catch (AddressException e) {
        	exception = -1;
            e.printStackTrace();
            //Toast.makeText(context, "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (MessagingException e) {
            e.printStackTrace();
            exception = -1;
            //Toast.makeText(context, "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            exception = -1;
            //Toast.makeText(context, "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    public void setException(int e){
    	exception = e;
    }
    
    public int getException(){
    	return exception;
    }
    
}
