package com.example.ranter.app;

import android.content.Context;
import android.hardware.Camera;
import android.os.Environment;
import android.widget.Toast;

import com.couchbase.lite.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;

/**
 * Created by roadan on 6/13/14.
 */
public class PictureHandler implements Camera.PictureCallback {

    private final Context context;
    private final String TAG = "PictureHandler";

    public PictureHandler(Context context) {
        this.context = context;
    }

    private File getPicturesDir() {
        File sdDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(sdDir, "CameraAPIDemo");
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

        File pictureDir = getPicturesDir();

        if (!pictureDir.exists() && !pictureDir.mkdirs()) {

            Log.d(TAG, "Can't create directory to save image.");
            Toast.makeText(context, "Can't create directory to save image.",
                    Toast.LENGTH_LONG).show();
            return;

        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
        String date = dateFormat.format(new java.util.Date());
        String fileName = "IMG_" + date + ".jpg";

        String fileFullName = pictureDir.getPath() + File.separator + fileName;

        File pictureFile = new File(fileFullName);

        try {
            FileOutputStream stream = new FileOutputStream(pictureFile);
            stream.write(data);
            stream.close();
            Toast.makeText(context, "Image saved:" + fileName,
                    Toast.LENGTH_LONG).show();
        } catch (Exception error) {
            Log.d(TAG, "File" + fileFullName + "not saved: "
                    + error.getMessage());
            Toast.makeText(context, "Image could not be saved.",
                    Toast.LENGTH_LONG).show();
        }
    }

}
