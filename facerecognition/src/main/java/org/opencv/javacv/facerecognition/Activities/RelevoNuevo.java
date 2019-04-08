package org.opencv.javacv.facerecognition.Activities;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.opencv.javacv.facerecognition.Adapters.SuministrosAdapter;
import org.opencv.javacv.facerecognition.Database.DatabaseAdmin;
import org.opencv.javacv.facerecognition.Helpers.CustomHelper;
import org.opencv.javacv.facerecognition.Models.GenericObject;
import org.opencv.javacv.facerecognition.R;

import java.util.ArrayList;

public class RelevoNuevo extends AppCompatActivity{

    private static final int LOGIN_USUARIO_SALIENTE = 10;
    private static final int LOGIN_USUARIO_ENTRANTE = 20;
    EditText etComentario,etCantidad;
    ContentValues usuarioSaliente,usuarioEntrante;
    TextView txtFecha;
    TextView tvGuardiaSaliente;
    TextView tvGuardiaEntrante;
    TextView tvNominativo;
    AutoCompleteTextView tvSuministro;
    ContentValues userData;
    int modo_view = -1,num_suministros=0;
    GenericObject object;
    MenuItem item_send,item_edit;
    Toolbar toolbar;
    SuministrosAdapter inventarioAdapter;
    RecyclerView listaInventario;
    ArrayList<GenericObject> suministros;// los suministros que foran parte del inventario de relevo del puesto
    ArrayList<GenericObject> todosSuministros;//todos los suministros asignados al puesto
    LinearLayoutManager llm;
    LinearLayout infoSaliente,infoEntrante, infoNominativo;
    ImageButton addBtn;
    Spinner spSuministros;
    static ArrayAdapter<GenericObject> adSuministro;
    Intent intentLogin;



