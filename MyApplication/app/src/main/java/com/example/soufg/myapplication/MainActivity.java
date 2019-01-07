package com.example.soufg.myapplication;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;


import com.android.rssample.ScriptC_bestscript;

import static android.graphics.Bitmap.createScaledBitmap;
import static java.lang.Math.random;

public class MainActivity extends AppCompatActivity {


    @SuppressLint("DefaultLocale")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView img = findViewById(R.id.pewpew);
        int iw = img.getWidth();
        int ih = img.getHeight();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        options.outHeight = ih;
        options.outWidth = iw;
        final Bitmap bmpList[] = new Bitmap[3];
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.crayon, options);
        bmpList[0] = createScaledBitmap(bmp, 700, 500, false);
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.fruit, options);
        bmpList[1] = createScaledBitmap(bmp, 700, 500, false);
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.evolution, options);
        bmpList[2] = createScaledBitmap(bmp, 700, 500, false);
        final int[] imgId = {0};


        final Bitmap bitmap = bmpList[0].copy(bmp.getConfig(),true);
        img.setImageBitmap(bitmap);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        TextView tv = findViewById(R.id.size);
        tv.setText(String.format("Image size: %dx%d", width,height));

        final double maskBoxBlur[]= new double[9];
        for(int i=0;i<9;i++){
            maskBoxBlur[i]=0.111;
        }
        final double maskExtr[]= new double[9];
        for(int i=0;i<9;i++){
            maskExtr[i]=-1;
        }
        maskExtr[4]=8;
        final double maskNet[]= {0,-1,0,-1,5,-1,0,-1,0};

        final Switch switch1 = findViewById(R.id.renderswitch);
        final Switch switch2 = findViewById(R.id.baseswitch);

        final Bitmap old = bitmap.copy(bitmap.getConfig(),true);

        final Button button = findViewById(R.id.butt);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(switch2.isChecked()) threshold(bitmap,old); else threshold(bitmap,bitmap);
            }
        });
        final Button button2 = findViewById(R.id.butt2);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(switch1.isChecked()) {
                    if(switch2.isChecked()) toGreyRS(bitmap, old); else toGreyRS(bitmap,bitmap);
                }else{
                    if(switch2.isChecked()) toGrayTab(bitmap,old); else toGrayTab(bitmap,bitmap);
                }
            }
        });
        final Button button3 = findViewById(R.id.butt3);
        button3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(switch1.isChecked()) resetRS(bitmap,old); else reset(bitmap,old);
            }
        });
        final Button button4 = findViewById(R.id.butt4);
        button4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(switch1.isChecked()){
                    if(switch2.isChecked()) colorizeRS(bitmap,old); else colorizeRS(bitmap,bitmap);
                } else{
                    if(switch2.isChecked()) colorize(bitmap,old); else colorize(bitmap,bitmap);
                }
            }
        });
        final Button button5 = findViewById(R.id.butt5);
        button5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(switch2.isChecked()) contrastUp(bitmap,old); else contrastUp(bitmap,bitmap);
            }
        });
        final Button button6 = findViewById(R.id.butt6);
        button6.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(switch2.isChecked()) contrastDown(bitmap,old); else contrastDown(bitmap,bitmap);
            }
        });
        final Button button7 = findViewById(R.id.butt7);
        button7.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(switch2.isChecked()) convolution(bitmap,old,maskBoxBlur); else convolution(bitmap,bitmap,maskBoxBlur);
            }
        });
        final Button button8 = findViewById(R.id.butt8);
        button8.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               if(switch2.isChecked()) toGrayTab(bitmap, old); else toGrayTab(bitmap,bitmap);
                convolution(bitmap,bitmap,maskExtr);
            }
        });
        final Button button9 = findViewById(R.id.butt9);
        button9.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(switch2.isChecked()) convolution(bitmap,old,maskNet); else convolution(bitmap,bitmap,maskNet);
            }
        });
        final Button button10 = findViewById(R.id.butt10);
        button10.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(imgId[0] ==2){
                    imgId[0] = 0;
                }
                else{
                    imgId[0]++;
                }
                if(switch1.isChecked()) {
                    resetRS(old,bmpList[imgId[0]]);
                    resetRS(bitmap,old);
                } else {
                    reset(old,bmpList[imgId[0]]);
                    reset(bitmap,old);
                }
            }
        });
    }

    public void convolution(Bitmap bmp, Bitmap old, double mask[]){
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int pixels[] = new int[width*height];
        int pixels2[] = new int[width*height];
        old.getPixels(pixels,0,width,0,0,width,height);
        for(int i= 0; i<height;i++){
            for(int j = 0;j<width;j++) {
                int pos = i * width + j;
                if (i == 0 || j == 0 || i == height-1 || j==width-1) {
                    pixels2[pos] = Color.rgb(0, 0, 0);
                } else {
                    double sommeR = 0;
                    double sommeG = 0;
                    double sommeB = 0;
                    for (int k = 0; k < 3; k++) {
                        for (int l = 0; l < 3; l++) {
                            sommeR += (Color.red(pixels[pos - (1 - k) * width - (1 - l)])* mask[k*3+l]);
                            sommeG += (Color.green(pixels[pos - (1 - k) * width - (1 - l)])* mask[k*3+l]);
                            sommeB += (Color.blue(pixels[pos - (1 - k) * width - (1 - l)])* mask[k*3+l]);
                        }
                    }
                    if(sommeR < 0) sommeR = 0; if(sommeG<0) sommeG = 0; if(sommeB<0) sommeB=0;
                    if(sommeR > 255) sommeR = 255; if(sommeG>255) sommeG = 255; if(sommeB>255) sommeB=255;
                    pixels2[pos] = Color.rgb((int) sommeR,(int) sommeG,(int) sommeB);
                }
            }
        }
        bmp.setPixels(pixels2,0,width,0,0,width,height);
    }

    public void contrastDown(Bitmap bmp, Bitmap old){
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int pixels[] = new int[width*height];
        old.getPixels(pixels,0,width,0,0,width,height);
        int lut[] = new int[256];
        for(int i=0;i<256;i++){
            lut[i] = (int) (((i-128)/1.1)+128);
            if(lut[i]<0) lut[i] = 0; else if(lut[i]>255) lut[i] = 255;
        }
        for(int i=0;i<pixels.length;i++){
            pixels[i] = Color.rgb(lut[Color.red(pixels[i])],lut[Color.green(pixels[i])],lut[Color.blue(pixels[i])]);
        }
        bmp.setPixels(pixels,0,width,0,0,width,height);
    }

    public void contrastUp(Bitmap bmp, Bitmap old){
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int pixels[] = new int[width*height];
        old.getPixels(pixels,0,width,0,0,width,height);
        int lut[] = new int[256];
        for(int i=0;i<256;i++){
            lut[i] = (int) (((i-128)*1.1)+128);
            if(lut[i]<0) lut[i] = 0; else if(lut[i]>255) lut[i] = 255;
        }
        for(int i=0;i<pixels.length;i++){
            pixels[i] = Color.rgb(lut[Color.red(pixels[i])],lut[Color.green(pixels[i])],lut[Color.blue(pixels[i])]);
        }
        bmp.setPixels(pixels,0,width,0,0,width,height);
    }

    public void threshold(Bitmap bmp, Bitmap old){
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int pixels[] = new int[width*height];
        old.getPixels(pixels,0,width,0,0,width,height);
        int hue = 30 * ((int) (random() * 12)) ;
        for(int i=0;i<pixels.length;i++){
            float[] hsv = new float[3];
            Color.colorToHSV(pixels[i],hsv);
            if(hue == 0){
                if(!(hsv[0] > 335 || hsv[0] < 25)){
                    int light = (int) ((Color.red(pixels[i])*0.3 + Color.green(pixels[i])*0.59 + Color.blue(pixels[i])*0.11));
                    pixels[i] = Color.rgb(light,light,light);
                }
            }
            else{
                if(!(hsv[0] > hue -25 && hsv[0] < hue+25)){
                    int light = (int) ((Color.red(pixels[i])*0.3 + Color.green(pixels[i])*0.59 + Color.blue(pixels[i])*0.11));
                    pixels[i] = Color.rgb(light,light,light);
                }
            }
        }
        bmp.setPixels(pixels,0,width,0,0,width,height);
    }

    public void colorize(Bitmap bmp, Bitmap old){
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int pixels[] = new int[width*height];
        old.getPixels(pixels,0,width,0,0,width,height);
        int hue = (int) (random() * 360) ;
        for(int i=0;i<pixels.length;i++){
            float[] hsv = new float[3];
            hsv[0] = hue;
            float cMax = (float) (Math.max(Color.red(pixels[i]), Math.max(Color.green(pixels[i]),Color.blue(pixels[i]))));
            if(cMax == 0)
                hsv[1]=0;
            else{
                float cMin = (float) (Math.min(Color.red(pixels[i]), Math.min(Color.green(pixels[i]),Color.blue(pixels[i]))));
                hsv[1]=(cMax - cMin)/cMax;
            }
            hsv[2]=cMax/255;
            pixels[i] = Color.HSVToColor(hsv);
        }
        bmp.setPixels(pixels,0,width,0,0,width,height);
    }

    public void reset(Bitmap bmp,Bitmap old){
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int pixels[] = new int[width*height];
        old.getPixels(pixels,0,width,0,0,width,height);
        bmp.setPixels(pixels,0,width,0,0,width,height);
    }

    public void toGrayTab(Bitmap bmp,Bitmap old){
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int pixels[] = new int[width*height];
        old.getPixels(pixels,0,width,0,0,width,height);
        for(int i=0;i<pixels.length;i++){
            int light = (int) ((Color.red(pixels[i])*0.3 + Color.green(pixels[i])*0.59 + Color.blue(pixels[i])*0.11));
            pixels[i] = Color.rgb(light,light,light);
        }
        bmp.setPixels(pixels,0,width,0,0,width,height);
    }

    private void colorizeRS(Bitmap bmp, Bitmap old){
        RenderScript rs = RenderScript.create(this);
        Allocation input = Allocation.createFromBitmap(rs, old);
        Allocation output = Allocation.createTyped(rs, input.getType());
        ScriptC_bestscript colorScript = new ScriptC_bestscript(rs);
        //colorScript.set_H((float) Math.random()*360);
        //colorScript.set_option(2.0f);
        colorScript.forEach_func(input, output);
        output.copyTo(bmp);
        input.destroy();
        output.destroy();
        colorScript.destroy();
        rs.destroy();
    }

    private void resetRS(Bitmap bmp, Bitmap old){
        RenderScript rs = RenderScript.create(this);
        Allocation input = Allocation.createFromBitmap(rs, old);
        input.copyTo(bmp);
        input.destroy();
        rs.destroy();
    }

    private void toGreyRS(Bitmap bmp,Bitmap old) {
        RenderScript rs = RenderScript.create(this);
        Allocation input = Allocation.createFromBitmap(rs, old);
        Allocation output = Allocation.createTyped(rs, input.getType());
        ScriptC_bestscript greyScript = new ScriptC_bestscript(rs);
        greyScript.forEach_func(input, output);
        output.copyTo(bmp);
        input.destroy();
        output.destroy();
        greyScript.destroy();
        rs.destroy();
    }


}

