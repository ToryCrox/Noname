package com.aleaf.lottie;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final ImageView imageView = findViewById(R.id.image);
        imageView.setImageResource(R.drawable.sleep);

        /*imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Drawable drawable = imageView.getDrawable();
                if (drawable instanceof Animatable){
                    ((Animatable) drawable).start();
                }
            }
        });*/
    }
}
