package com.lerry.test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.lerry.route_core.FRouter;
import com.lerry.router_annotation.Route;

@Route(path = "/test/main")
public class Main1Activity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);
        View toTest2 = findViewById(R.id.hello_start);
        toTest2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        FRouter.getInstance().build("/test2/main").navigation();
    }
}
