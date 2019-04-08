package org.opencv.javacv.facerecognition.Activities;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Strings;

import org.opencv.javacv.facerecognition.Database.DatabaseAdmin;
import org.opencv.javacv.facerecognition.Helpers.CustomHelper;
import org.opencv.javacv.facerecognition.Models.GenericObject;
import org.opencv.javacv.facerecognition.R;

import java.io.ByteArrayInputStream;

public class InformeNuevo extends AppCompatActivity{
    GenericObject object;
    ContentValues userData;
    TextView tvFechaIEN1;
    EditText etTituloIEN1, etObservacionIEN1;
    ImageButton btCameraIEN1;
    Button btImageIEN1, btImageIEN2, btImageIEN3;
    Bitmap bitmapResult1 = null, bitmapResult2 = null, bitmapResult3 = null;
    AppBarLayout appbar;
    boolean isFoto1 = false, isFoto2 = false, isFoto3 = false;
    MenuItem item_send,item_edit;
    Toolbar toolbar;
    boolean withButtonDelete;
    int modo_view = -1;

    public InformeNuevo(){
        object = new GenericObject();
        userData = new ContentValues();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_informe_nuevo);
        cargarParametros();
        toolbar = (Toolbar) findViewById(R.id.toolbar4);
        try {
            String formattedDate = CustomHelper.getDateTimeString();

            tvFechaIEN1 = (TextView) findViewById(R.id.textFecha);
            etTituloIEN1 = (EditText) findViewById(R.id.etTituloIEN1);
            etObservacionIEN1 = (EditText) findViewById(R.id.etObservacionIEN1);
            btCameraIEN1 = (ImageButton) findViewById(R.id.btCameraIEN1);

            btImageIEN1 = (Button) findViewById(R.id.btImageIEN1);
            btImageIEN2 = (Button) findViewById(R.id.btImageIEN2);
            btImageIEN3 = (Button) findViewById(R.id.btImageIEN3);

            tvFechaIEN1.setText(formattedDate);
            appbar = (AppBarLayout) findViewById(R.id.appbar);

            cargarDatos();
            invalidateOptionsMenu();
            //establecer toolbar
            cambiarTitulo();

            btCameraIEN1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (bitmapResult1 == null) {
                        isFoto1 = true;
                        isFoto2 = false;
                        isFoto3 = false;
                    } else if (bitmapResult2 == null) {
                        isFoto1 = false;
                        isFoto2 = true;
                        isFoto3 = false;
                    } else if (bitmapResult3 == null) {
                        isFoto1 = false;
                        isFoto2 = false;
                        isFoto3 = true;
                    }
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, 0);
                }
            });

            btImageIEN1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alert(bitmapResult1, "img1", "Imagen 1", withButtonDelete).show();
                }
            });

            btImageIEN2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alert(bitmapResult2, "img2", "Imagen 2", withButtonDelete).show();
                }
            });

            btImageIEN3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alert(bitmapResult3, "img3", "Imagen 3", withButtonDelete).show();
                }
            });

        } catch (Exception e) {
            Log.e(InformeNuevo.class.getName(), e.getMessage(), e);
        }
    }

    private void cambiarTitulo(){
        if(modo_view == CustomHelper.MODO_EDITAR) {
            toolbar.setTitle("Editar Registro");
            appbar.setVisibility(View.VISIBLE);
            setEnabled(true);
        }else if(modo_view == CustomHelper.MODO_VER) {
            toolbar.setTitle("Ver Registro");
            appbar.setVisibility(View.VISIBLE);
            setEnabled(false);

        }else if(modo_view == CustomHelper.MODO_NUEVO) {
            toolbar.setTitle("Nuevo Informe");
            appbar.setVisibility(View.VISIBLE);
            setEnabled(true);
        }
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void cargarParametros() {
    Log.i("app","cargando parametros...");
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.get("userData") != null) {
                userData = (ContentValues) bundle.get("userData");
                Log.i("app","userData: "+userData);
            }

            if (bundle.get("modo_view") != null) {
                modo_view = bundle.getInt("modo_view");
                Log.i("app","modo_view: "+modo_view);
            }

            if (bundle.getInt("id_objeto") > 0) {
                int id = bundle.getInt("id_objeto");
                DatabaseAdmin db = new DatabaseAdmin(this);
                object = db.getInformeEspecial(id);
                Log.i("app","object: "+object);
                db.close();
            }
        }
    }

    private void cargarDatos(){

        withButtonDelete = modo_view == CustomHelper.MODO_NUEVO;

       if(modo_view == CustomHelper.MODO_EDITAR || modo_view == CustomHelper.MODO_VER && object != null) {
           etTituloIEN1.setText(object.getAsString("titulo"));
           etObservacionIEN1.setText(object.getAsString("observacion"));
           tvFechaIEN1.setText(object.getAsString("timestamp"));

           if (!Strings.isNullOrEmpty(object.getAsString("foto1"))){
               ByteArrayInputStream imageStream1 = new ByteArrayInputStream(object.getAsByte("foto1"));
               bitmapResult1 = BitmapFactory.decodeStream(imageStream1);
           }

           if (!Strings.isNullOrEmpty(object.getAsString("foto2"))){
               ByteArrayInputStream imageStream2 = new ByteArrayInputStream(object.getAsByte("foto2"));
               bitmapResult2 = BitmapFactory.decodeStream(imageStream2);
           }

           if (!Strings.isNullOrEmpty(object.getAsString("foto3"))){
               ByteArrayInputStream imageStream3 = new ByteArrayInputStream(object.getAsByte("foto3"));
               bitmapResult3 = BitmapFactory.decodeStream(imageStream3);
           }
       }
    }

    private void setEnabled(boolean state){
        etTituloIEN1.setEnabled(state);
        etObservacionIEN1.setEnabled(state);
        btCameraIEN1.setEnabled(state);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.radio_nuevo_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id){
        //noinspection SimplifiableIfStatement
        case R.id.action_send: {
            if (etTituloIEN1.getText().toString().isEmpty()) {
                Toast.makeText(this, "Ingrese titulo", Toast.LENGTH_SHORT).show();
                return false;
            } else if (etObservacionIEN1.getText().toString().isEmpty()) {
                Toast.makeText(this, "Ingrese Observacion", Toast.LENGTH_SHORT).show();
                return false;
            }
            int id_u = 0, id_p = 0, id_b = 0;
            long id_query;
            if(modo_view == CustomHelper.MODO_VER || modo_view == CustomHelper.MODO_EDITAR) {
                id_u = object.getAsInt("usuario_id");
                id_p = object.getAsInt("puesto_id");
                id_b = object.getAsInt("id");
            }else if(modo_view == CustomHelper.MODO_NUEVO){
                id_u = userData.getAsInteger("id");
                id_p = userData.getAsInteger("puesto_id");
            }

            ContentValues radio = new ContentValues();

            radio.put("observacion", Strings.isNullOrEmpty(etObservacionIEN1.getText().toString()) ? "Ninguno" : etObservacionIEN1.getText().toString());
            radio.put("titulo", etTituloIEN1.getText().toString());
            radio.put("usuario_id", id_u);
            radio.put("puesto_id", id_p);
            if (bitmapResult1 != null) {
                radio.put("foto1",CustomHelper.convertBitmapToBytes(bitmapResult1));
            }
            if (bitmapResult2 != null) {
                radio.put("foto2",CustomHelper.convertBitmapToBytes(bitmapResult2));
            }
            if (bitmapResult3 != null) {
                radio.put("foto3",CustomHelper.convertBitmapToBytes(bitmapResult3));
            }
            String formattedDate = CustomHelper.getDateTimeString();
            radio.put("timestamp", formattedDate);
                if (modo_view == CustomHelper.MODO_NUEVO) {
                    DatabaseAdmin db = new DatabaseAdmin(getApplicationContext());
                    id_query = db.registrar("informe", radio);
                    db.close();
                    if (id_query > 0) {

                        Toast.makeText(getApplicationContext(), "Registro exitoso!", Toast.LENGTH_SHORT).show();
                        finish();

                    } else {
                        Toast.makeText(getApplicationContext(), "Registro fallido!", Toast.LENGTH_SHORT).show();
                    }

                } else if (modo_view == CustomHelper.MODO_EDITAR) {
                    DatabaseAdmin db = new DatabaseAdmin(this);
                    if (db.update("informe", radio, "id = "+id_b)) {
                        radio.put("id",id_b);
                        object.setValues(radio);
                        Toast.makeText(getApplicationContext(), "Actualizacion exitosa!", Toast.LENGTH_SHORT).show();
                        modo_view = CustomHelper.MODO_VER;
                        cambiarTitulo();
                        invalidateOptionsMenu();
                        setEnabled(false);
                    } else {
                        Toast.makeText(getApplicationContext(), "Actualizacion fallida!", Toast.LENGTH_SHORT).show();
                    }
                    db.close();
                }

        }
        break;
            case R.id.action_edit: {

                    modo_view = CustomHelper.MODO_EDITAR;
                    cambiarTitulo();
                    invalidateOptionsMenu();
                    setEnabled(true);

            }
            break;
            case R.id.home:{
                onBackPressed();
            }break;
            default:

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){

        if(menu != null) {
            item_send = menu.getItem(0);
            item_edit = menu.getItem(1);
            if (modo_view == CustomHelper.MODO_EDITAR) {
                item_edit.setVisible(false);
                item_send.setVisible(true);
            } else if (modo_view == CustomHelper.MODO_VER) {
                //item_edit.setVisible(true); habilite esto para porporcionar edicion en el modo ver
                item_send.setVisible(false);
            } else if (modo_view == CustomHelper.MODO_NUEVO) {
                item_edit.setVisible(false);
                item_send.setVisible(true);
            }
        }else{
            Log.e("app","Menu es NULL");
        }

        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("app","onActivityResult()");
        try {
            if (requestCode == 0){
                if (isFoto1) {
                    bitmapResult1 = (Bitmap) data.getExtras().get("data");
                    Log.i("app", "onActivityResult()->isFoto1");
                } else if (isFoto2) {
                    bitmapResult2 = (Bitmap) data.getExtras().get("data");
                    Log.i("app", "onActivityResult()->isFoto2");
                } else if (isFoto3) {
                    bitmapResult3 = (Bitmap) data.getExtras().get("data");
                    Log.i("app", "onActivityResult()->isFoto3");
                } else {
                    Log.i("app", "onActivityResult()->requestCode = 0, pero ningun isFoto es true");
                }
            }else{
                Log.i("app", "onActivityResult()->requestCode != 0");
            }
        } catch (Exception e) {
            Log.e(this.getLocalClassName(), e.getMessage(), e);
            if (isFoto1) {
                isFoto1 = false;
                Log.i("app","onActivityResult()->Exception isFoto1");
            } else if (isFoto2) {
                isFoto2 = false;
                Log.i("app","onActivityResult()->Exception isFoto2");
            } else if (isFoto3) {
                isFoto3 = false;
                Log.i("app","onActivityResult()->Exception isFoto3");
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    public AlertDialog alert(final Bitmap bitmap, final String img, final String texto, boolean withButtonDelete) {

        AlertDialog.Builder builder = new AlertDialog.Builder(InformeNuevo.this);
        LayoutInflater factory = LayoutInflater.from(InformeNuevo.this);

        final View view = factory.inflate(R.layout.activity_dialog, null);
        ImageView image = view.findViewById(R.id.ivImageDialog);
        image.setImageBitmap(bitmap);

        TextView text = view.findViewById(R.id.tvTitleDialog);
        text.setText(texto);

        builder.setTitle("Visor de imágenes");
        builder.setView(view);
        builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        if (withButtonDelete) {
            builder.setNegativeButton("Eliminar Foto", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (img.equalsIgnoreCase("img1")) {
                        bitmapResult1 = null;
                    } else if (img.equalsIgnoreCase("img2")) {
                        bitmapResult2 = null;
                    } else if (img.equalsIgnoreCase("img3")) {
                        bitmapResult3 = null;
                    }
                    dialog.cancel();
                }
            });
        }
        return builder.create();
    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle state){
        super.onSaveInstanceState(state);

        state.putParcelable("userData",userData);

    }

    @Override
    public void onDestroy(){

        super.onDestroy();
    }
}
