package com.uoscs09.theuos2;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.uoscs09.theuos2.tab.TabTest;

public class TestActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content, new TabTest())
                .commit();
    }


    /*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_test_f);

        findViewById(R.id.pink_icon).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TestActivity.this, "Clicked pink Floating Action Button", Toast.LENGTH_SHORT).show();
            }
        });

        FloatingActionButton button = (FloatingActionButton) findViewById(R.id.setter);
        button.setSize(FloatingActionButton.SIZE_MINI);
        button.setColorNormalResId(R.color.pink);
        button.setColorPressedResId(R.color.pink_pressed);
        button.setIcon(R.drawable.theme_ic_action_action_about);
        button.setStrokeVisible(false);

        final View actionB = findViewById(R.id.action_b);

        FloatingActionButton actionC = new FloatingActionButton(getBaseContext());
        actionC.setTitle("Hide/Show Action above");
        actionC.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                actionB.setVisibility(actionB.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
            }
        });
        ((FloatingActionsMenu) findViewById(R.id.multiple_actions)).addButton(actionC);

        final FloatingActionButton removeAction = (FloatingActionButton) findViewById(R.id.button_remove);
        removeAction.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((FloatingActionsMenu) findViewById(R.id.multiple_actions_down)).removeButton(removeAction);
            }
        });

        ShapeDrawable drawable = new ShapeDrawable(new OvalShape());
        drawable.getPaint().setColor(getResources().getColor(R.color.white));
        ((FloatingActionButton) findViewById(R.id.setter_drawable)).setIconDrawable(drawable);

        final FloatingActionButton actionA = (FloatingActionButton) findViewById(R.id.action_a);
        actionA.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                actionA.setTitle("Action A clicked");
            }
        });

        // Test that FAMs containing FABs with visibility GONE do not cause crashes
        findViewById(R.id.button_gone).setVisibility(View.GONE);
    }*/
}

