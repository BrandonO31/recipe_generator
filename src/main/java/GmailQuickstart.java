import java.util.Arrays;
import javax.swing.JOptionPane;
import java.util.Set;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListLabelsResponse;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import javax.mail.MessagingException;

import java.io.ByteArrayOutputStream;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.util.Base64;
import com.google.api.services.gmail.model.Message;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

import com.google.api.client.googleapis.json.GoogleJsonError;

/* class to demonstrate use of Gmail list labels API */

public class GmailQuickstart {
  
  private static final String APPLICATION_NAME = "Gmail API Java Quickstart";
  
  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
  
  private static final String TOKENS_DIRECTORY_PATH = "tokens";

 
  private static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_SEND);
  
  private static final String CREDENTIALS_FILE_PATH = "/client_secret_103951197111-6u9pbh8dsvqvmehrqr5rrn3v8c3ol15t.apps.googleusercontent.com.json";

  /**
   * Creates an authorized Credential object.
   *
   * @param HTTP_TRANSPORT The network HTTP Transport.
   * @return An authorized Credential object.
   * @throws IOException If the credentials.json file cannot be found.
   */
  private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
      throws IOException {
    // Load client secrets.
    InputStream in = GmailQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
    if (in == null) {
      throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
    }
    GoogleClientSecrets clientSecrets =
        GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

    // Build flow and trigger user authorization request.
    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
        .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
        .setAccessType("offline")
        .build();
    LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
    Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    //returns an authorized Credential object.
    return credential;
  }
  
  public static String makeRecipeApiCall(String food) throws IOException {
	  String appId = "0482f7ae"; // Replace with your actual app ID
      String appKey = "7a466c603def43d87939ffc4c0f83bb3"; // Replace with your actual app key
      String query = food; // Replace with your search query
      String type = "public"; // Specify the type parameter

      HttpClient client = HttpClient.newHttpClient();
      String apiUrl = "https://api.edamam.com/api/recipes/v2";
      URI uri = URI.create(apiUrl + "?q=" + query + "&app_id=" + appId + "&app_key=" + appKey + "&type=" + type); // Include the "type" parameter
      HttpRequest request = HttpRequest.newBuilder()
              .uri(uri)
              .GET()
              .build();

      CompletableFuture<HttpResponse<String>> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
      return response.thenApply(res -> {
          int statusCode = res.statusCode();
          String responseBody = res.body();

          System.out.println("Status Code: " + statusCode);
         

          return responseBody; // Return the API response
      }).join();
  }
	   
	




  public static void main(String... args) throws IOException, GeneralSecurityException, MessagingException {
      // Build a new authorized API client service.
      final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
      Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
              .setApplicationName(APPLICATION_NAME)
              .build();

      
      
      
      //JOption for inputs of food and gmail
      boolean val = false;
  	  String address;

      do{
    	  address = JOptionPane.showInputDialog("What is your gmail address?");
          System.out.println(address);
          
          if(address != null && address.endsWith("@gmail.com")) {
          	val = true;
          } else {
          	JOptionPane.showMessageDialog(null, "Please enter a valid address");
          }
      } while(!val);
      
      
      String food = JOptionPane.showInputDialog("What food would you like to create?");
      
      String responseBody = makeRecipeApiCall(food);
      System.out.println(food);
      
      
  	 //tester.initialize(address, food); THIS IS FOR GUI

      
      
      // Declare and initialize email addresses
      String fromEmailAddress = "your.recipe.generator@gmail.com";
      String toEmailAddress = address;

      if (toEmailAddress == null) {
          // The user canceled the input dialog, so we exit gracefully
          System.out.println("Email address input canceled. Exiting.");
          System.exit(0);
      }

      // Create the email content
      String messageSubject = "Test message";
      String bodyText = responseBody;

      // Encode as MIME message
      Properties props = new Properties();
      Session session = Session.getDefaultInstance(props, null);
      MimeMessage email = new MimeMessage(session);
      email.setFrom(new InternetAddress(fromEmailAddress));
      email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(toEmailAddress));
      email.setSubject(messageSubject);
      email.setText(bodyText);

      // Encode and wrap the MIME message into a Gmail message
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      email.writeTo(buffer);
      byte[] rawMessageBytes = buffer.toByteArray();
      String encodedEmail = Base64.encodeBase64URLSafeString(rawMessageBytes);
      Message message = new Message();
      message.setRaw(encodedEmail);

      try {
    	    // Create and send the message
    	    message = service.users().messages().send("me", message).execute(); 
    	    System.out.println("Message id: " + message.getId());
    	    System.out.println(message.toPrettyString());
    	} catch (GoogleJsonResponseException e) {
    	    // Handle the exception appropriately
    	    e.printStackTrace();
    	    GoogleJsonError error = e.getDetails();
    	    if (error.getCode() == 403) {
    	        System.err.println("Unable to send message: " + e.getDetails());
    	    } else {
    	        throw e;
    	    }
    	}


    
  }
}