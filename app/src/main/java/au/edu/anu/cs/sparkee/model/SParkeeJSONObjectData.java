package au.edu.anu.cs.sparkee.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SParkeeJSONObjectData {
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

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
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
    JSONObject data;

    public SParkeeJSONObjectData(String status, String path, String msg, JSONObject jo, String trx_id) {
        this.status = status;
        this.msg = msg;
        this.path = path;
        this.data = jo;
        this.trx_id =trx_id;
    }

    public static SParkeeJSONObjectData parse(String json) throws JSONException {
        JSONObject jo = new JSONObject(json);
        String status = jo.getString("status");
        String path = jo.getString("path");
        String msg = jo.getString("msg");
        String trx_id= jo.getString("trx_id");
        JSONObject data = jo.getJSONObject("data");
        return  new SParkeeJSONObjectData(status, path, msg, data, trx_id);
    }
}
