package edu.gvsu.restapi.client;

import java.io.IOException;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Client;
import org.restlet.data.*;
import org.restlet.*;
import org.restlet.representation.Representation;

/**
 * Sample client program that uses the RESTlet framework to access a RESTful web service.
 *
 * @author Jonathan Engelsma (http://themobilemontage.com)
 */
public class SampleRESTClient implements PresenceService {

    // The base URL for all requests.
    public static final String APPLICATION_URI = "http://cis-654-lab-4.appspot.com";


    public void register(RegistrationInfo reg) throws Exception {
        // POST /users

        JSONObject registration = new JSONObject();
        registration.put("name", reg.getUserName());
        registration.put("ipAddress", reg.getHost());
        registration.put("port", reg.getPort());
        registration.put("status", true);
        String usersURL = APPLICATION_URI + "/users";
        Request request = new Request(Method.POST, usersURL);
        request.getClientInfo().getAcceptedMediaTypes().add(new Preference(MediaType.APPLICATION_JSON));
        request.setEntity(registration.toString(), MediaType.APPLICATION_JSON);
        Response resp = new Client(Protocol.HTTP).handle(request);
    }

    public void unregister(String userName) throws Exception {
        // DELETE /users/{name}

        String usersResourceURL = APPLICATION_URI + "/users/" + userName;
        Request request = new Request(Method.DELETE, usersResourceURL);
        request.getClientInfo().getAcceptedMediaTypes().add(new Preference<MediaType>(MediaType.APPLICATION_JSON));
        Response resp = new Client(Protocol.HTTP).handle(request);
    }

    public RegistrationInfo lookup(String name) throws Exception {
        // GET /users/{name}

        String usersURL = APPLICATION_URI + "/users/" + name;
        Request request = new Request(Method.GET, usersURL);
        request.getClientInfo().getAcceptedMediaTypes().add(new Preference(MediaType.APPLICATION_JSON));
        Response resp = new Client(Protocol.HTTP).handle(request);
        if (resp.getStatus().getCode() == 200 ){
            JSONObject userJson = new JSONObject(resp.getEntity().getText());
            RegistrationInfo user = new RegistrationInfo();
            user.setUserName(userJson.getString("name"));
            user.setHost(userJson.getString("ipAddress"));
            user.setPort(Integer.parseInt(userJson.getString("port")));
            user.setStatus(userJson.getBoolean("status"));
            return user;
        }
        else {
            return null;
        }
    }

    public void setStatus(String userName, boolean status) throws Exception {
        // PUT /users/{name}

        JSONObject statusObject = new JSONObject();
        statusObject.put("name", userName);
        statusObject.put("status", status);

        String usersResourceURL = APPLICATION_URI + "/users/" + userName;
        Request request = new Request(Method.PUT, usersResourceURL);
        request.getClientInfo().getAcceptedMediaTypes().add(new Preference(MediaType.APPLICATION_JSON));
        request.setEntity(statusObject.toString(), MediaType.APPLICATION_JSON);
        Response resp = new Client(Protocol.HTTP).handle(request);
    }

    public RegistrationInfo[] listRegisteredUsers() {
        // GET /users

        String usersURL = APPLICATION_URI + "/users";
        RegistrationInfo[] registeredUsers = new RegistrationInfo[0];
        Request request = new Request(Method.GET, usersURL);
        request.getClientInfo().getAcceptedMediaTypes().add(new Preference(MediaType.APPLICATION_JSON));
        Response resp = new Client(Protocol.HTTP).handle(request);

        if (resp.getStatus().equals(Status.SUCCESS_OK)) {
            Representation responseData = resp.getEntity();
            try {
                String jsonString = responseData.getText();
                JSONArray jObj = new JSONArray(jsonString);
                registeredUsers = new RegistrationInfo[jObj.length()];
                for (int i = 0; i < jObj.length(); i++) {
                    JSONObject item = jObj.getJSONObject(i);
                    Map itemMap = item.toMap();
                    RegistrationInfo info = new RegistrationInfo();
                    info.setUserName(itemMap.get("name").toString());
                    info.setHost(itemMap.get("ipAddress").toString());
                    info.setPort(Integer.parseInt(itemMap.get("port").toString()));
                    info.setStatus(Boolean.parseBoolean(itemMap.get("status").toString()));
                    registeredUsers[i] = info;
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JSONException je) {
                je.printStackTrace();
            }
        }
        return registeredUsers;
    }
}
