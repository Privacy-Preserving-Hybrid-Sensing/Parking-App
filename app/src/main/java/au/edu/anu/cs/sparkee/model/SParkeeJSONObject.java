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

    public String getTrx_id() {
        return trx_id;
    }

    public void setTrx_id(String trx_id) {
        this.trx_id = trx_id;
    }

    String trx_id;
    JSONArray data;

    public SParkeeJSONObject(String status, String path, String msg, JSONArray ja, String trx_id) {
        this.status = status;
        this.msg = msg;
        this.path = path;
        this.data = ja;
        this.trx_id =trx_id;
    }

    public static SParkeeJSONObject parse(String json) throws JSONException {
        JSONObject jo = new JSONObject(json);
        String status = jo.getString("status");
        String path = jo.getString("path");
        String msg = jo.getString("msg");
        String trx_id= jo.getString("trx_id");
        JSONArray ja = jo.getJSONArray("data");
        return  new SParkeeJSONObject(status, path, msg, ja, trx_id);
    }
}
