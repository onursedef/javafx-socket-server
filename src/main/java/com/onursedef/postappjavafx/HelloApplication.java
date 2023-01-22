package com.onursedef.postappjavafx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.google.gson.Gson;

public class HelloApplication extends Application {
    private ServerSocket serverSocket;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Create a new server socket
        serverSocket = new ServerSocket(8000);
        System.out.println("Server started on port 8000");

        Label label = new Label("Server starting...");
        StackPane root = new StackPane();
        root.getChildren().add(label);
        Scene scene = new Scene(root, 300, 250);
        primaryStage.setScene(scene);
        primaryStage.show();

        Label label2 = new Label("Onay bekleniyor...");
        Label productName = new Label();
        Label productPrice = new Label();
        Label confirmationText = new Label();
        VBox root2 = new VBox();
        root2.setSpacing(25);
        root2.getChildren().add(label2);
        root2.getChildren().add(confirmationText);
        root2.getChildren().add(productName);
        root2.getChildren().add(productPrice);

        Button acceptButton = new Button("Onayla");
        acceptButton.setStyle("-fx-background-color: #00cc18;");
        acceptButton.applyCss();

        Button declineButton = new Button("Reddet");
        declineButton.setStyle("-fx-background-color: #f54e42;");
        declineButton.applyCss();

        root2.getChildren().add(acceptButton);
        root2.getChildren().add(declineButton);

        root2.setAlignment(javafx.geometry.Pos.CENTER);

        Scene scene2 = new Scene(root2, 480, 500);

        // Create a new thread to handle incoming connections
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    System.out.println("New connection from " + socket.getInetAddress());

                    // Read the incoming message
                    byte[] buffer = new byte[1024];
                    int bytesRead = socket.getInputStream().read(buffer);
                    if (bytesRead > 0) {
                        String message = new String(buffer, 0, bytesRead);
                        System.out.println("Message received from " + socket.getInetAddress() + ": " + message);
                        Gson gson = new Gson();
                        Req req = gson.fromJson(message, Req.class);
                        if (req.getType().equals("BANK_APPROVE_PENDING")) {

                            Platform.runLater(() -> {
                                productName.setText(req.getName());
                                productPrice.setText(String.valueOf(req.getPrice()));
                                confirmationText.setText(req.getPrice() + " tutarında ödeme var, onaylıyor musun?");
                                primaryStage.setScene(scene2);
                                primaryStage.show();
                            });

                            acceptButton.setOnAction(e -> {
                                String respJson = "{\"type\":\"BANK_APPROVE_APPROVED\"}";
                                sendMessage(respJson, socket);
                                StackPane root3 = new StackPane();
                                Label declinedText = new Label("Kabul Edildi");
                                root3.getChildren().add(declinedText);
                                Scene scene3 = new Scene(root3, 480, 500);
                                primaryStage.setScene(scene3);
                                primaryStage.show();
                            });

                            declineButton.setOnAction(e -> {
                                String respJson = "{\"type\":\"BANK_APPROVE_DECLINED\"}";
                                sendMessage(respJson, socket);
                                StackPane root3 = new StackPane();
                                Label declinedText = new Label("Reddedildi");
                                root3.getChildren().add(declinedText);
                                Scene scene3 = new Scene(root3, 480, 500);
                                primaryStage.setScene(scene3);
                                primaryStage.show();
                            });
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();
    }

    private void sendMessage(String message, Socket socket) {
        try {
            DataOutputStream os = new DataOutputStream(socket.getOutputStream());
            os.writeUTF(message);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void stop() throws Exception {
        super.stop();
        serverSocket.close();
    }


    public static void main(String[] args) {
        launch();
    }
}