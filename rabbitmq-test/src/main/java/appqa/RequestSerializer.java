package appqa;

import com.onshape.api.Onshape;
import com.onshape.api.types.OnshapeDocument;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

public class RequestSerializer {
    private String tags;
    private String operationId;
    private Onshape onshape;
    private OnshapeDocument document;

    public RequestSerializer() {}

    public void setOnshape(Onshape onshape) {this.onshape = onshape;}

    public void setDocument(OnshapeDocument document) {this.document = document;}

    public void setTags(String tags) {
        if ( tags.substring(tags.length() - 1).equals("y") ) {
            this.tags = tags.substring(0,tags.length()- 1) + "ies";
        }
        else {
            this.tags = tags + "s";
        }
    }

    public void setOperationId(String operationId) {this.operationId = operationId;}

    public OnshapeDocument getDocument() {
        return document;
    }
    public String makeCall() {
        String path1 = tags;
        try {
            Class c1 = Class.forName("com.onshape.api.Onshape");
            Method m1 = c1.getDeclaredMethod(path1.substring(0,1).toLowerCase() + path1.substring(1), null);

            String c = "com.onshape.api." + path1.substring(0, 1).toUpperCase() + path1.substring(1);
            Class c2 = Class.forName(c);
            Method m2 = c2.getDeclaredMethod(operationId, null);

            Class c4 = Class.forName("com.onshape.api.types.OnshapeDocument");

            Class c3 = Class.forName("com.onshape.api.requests." + path1.substring(0, 1).toUpperCase() + path1.substring(1) + operationId.substring(0,1).toUpperCase() + operationId.substring(1) + "Request$Builder");
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
