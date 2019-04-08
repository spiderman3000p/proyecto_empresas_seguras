package org.opencv.javacv.facerecognition.Activities;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.opencv.javacv.facerecognition.Adapters.CommentAdapter;
import org.opencv.javacv.facerecognition.Database.DatabaseAdmin;
import org.opencv.javacv.facerecognition.Helpers.CustomHelper;
import org.opencv.javacv.facerecognition.Models.GenericObject;
import org.opencv.javacv.facerecognition.R;

import java.util.ArrayList;

public class BitacoraNuevo extends AppCompatActivity{

    EditText etNovedadN;
    TextView txtFecha;
    ContentValues userData;
    Switch tipoUrgencia;
    int modo_view = -1,num_comentarios=0;
    FloatingActionButton grabarBtn;
    GenericObject object;
    MenuItem item_send,item_edit;
    Toolbar toolbar;
    CommentAdapter commentsAdapter;
    RecyclerView listaComentarios;
    ArrayList<GenericObject> comentarios;
    LinearLayoutManager llm;
    private static final int RECOGNIZE_SPEECH_ACTIVITY = 100;

    public BitacoraNuevo(){
        object = new GenericObject();
        userData = new ContentValues();
        comentarios = Lists.newArrayList();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_bitacora_nuevo);
        cargarParametros();
        etNovedadN = (EditText) findViewById(R.id.textNovedad);
        tipoUrgencia = (Switch) findViewById(R.id.switchRespondio);
        grabarBtn = (FloatingActionButton) findViewById(R.id.grabarButton);
        txtFecha = (TextView) findViewById(R.id.txtFecha);
        AppBarLayout appbar = (AppBarLayout) findViewById(R.id.appbar);
        listaComentarios = (RecyclerView) findViewById(R.id.rvComentarios);
        listaComentarios.setHasFixedSize(true);
        llm = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        listaComentarios.setLayoutManager(llm);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar4);
        //toolbar.setNavigationIcon(R.drawable.user);
        /*toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });*/
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //String formattedDate = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(System.currentTimeMillis());
        String formattedDate = CustomHelper.getDateTimeString();
        grabarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                grabarVoz();
            }
        });
        try {
            txtFecha.setText(formattedDate);

            cargarDatos();
            if(modo_view == CustomHelper.MODO_EDITAR) {
                toolbar.setTitle("Editar bitacora");
                setSupportActionBar(toolbar);
                setEnabled(true);
            }
            if(modo_view == CustomHelper.MODO_VER) {
                setEnabled(false);
                toolbar.setTitle("Ver bitacora");
                setSupportActionBar(toolbar);

            }

        } catch (Exception e) {
            Log.e(BitacoraNuevo.class.getName(), e.getMessage(), e);
        }
    }

    private void cargarDatos() {

        if(modo_view == CustomHelper.MODO_EDITAR || modo_view == CustomHelper.MODO_VER && object != null) {
            etNovedadN.setText(object.getAsString("observacion"));
            txtFecha.setText(object.getAsString("timestamp"));
            tipoUrgencia.setChecked(object.getAsInt("tipo") == 1);
            //TODO: aqui falta cargar la lista de comentarios y mostrarlas
            if(object.getAsInt("num_comentarios") > 0) {
                DatabaseAdmin db = new DatabaseAdmin(this);
                comentarios = new ArrayList<GenericObject>();

                Log.i("app", "Comentarios recuperados: " + comentarios);
                commentsAdapter = new CommentAdapter(this, comentarios, modo_view);
                listaComentarios.setAdapter(commentsAdapter);
                comentarios.addAll(db.obtenerComentariosBitacora2(object.getAsInt("id")));
                commentsAdapter.notifyDataSetChanged();
                db.close();
            }

        }else{
            Log.i("app","Objeto bitacora es NULL");
        }
    }

    private void setEnabled(boolean state){
        tipoUrgencia.setEnabled(state);
        etNovedadN.setEnabled(state);
        etNovedadN.setFocusable(state);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            etNovedadN.setFocusedByDefault(state);
        }
        grabarBtn.setVisibility(state?View.VISIBLE:View.GONE);
        if(num_comentarios > 0) {
            commentsAdapter = new CommentAdapter(this, comentarios, modo_view);
            listaComentarios.setAdapter(commentsAdapter);
            commentsAdapter.notifyDataSetChanged();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RECOGNIZE_SPEECH_ACTIVITY:

                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> speech = data
                            .getStringArrayListExtra(RecognizerIntent.
                                    EXTRA_RESULTS);
                    String strSpeech2Text = speech.get(0);

                    etNovedadN.setText(strSpeech2Text);
                }

                break;
            default:

                break;
        }
    }

    public void grabarVoz() {

        Intent intentActionRecognizeSpeech = new Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // Configura el Lenguaje (Español-México)
        intentActionRecognizeSpeech.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intentActionRecognizeSpeech.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            intentActionRecognizeSpeech.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
        }
        String languages[] = {"en-US","es-ES","es-MEX","es-ECU"};
        intentActionRecognizeSpeech.putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, languages);
        intentActionRecognizeSpeech.putExtra(RecognizerIntent.EXTRA_PROMPT, "Presione el boton para empezar a grabar");


        try {
            startActivityForResult(intentActionRecognizeSpeech,
                    RECOGNIZE_SPEECH_ACTIVITY);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "Tú dispositivo no soporta el reconocimiento por voz",
                    Toast.LENGTH_SHORT).show();
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

            if (bundle.get("id_objeto") != null) {
                int id = bundle.getInt("id_objeto");
                DatabaseAdmin db = new DatabaseAdmin(this);
                object = db.obtenerBitacora(id);
                num_comentarios = object.getAsInt("num_comentarios");
                Log.i("app","object: "+object);
                db.close();
            }
        }

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
        int id_u = 0,id_p = 0, id_b = 0;
        long id_query,num_comentarios = 0;

        if (id == R.id.action_send) {
            if (etNovedadN.getTextSize() == 0) {
                Toast.makeText(this, "Ingrese una novedad", Toast.LENGTH_SHORT).show();
                return false;
            }
            ContentValues o = new ContentValues();
            if(modo_view == CustomHelper.MODO_NUEVO){
                id_u = userData.getAsInteger("id");
                id_p = userData.getAsInteger("puesto_id");
            }else if(modo_view == CustomHelper.MODO_VER || modo_view == CustomHelper.MODO_EDITAR) {
                id_u = object.getAsInt("usuario_id");
                id_p = object.getAsInt("puesto_id");
                id_b = object.getAsInt("id");
            }

                    o.put("observacion",Strings.isNullOrEmpty(etNovedadN.getText().toString()) ? "Ninguna" : etNovedadN.getText().toString().toUpperCase());
                    o.put("tipo",tipoUrgencia.isChecked()?1:0);
                    //o.put("num_comentarios",num_comentarios);
                    o.put("puesto_id",id_p);
                    o.put("usuario_id",id_u);
                    String formattedDate = CustomHelper.getDateTimeString();
                    o.put("timestamp",formattedDate);


            if (modo_view == CustomHelper.MODO_NUEVO) {
                DatabaseAdmin db = new DatabaseAdmin(this);
                id_query = db.registrar("bitacora", o);
                db.close();
                if (id_query > 0) {

                    Toast.makeText(getApplicationContext(), "Registro exitoso!", Toast.LENGTH_SHORT).show();
                    finish();
                    /* habilite estos comentarios y quite el finish() para proporcionar edicion al registrar
                    o.put("id",id_query);
                    object.setValues(o);
                    modo_view = CustomHelper.MODO_VER;
                    toolbar.setTitle("Ver registro");
                    invalidateOptionsMenu();
                    setEnabled(false);*/

                } else {
                    Toast.makeText(getApplicationContext(), "Registro fallido!", Toast.LENGTH_SHORT).show();
                }

            } else if (modo_view == CustomHelper.MODO_EDITAR) {
                DatabaseAdmin db = new DatabaseAdmin(this);
                    if (db.update("bitacora", o, "id = "+id_b)) {
                        o.put("id",id_b);
                        object.setValues(o);
                        modo_view = CustomHelper.MODO_VER;
                        toolbar.setTitle("Ver registro");
                        invalidateOptionsMenu();
                        setEnabled(false);
                    } else {
                        Toast.makeText(getApplicationContext(), "Actualizacion fallida!", Toast.LENGTH_SHORT).show();
                    }
                    db.close();
            }


        }else if(id == R.id.action_edit){

            modo_view = CustomHelper.MODO_EDITAR;
            toolbar.setTitle("Editar registro");
            invalidateOptionsMenu();
            setEnabled(true);

        }
        if(id == R.id.home){
            onBackPressed();
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
                //item_edit.setVisible(true); quite este comentario para habilitar la edicion
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
        state.putParcelable("object",object);
    }

    @Override
    public void onDestroy(){

        this.commentsAdapter = null;
        this.listaComentarios = null;
        super.onDestroy();
    }
}
