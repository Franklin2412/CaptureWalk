package com.ran.capturewalk.capturewalk;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    /* renamed from: com.ran.capturewalk.capturewalk.SplashActivity.1 */
    class C05921 implements Runnable {
        C05921() {
        }

        public void run() {
            SplashActivity.this.startActivity(new Intent(SplashActivity.this, MainActivity.class));
            SplashActivity.this.finish();
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_splash);
        new Handler().postDelayed(new C05921(), 3000);
    }
}
