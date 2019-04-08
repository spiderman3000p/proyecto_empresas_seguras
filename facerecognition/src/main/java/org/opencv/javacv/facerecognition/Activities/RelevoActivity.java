package org.opencv.javacv.facerecognition.Activities;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.opencv.javacv.facerecognition.Adapters.MyListAdapter5;
import org.opencv.javacv.facerecognition.Database.DatabaseAdmin;
import org.opencv.javacv.facerecognition.DatePickerFragment;
import org.opencv.javacv.facerecognition.Helpers.CustomHelper;
import org.opencv.javacv.facerecognition.Models.GenericObject;
import org.opencv.javacv.facerecognition.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RelevoActivity extends AppCompatActivity {

    ContentValues userData;
    MyListAdapter5 myAdapter;
    LayoutInflater layout;
    View myView;
    EditText txtFindRadio;
    EditText txtDesdeFindRadio;
    EditText txtHastaFindRadio;
    EditText searchText;
    DialogFragment dfDesde, dfHasta;
    DateTime dtDesde, dtHasta;
    String txtBuscar;
    RecyclerView rv;
    LinearLayoutManager llm;
    List<GenericObject> myList;
    private boolean searchDone;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_lists);
        rv = (RecyclerView) findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        llm = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        rv.setLayoutManager(llm);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar3);
        toolbar.setTitle(R.string.title_activity_relevo);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        cargarParametros();

        searchText = (EditText) findViewById(R.id.searchText);
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i("app","onTextChanged: "+s.toString());
                myAdapter.getFilter().filter(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        try {
            DateTimeZone tz = DateTimeZone.forID("America/Guayaquil");
            DateTime hoy = DateTime.now(tz);

            dtDesde = hoy.minusDays(7);
            dtHasta = hoy;
            txtBuscar = "";

            inicializamyAdapter();

            rv.requestFocus();
        } catch (Exception e) {
            Log.e(RelevoActivity.class.getName(), e.getMessage(), e);
        }

    }

    private void cargarParametros() {
        Bundle extras = getIntent().getExtras();

        if(extras != null) {

            if (extras.get("userData") != null) {//tipo de accion a ejecutar por el login (Si es para acceder al dashboard o por solo comprobar identidad)
                userData = (ContentValues) extras.get("userData");
            }

        }
    }

    public void inicializamyAdapter(){
        myList = new ArrayList<>();
        obtenerItems();
        myAdapter = new MyListAdapter5(this,myList,userData);
        if(searchText.getText().length() > 0)
            myAdapter.getFilter().filter(searchText.getText());
        rv.setAdapter(myAdapter);
    }

    public void obtenerItems(){
        DatabaseAdmin db = new DatabaseAdmin(this);
        Log.i("app","obteniendo items...: myList vacio: "+myList);
        Log.i("app","obteniendo items...: userData: "+userData);
        SharedPreferences preferences = getSharedPreferences("pref",MODE_PRIVATE);
        int idPuesto = preferences.getInt("id_puesto",0);
        myList = db.obtenerRelevosByPuesto(idPuesto);
        Log.i("app","myList: "+myList);
        db.close();
    }

    private void consultaRelevos(DateTime desde, DateTime hasta, String texto) throws IOException {

        if (myList.isEmpty()) {
            Log.i("app", "Lista de relevos esta vacia!");
            return;
        }else{
            Log.i("app", "Lista de relevos esta llena con " + myList.size() + " elementos");
        }

        myAdapter.buscar(desde, hasta, texto);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_radios, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.addRadio_action) {
            Intent intent = new Intent(RelevoActivity.this, RelevoNuevo.class);
            intent.putExtra("userData", userData);
            intent.putExtra("modo_view", CustomHelper.MODO_NUEVO);
            startActivity(intent);
            return true;
        }
        if (id == R.id.searchRadio_action) {
            if(searchDone) // si se muestran resultados de una busqueda, se limpian los resultados y se pone el icono normal
            {
                myAdapter.getFilter().filter("");
                item.setIcon(android.R.drawable.ic_menu_search);
                searchDone = false;
                item.setChecked(false);
                return super.onOptionsItemSelected(item);
            }
            try {
                DateTimeFormatter fmtDay = DateTimeFormat.forPattern("yyyy-MM-dd");
                // Cargamos información del popup de búsqueda
                layout = LayoutInflater.from(getBaseContext());
                myView = layout.inflate(R.layout.find_radio, null);
                txtFindRadio = myView.findViewById(R.id.etTextoRadioFind);
                txtDesdeFindRadio = myView.findViewById(R.id.etDesdeRadioFind);
                txtHastaFindRadio = myView.findViewById(R.id.etHastaRadioFind);
                txtFindRadio.setText(txtBuscar);
                txtDesdeFindRadio.setText(fmtDay.print(dtDesde));
                txtHastaFindRadio.setText(fmtDay.print(dtHasta));

                // click en caja "fecha desde"
                txtDesdeFindRadio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //CustomHelper.showDialogDate(RelevoActivity.this,dfDesde,dtDesde,txtDesdeFindRadio);
                        dfDesde = new DatePickerFragment(dtDesde,txtDesdeFindRadio);

                        dfDesde.show(getFragmentManager(), "Elija una fecha");
                    }
                });

                // click en caja "fecha hasta"
                txtHastaFindRadio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //CustomHelper.showDialogDate(RelevoActivity.this,dfHasta,dtHasta,txtHastaFindRadio);
                        dfHasta = new DatePickerFragment(dtHasta,txtHastaFindRadio);

                        dfHasta.show(getFragmentManager(), "Elija una fecha");
                    }
                });

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(RelevoActivity.this);
                alertDialogBuilder.setView(myView);
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("Buscar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                //cambiamos el icono del buscador en la barra de herramientas o toolbar
                                item.setIcon(android.R.drawable.ic_menu_revert);
                                item.setChecked(true);
                                searchDone = true;

                                txtBuscar = txtFindRadio.getText().toString();
                                txtFindRadio.dispatchWindowFocusChanged(false);
                                txtFindRadio.clearFocus();
                                DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd");
                                dtDesde = format.parseDateTime(txtDesdeFindRadio.getText().toString());
                                dtHasta = format.parseDateTime(txtHastaFindRadio.getText().toString());

                                try {
                                    consultaRelevos(
                                            dtDesde,
                                            dtHasta,
                                            txtBuscar
                                    );
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .setNegativeButton("Cancelar",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alertD = alertDialogBuilder.create();
                alertD.show();
            } catch (Exception e) {
                Log.i("Debug", "Error popup buscar relevos" + e.getMessage());
                e.printStackTrace();
            }
        }
        if(id == R.id.home){
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

    }

    @Override
    protected void onResume() {
        super.onResume();
        inicializamyAdapter();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }



    public void removeItem(GenericObject o, int pos){
        myAdapter.removeItemFromFilter(pos);
        int index = myList.indexOf(o);
        myList.remove(index);
        myAdapter.notifyItemRemoved(index);
        myAdapter.notifyDataSetChanged();
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
    protected void onRestoreInstanceState(Bundle savedState) {
        super.onRestoreInstanceState(savedState);

    }

    @Override
    public void onDestroy(){

        rv.setAdapter(null);
        myAdapter = null;
        myList = null;

        super.onDestroy();
    }
}
