package com.imageselect.yhck;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.imageselect.yhck.imageselector.ImageSelectorActivity;
import com.imageselect.yhck.imageselector.constant.Constants;
import com.imageselect.yhck.imageselector.utils.ImageSelectorUtils;
import com.webview.yhck.WebViewActivity;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ImageSelectorPlugin extends CordovaPlugin{
  private static final int REQUEST_CODE = 0x00000011;
  private CallbackContext mCallbackContext;
  private String getParam;
  private String mImagePath="";
  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
  }
  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    this.getParam="";
    this.mCallbackContext = callbackContext;
    if (!"".equals(action)||action!=null) {
     getParam=args.getJSONObject(0).toString();
      openActivity(getParam);
      return true;
    }
    mCallbackContext.error("error");
    return false;
  }

  private void openActivity(String getParam) {
    try {
      JSONObject jsonObject = new JSONObject(getParam);
      mImagePath = jsonObject.get("imagePath").toString();
    } catch (JSONException e) {
      e.printStackTrace();
    }
    SharedPreferences mSharedPre = cordova.getActivity().getSharedPreferences("imagePath", Context.MODE_PRIVATE);
    SharedPreferences.Editor mEditor = mSharedPre.edit();
    mEditor.putString("PATH", mImagePath);
    mEditor.commit();

    Intent intent = new Intent(cordova.getActivity(), ImageSelectorActivity.class);
    intent.putExtra(Constants.MAX_SELECT_COUNT, 0);
    intent.putExtra(Constants.IS_SINGLE, true);
    cordova.startActivityForResult(this, intent, REQUEST_CODE);//单选
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_CODE && data != null) {
      ArrayList<String> images = data.getStringArrayListExtra(ImageSelectorUtils.SELECT_RESULT);
      mCallbackContext.success(images.get(0));
    }
  }
}
