package au.edu.anu.cs.sparkee.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SParkeeJSONObject {
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public JSONArray getData() {
        return data;
    }

    public void setData(JSONArray data) {
        this.data = data;
    }

    String status;
    String msg;
    String path;
    JSONArray data;

    public SParkeeJSONObject(String status, String path, String msg, JSONArray ja) {
        this.status = status;
        this.msg = msg;
        this.path = path;
        this.data = ja;
    }

    public static SParkeeJSONObject parse(String json) throws JSONException {
        Log.d("JSON", json);
        JSONObject jo = new JSONObject(json);
        String status = jo.getString("status");
        String path = jo.getString("path");
        String msg = jo.getString("msg");
        JSONArray ja = jo.getJSONArray("data");
        return  new SParkeeJSONObject(status, path, msg, ja);
    }
}
