package appqa;

import appqa.RequestSerializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.onshape.api.Onshape;
import com.onshape.api.desktop.OnshapeDesktop;
import com.onshape.api.exceptions.OnshapeException;
import com.onshape.api.types.OAuthTokenResponse;
import com.onshape.api.types.OnshapeDocument;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.lang3.StringUtils;
import org.example.MethodSerializer;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.concurrent.TimeoutException;

public class AppQaServer {

    public static final String PROD_URL = "https://cad.onshape.com";

    public static void main ( String[] args ) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException, IOException, TimeoutException {

        Onshape onshapeWithOAuth = new Onshape();
        onshapeWithOAuth.addRequestListener((final String method, final URI uri, Entity entity) -> {
            final long t0 = System.currentTimeMillis();
            return (Response rspns) -> {
                System.out.printf("%s %s %s -> %d (%f s)\n", new Date().toString(), method, uri.toString(), rspns.getStatus(), (System.currentTimeMillis() - t0) / 1e3);
            };
        });
        RequestSerializer caller = new RequestSerializer();

        Date myDate = new Date(1618016007445L);

        //Establish connection
        ConnectionFactory factoryCtoS = new ConnectionFactory();
        factoryCtoS.setUri("amqp://localhost");
        Connection connectionCtoS = factoryCtoS.newConnection();
        Channel channelCtoS = connectionCtoS.createChannel();

        //Queue to receive oauth info
        channelCtoS.queueDeclare("auth",true,false,false,null);
        channelCtoS.basicConsume("auth", true, (s, delivery) -> {
            String auth = new String(delivery.getBody(),"UTF-8");
            System.out.println(auth);
            JsonObject callJson = new JsonParser().parse(auth).getAsJsonObject();
            String refreshToken = callJson.get("refreshToken").getAsString();
            String accessToken = callJson.get("accessToken").getAsString();
            String oauthURL = callJson.get("oauthURL").getAsString();
            String baseURL= callJson.get("baseURL").getAsString();
            String clientSecret = callJson.get("clientSecret").getAsString();
            String clientId = callJson.get("clientID").getAsString();
            OAuthTokenResponse myToken = new OAuthTokenResponse(accessToken,"bearer",3600,refreshToken);
            onshapeWithOAuth.setBaseURL(baseURL);
            onshapeWithOAuth.setOauthURL(oauthURL);
            OnshapeDesktop myDesktop = new OnshapeDesktop(clientId,clientSecret);
            myDesktop.setupClient(onshapeWithOAuth,myToken,myDate);
            caller.setOnshape(onshapeWithOAuth);
        } ,s -> {});

        //This is the queue to receive the request
        channelCtoS.queueDeclare("apiData",true,false,false,null);
        channelCtoS.basicConsume("apiData", true, (s, delivery) -> {
            String info = new String(delivery.getBody(),"UTF-8");
            System.out.println("I just received the call info: " + info);

            //Api call stuff is here
            JsonObject callJson = new JsonParser().parse(info).getAsJsonObject();
            String operationId = callJson.get("operationId").getAsString();
            String tags= callJson.get("tags").getAsString();
            String path = callJson.get("valuePath").getAsString();
            caller.setOperationId(operationId);
            caller.setTags(tags);

            //Make document
            int i1 = StringUtils.ordinalIndexOf(path,"/",3);
            int i2 = path.lastIndexOf("/");
            String url = PROD_URL + "/documents" + path.substring(i1+2,i2);
            System.out.println(url);
            String results = "";
            String results2 = "";
            try {
                OnshapeDocument document = new OnshapeDocument(url);
                caller.setDocument(document);
                //Execute the call


                results = caller.makeCall();
            } catch (OnshapeException e) {
                results = "Document Not found";
            }

            //System.out.println("Document status: " + results2);
            System.out.println(caller.getDocument());
            System.out.println("response from call: \n" + results);



            //This send API response back to client upon completion of call
            try (Connection connectionStoC = factoryCtoS.newConnection() ) {
                Channel channelStoC = connectionStoC.createChannel();
                channelStoC.queueDeclare("response",true,false,false,null);
                channelStoC.basicPublish("","response",false,null, results.getBytes());

            } catch (TimeoutException e) {
                e.printStackTrace();
            }

        } ,s -> {});
    }
}
