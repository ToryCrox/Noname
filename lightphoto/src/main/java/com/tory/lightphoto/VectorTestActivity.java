package com.tory.lightphoto;

import android.app.Activity;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.TintContextWrapper;
import android.support.v7.widget.VectorEnabledTintResources;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class VectorTestActivity extends Activity {

    @BindView(R.id.image)
    ImageView imageView;
    @BindView((R.id.text))
    TextView textView;
    private Resources mResources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vector_test);
        ButterKnife.bind(this);
        
        textView.setText("drawable is = "+imageView.getDrawable());
    }

    private boolean f = false;
    @OnClick(R.id.test)
    public void testRotationX(View view){
        view.setRotationX(f ? 0 : 180f);
        f = !f;
    }
}
