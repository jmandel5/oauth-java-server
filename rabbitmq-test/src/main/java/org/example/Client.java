package org.example;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.TimeoutException;

public class Client {

    public static void main (String[] args ) throws IOException, TimeoutException {

        //This is simulating a client on a web app

        //Simulate sending API request to server
//        ConnectionFactory factoryCtoS = new ConnectionFactory();
//        try (Connection connectionCtoS = factoryCtoS.newConnection() ) {
//            Channel channelCtoS = connectionCtoS.createChannel();
//            channelCtoS.queueDeclare("Client-to-Server",false,false,false,null);
//
//            String message = "I am sending the API request info " + LocalDateTime.now();
//
//            channelCtoS.basicPublish("","Client-to-Server",false,null, message.getBytes());
//
//            System.out.println("!! The API request info has been sent");
//        }

        //Simulating receiving api response from server
//        ConnectionFactory factoryStoC = new ConnectionFactory();
//
//        Connection connectionStoC = factoryStoC.newConnection();
//        Channel channelStoC = connectionStoC.createChannel();
//        channelStoC.queueDeclare("Server-to-Client",false,false,false,null);
//
//        channelStoC.basicConsume("Server-to-Client", true, (s, delivery) -> {
//           String m = new String(delivery.getBody() , "UTF-8");
//           System.out.println("I just received the API response: " + m);
//        }, s -> {});






    }
}
