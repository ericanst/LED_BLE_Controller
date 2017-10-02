package com.mbkim.led_ble_controller;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

/**
 * Created by mbkim on 2017-09-03.
 */
public class PermissionControl {
    // 권한 요청 코드
    private static final int REQ_PERMISSION = 9760345;
    private static Callback callback = null;

    // 요청할 권한 목록
    public static final String PERMISSION_ARRAY[] = {
            Manifest.permission.ACCESS_COARSE_LOCATION
            , Manifest.permission.ACCESS_FINE_LOCATION
    };

    // 권한체크 함수
    @TargetApi(Build.VERSION_CODES.M)
    public static void checkPermission(Callback object) {
        callback = object;

        // 권한체크가 필요한 버전인지 확인.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Activity activity = callback.getActivity();
            boolean permCheck = true;

            // 런타임 권한 체크
            for (String perm : PERMISSION_ARRAY) {
                if (activity.checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
                    permCheck = false;
                    break;
                }
            }

            // 퍼미션이 모두 true이면 그냥 프로그램 실행
            if (permCheck) {
                callback.init();
            } else {
                activity.requestPermissions(PERMISSION_ARRAY, REQ_PERMISSION);
            }
        } else {
            callback.init();
        }
    }


    // 권한체크 후 콜백 처리
    public static void onCheckResult(int requestCode, int[] grantResults) {
        boolean checkResult = true;

        if (requestCode == REQ_PERMISSION) {
            // 권한쿼리 결과값을 모두 확인한 후 하나라도 승인되지 않았다면 false를 리턴.
            for(int result : grantResults){
                if(result != PackageManager.PERMISSION_GRANTED) {
                    checkResult = false;
                    break;
                }
            }

            if(callback != null){
                if(checkResult) {
                    callback.init();
                } else {
                    Toast.makeText(callback.getActivity()
                            , "권한을 허용하지 않으시면 프로그램을 실행할 수 없습니다."
                            , Toast.LENGTH_LONG).show();
                }

                callback = null;
            }
        }
    }

    interface Callback {
        public Activity getActivity();
        public void init();
    }
}
