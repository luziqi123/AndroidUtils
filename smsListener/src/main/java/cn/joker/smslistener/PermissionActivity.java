package cn.joker.smslistener;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;


public class PermissionActivity extends Activity{

    private int PERMISSION_STORAGE_CODE = 10001;
    String[] PERMS = {
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermission();
    }

    private void requestPermission(){
        if (isAllGranted(PERMS)) {
            Toast.makeText(this,"您已经申请了权限!",Toast.LENGTH_SHORT).show();
            finish();
        }else {
            ActivityCompat.requestPermissions(this, PERMS, 1011);
        }
    }

    private boolean isAllGranted(String[] perms) {
        boolean isAllGranted = true;
        for (String perm : perms) {
            if (ContextCompat.checkSelfPermission(this, perm) != PERMISSION_GRANTED) {
                isAllGranted = false;
                break;
            }
        }
        return isAllGranted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1011) {
            if (isAllGranted(PERMS)) {
                setResult(200);
            } else {
                setResult(-1);
            }
            finish();
        }
    }
}
