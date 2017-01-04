/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.down.notifications;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author robal
 */
public class CustomPopUp 
 {
Stage stage;
Button btn;
TextField input;
Label prompt;
VBox pane;
Scene scene;
    
  
    
    //constructors
    public CustomPopUp() //defualut
    {
       
        stage = new Stage();
        btn = new Button("ok");
        prompt = new Label("Error");
        pane = new VBox();
        pane.getChildren().addAll(prompt,btn);
         pane.setAlignment(Pos.CENTER);
        scene = new Scene(pane,300,70);
        stage.setTitle("Error");
        stage.setScene(scene);
        
        btn.setOnAction(event ->
        {
          this.stage.close();
        });
        
    
    
    }
    public CustomPopUp(int height, int width) //specify size
    {
          stage = new Stage();
        btn = new Button("ok");
        prompt = new Label("Error");
        pane = new VBox();
        pane.getChildren().addAll(prompt,btn);
        pane.setAlignment(Pos.CENTER);
        scene = new Scene(pane,height,width);
        
        stage.setTitle("Error");
        stage.setScene(scene);
        
        btn.setOnAction(event ->
        {
          this.stage.close();
        });
        
    }
    //specify size and text
    public CustomPopUp(String lblText, String titleText, String btnText, int height, int width) 
    {
                 
        stage = new Stage();
        btn = new Button(btnText);
        prompt = new Label(lblText);
        pane = new VBox();
        pane.getChildren().addAll(prompt,btn);
          pane.setAlignment(Pos.CENTER);
        scene = new Scene(pane,height,width);
        stage.setTitle(titleText);
        stage.setScene(scene);
        
        btn.setOnAction(event ->
        {
          this.stage.close();
        }); 
                
    }
    
    public void show(){this.stage.show();} //show popp
    public void close(){this.stage.close();}//close popUp
    
  }   

