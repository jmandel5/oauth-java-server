package org.example;
//import com.onshape.api.Assemblies;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.onshape.api.Onshape;
import com.onshape.api.desktop.OnshapeDesktop;
import com.onshape.api.exceptions.OnshapeException;
import com.onshape.api.responses.AssembliesGetBoundingBoxesResponse;
import com.onshape.api.types.OAuthTokenResponse;
import com.onshape.api.types.OnshapeDocument;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Method;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.TimeoutException;
import java.lang.reflect.*;

public class Server {
//    public static final String CLIENT_ID = "IIRSKZKOT4MJ4MRG5UHIJ3EVDMPAY6IEEXNZK6Q=";
//    public static final String CLIENT_SECRET = "FLJSMWYUXMDHCULPL6QKP3NGHLZCWJM3ZJATFN6BGNKTUM7NOQOA====";
//
//    public static final String ACCESS_TOKEN = "118pBbjeytI0rbhRauLDdw==";
//    public static final String REFRESH_TOKEN = "629780f474c8e9cb9d383fcaea16cd36";
//
//    public static final String BASE_URL = "https://demo-c.dev.onshape.com";
//    public static final String OAUTH_URL = "https://demo-c-oauth.dev.onshape.com/oauth/token";

    public static void main (String[] args ) throws IOException, TimeoutException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException {

        //Onshape Instance
        Onshape onshapeWithOAuth = new Onshape();
        onshapeWithOAuth.addRequestListener((final String method, final URI uri, Entity entity) -> {
            final long t0 = System.currentTimeMillis();
            return (Response rspns) -> {
                System.out.printf("%s %s %s -> %d (%f s)\n", new Date().toString(), method, uri.toString(), rspns.getStatus(), (System.currentTimeMillis() - t0) / 1e3);
            };
        });

        //Call Serializer
        MethodSerializer callSerializer = new MethodSerializer();

        //Test Document
//        String d = "3494996efc14cbf22c0b348f";
//        String w = "e0f178d13c5b98d94a824279";
//        String el = "6177f782b5155c4dc9115237";
        //OnshapeDocument document = new OnshapeDocument(d,w,el);

        //Create new Connection and Channel
        ConnectionFactory factoryCtoS = new ConnectionFactory();
        factoryCtoS.setUri("amqp://localhost.dev.onshape.com");
        factoryCtoS.setPort(5672);
        Connection connectionCtoS = factoryCtoS.newConnection();
        Channel channelCtoS = connectionCtoS.createChannel();

        //This is the queue to receive the onshape info and oauth credentials
        channelCtoS.queueDeclare("Info",false,false,false,null);
        channelCtoS.basicConsume("Info", true, (s, delivery) -> {
            String info = new String(delivery.getBody(),"UTF-8");
            System.out.println("I just received the call info: " + info);

            JsonObject infoJson = new JsonParser().parse(info).getAsJsonObject();
            String client_id = infoJson.get("clientId").getAsString();
            String client_secret = infoJson.get("clientSecret").getAsString();
            String access_token = infoJson.get("accessToken").getAsString();
            String refresh_token = infoJson.get("refreshToken").getAsString();
            String base_url = infoJson.get("baseUrl").getAsString();
            String oauth_url = infoJson.get("oauthUrl").getAsString();

            //Set up all new info.
            onshapeWithOAuth.setBaseURL(base_url);
            onshapeWithOAuth.setOauthURL(oauth_url);
            OAuthTokenResponse myToken = new OAuthTokenResponse(access_token,"bearer",3600,refresh_token);
            Date myDate = new Date(1627066022021L);
            OnshapeDesktop myDesktop = new OnshapeDesktop(client_id,client_secret);
            myDesktop.setupClient(onshapeWithOAuth,myToken,myDate);

            //Method serializer instance
            callSerializer.setOnshape(onshapeWithOAuth);
            //callSerializer.setDocument(document);
        } ,s -> {});


        //This is the queue for receiving the api request info from the client
        channelCtoS.queueDeclare("Client-to-Server",false,false,false,null);
        channelCtoS.basicConsume("Client-to-Server", true, (s, delivery) -> {
            String call = new String(delivery.getBody() , "UTF-8");
            System.out.println("I just received the api request info: " + call);

            JsonObject callJson = new JsonParser().parse(call).getAsJsonObject();
            String firstPath = callJson.get("first_path").getAsString();
            String secondPath = callJson.get("second_path").getAsString();
            String d = callJson.get("d").getAsString();
            String w = callJson.get("w").getAsString();
            String el = callJson.get("e").getAsString();
            callSerializer.setDocument(new OnshapeDocument(d,w,el));
            callSerializer.setPath1(firstPath);
            callSerializer.setPath2(secondPath);

            //Make the API call
            String results  = null;
            //results = testBoundingBoxCall(onshapeWithOAuth,document);
            results = callSerializer.serializeMethod();

            //This send API response back to client upon completion of call
            try (Connection connectionStoC = factoryCtoS.newConnection() ) {
                Channel channelStoC = connectionStoC.createChannel();
                channelStoC.queueDeclare("Server-to-Client",false,false,false,null);

                channelStoC.basicPublish("","Server-to-Client",false,null, results.getBytes());

                System.out.println("!! the API response has been sent");
            } catch (TimeoutException e) {
                e.printStackTrace();
            }

        }, s -> {});

    }
    public static String testBoundingBoxCall(Onshape onshape, OnshapeDocument document) throws OnshapeException {
        AssembliesGetBoundingBoxesResponse boundingBoxResponse = onshape.assemblies().getBoundingBoxes().call(document);
        return boundingBoxResponse.toString();
    }

}
