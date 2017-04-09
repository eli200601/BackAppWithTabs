package com.app.random.backApp.Activitys;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;

import com.app.random.backApp.Fragments.AccountInfoFragment;
import com.app.random.backApp.R;


public class DBAccountInfoActivity extends AppCompatActivity {
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.toolbar);
        toolbar = (Toolbar) findViewById(R.id.toolbar_settings);
        toolbar.setTitle("DropBox Account Info");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();

            }
        });
        getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new AccountInfoFragment()).commit();
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);

    }
}
