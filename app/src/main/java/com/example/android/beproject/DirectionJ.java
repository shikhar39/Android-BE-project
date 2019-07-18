package com.example.android.beproject;

import android.util.Log;
import com.google.maps.android.PolyUtil;
import org.json.JSONException;
import org.json.JSONObject;

public class DirectionJ {
    private JSONObject directionJson;
    private String mStatus;
    private final String TAG = "DirectionJ";

    public DirectionJ(String urlResponse) {
        try {
            directionJson = new JSONObject(urlResponse);
            mStatus = directionJson.getString("status");
        }
        catch (JSONException e)  {
            Log.e(TAG,"jsonexception");
        }
    }

    protected String getStatus() {
        return mStatus;
    }


}