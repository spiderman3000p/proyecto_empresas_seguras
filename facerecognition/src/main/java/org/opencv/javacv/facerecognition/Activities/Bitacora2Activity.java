package org.opencv.javacv.facerecognition.Activities;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import org.opencv.javacv.facerecognition.Adapters.MyListAdapter2;
import org.opencv.javacv.facerecognition.Database.DatabaseAdmin;
import org.opencv.javacv.facerecognition.Helpers.CustomHelper;
import org.opencv.javacv.facerecognition.Models.GenericObject;
import org.opencv.javacv.facerecognition.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Bitacora2Activity extends AppCompatActivity {
    ContentValues userData;
    MyListAdapter2 myAdapter;
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

        toolbar.setTitle(R.string.title_activity_bitacora);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

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
            Bundle extras = getIntent().getExtras();
            if (extras != null) userData = (ContentValues) extras.get("userData");

        } catch (Exception e) {
            Log.e(Bitacora2Activity.class.getName(), e.getMessage(), e);
        }

    }


    public void inicializamyAdapter(){
        myList = new ArrayList<>();
        obtenerItems();
        myAdapter = new MyListAdapter2(this,myList,userData);
        if(searchText.getText().length() > 0)
            myAdapter.getFilter().filter(searchText.getText());
        rv.setAdapter(myAdapter);

    }

    public void obtenerItems(){
        DatabaseAdmin db = new DatabaseAdmin(this);
        myList = db.obtenerRegistrosBitacora();
        db.close();
    }

    private void consultaBitacoras(DateTime desde, DateTime hasta, String texto) throws IOException {

        if (myList.isEmpty()) {
            Log.i("app", "Lista de bitacoras esta vacia!");
            return;
        }else{
            Log.i("app", "Lista de bitacoras esta llena con " + myList.size() + " elementos");
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
            Intent intent = new Intent(Bitacora2Activity.this, BitacoraNuevo.class);
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

                        CustomHelper.showDialogDate(Bitacora2Activity.this,dfDesde,dtDesde,txtDesdeFindRadio);
                    }
                });

                // click en caja "fecha hasta"
                txtHastaFindRadio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CustomHelper.showDialogDate(Bitacora2Activity.this,dfHasta,dtHasta,txtHastaFindRadio);
                    }
                });


                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Bitacora2Activity.this);
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
                                    consultaBitacoras(
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
                Log.i("Debug", "Error popup buscar radios" + e.getMessage());
                e.printStackTrace();
            }
        }
        if(id == R.id.home){
            onBackPressed();
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
        state.putBoolean("searchDone",searchDone);
        state.putString("txtBuscar",txtBuscar);

    }

    @Override
    public void onDestroy(){

        rv.setAdapter(null);
        myAdapter = null;
        myList = null;

        super.onDestroy();
    }
}
