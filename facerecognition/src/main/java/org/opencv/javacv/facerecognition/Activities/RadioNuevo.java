package org.opencv.javacv.facerecognition.Activities;

import android.content.ContentValues;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.Lists;

import org.opencv.javacv.facerecognition.Database.DatabaseAdmin;
import org.opencv.javacv.facerecognition.Helpers.CustomHelper;
import org.opencv.javacv.facerecognition.Models.GenericObject;
import org.opencv.javacv.facerecognition.R;

import java.util.ArrayList;

public class RadioNuevo extends AppCompatActivity{

    TextView etFechaE;
    AutoCompleteTextView spPuesto;
    Switch respondio;
    GenericObject peRadios;
    int modo_view = -1;
    ArrayAdapter<GenericObject> adapterPuestos;
    ArrayList<GenericObject> list2;
    MenuItem item_send,item_edit;
    Toolbar toolbar;
    int idComp,idPuesto;
    ContentValues userData;
    GenericObject  selectedPuesto;

    public RadioNuevo(){
        peRadios = new GenericObject();
        selectedPuesto = new GenericObject();
        list2 = Lists.newArrayList();
        userData = new ContentValues();
        idComp = 0;
        idPuesto = 0;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_radio_nuevo);
        cargarParametros();

        spPuesto = (AutoCompleteTextView) findViewById(R.id.selectPuesto);
        etFechaE = (TextView) findViewById(R.id.txtFecha);
        respondio = (Switch) findViewById(R.id.switchRespondio);
        AppBarLayout appbar = (AppBarLayout) findViewById(R.id.appbar);

        String formattedDate = CustomHelper.getDateTimeString();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar4);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

            etFechaE.setText(formattedDate);
            // cargar datos
            spPuesto.setThreshold(1);

            cargarPuestos();
            cargarDatos();
            invalidateOptionsMenu();
            //establecer toolbar
            if(modo_view == CustomHelper.MODO_EDITAR) {
                toolbar.setTitle("Editar Registro");
                setSupportActionBar(toolbar);
                appbar.setVisibility(View.VISIBLE);
                setEnabled(true);
            }else if(modo_view == CustomHelper.MODO_VER) {
                toolbar.setTitle("Ver Registro");
                setSupportActionBar(toolbar);
                appbar.setVisibility(View.VISIBLE);
                setEnabled(false);
            }else if(modo_view == CustomHelper.MODO_NUEVO){
                toolbar.setTitle("Nuevo Control de Radio");
                setSupportActionBar(toolbar);
                appbar.setVisibility(View.VISIBLE);
                setEnabled(true);
            }


    }

    private void cargarParametros(){
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            if(bundle.get("modo_view") != null)
                modo_view = bundle.getInt("modo_view");
            if(bundle.get("userData") != null)
                userData = (ContentValues)bundle.get("userData");
            if (bundle.getInt("id_objeto") > 0) {
                int id = bundle.getInt("id_objeto");
                DatabaseAdmin db = new DatabaseAdmin(this);
                peRadios = db.buscarRadioPorId(id);
                db.close();
                Log.i("app","peRadios: "+peRadios);
            }
        }
    }

    private void cargarPuestos(){
        DatabaseAdmin db = new DatabaseAdmin(this);
        list2 = db.getPuestos();
        db.close();

        Log.i("app","puestos: "+list2);
        adapterPuestos = new ArrayAdapter<GenericObject>(this,android.R.layout.simple_spinner_dropdown_item,list2);

        spPuesto.setAdapter(adapterPuestos);
        spPuesto.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //idPuesto = getSelectedPuesto(i);

                try {
                    selectedPuesto = (GenericObject) adapterView.getItemAtPosition(i);
                    idPuesto = selectedPuesto.getAsInt("id");
                    Log.i("app", "id del puesto seleccionado:" + idPuesto);
                   // Toast.makeText(RadioNuevo.this, "Puesto seleccionado: id: " + idPuesto + " valores: " + object.getValues().toString(), Toast.LENGTH_SHORT).show();
                }catch(Exception e){
                    Toast.makeText(RadioNuevo.this,"Ha ocurrido un error: "+e.getMessage(),Toast.LENGTH_SHORT).show();
                }

            }
        });
        adapterPuestos.notifyDataSetChanged();

        if(peRadios.getValues().size() > 0) {

            for (GenericObject p : list2) {
                //nombreList[k] = c.getNombres();
                if (p.getAsInt("id") == peRadios.getAsInt("puesto_id")) {
                    spPuesto.setText(p.getAsString("nominativo"));
                }
            }


        }

    }

    private void cargarDatos() {

        if(modo_view == CustomHelper.MODO_EDITAR || modo_view == CustomHelper.MODO_VER){


            etFechaE.setText(peRadios.getAsString("timestamp"));
            //respondio.setText(radios.getResponde() == 1?"Si":"No");
            respondio.setChecked(peRadios.getAsInt("responde") == 1);

        }

    }

    private void setEnabled(boolean state){
        respondio.setEnabled(state);
        etFechaE.setEnabled(state);
        spPuesto.setEnabled(state);
        spPuesto.setFocusable(state);
        etFechaE.requestFocus();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
        int id_u=0,id_r=0;
        long id_query;

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send) {
            if(modo_view == CustomHelper.MODO_VER || modo_view == CustomHelper.MODO_EDITAR) {
                id_u = peRadios.getAsInt("usuario_id");
                id_r = peRadios.getAsInt("id");
            }else {
                id_u = userData.getAsInteger("id");
            }

            if(idPuesto <= 0 || !selectedPuesto.hasKey("nominativo") || !selectedPuesto.getAsString("nominativo").equals(spPuesto.getText().toString()))
            {
                Toast.makeText(this, "Puesto seleccionado invalido", Toast.LENGTH_SHORT).show();
                return false;
            }
                        ContentValues radio = new ContentValues();

                        radio.put("timestamp", etFechaE.getText().toString());
                        radio.put("responde", respondio.isChecked() ? 1 : 0);
                        radio.put("puesto_id", idPuesto);
                        radio.put("usuario_id", id_u);

                            if(modo_view == CustomHelper.MODO_EDITAR) {
                                DatabaseAdmin db = new DatabaseAdmin(getApplicationContext());
                                if (db.update("radio", radio, "id = "+id_r)) {
                                    radio.put("id",peRadios.getAsInt("id"));
                                    peRadios.setValues(radio);
                                    Toast.makeText(getApplicationContext(), "Actualizacion exitosa!", Toast.LENGTH_SHORT).show();
                                    modo_view = CustomHelper.MODO_VER;
                                    toolbar.setTitle("Ver registro");
                                    invalidateOptionsMenu();
                                    setEnabled(false);
                                } else {
                                    Toast.makeText(getApplicationContext(), "Actualizacion fallida!", Toast.LENGTH_SHORT).show();
                                }
                                db.close();
                            }else if(modo_view == CustomHelper.MODO_NUEVO) {

                                DatabaseAdmin db = new DatabaseAdmin(getApplicationContext());
                                id_query = db.registrar("radio", radio);
                                db.close();
                                if (id_query > 0) {

                                    Toast.makeText(getApplicationContext(), "Registro exitoso!", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Registro fallido!", Toast.LENGTH_SHORT).show();
                                }

                            }

                        //finish();
                        //onBackPressed();
            }
        if(id == R.id.action_edit){
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
        adapterPuestos = null;
        super.onDestroy();
    }
}
