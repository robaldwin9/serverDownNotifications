package server.down.notifications;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.TextField;
import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Scanner;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.util.Properties;
import java.util.Timer;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

//Pings server url and notifys user when the server is down
//Ronald Baldwin
//1/1/17

/*dev notes:
    program says that it is not responding when it is in fact waiting to access servers again
    Im assuming adding threads for paralell programming will fix this because it will allow users
    to give input while the programming is waiting to ping servers again.
*/
public class ServerDownNotifications extends Application {
 
    @Override
    public void start(Stage primaryStage) {
        VBox primaryPane = new VBox();   //primary pane to contain all nodes
        HBox hContainer = new HBox();   //contains input box + prompt
        HBox hContainer1 = new HBox();  //contains input box + prompt
        HBox btnContainer = new HBox();//contains buttons
        btnContainer.setAlignment(Pos.CENTER);
 
        //explains functionality in gui
        Label lblPrompt = new Label("Add email to recive notifications (ex: rjservers.com@gmail.com)");
        Label lblPrompt1 = new Label("Add a server for observence of uptime (ex: www.rjservers.com");
        
        //allows for user input
        TextField ipInput = new TextField();
        TextField emailInput = new TextField();
        
        //initialize buttons
        Button btnAddServer = new Button("Add Server");
        Button btnAddEmail = new Button("Add Email");
        Button btnStart = new Button("Start Listening");
        
        //add nodes to containers then create scene
        hContainer.getChildren().addAll(emailInput,lblPrompt);
        hContainer1.getChildren().addAll(ipInput, lblPrompt1);
        btnContainer.getChildren().addAll(btnAddServer,btnAddEmail,btnStart);
        primaryPane.getChildren().addAll(hContainer,hContainer1,btnContainer);
        Scene primaryScene = new Scene(primaryPane,500,80);
        
       //show the windows
        primaryStage.setTitle("Flag Server Down");
        primaryStage.setScene(primaryScene);
        primaryStage.show();
        
        //inner class object to handle events
        EventMethods eventMethods = new EventMethods();
        
        //Event Handlers
        btnAddServer.setOnAction(event -> //add server to file list
        {
            eventMethods.addServer(ipInput);
        });
        
        btnAddEmail.setOnAction(event -> //add and save email for notifications
        {
            eventMethods.addEmail(emailInput);
        });
        //check servers in 5 minute intervals to see if they have failed end loop and notify user when failure occurs
        btnStart.setOnAction(event -> 
        {
            
       while(!eventMethods.Listen()) 
       {
             try
             {
                TimeUnit.MINUTES.sleep(5);
             }
             catch(Exception e)
             {
                 //not sure what to do if sleep method fails
             }
        }
    });
}
    

    //class holding methods for gui events
     class EventMethods
    {
         //makes email file and stores user input
        public void addEmail(TextField emailInput)
        {
        
              File emailList = new File("emailList.txt"); 
            try{
                PrintWriter emailOutput = new PrintWriter("emailList.txt");
                if(!emailInput.getText().equals(""))
                   {
                        emailOutput.println(emailInput.getText());
                        emailOutput.close();
                        emailInput.setText("");
                    }
                        else
                            {
                                CustomPopUp fileError = new CustomPopUp("email field was left empty"
                                ,"invalid input", "ok", 250,50);
                                fileError.show();
                            }
                    
            }
          catch(IOException ex)
          {
              CustomPopUp fileError = new CustomPopUp("could not find emailList.txt"
              , "File error", "ok", 200,200);
              fileError.show();
          }
        }
        
