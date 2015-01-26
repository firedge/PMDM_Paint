package com.fdgproject.firedge.pmdm_paint;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Firedge on 12/01/2015.
 */
public class Dibujo extends View implements Serializable{

    private int tipo;
    private Paint pincel, goma;
    private Bitmap mapaDeBits;
    private Canvas lienzoFondo;
    private double radio;
    private Path rectaPoligonal = new Path();
    private MainActivity main;
    private final static int IDLAPIZ = 0, IDRECTANGULO = 1, IDCIRCULO = 2, IDRECTA = 3,
            IDRELLENO = 4, IDCOGERCOLOR = 5, IDGOMA = 6;

    public Dibujo(Context context){
        super(context);
        pincel = new Paint();
        pincel.setAntiAlias(true);
        pincel.setStrokeWidth(10);
        pincel.setColor(Color.BLACK);
        pincel.setStyle(Paint.Style.STROKE);
        tipo = 0;
        goma = new Paint();
        goma.setAntiAlias(true);
        goma.setStrokeWidth(10);
        goma.setColor(Color.WHITE);
        goma.setStyle(Paint.Style.STROKE);
    }

    public void setSize(float x){
        pincel.setStrokeWidth(x);
        goma.setStrokeWidth(x);
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public void setColor(int color){
        pincel.setColor(color);
    }

    public void nuevo(){
        lienzoFondo = new Canvas(mapaDeBits);
        lienzoFondo.drawColor(Color.WHITE);
    }

    public void abrir(Bitmap bm){
        if(bm!=null) {
            Rect source = new Rect(0, 0, bm.getWidth(), bm.getHeight());
            Rect bitmapRect = new Rect(0, 0, lienzoFondo.getWidth(), lienzoFondo.getHeight());
            lienzoFondo.drawBitmap(bm, source, bitmapRect, null);
        } else {
            lienzoFondo = new Canvas(mapaDeBits);
            lienzoFondo.drawColor(Color.WHITE);
        }
    }

    public void guardar(){
        Bitmap imagen = getBitmap(lienzoFondo);

        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "PAINT_" + timeStamp + "_";
            File storageDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".png",         /* suffix */
                    storageDir      /* directory */
            );
            FileOutputStream salida = new FileOutputStream(image);
            imagen.compress(Bitmap.CompressFormat.PNG, 0, salida);
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(image);
            mediaScanIntent.setData(contentUri);
            main.sendBroadcast(mediaScanIntent);
            Toast.makeText(main, getResources().getString(R.string.guardar), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(main, getResources().getString(R.string.noguardar), Toast.LENGTH_SHORT).show();
        }
    }

    public String generaNombre(){
        String s = "dibujo_";
        Calendar cal = new GregorianCalendar();
        Date date = cal.getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
        String formatteDate = df.format(date);
        s+=formatteDate+".png";
        return s;
    }

    public void setView(MainActivity v){
        main = v;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mapaDeBits, 0, 0, null);
        switch (tipo){
            case IDRECTANGULO:
                float oxorigen = Math.min(x0, xi);
                float xdestino = Math.max(x0,xi);
                float yorigen = Math.min(y0, yi);
                float ydestino = Math.max(y0, yi);
                canvas.drawRect(oxorigen,yorigen,xdestino,ydestino,pincel);
                break;
            case IDCIRCULO:
                canvas.drawCircle(x0,y0,(float)radio,pincel);
                break;
            case IDRECTA:
                canvas.drawLine(x0, y0, xi, yi, pincel);
                break;
            default:
                break;
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mapaDeBits = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        lienzoFondo = new Canvas(mapaDeBits);
        lienzoFondo.drawColor(Color.WHITE);
    }

