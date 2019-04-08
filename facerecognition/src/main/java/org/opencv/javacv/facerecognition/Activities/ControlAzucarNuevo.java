package org.opencv.javacv.facerecognition.Activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
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
import android.view.Window;
import android.view.WindowManager;
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

public class ControlAzucarNuevo extends AppCompatActivity{

    EditText etComentario,etOrdenTrabajo;
    TextView txtFecha;
    ContentValues userData;
    GenericObject object;
    int modo_view;
    ImageView ivFoto;
    ImageButton btCamera;
    Bitmap bitmapResult;
    MenuItem item_send,item_edit;
    Toolbar toolbar;

    public  ControlAzucarNuevo(){
        object = new GenericObject();
        userData = new ContentValues();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_azucar_nuevo);
        cargarParametros();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar4);

        try {

            etOrdenTrabajo = (EditText) findViewById(R.id.etOrdenTrabajoCSN);
            etComentario = (EditText) findViewById(R.id.etComentarioCSN);
            ivFoto = (ImageView) findViewById(R.id.ivFotoCSN);
            btCamera = (ImageButton) findViewById(R.id.btCameraCSN);
            txtFecha = (TextView) findViewById(R.id.txtFecha);
            String formattedDate = CustomHelper.getDateTimeString();
            txtFecha.setText(formattedDate);
            AppBarLayout appbar = (AppBarLayout) findViewById(R.id.appbar);
            setSupportActionBar(toolbar);

            cargarDatos();
            invalidateOptionsMenu();
            //establecer toolbar
            if(modo_view == CustomHelper.MODO_EDITAR) {
                toolbar.setTitle("Editar Registro");
                appbar.setVisibility(View.VISIBLE);
                setEnabled(true);
            }else if(modo_view == CustomHelper.MODO_VER) {
                toolbar.setTitle("Ver Registro");
                appbar.setVisibility(View.VISIBLE);
                setEnabled(false);
                ivFoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alert(bitmapResult, "Foto 1", false).show();
                    }
                });
            }else if(modo_view == CustomHelper.MODO_NUEVO) {
                toolbar.setTitle("Nuevo control de azucar");
                appbar.setVisibility(View.VISIBLE);
                setEnabled(true);
                ivFoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alert(bitmapResult,"Foto 1", true).show();
                    }
                });
            }

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            btCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bitmapResult = null;
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, 0);
                }
            });

        } catch (Exception e) {
            Log.e(ControlAzucarNuevo.class.getName(), e.getMessage(), e);
        }
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
                object = db.getControlAzucar(id);
                Log.i("app","object: "+object);
                db.close();
            }
        }
    }

    private void cargarDatos(){

       if(modo_view == CustomHelper.MODO_EDITAR || modo_view == CustomHelper.MODO_VER && object != null) {
           etOrdenTrabajo.setText(object.getAsString("orden_trabajo"));
           etComentario.setText(object.getAsString("comentario"));
           txtFecha.setText(object.getAsString("timestamp"));
           if (!Strings.isNullOrEmpty(object.getAsString("foto"))){
                ByteArrayInputStream imageStream = new ByteArrayInputStream(object.getAsByte("foto"));
                bitmapResult = BitmapFactory.decodeStream(imageStream);
               ivFoto.setImageBitmap(bitmapResult);
            }

       }
    }

    private void setEnabled(boolean state){
        etComentario.setEnabled(state);
        etComentario.setFocusable(state);
        etOrdenTrabajo.setEnabled(state);
        etOrdenTrabajo.setFocusable(state);
        btCamera.setEnabled(state);
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
            if (etOrdenTrabajo.getText().toString().isEmpty()) {
                Toast.makeText(this, "Ingrese orden de trabajo", Toast.LENGTH_SHORT).show();
                return false;
            } else if (etComentario.getText().toString().isEmpty()) {
                Toast.makeText(this, "Ingrese comentario", Toast.LENGTH_SHORT).show();
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

            radio.put("comentario", Strings.isNullOrEmpty(etComentario.getText().toString()) ? "Ninguno" : etComentario.getText().toString());
            radio.put("orden_trabajo", etOrdenTrabajo.getText().toString());
            radio.put("usuario_id", id_u);
            radio.put("puesto_id", id_p);
            radio.put("foto", CustomHelper.convertBitmapToBytes(bitmapResult));
            radio.put("timestamp", CustomHelper.getDateTimeString());

            //radio.setFecha(formattedDate);

                if (modo_view == CustomHelper.MODO_NUEVO) {
                    DatabaseAdmin db = new DatabaseAdmin(getApplicationContext());
                    id_query = db.registrar("controlsalida", radio);
                    db.close();
                    if (id_query > 0) {
                        Toast.makeText(getApplicationContext(), "Control de azucar ingresado correctamente!", Toast.LENGTH_SHORT).show();
                        finish();
                        /*habilite estos comentarios y quite el finish() para dotar de edicion despues de registrar
                        radio.put("id",id_query);
                        object.setValues(radio);
                        modo_view = CustomHelper.MODO_VER;
                        toolbar.setTitle("Ver registro");
                        invalidateOptionsMenu();
                        setEnabled(false);*/

                    } else {
                        Toast.makeText(getApplicationContext(), "Control de azucar no ingresado!", Toast.LENGTH_SHORT).show();
                    }

                } else if (modo_view == CustomHelper.MODO_EDITAR) {
                    DatabaseAdmin db = new DatabaseAdmin(getApplicationContext());
                    if (db.update("controlsalida", radio,"id="+ id_b)) {
                        radio.put("id",id_b);
                        object.setValues(radio);
                        Toast.makeText(getApplicationContext(), "Control de azucar ingresado correctamente!", Toast.LENGTH_SHORT).show();
                        modo_view = CustomHelper.MODO_VER;
                        toolbar.setTitle("Ver registro");
                        invalidateOptionsMenu();
                        setEnabled(false);
                    } else {
                        Toast.makeText(getApplicationContext(), "Control de azucar no ingresado!", Toast.LENGTH_SHORT).show();
                    }
                    db.close();
                }

        }
        break;
            case R.id.action_edit: {

                    modo_view = CustomHelper.MODO_EDITAR;
                    toolbar.setTitle("Editar registro");
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
                //item_edit.setVisible(true); habilite esto para proporcionar edicion en el modo ver
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
        try {
            bitmapResult = (Bitmap) data.getExtras().get("data");
            ivFoto.setImageBitmap(bitmapResult);
        } catch (Exception e){
            Log.e("app", e.getMessage(), e);
        }
    }

    public AlertDialog alert(final Bitmap bitmap, final String titulo,  boolean withButtonDelete) {

        AlertDialog.Builder builder = new AlertDialog.Builder(ControlAzucarNuevo.this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.activity_dialog,null);
        ImageView image = (ImageView) layout.findViewById(R.id.ivImageDialog);
        image.setImageBitmap(bitmap);
        TextView text = layout.findViewById(R.id.tvTitleDialog);
        text.setText(titulo);

        Dialog dialogo = builder.setView(layout).create();

        dialogo.requestWindowFeature(Window.FEATURE_NO_TITLE);

        try {
            dialogo.getWindow().getAttributes().width = WindowManager.LayoutParams.FILL_PARENT;
        }catch (Exception e){

        }

        builder.setTitle("Visor de imágenes");

        builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        if (withButtonDelete) {
            builder.setNegativeButton("Eliminar Foto", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    bitmapResult = null;
                    ivFoto.setImageBitmap(bitmapResult);
                    dialog.cancel();
                }
            });
        }

        return builder.create();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

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
        state.putInt("modo_view",modo_view);
        state.putString("etComentario",etComentario.getText().toString());
        state.putString("etOrdenTrabajo",etOrdenTrabajo.getText().toString());
    }

    @Override
    public void onDestroy(){

        super.onDestroy();
    }
}