    public RelevoNuevo(){
        object = new GenericObject();
        userData = new ContentValues();
        suministros = Lists.newArrayList();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_relevo);
        cargarParametros(savedInstanceState);
        addBtn = (ImageButton) findViewById(R.id.btAddSuministroNR1);
        spSuministros = (Spinner) findViewById(R.id.spSuministroNR1);
        etComentario = (EditText) findViewById(R.id.etComentario);
        etCantidad = (EditText) findViewById(R.id.etCantidad);
        txtFecha = (TextView) findViewById(R.id.txtFecha);
        tvGuardiaEntrante = (TextView) findViewById(R.id.tvGuardiaEntrante);
        tvGuardiaSaliente = (TextView) findViewById(R.id.tvGuardiaSaliente);
        tvNominativo = (TextView) findViewById(R.id.tvNominativo);
        infoSaliente = (LinearLayout) findViewById(R.id.infoSaliente);
        infoEntrante = (LinearLayout) findViewById(R.id.infoEntrante);
        infoNominativo = (LinearLayout) findViewById(R.id.infoNominativo);
        tvSuministro = (AutoCompleteTextView) findViewById(R.id.tvSuministro);
        AppBarLayout appbar = (AppBarLayout) findViewById(R.id.appbar);
        listaInventario = (RecyclerView) findViewById(R.id.rvInventarioRelevo);
        listaInventario.setHasFixedSize(true);
        llm = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        listaInventario.setLayoutManager(llm);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar4);
        toolbar.setTitle(R.string.title_activity_relevo_nuevo);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        appbar.setVisibility(View.VISIBLE);
        String formattedDate = CustomHelper.getDateTimeString();
        cargarDatos();



        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cantidad = etCantidad.getText().toString();
                if(!Strings.isNullOrEmpty(cantidad) && Integer.parseInt(cantidad) > 0 && todosSuministros.size() > 0) {
                int index = spSuministros.getSelectedItemPosition();
                GenericObject suministro = (GenericObject)spSuministros.getSelectedItem();
                    suministro.set("cantidad",cantidad);
                    Log.i("app","cantidad: "+cantidad);
                    Log.i("app","agregando suministro seleccionado (nombre): "+suministro.getAsString("nombre"));
                    Log.i("app","agregando suministro seleccionado (cantidad): "+suministro.getAsString("cantidad"));
                    Log.i("app","agregando suministro seleccionado (values): "+suministro.getValues());
                suministros.add(suministro);//agregamos el suministro selecionado a la lista de inventario
                    Log.i("app","lista de suministros seleccionados: "+suministros);
                inventarioAdapter.notifyDataSetChanged();//actualizamos la vista de la lista de inventario
                todosSuministros.remove(index);//sacamos el suministro seleccionado de la lista de suministros (el spinner)
                adSuministro.notifyDataSetChanged();

                Log.i("app","suministro a insertar: "+suministro);
                }else{
                    Toast.makeText(RelevoNuevo.this, "Operacion invalida", Toast.LENGTH_SHORT).show();
                }
            }
        });

        try {
            txtFecha.setText(formattedDate);

            if(modo_view == CustomHelper.MODO_VER) {
                setEnabled(false);
                toolbar.setTitle("Ver Relevo");
                setSupportActionBar(toolbar);
                etComentario.setFocusable(false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    etComentario.setFocusedByDefault(false);
                }
            }

        } catch (Exception e) {
            Log.e(RelevoNuevo.class.getName(), e.getMessage(), e);
        }
    }

    private void cargarDatos() {

        DatabaseAdmin db = new DatabaseAdmin(this);
        int idPuesto[] = {0};
        todosSuministros = new ArrayList<GenericObject>();//el inventario del puesto
        CustomHelper.getIdPuesto(this,CustomHelper.getAndroidId(getContentResolver()));
        todosSuministros = db.obtenerSuministrosByPuesto(idPuesto[0]);
        db.close();

        if(todosSuministros != null) {
            ///////////////////////////////////////////
            Log.i("app","lista de suministros asignados al puesto: "+todosSuministros);
        }

        if(modo_view == CustomHelper.MODO_NUEVO){
            inventarioAdapter = new SuministrosAdapter(this, suministros, todosSuministros, CustomHelper.MODO_VER);
            listaInventario.setAdapter(inventarioAdapter);
            inventarioAdapter.notifyDataSetChanged();
        }

        if(modo_view == CustomHelper.MODO_VER && object != null){
            etComentario.setText(object.getAsString("comentario"));
            txtFecha.setText(object.getAsString("timestamp"));
            //TODO: aqui falta cargar la lista de suministros y mostrarlas
            if(object.getAsInt("num_suministros") > 0) {
                db = new DatabaseAdmin(this);
                suministros.addAll(db.obtenerInventarioRelevo(object.getAsInt("id")));
                db.close();
                inventarioAdapter = new SuministrosAdapter(this,todosSuministros,suministros, modo_view);//el adapter muestra todos los suministros asignados al puesto
                listaInventario.setAdapter(inventarioAdapter);

            }
            tvGuardiaEntrante.setText(object.getAsString("nombre_usuario_entrante"));
            tvGuardiaSaliente.setText(object.getAsString("nombre_usuario_saliente"));
            tvNominativo.setText(object.getAsString("nominativo_puesto"));
            infoNominativo.setVisibility(View.VISIBLE);
            infoSaliente.setVisibility(View.VISIBLE);
            infoEntrante.setVisibility(View.VISIBLE);
        }

    }

    private void setEnabled(boolean state){

        etComentario.setEnabled(state);
        etCantidad.setEnabled(state);
        spSuministros.setEnabled(state);
        listaInventario.setEnabled(state);
        etComentario.setFocusable(state);
        if(!state) {
            TextView textView = (TextView) findViewById(R.id.tvAgregarSuministros);
            textView.setVisibility(View.GONE);
            RelativeLayout layout = (RelativeLayout) findViewById(R.id.layoutAgregarSuministros);
            layout.setVisibility(View.GONE);

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case LOGIN_USUARIO_ENTRANTE: //al regresar de comprobar la identidad del login
                //recuperamos los datos del usuario saliente
                if(resultCode == RESULT_OK) {
                    intentLogin = new Intent(this, LoginActivity.class);
                    intentLogin.putExtra("action", "login_relevo_saliente");
                    //usuarioSaliente = (ContentValues) data.getExtras().get("usuarioSaliente");
                    if(data.getExtras().getParcelableArrayList("suministros") != null){//estos son los suministros confirmados
                        suministros = data.getExtras().getParcelableArrayList("suministros");
                        Log.i("app", "on RelevoNuevo-> suministros despues de LOGIN_USUARIO_ENTRANTE:" + suministros);
                    }
                    if(data.getExtras().get("comentario") != null){//estos son los suministros confirmados
                        etComentario.setText(data.getExtras().getString("comentario"));
                        Log.i("app", "on RelevoNuevo-> comentario despues de LOGIN_USUARIO_ENTRANTE:" + data.getExtras().getString("comentario"));
                    }
                    if (data.getExtras().get("usuarioEntrante") != null)
                        usuarioEntrante = (ContentValues) data.getExtras().get("usuarioEntrante");
                    ContentValues info = obtenerInformacionRelevo();
                    Log.i("app", "usuarioEntrante:" + usuarioEntrante.toString());
                    Log.i("app", "usuarioSaliente:" + usuarioSaliente.toString());

                    intentLogin.putExtra("cedula_saliente", usuarioSaliente.getAsString("dni"));
                    intentLogin.putExtra("mensaje", getString(R.string.usuario_saliente_subtitulo));
                    startActivityForResult(intentLogin, LOGIN_USUARIO_SALIENTE);
                }
                break;
            case LOGIN_USUARIO_SALIENTE:
                if(resultCode == RESULT_OK) {
                    Log.i("app", "usuarioEntrante:" + usuarioEntrante.toString());
                    Log.i("app", "usuarioSaliente:" + usuarioSaliente.toString());
                    /*if (data.getExtras().get("suministros") != null) {
                        Bundle bdl = getIntent().getExtras();
                        suministros = bdl.getParcelableArrayList("suministros");
                    }*/
                    Log.i("app", "suministros despues de login_saliente:" + suministros);
                    userData = usuarioEntrante;

                        finalizarRelevo();


                }
                break;
            default:
        }
    }

    private ContentValues obtenerInformacionRelevo(){
        ContentValues o = new ContentValues();
        int id_us = 0,id_ue = 0,id_p[] = {0};
        Log.i("app","Obteniendo informacion de relevo...");
        if(modo_view == CustomHelper.MODO_NUEVO){

            String id_dispositivo = CustomHelper.getAndroidId(getContentResolver());
            CustomHelper.getIdPuesto(this,id_dispositivo);
            Log.i("app","puesto_id del dispositivo "+id_dispositivo+": "+id_p[0]);
            if(usuarioSaliente != null)
                id_us = usuarioSaliente.getAsInteger("id");
            if(usuarioEntrante != null)
                id_ue = usuarioEntrante.getAsInteger("id");
            Log.i("app","id_entrante: "+id_ue);
            Log.i("app","id_saliente: "+id_us);

        }else if(modo_view == CustomHelper.MODO_VER) {
            id_ue = object.getAsInt("usuario_id_entrante");
            id_us = object.getAsInt("usuario_id_saliente");
            id_p[0] = object.getAsInt("puesto_id");

        }

        o.put("comentario",Strings.isNullOrEmpty(etComentario.getText().toString()) ? "Ninguna" : etComentario.getText().toString());
        //o.put("num_suministros",num_suministros);
        o.put("puesto_id",id_p[0]);
        o.put("usuario_id_saliente",id_us);
        o.put("usuario_id_entrante",id_ue);
        String formattedDate = CustomHelper.getDateTimeString();
        o.put("timestamp",formattedDate);

        return o;

    }

    private void finalizarRelevo() {
        ContentValues o = new ContentValues();
        long id_query;
        Log.e("app","Finalizando relevo...");
        o = obtenerInformacionRelevo();

        if (modo_view == CustomHelper.MODO_NUEVO) {
            DatabaseAdmin db = new DatabaseAdmin(getApplicationContext());
            Log.e("app","usuarioSaliente: "+usuarioSaliente);
            db.cerrarSesion(usuarioSaliente); // se cierra la sesion del usuario saliente
            long sesion_id = db.iniciarSesion(usuarioEntrante);

            userData.put("sesion_id",sesion_id); // se inicia la sesion del usuario entrante

            id_query = db.registrar("relevo", o);
            db.close();
            if (id_query > 0) {
                o.put("id",id_query);
                object.setValues(o);
                //ahora toca registrar la lista de inventario
                db = new DatabaseAdmin(getApplicationContext());
                db.registrarInventarioRelevo(id_query,suministros);
                db.close();
                Toast.makeText(getApplicationContext(), "Relevo exitoso!", Toast.LENGTH_LONG).show();
                //cerar sesion de usuario saliente y enviar al dashboard con un intent con el nuevo userData
                Intent intent = new Intent(RelevoNuevo.this, DashboardActivity.class);
                intent.putExtra("userData",userData);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                /*
                modo_view = CustomHelper.MODO_VER;
                toolbar.setTitle("Ver registro");
                inventarioAdapter = new SuministrosAdapter(this, suministros, modo_view);
                listaInventario.setAdapter(inventarioAdapter);
                inventarioAdapter.notifyDataSetChanged();
                invalidateOptionsMenu();
                setEnabled(false);
                */
            } else {
                Toast.makeText(getApplicationContext(), "Relevo fallido!", Toast.LENGTH_SHORT).show();
            }
        }


    }

    private void cargarParametros(Bundle savedState) {
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
                Log.i("app","id_object a buscar: "+id);
                object = db.obtenerRelevo(id);
                db.close();
                num_suministros = object.getAsInt("num_suministros");
                Log.i("app","object recuperado: "+object.getValues());
            }
        }

        if(savedState != null){

            if(savedState.getParcelable("userData") != null){
                userData = (ContentValues) savedState.getParcelable("userData");
            }
            if(savedState.getParcelableArrayList("suministros") != null){
                suministros = savedState.getParcelableArrayList("suministros");
            }
            if(savedState.getParcelable("usuarioEntrante") != null){
                usuarioEntrante = (ContentValues) savedState.getParcelable("usuarioEntrante");
            }
            if(savedState.getParcelable("usuarioSaliente") != null){
                usuarioSaliente = (ContentValues) savedState.getParcelable("usuarioSaliente");
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

        if (id == R.id.action_send) {
            if (etComentario.getTextSize() == 0) {
                Toast.makeText(this, "Ingrese un comentario", Toast.LENGTH_SHORT).show();
                return false;
            }
           //ahora se deben identificar los usuarios, el saliente y el entrante
            //login usuario saliente
            intentLogin = new Intent(this,LoginActivity.class);
            intentLogin.putExtra("action","login_relevo_entrante");
            intentLogin.putExtra("mensaje",getString(R.string.usuario_entrante_subtitulo));
            intentLogin.putExtra("comentario",etComentario.getText().toString());
            Log.i("app","Lista final de inventario (suinistros): "+suministros);
            intentLogin.putParcelableArrayListExtra("suministros",suministros);//suministros es un ArrayList<GenericObject>. cada suministro tiene cantidad y los campos de suministro
            usuarioSaliente = userData;
            intentLogin.putExtra("cedula_saliente",usuarioSaliente.getAsString("dni"));
            Log.i("app","usuarioSaliente:"+usuarioSaliente.toString());
            startActivityForResult(intentLogin,LOGIN_USUARIO_ENTRANTE);

        }
        if(id == R.id.home){
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){

        if(menu != null) {
            item_send = menu.getItem(0);
            item_edit = menu.getItem(1);
            item_edit.setVisible(false);
            if (modo_view == CustomHelper.MODO_VER) {
                item_send.setVisible(false);
            } else if (modo_view == CustomHelper.MODO_NUEVO) {
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

        state.putParcelableArrayList("suministros",suministros);
        state.putParcelableArrayList("todosSuministros",todosSuministros);
        state.putParcelable("usuarioEntrante",usuarioEntrante);
        state.putParcelable("usuarioSaliente",usuarioSaliente);
        state.putParcelable("userData",userData);
    }

    @Override
    public void onDestroy(){

        inventarioAdapter = null;
        listaInventario = null;
        suministros = null;// los suministros que foran parte del inventario de relevo del puesto
        todosSuministros = null;//todos los suministros asignados al puesto
        super.onDestroy();
    }
}
