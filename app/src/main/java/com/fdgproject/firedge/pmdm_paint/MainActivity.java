package com.fdgproject.firedge.pmdm_paint;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;


public class MainActivity extends Activity implements ColorPicker.OnColorChangedListener{

    private Dibujo dibujo;
    private final static int IDLAPIZ = 0, IDRECTANGULO = 1, IDCIRCULO = 2, IDRECTA = 3,
                                IDRELLENO = 4, IDCOGERCOLOR = 5, IDGOMA = 6, PICK = 8;
    private ImageView aux;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(
                Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        //Añadir el canvas al layout de activitymain
        final LinearLayout prin = (LinearLayout)findViewById(R.id.lyDibujo);
        dibujo = new Dibujo(this);
        prin.addView(dibujo);

        //Funcionamiento del SeekBar
        SeekBar sbSize = (SeekBar) findViewById(R.id.sbSize);
        sbSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                dibujo.setSize(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //Funcionalidad de los botones:
        //Lapiz
        final ImageView lapiz = (ImageView)findViewById(R.id.ivLapiz);
        lapiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dibujo.setTipo(IDLAPIZ);
                cambiarBotones(lapiz);
            }
        });
        aux = lapiz;
        //Rectangulo
        final ImageView rectangulo = (ImageView)findViewById(R.id.ivRectangulo);
        rectangulo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dibujo.setTipo(IDRECTANGULO);
                cambiarBotones(rectangulo);
            }
        });

        //Circulo
        final ImageView circulo = (ImageView)findViewById(R.id.ivCirculo);
        circulo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dibujo.setTipo(IDCIRCULO);
                cambiarBotones(circulo);
            }
        });

        //Recta
        final ImageView recta = (ImageView)findViewById(R.id.ivRecta);
        recta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dibujo.setTipo(IDRECTA);
                cambiarBotones(recta);
            }
        });

        //Relleno
        final ImageView relleno = (ImageView)findViewById(R.id.ivRelleno);
        relleno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dibujo.setTipo(IDRELLENO);
                cambiarBotones(relleno);
            }
        });

        //Coger color
        final ImageView cogercolor = (ImageView) findViewById(R.id.ivCogercolor);
        cogercolor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dibujo.setTipo(IDCOGERCOLOR);
                dibujo.setView(MainActivity.this);
                cambiarBotones(cogercolor);
            }
        });

        //Goma
        final ImageView goma = (ImageView) findViewById(R.id.ivGoma);
        goma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dibujo.setTipo(IDGOMA);
                cambiarBotones(goma);
            }
        });

        //Selector de color
        ImageView sc = (ImageView) findViewById(R.id.ivColor);
        sc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ColorPicker(view.getContext(), MainActivity.this, "", Color.BLACK, Color.WHITE).show();
            }
        });

        //Nuevo
        ImageView nuevo = (ImageView) findViewById(R.id.ivNuevo);
        nuevo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dibujo.nuevo();
                prin.removeView(dibujo);
                prin.addView(dibujo);
            }
        });

        //Abrir
        ImageView abrir = (ImageView) findViewById(R.id.ivAbrir);
        abrir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryIntent.setType("image/*");
                Intent chooser = Intent.createChooser(galleryIntent, "imagen");
                startActivityForResult(chooser, PICK);
            }
        });

        //Guardar
        ImageView guardar = (ImageView) findViewById(R.id.ivGuardar);
        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dibujo.setView(MainActivity.this);
                dibujo.guardar();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK && resultCode == RESULT_OK){
            Uri uri = data.getData();
            Bitmap bitmap;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            } catch (Exception ex){
                bitmap = null;
            };
            dibujo.abrir(bitmap);
        }
    }

    private void cambiarBotones(ImageView iv){
        aux.setBackgroundColor(Color.WHITE);
        iv.setBackgroundColor(Color.parseColor("#ff888888"));
        aux = iv;
    }

    @Override
    public void colorChanged(String key, int color) {
        findViewById(R.id.ivColor).setBackgroundColor(color);
        dibujo.setColor(color);
    }
}
