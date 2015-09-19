package com.fiuba.tdp.petadopt.service;


import android.content.Context;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;

/**
 * Created by joaquinstankus on 13/09/15.
 */
public class AuthClient extends HttpClient {

    public void signUp(Context context, String fb_id, String fb_token, JsonHttpResponseHandler handler) {
        String url = getApiUrl("/users.json");
        JSONObject user = new JSONObject();
        JSONObject userData = new JSONObject();
        StringEntity entity;
        try {
            userData.put("facebook_id", fb_id);
            userData.put("facebook_token", fb_token);
            user.put("user", userData);
            entity = new StringEntity(user.toString());
            client.post(context, url, entity, "application/json", handler);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void login(Context context) {

    }

//    public void signUp(String email, String password){
//
//    }
}
