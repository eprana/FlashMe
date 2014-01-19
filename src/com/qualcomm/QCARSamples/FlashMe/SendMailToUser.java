package com.qualcomm.QCARSamples.FlashMe;

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

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class SendMailToUser {
	
    private final static String applimail = "flashme.app@gmail.com";
    private final static String applipass = "flashmeapp";
    private Context context;

    public SendMailToUser(Context ctx){
    	this.context = ctx;
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
        }
     
        @Override
        protected Void doInBackground(Message... messages) {
            try {
                Transport.send(messages[0]);
            } catch (MessagingException e) {
                e.printStackTrace();
                Toast.makeText(context, "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show();
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
            e.printStackTrace();
            Toast.makeText(context, "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (MessagingException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
}
