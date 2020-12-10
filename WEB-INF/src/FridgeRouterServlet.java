
import java.io.*;
import java.lang.reflect.*;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

/*
 * @Author Connor Parker
 * @ID 20140728
 * CSE3OAD
 * */
public class FridgeRouterServlet extends HttpServlet {
    public static final String CONTENT_TYPE = "application/json";
    public static final String CHARACTER_ENCODING = "utf-8";

    // these constants are also available at javax.ws.rs.HttpMethod
    // but it does not define PATCH, we we are creating our own.
    // more: https://www.restapitutorial.com/lessons/httpmethods.html
    public static final String HTTP_GET = "GET";
    public static final String HTTP_POST = "POST";
    public static final String HTTP_PUT = "PUT";
    public static final String HTTP_DELETE = "DELETE";

    // status codes: http://www.informit.com/articles/article.aspx?p=29817&seqNum=7
    // more https://www.restapitutorial.com/lessons/httpmethods.html
    // we will be using those defined in HttpServletResponse
    // they are listed at
    // https://tomcat.apache.org/tomcat-7.0-doc/servletapi/javax/servlet/http/HttpServletResponse.html
    // more: https://www.restapitutorial.com/httpstatuscodes.html

    public static final String CONTROLLER_STR = "Controller";

    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // DONE 16: set the response CONTENT_TYPE and CHARACTER_ENCODING

        response.setContentType(CONTENT_TYPE);

        response.setCharacterEncoding(CHARACTER_ENCODING);

        Object responseObject = null;

        // DONE 17: grab the path info from HttpServletRequest argument
        String pathInfo = request.getPathInfo(); // <-- some changes needed here

        // DONE 18: grab the http method from HttpServletRequest argument
        String httpMethod = request.getMethod(); // <-- some changes needed here

        // pathInfo will be in format: /{resource-name}/{query-string}
        // we want resource-name; we split on "/" and take the
        // second occurence, which is array position 1 of split("/");
        // any third occurence would be a model id or a search query parameter
        String pathInfoArray[] = pathInfo.split("/");

