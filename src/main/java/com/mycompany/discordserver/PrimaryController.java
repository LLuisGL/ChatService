package com.mycompany.discordserver;

import com.rabbitmq.client.*;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.TimeoutException;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class PrimaryController implements Initializable{
    @FXML
    private TextField txtFieldNombre;

    @FXML
    private Label txtNombre;
    
    @FXML
    private TextField txtFieldMensaje;
    
    @FXML
    private VBox VBoxChat;
    
    @FXML
    private Label flag;
    
    Channel channel;
    
    //CONEXION DE LA QUEUE
    private static final String QUEUE_NAME = "chatDiscord";
    
    public void conectarServidor(){
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost"); // Conectarse a localhost
        
        try {
            Connection connection = factory.newConnection();
            channel = connection.createChannel();
            channel.exchangeDeclare("chatDiscord", "fanout");
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, QUEUE_NAME, "");

            // Configurar recepción de mensajes
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                String messageNuevo = borrarNombre(message);
                Platform.runLater(() -> {
                    if(VBoxChat.getChildren().size() >= 9){
                        VBoxChat.getChildren().remove(0);
                    }
                    HBox contenedorMensaje = new HBox(); 
                    VBox contenedorInfo = new VBox();
                    VBox mensajeBackground = new VBox();
                    Label labelNombre = new Label(encontrarNombre(message));
                    Label labelMensaje = new Label(messageNuevo);
                    mensajeBackground.setStyle("-fx-background-color: #32353C; -fx-background-radius: 25px;");
                    mensajeBackground.setAlignment(Pos.CENTER);
                    labelMensaje.setStyle("-fx-text-fill: #FFFFFF;-fx-font: 14px Arial;");
                    labelNombre.setStyle("-fx-text-fill: #FFFFFF;-fx-font-weight:bold;");
                    contenedorMensaje.setPrefHeight(40);
                    mensajeBackground.setPrefWidth(renderizarMensaje(messageNuevo));
                    mensajeBackground.setPrefHeight(40);
                    mensajeBackground.getChildren().add(labelMensaje);
                    if(!flag.getText().equals(encontrarNombre(message))){
                        contenedorInfo.getChildren().add(labelNombre);
                        contenedorMensaje.setPrefHeight(60);
                    }
                    contenedorInfo.getChildren().add(mensajeBackground);
                    contenedorMensaje.getChildren().add(contenedorInfo);
                    VBox.setMargin(contenedorMensaje, new Insets(2));
                    if(encontrarNombre(message).equals(txtNombre.getText())){
                        contenedorMensaje.setAlignment(Pos.CENTER_RIGHT);
                        contenedorInfo.setAlignment(Pos.CENTER_RIGHT);
                        VBox.setMargin(labelNombre, new Insets(0,5,0,0));
                    } else if(encontrarNombre(message).equals("MSGJOIN")){
                        contenedorMensaje.setPrefHeight(40);
                        contenedorInfo.getChildren().remove(labelNombre);
                        contenedorMensaje.setAlignment(Pos.CENTER);
                        labelMensaje.setStyle("-fx-text-fill: #FFFFFF;-fx-font: 14px Arial;-fx-font-weight: bold;");
                        mensajeBackground.setStyle("-fx-background-color: none; -fx-background-radius: 25px;");
                    }
                    VBoxChat.getChildren().add(contenedorMensaje);  
                    flag.setText(encontrarNombre(message));
                });
            };
            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});

        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
        try {
            channel.basicPublish(QUEUE_NAME, "", null, ("[MSGJOIN]" + "Se ha unido " + txtNombre.getText() + ".").getBytes("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void close(){
        try {
            channel.basicPublish(QUEUE_NAME, "", null, ("[MSGJOIN]" + txtNombre.getText() + " ha salido.").getBytes("UTF-8"));
            channel.close();
        } catch (IOException e) {
        } catch (TimeoutException ex) {
        } finally {
            System.exit(0);
        }
        
    }
    
    public void cambiarNombre(){
        txtNombre.setText(txtFieldNombre.getText());
    }
    
    public void enviarMensaje(){
        String message = "[" + txtNombre.getText() + "]" + txtFieldMensaje.getText();
        if (!message.isEmpty()) {
            try {
                channel.basicPublish(QUEUE_NAME, "", null, message.getBytes("UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public int renderizarMensaje(String message){
        int length = 30;
        for(int i=0; i<message.length(); i++){
            length += 8;
        }
        return length;
    }
    
    public String encontrarNombre(String message){
        String nombre = "";
        for(int i=1; i<message.length(); i++){
            if( message.charAt(i) != ']'){
                nombre += message.charAt(i);
            } else{
                break;
            }
        }
        return nombre;
    }
    
    public String borrarNombre(String message){
        String nombreUsuario = encontrarNombre(message);
        int lenght = nombreUsuario.length() + 2;
        String mensajeBorrado = "";
        for(int i = lenght; i<message.length(); i++){
            mensajeBorrado += message.charAt(i);
        }
        return mensajeBorrado;
    }
    
    public void secundary() throws IOException{
        try {
            channel.basicPublish(QUEUE_NAME, "", null, ("[MSGJOIN]" + txtNombre.getText() + " ha salido.").getBytes("UTF-8"));
            channel.close();
        } catch (IOException e) {
        } catch (TimeoutException ex) {
        } finally {
            App.setRoot("secundary");
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        flag.setOpacity(0);
        flag.setText("0");
        
        //Detectar las teclas
        txtFieldMensaje.setOnKeyPressed(new EventHandler<KeyEvent>(){
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER && !"".equals(txtFieldMensaje.getText())) {
                    enviarMensaje();
                    txtFieldMensaje.clear();
                }
            }
        });
        
    }
}
