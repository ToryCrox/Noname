package com.tory.lightphoto;

import android.app.Activity;
import android.content.res.Resources;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.TintContextWrapper;
import androidx.appcompat.widget.VectorEnabledTintResources;
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

    @Override
    public Resources getResources() {
        if (mResources == null && VectorEnabledTintResources.shouldBeUsed()) {
            mResources = new VectorEnabledTintResources(this, super.getResources());
        }
        return mResources == null ? super.getResources() : mResources;
    }

    private boolean f = false;
    @OnClick(R.id.test)
    public void testRotationX(View view){
        view.setRotationX(f ? 0 : 180f);
        f = !f;
    }
}