        try {
            Map<String, String> message = new HashMap<String, String>();
            // pathInfo has to have at least a resource-name, which is at array
            // location 1 in pathInfoArray - if no resource-name found throws error
            if (pathInfoArray.length <= 1)
                throw new MissingArgumentException("Resource target not defined.");

            // the model is needed for json parsing purposes using Gson library
            // following uppercase-camel-case convention for class naming, that is
            // - resource-name grocery will become class Grocery, or
            // - resource-name GroCerY will become class Grocery, or ...

            String modelName = pathInfoArray[1]; // <-- some changes needed here
            modelName = modelName.substring(0, 1).toUpperCase() + modelName.substring(1).toLowerCase();// <-- some

            String controllerName = String.join("", modelName, CONTROLLER_STR);
            Class<?> controllerClass = Class.forName(controllerName);

            Class<?> modelClass = Class.forName(modelName);
            String[] dbConfig = new String[3];
            dbConfig[0] = getServletContext().getInitParameter("dbhost");
            dbConfig[1] = getServletContext().getInitParameter("dbusername");
            dbConfig[2] = getServletContext().getInitParameter("dbpassword");
            String dblength = String.valueOf(dbConfig.length);

            // creating an instance of controllerClass; NOTE that the next 2 lines finds
            // the matching constructor (with 3 String arguments) and instantiate the
            // class using that constructor passing in String array dbConfig of length 3

            Constructor constructor = controllerClass.getConstructor(new Class[]{String.class, String.class, String.class});
            Object controllerInstance = constructor.newInstance((Object[]) dbConfig); // <-- some changes needed here

            int modelId = 0;
            Method method = null;
            switch (httpMethod) {
                case HTTP_GET:
                    // if pathInfoArray has 3rd argument (our design denotes that
                    // any 3rd argument for an HTTP GET is the relevant model id)
                    // then find the matching controllerClass method get(int id)
                    // NOTE: the 3rd argument is sent as a String and needs to be
                    // parsed as our model id is of type int.

                    if (pathInfoArray.length >= 3) {
                        modelId = Integer.parseInt(pathInfoArray[2]);
                        method = controllerClass.getMethod("get", int.class);

                        responseObject = method.invoke(controllerInstance, modelId);

                        if (responseObject == null){
                            response.getWriter().write(new Gson().toJson(buildMessage("Invalid ID Provided")));
                            throw new ResourceNotFoundException(modelName + " with id " + modelId + " not found!");
                        }

                    }
                    // else find the matching controllerClass method get()
                    else {

                        method = controllerClass.getMethod("get"); // <-- some changes needed here
                        try {
                            responseObject = method.invoke(controllerInstance);
                        } catch (Exception e) {
                            response.getWriter().write(new Gson().toJson(buildMessage("IN INVOKE CATCH")));
                            e.printStackTrace();
                        }
                    }
                    break;
                case HTTP_POST: // NOTE: this case is given fully complete; it is the most complex part; use it
                    // as a reference example
                    // grab post data
                    String resourceData = buildResourceData(request);

                    // find relevant add method in controllerClass
                    method = controllerClass.getMethod("add", modelClass);

                    // invoke method with parse (converted from JSON to modelClass) post data
                    Object id = method.invoke(controllerInstance, new Gson().fromJson(resourceData, modelClass));

                    message.put("message", "created ok! " + modelName + " id " + Integer.parseInt(id.toString()));
                    responseObject = message;
                    break;
                case HTTP_PUT:
                    if (pathInfoArray.length >= 3) {
                        modelId = Integer.parseInt(pathInfoArray[2]);
                        method = controllerClass.getMethod("update", int.class);
                        responseObject = method.invoke(controllerInstance, modelId);
                        if (responseObject == null){
                            response.getWriter().write(new Gson().toJson(buildMessage(modelName+" with id "+modelId+ "Not Found, Cannot Update")));
                            throw new ResourceNotFoundException(
                                    modelName + " with id " + modelId + " not found! Cannot Update!");
                        }
                    } else{
                        response.getWriter().write(new Gson().toJson(buildMessage("No Attribute id Provided, Cannot Update")));
                        throw new MissingArgumentException("Attribute id required! Cannot Update!");

                    }

                    break;
                case HTTP_DELETE:
                    if (pathInfoArray.length >= 3) {
                        modelId = Integer.parseInt(pathInfoArray[2]); // <-- some changes needed here
                        method = controllerClass.getMethod("delete", int.class); // <-- some changes needed here
                        responseObject = method.invoke(controllerInstance, modelId); // <-- some changes needed here

                        if (Integer.parseInt(responseObject.toString()) <= 0){
                            response.getWriter().write(new Gson().toJson(buildMessage(modelName+" with id "+modelId+ "Not Found, Cannot Delete")));
                            throw new ResourceNotFoundException(
                                    modelName + " with id " + modelId + " not found! Cannot Delete!");
                        }
                        responseObject = buildMessage(modelName + " with id " + modelId + " deleted!");
                    } else {
                        response.getWriter().write(new Gson().toJson(buildMessage("Attribute Id Required, Cannot Delete")));
                        throw new MissingArgumentException("Attribute id required! Cannot Delete!");
                    }
                    break;
                default:
                    // we do not provide action for any other HTTP methods
                    throw new NoSuchMethodException();
            }


            response.getWriter().print(new Gson().toJson(responseObject));
        } catch (Exception exp) {
            String message = exp.getMessage();
            // setting default error status code
            response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);

            if (exp instanceof ResourceNotFoundException){
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                message = "Resource not Found";
            }
            if (exp instanceof MissingArgumentException) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }

            if (exp instanceof ClassNotFoundException) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                message = "Resource not found!";
            }

            if (exp instanceof NoSuchMethodException) {
                response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                message = "Method not allowed on specified resource!";
            }

            if (exp instanceof UpdateNotAllowedException) {
                response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                message = "Update Not Allowed";
            }
            if (exp instanceof ValidationException){
                response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
                message = "Validation Exception";
            }
        }

    }
    // HELPER METHODS
    private String buildResourceData(HttpServletRequest request) throws Exception {
        // request has post/put data
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } finally {
            reader.close();
        }
        return sb.toString();
    }

    private Map buildMessage(String msg) {
        Map<String, String> message = new HashMap<String, String>();
        message.put("message", msg);
        return message;
    }

}