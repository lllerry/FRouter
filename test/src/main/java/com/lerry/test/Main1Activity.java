package com.lerry.test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.lerry.route_core.FRouter;
import com.lerry.route_core.template.ITestService;
import com.lerry.router_annotation.Route;

@Route(path = "/test/main")
public class Main1Activity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);
        View toTest2 = findViewById(R.id.hello_start);
        View testService = findViewById(R.id.hello_start2);
        toTest2.setOnClickListener(this);
        testService.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.hello_start) {

            FRouter.getInstance().build("/test2/main").navigation();
        } else {
            ITestService service = (ITestService) FRouter.getInstance().build("/test2/service").navigation();
            service.test();
            Toast.makeText(this, service.test(), Toast.LENGTH_SHORT).show();
        }
    }
}
