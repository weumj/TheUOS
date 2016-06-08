package com.uoscs09.theuos2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class TitleActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().getDecorView().postDelayed(() -> {

            startActivity(new Intent(getApplicationContext(), UosMainActivity.class));
            overridePendingTransition(R.anim.enter_fade, R.anim.exit_hold);
            finish();

        }, 150);
    }
}
