package org.example;

import com.onshape.api.Onshape;
import com.onshape.api.types.OnshapeDocument;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.*;

public class MethodSerializer {

    private OnshapeDocument document;
    private Onshape onshape;
    private String path1;
    private String path2;

    public MethodSerializer(Onshape onshape) {
        this.onshape = onshape;

    }
    public MethodSerializer() {
    }
    public void setPath1 (String path1) {
        this.path1 = path1;
    }
    public void setPath2 (String path2) {
        this.path2 = path2;
    }
    public void setOnshape( Onshape onshape) {
        this.onshape = onshape;
    }
    public void setDocument(OnshapeDocument document) {
        this.document = document;
    }
    public String serializeMethod() {
        try {
            Class c1 = Class.forName("com.onshape.api.Onshape");
            Method m1 = c1.getDeclaredMethod(path1, null);

            String c = "com.onshape.api." + path1.substring(0, 1).toUpperCase() + path1.substring(1);
            Class c2 = Class.forName(c);
            Method m2 = c2.getDeclaredMethod("get" + path2.substring(0, 1).toUpperCase() + path2.substring(1), null);

            Class c4 = Class.forName("com.onshape.api.types.OnshapeDocument");

            Class c3 = Class.forName("com.onshape.api.requests." + path1.substring(0, 1).toUpperCase() + path1.substring(1) + "Get" + path2.substring(0, 1).toUpperCase() + path2.substring(1) + "Request$Builder");
            Method m3 = c3.getDeclaredMethod("call", c4);
            return m3.invoke((m2.invoke(m1.invoke(onshape, null), null)), document).toString();
            //return "happy";
        } catch (ClassNotFoundException | NoSuchMethodException e ) {
            return "Not found";
        } catch (InvocationTargetException | IllegalAccessException e) {
            return e.getCause().toString();
        }
    }
}