        //adds server to list of servers to check for uptime
        public void addServer(TextField ipInput)
        {
        
            File serverList = new File("serverList.txt"); //file to hold web servers to be checked
            //add server to file
            try{
                if(serverList.exists())
                {
                    PrintWriter ipOutput = new PrintWriter(new FileOutputStream(serverList,true));
                        if(!ipInput.getText().equals("")) // makes sure user has entered data
                            {
                                ipOutput.println(ipInput.getText());
                                ipInput.setText("");
                            }
                        else
                            {
                                CustomPopUp fileError = new CustomPopUp("server Field was left empty"
                                ,"invalid input", "ok", 250,50);
                                fileError.show(); 
                            }
              
                   ipOutput.close();
                    
                }
                
                else
                    {
                        PrintWriter ipOutput = new PrintWriter("serverList.txt");
                        if(!ipInput.getText().equals(""))
                            {
                                ipOutput.println(ipInput.getText());
                                ipOutput.close();
                                ipInput.setText("");
                            }
                        else
                            {
                                CustomPopUp fileError = new CustomPopUp("server Field was left empty"
                                ,"invalid input", "ok", 250,50);
                                fileError.show();
                            }
                    }
            }
          catch(IOException ex)
          {
              CustomPopUp fileError = new CustomPopUp("could not find serverList.txt"
              , "File error", "ok", 250,50);
              fileError.show();
          }
        
        
        
        
        }
        
       //checks if server is up. returns true if server not reached
        public boolean Listen() 
        {
            boolean serverDown = false; // flag that is returned 
            try
            {
            Scanner servers = new Scanner( new File("serverList.txt")); //read from file
            
           
            while(servers.hasNextLine())//till end of file try to connect to servers if failure notify user
            {
                String currentLine = servers.nextLine(); //stores each line of file temperorairly
                if(!reachable(currentLine)) //if site isnt reached send notification
                {
                    serverDown = true; //change flag because web server unreachable
                    System.out.println("error");
                    sendEmail(currentLine);
                     CustomPopUp fileError = new CustomPopUp(currentLine + " web service down"
                    , "Web Service Down", "ok", 250,50);
                     fileError.show();
                     
                }
                else
                    System.out.println("loop again");
                    
            }
            
            }
            
            catch(IOException e)
            {
              CustomPopUp fileError = new CustomPopUp("problem reading files"
              ,"Input Output error", "ok", 250,50);
              fileError.show();
            }
            return serverDown;
        }
        
        public Boolean reachable(String url) //tells us if a url is reachable
        {
              try
                {
                    InetAddress address = InetAddress.getByName(url);
                    return address.isReachable(1000);
              
                }
                catch(IOException e)
                {
                    return false;
                  
                } 
            
            
        }
        
        public void sendEmail(String affectedServer)
        {
            try
            {
             //open file get recipient email
             Scanner servers = new Scanner( new File("emailList.txt"));
             String to = servers.nextLine();
             
             //email made for notifying users via google smtp
             String sender = "rjservers.com@gmail.com";
             String pass = "Gears177!";
             
             //email properties
             Properties properties = System.getProperties();
             properties.setProperty("mail.smtp.host", "smtp.gmail.com");
             properties.put("mail.smtp.socketFactory.port", "465");
             properties.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
             properties.put("mail.smtp.port","465");
             properties.put("mail.smtp.auth","true");
             
             //include authentication from google
             //create session using properties
             Session session = Session.getDefaultInstance(properties,
                 new javax.mail.Authenticator()
                    {
                        protected PasswordAuthentication getPasswordAuthentication(){
                        return new PasswordAuthentication(sender,pass) ;
               
                    }
                    });
           
                //create message using session with properties
               MimeMessage message = new MimeMessage(session);
               
               try{
                   //create message
                    message.setFrom(new InternetAddress(sender));
                    message.setRecipients(Message.RecipientType.TO, to);
                    message.setSubject("server down");
                    message.setText("web service down at " + affectedServer);
               
                    Transport.send(message); // send message
               
               }
                
               catch(MessagingException e)
            {
              CustomPopUp fileError = new CustomPopUp("could not send email"
              , "Message Error", "ok", 250,50);
              fileError.show();
             
            }
                }
     
         catch(IOException event)
            {
                CustomPopUp fileError = new CustomPopUp("Issue reading emailList"
              , "File error", "ok", 250,50);
              fileError.show();
            }
        }
     }
}
