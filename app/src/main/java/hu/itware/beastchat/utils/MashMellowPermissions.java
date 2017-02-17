package hu.itware.beastchat.utils;

import android.Manifest.permission;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import hu.itware.beastchat.activities.BaseFragmentActivity;

/**
 * Created by gyongyosit on 2017.02.02..
 */

public class MashMellowPermissions {

    private static final int EXTERNAL_STORAGE_WRITE_PERMISSION_REQUEST_CODE = 10;
    private static final int EXTERNAL_STORAGE_READ_PERMISSION_REQUEST_CODE = 11;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 12;
    private BaseFragmentActivity activity;

    public MashMellowPermissions(BaseFragmentActivity activity) {
        this.activity = activity;
    }

    public void requestPermissionForReadExternalStorage() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission.READ_EXTERNAL_STORAGE)) {
            Toast.makeText(activity, "External storage read permission is needed. Please turn it on inside the settings", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_READ_PERMISSION_REQUEST_CODE);
        }
    }

    public void requestPermissionForWriteExternalStorage() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(activity, "External storage write permission is needed. Please turn it on inside the settings", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_WRITE_PERMISSION_REQUEST_CODE);
        }
    }

    public void requestPermissionForCamera() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission.CAMERA)) {
            Toast.makeText(activity, "Camera permission is needed. Please turn it on inside the settings", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    public boolean checkPermissionForReadExternalStorage() {
        int result = ContextCompat.checkSelfPermission(activity, permission.READ_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public boolean checkPermissionForWriteExternalStorage() {
        int result = ContextCompat.checkSelfPermission(activity, permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public boolean checkPermissionForCamera() {
        int result = ContextCompat.checkSelfPermission(activity, permission.CAMERA);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }
}
