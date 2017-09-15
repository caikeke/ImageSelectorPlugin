package com.imageselect.yhck;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ImageSelectorPlugin extends CordovaPlugin{
  private static final int REQUEST_CODE = 0x00000011;
  private CallbackContext mCallbackContext;
  private String getParam;
  private String mImagePath="";
  private String mUserPhone="";
  private String sdCardDir = "";
  private String mImageName = "";
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
      mImagePath = jsonObject.get("imgUuid").toString();
      mUserPhone = jsonObject.get("mUserPhone").toString();
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
      String mNewPath = getSmallImagePath(images.get(0));
      JSONObject jsonData = new JSONObject();
      try {
        jsonData.put("imgUuid",images.get(0));
        jsonData.put("imgPath",mNewPath);
      } catch (JSONException e) {
        e.printStackTrace();
      }
      mCallbackContext.success(jsonData);
    }
  }
  public String getSmallImagePath(String imagePath) {
    FileOutputStream out = null;
    int mQuality = 100;
    Bitmap mBitmap = getSmallBitmap(imagePath);
    if (mBitmap == null) {
      return "";
    }
    long fileLen = getBitmapsize(mBitmap);
    if (fileLen > 5 * 1024 * 1024) {
      mQuality = 20;
    } else if (fileLen > 1024 * 1024) {
      mQuality = 40;
    } else if (fileLen > 500 * 1024) {
      mQuality = 60;
    } else if (fileLen > 100 * 1024) {
      mQuality = 90;
    }
    // 判断是否可以对SDcard进行操作
    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
      // 获取SDCard指定目录下
      sdCardDir = Environment.getExternalStorageDirectory() + "/.SmallImagePath/";
      //目录转化成文件夹
      File dirFile = new File(sdCardDir);
      //如果不存在，那就建立这个文件夹
      if (!dirFile.exists()) {
        dirFile.mkdirs();
      }
      // 在SDcard的目录下创建图片,指定名称
      mImageName = mUserPhone +"_"+ System.currentTimeMillis()+".PNG";
      File file = new File(sdCardDir, mImageName);
      try {
        out = new FileOutputStream(file);
        mBitmap.compress(Bitmap.CompressFormat.JPEG, mQuality, out);
        System.out.println("保存到sd指定目录文件夹下");
      } catch (FileNotFoundException e) {
        e.printStackTrace();
        return sdCardDir;
      }
      try {
        out.flush();
        out.close();
      } catch (IOException e) {
        e.printStackTrace();
        return sdCardDir;
      }
    }
    return sdCardDir + mImageName;
  }

  public Bitmap getSmallBitmap(String imagePath) {
    final BitmapFactory.Options options = new BitmapFactory.Options();
    options.inSampleSize = 1;
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(imagePath, options);
    // Calculate inSampleSize
    options.inSampleSize = calculateInSampleSize(options, 1080, 1920);
    // Decode bitmap with inSampleSize set
    options.inJustDecodeBounds = false;
    Bitmap bm = BitmapFactory.decodeFile(imagePath, options);
    if (bm == null) {
      return null;
    }
    ByteArrayOutputStream baos = null;
    try {
      baos = new ByteArrayOutputStream();
      bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
    } finally {
      try {
        if (baos != null)
          baos.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return bm;
  }

  private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {

      // Calculate ratios of height and width to requested height and
      // width
      final int heightRatio = Math.round((float) height
        / (float) reqHeight);
      final int widthRatio = Math.round((float) width / (float) reqWidth);

      // Choose the smallest ratio as inSampleSize value, this will
      // guarantee
      // a final image with both dimensions larger than or equal to the
      // requested height and width.
      inSampleSize = heightRatio < widthRatio ? widthRatio : heightRatio;
    }

    return inSampleSize;
  }

  public long getBitmapsize(Bitmap bitmap) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
      return bitmap.getByteCount();
    }
    // Pre HC-MR1
    return bitmap.getRowBytes() * bitmap.getHeight();
  }

}