    private float x0, y0, xi, yi;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x0 = xi = x;
                y0 = yi = y;
                switch (tipo){
                    case IDLAPIZ:
                        rectaPoligonal.reset();
                        rectaPoligonal.moveTo(x0, y0);
                        break;
                    case IDRELLENO:
                        int x2 = (int)x;
                        int y2 = (int)y;
                        floodFill(new Point(x2, y2), cogerColor(x2, y2), pincel.getColor());
                        break;
                    case IDCOGERCOLOR:
                        int x1 = (int)x;
                        int y1 = (int)y;
                        int color = cogerColor(x1, y1);
                        pincel.setColor(color);
                        main.findViewById(R.id.ivColor).setBackgroundColor(color);
                        break;
                    case IDGOMA:
                        rectaPoligonal.reset();
                        rectaPoligonal.moveTo(x0, y0);
                        break;
                    default:
                        break;
                }
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                switch (tipo){
                    case IDLAPIZ:
                        rectaPoligonal.quadTo(xi, yi, (x + xi) / 2, (y + yi)/2);
                        rectaPoligonal.quadTo((x+xi)/2, (y+yi)/2, x, y);
                        lienzoFondo.drawPath(rectaPoligonal, pincel);
                        break;
                    case IDCIRCULO:
                        radio = Math.sqrt(((xi-x0)*(xi-x0))+((yi-y0)*(yi-y0)));
                        break;
                    case IDRECTA:
                        //lienzoFondo.drawLine(x0, y0, xi, yi, pincel);
                        break;
                    case IDGOMA:
                        rectaPoligonal.quadTo(xi, yi, (x + xi) / 2, (y + yi)/2);
                        rectaPoligonal.quadTo((x+xi)/2, (y+yi)/2, x, y);
                        lienzoFondo.drawPath(rectaPoligonal, goma);
                        break;
                    default:
                        break;
                }
                xi = x;
                yi = y;
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                xi = x;
                yi = y;
                switch (tipo){
                    case IDLAPIZ:
                        lienzoFondo.drawPath(rectaPoligonal, pincel);
                        break;
                    case IDRECTANGULO:
                        lienzoFondo.drawRect(x0,y0,xi,yi,pincel);
                        break;
                    case IDCIRCULO:
                        radio = Math.sqrt(((xi-x0)*(xi-x0))+((yi-y0)*(yi-y0)));
                        lienzoFondo.drawCircle(x0,y0,(float)radio,pincel);
                        radio = 0;
                        break;
                    case IDRECTA:
                        lienzoFondo.drawLine(x0, y0, xi, yi, pincel);
                        break;
                    case IDGOMA:
                        lienzoFondo.drawPath(rectaPoligonal, goma);
                        break;
                    default:
                        break;
                }
                invalidate();
                x0=y0=xi=yi=-1;
                break;
        }

        return true;
    }

    //Sacar mapa de bits
    public static Bitmap getBitmap(Canvas canvas) {
        try {
            java.lang.reflect.Field field = Canvas.class.getDeclaredField("mBitmap");
            field.setAccessible(true);
            return (Bitmap)field.get(canvas);
        }
        catch (Throwable t) {
            return null;
        }
    }

    //Coger color
    public int cogerColor(int x, int y){
        Bitmap map = getBitmap(lienzoFondo);
        if (map != null) {
            return map.getPixel(x, y);
        }
        return -1;
    }

    //Funcion para rellenar
    public void floodFill(Point punto, int colorInicial, int colorFinal) {
        Bitmap image = getBitmap(lienzoFondo);
        int width = image.getWidth();
        int height = image.getHeight();
        int target = colorInicial;
        int replacement = colorFinal;
        if (target != replacement) {
            Queue<Point> queue = new LinkedList<Point>();
            do {
                int x = punto.x;
                int y = punto.y;
                while (x > 0 && image.getPixel(x - 1, y) == target) {
                    x--;
                }
                boolean spanUp = false;
                boolean spanDown = false;
                while (x < width && image.getPixel(x, y) == target) {
                    image.setPixel(x, y, replacement);
                    if (!spanUp && y > 0 && image.getPixel(x, y - 1) == target) {
                        queue.add(new Point(x, y - 1));
                        spanUp = true;
                    } else if (spanUp && y > 0
                            && image.getPixel(x, y - 1) != target) {
                        spanUp = false;
                    }
                    if (!spanDown && y < height - 1
                            && image.getPixel(x, y + 1) == target) {
                        queue.add(new Point(x, y + 1));
                        spanDown = true;
                    } else if (spanDown && y < height - 1
                            && image.getPixel(x, y + 1) != target) {
                        spanDown = false;
                    }
                    x++;
                }
            } while ((punto = queue.poll()) != null);
        }
    }
}
