package com.mycompany.discordserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import model.Draw;

public class SecundaryController implements Initializable{
    @FXML
    private Canvas canva;
    
    
    //GRAFICOS
    GraphicsContext gc;
    ObjectMapper objectMapper = new ObjectMapper();
    
    //SERVIDOR
    Channel channel;
    private static final String QUEUE_NAME = "discordPaint";
    
    

    
    public void close(){
        System.exit(0);
    }
    
    public void primary() throws IOException{
        App.setRoot("primary");
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        gc = canva.getGraphicsContext2D();
        canva.setOnMousePressed(e -> {
            gc.beginPath();
            gc.moveTo(e.getX(), e.getY());
            gc.stroke();
            Draw render = new Draw("DRAW", e.getX(), e.getY(), "PRESSED");
            try {
                String json = objectMapper.writeValueAsString(render);
                channel.basicPublish(QUEUE_NAME, "", null, json.getBytes("UTF-8"));

            } catch (JsonProcessingException ex) {
                Logger.getLogger(SecundaryController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(SecundaryController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        canva.setOnMouseDragged(e -> {
            gc.lineTo(e.getX(), e.getY());
            gc.stroke();
            Draw render = new Draw("DRAW", e.getX(), e.getY(), "DRAGGED");
            try {
                String json = objectMapper.writeValueAsString(render);
                channel.basicPublish(QUEUE_NAME, "", null, json.getBytes("UTF-8"));

            } catch (JsonProcessingException ex) {
                Logger.getLogger(SecundaryController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(SecundaryController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        
        //conectanto al servidor
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost"); // Conectarse a localhost
        try{
            Connection connection = factory.newConnection();
            channel = connection.createChannel();
            channel.exchangeDeclare("discordPaint", "fanout");
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, QUEUE_NAME, "");
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println(message);
                Draw render = objectMapper.readValue(message, Draw.class);
                System.out.println(render.getX());
                Platform.runLater(() -> {
                    if("DRAGGED".equals(render.getType())){
                        gc.lineTo(render.getX(), render.getY());
                        gc.stroke();
                    } else if("PRESSED".equals(render.getType())){
                        gc.beginPath();
                        gc.moveTo(render.getX(), render.getY());
                        gc.stroke();
                    }
                    
                });
                
            };
            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
        } catch(IOException | TimeoutException e){
            
        }
    }
    
    
}
