package org.example;

import com.onshape.api.Onshape;
import com.onshape.api.desktop.OnshapeDesktop;
import com.onshape.api.types.OnshapeDocument;
//
//import javax.ws.rs.client.Entity;
//import javax.ws.rs.core.Response;
//import java.net.URI;
//import java.util.Date;
//
//public class OnshapeOAuthClient {
//
//    private Onshape onshapeWithOAuth;
//    private OnshapeDesktop myDesktop;
//    private OnshapeDocument document;
//
//    public OnshapeOAuthClient(String baseurl, String oauthurl) {
//        Onshape onshapeWithOAuth = new Onshape();
//        onshapeWithOAuth.addRequestListener((final String method, final URI uri, Entity entity) -> {
//            final long t0 = System.currentTimeMillis();
//            return (Response rspns) -> {
//                System.out.printf("%s %s %s -> %d (%f s)\n", new Date().toString(), method, uri.toString(), rspns.getStatus(), (System.currentTimeMillis() - t0) / 1e3);
//            };
//        });
//        onshapeWithOAuth.setBaseURL(baseurl);
//        onshapeWithOAuth.setOauthURL(oauthurl);
//    }
//    public setDocument(OnshapeDocument document) {
//        this.document = document
//
//    }
//
//}
