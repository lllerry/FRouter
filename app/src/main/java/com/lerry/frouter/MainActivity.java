package com.lerry.frouter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.lerry.route_core.FRouter;
import com.lerry.router_annotation.Route;


@Route(path = "/main/home")
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View toTest = findViewById(R.id.btn_start);
        toTest.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        FRouter.getInstance().build("/test/main").navigation();
    }
}
