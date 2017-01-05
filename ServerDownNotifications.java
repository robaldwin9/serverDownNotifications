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
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

//Pings server url and notifys user when the server is down
//Ronald Baldwin
//1/5/17

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
        TextField urlInput = new TextField();
        TextField emailInput = new TextField();
        
        //initialize buttons
        Button btnAddServer = new Button("Add Server");
        Button btnAddEmail = new Button("Add Email");
        Button btnStart = new Button("Start Listening");

        //add nodes to containers then create scene
        hContainer.getChildren().addAll(emailInput,lblPrompt);
        hContainer1.getChildren().addAll(urlInput, lblPrompt1);
        btnContainer.getChildren().addAll(btnAddServer,btnAddEmail,btnStart);
        primaryPane.getChildren().addAll(hContainer,hContainer1,btnContainer);
        Scene primaryScene = new Scene(primaryPane,500,80);
        
       //show the window
        primaryStage.setTitle("Flag Server Down");
        primaryStage.setScene(primaryScene);
        primaryStage.show();

        //inner class object to handle events
        EventMethods eventMethods = new EventMethods();

         //Event Handlers
        btnAddServer.setOnAction(event -> //add server to file list
        {
            eventMethods.addServer(urlInput);
        });
        
        //add and save email for notifications
        btnAddEmail.setOnAction(event -> 
        {

            eventMethods.addEmail(emailInput);
        });

        //check servers in 5 minute intervals to see if they have failed end loop and notify user when failure occurs
        btnStart.setOnAction(event -> 
        {
            Runnable listen = new EventMethods();
            Thread listenThread = new Thread(listen);
            listenThread.start();
        });
}

//class holding methods for gui events
class EventMethods implements Runnable
    {
    //thread runs this
        @Override
        public void run()
        {
           //while servers are not down sleep for 5 minutes then try again
            while(!Listen())
            {
                   try
                {
                     TimeUnit.MINUTES.sleep(5);
                }

                catch(Exception e)
                {        //tells gui thread to make changes to userInterface javaFx requires this
                         Platform.runLater(new Runnable() 
                        {
                            @Override public void run() 
                            {
                                 CustomPopUp fileError = new CustomPopUp("Listen thread failed",
                                "thread failed", "ok", 250,50);
                                fileError.show();
                            }
                        });
                }  
                   
            }
            
        }
        //makes email file and stores user input
        public void addEmail(TextField emailInput)
        {
            File emailList = new File("emailList.txt"); 
            try
            {
                PrintWriter emailOutput = new PrintWriter("emailList.txt");
                if(!emailInput.getText().equals(""))
                {
                    emailOutput.print(emailInput.getText());
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
       public void addServer(TextField urlInput)
        {
            File serverList = new File("serverList.txt"); //file to hold web servers to be checked

            //add server to file
            try
            {
                if(serverList.exists())
                {
                    PrintWriter urlOutput = new PrintWriter(new FileOutputStream(serverList,true));
                    if(!urlInput.getText().equals("")) // makes sure user has entered data
                    {
                        urlOutput.print(urlInput.getText());
                        urlInput.setText("");

                    }

                    else
                    {
                        CustomPopUp fileError = new CustomPopUp("server Field was left empty"
                        ,"invalid input", "ok", 250,50);
                        fileError.show(); 

                    }
            urlOutput.close();
                
                }
                
                else
                {
                    PrintWriter urlOutput = new PrintWriter("serverList.txt");
                    if(!urlInput.getText().equals(""))
                    {
                        urlOutput.println(urlInput.getText());
                        urlOutput.close();
                        urlInput.setText("");
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
            System.out.println("listening");
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
                        sendEmail(currentLine);
                        
                        Platform.runLater(new Runnable() 
                        {
                            @Override public void run() 
                            {
                                 CustomPopUp fileError = new CustomPopUp("Email sent regaurding " + currentLine,
                                "Server Down", "ok", 250,50);
                                fileError.show();
                            }
                        });
                    }

                    
                }
            }
            catch(IOException e)
            {
                  Platform.runLater(new Runnable() 
                  {
                     @Override public void run() 
                      {
                         CustomPopUp fileError = new CustomPopUp("problem reading serverList.txt",
                        "Input Output error", "ok", 250,50);
                        fileError.show();
                      }
                  });
              }
                return serverDown;
        }
        
        //tells us if a url is reachable
        public Boolean reachable(String url) 
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
        
        //Send message to notify user
        public void sendEmail(String affectedServer)
        {
            try
            {
                //open file get recipient email
                Scanner servers = new Scanner( new File("emailList.txt"));
                String to = servers.nextLine();

                //email made for notifying users via google smtp
                String sender = "some email"; "error for the sake of showing you that you need valid strings here"
                String pass = "some password";

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
                    protected PasswordAuthentication getPasswordAuthentication()
                        {
                            return new PasswordAuthentication(sender,pass) ;
                        }

                });

             //create message using session with properties
            MimeMessage message = new MimeMessage(session);
            try{
                    //change message contents and send
                    message.setFrom(new InternetAddress(sender));
                    message.setRecipients(Message.RecipientType.TO, to);
                    message.setSubject("server down");
                    message.setText("web service down at " + affectedServer);
                    Transport.send(message); // send message
                }
            catch(MessagingException e)
            {
                    Platform.runLater(new Runnable() 
                  {
                     @Override public void run() 
                      {
                        CustomPopUp fileError = new CustomPopUp("could not send email"
                        ,"Message Error", "ok", 250,50);
                        fileError.show();
                      }
                  });
                
            }

        }

            catch(IOException event)
            {
                    Platform.runLater(new Runnable() 
                  {
                     @Override public void run() 
                      {
                        CustomPopUp fileError = new CustomPopUp("Issue reading emailList"
                        , "File error", "ok", 250,50);
                         fileError.show();
                      }
                  });
                
            }
        }
    }
}
