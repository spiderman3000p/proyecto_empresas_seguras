package org.opencv.javacv.facerecognition.Activities;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
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
import org.opencv.javacv.facerecognition.Adapters.MyListAdapter;
import org.opencv.javacv.facerecognition.Database.DatabaseAdmin;
import org.opencv.javacv.facerecognition.Helpers.CustomHelper;
import org.opencv.javacv.facerecognition.Models.GenericObject;
import org.opencv.javacv.facerecognition.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RadiosActivity extends AppCompatActivity {

    ContentValues userData;
    MyListAdapter rAdapter;
    LayoutInflater layoutFindRadio;
    View viewFindRadio;
    EditText txtFindRadio;
    EditText txtDesdeFindRadio;
    EditText txtHastaFindRadio;
    EditText searchText;
    DialogFragment dfDesde, dfHasta;
    DateTime dtDesde;
    DateTime dtHasta;
    String txtBuscar;
    RecyclerView rv;
    LinearLayoutManager llm;
    List<GenericObject> rList;
    boolean searchDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_lists);
        rv = (RecyclerView) findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        llm = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        rv.setLayoutManager(llm);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar3);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        searchText = (EditText) findViewById(R.id.searchText);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            searchText.setTooltipText("Puede filtrar los repondidos escribiendo: resp. \n Y para ver lo no respondididos: noresp.\n con el punto al final");
        }else{
            searchText.setHint("'resp.' para respondidos y 'noresp.' para los no respondidos");
        }
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                rAdapter.getFilter().filter(s.toString());
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

            inicializarAdapter();

            rv.requestFocus();
            Bundle extras = getIntent().getExtras();
            if (extras != null) userData = (ContentValues) extras.get("userData");

        } catch (Exception e) {
            Log.e(RadiosActivity.class.getName(), e.getMessage(), e);
        }

    }

    public void inicializarAdapter(){
        rList = new ArrayList<>();
        obtenerRadios();
        rAdapter = new MyListAdapter(this,rList);
        if(searchText.getText().length() > 0)
            rAdapter.getFilter().filter(searchText.getText());
        rv.setAdapter(rAdapter);
    }

    public void obtenerRadios(){
        DatabaseAdmin db = new DatabaseAdmin(this);
        rList.addAll(db.obtenerRegistrosRadios());
        db.close();
    }

    private void consultaRadios(DateTime desde, DateTime hasta, String texto) throws IOException {

        if (rList.isEmpty()) {
            Log.i("app", "Lista de radios esta vacia!");
            return;
        }else{
            Log.i("app", "Lista de radios esta llena con " + rList.size() + " elementos");
        }

        rAdapter.buscar(desde, hasta, texto);
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
            Intent intent = new Intent(RadiosActivity.this, RadioNuevo.class);
            intent.putExtra("userData", userData);
            intent.putExtra("modo_view", CustomHelper.MODO_NUEVO);
            startActivity(intent);
            return true;
        }
        if (id == R.id.searchRadio_action) {
            if(searchDone) // si se muestran resultados de una busqueda, se limpian los resultados y se pone el icono normal
            {
                rAdapter.getFilter().filter("");
                item.setIcon(android.R.drawable.ic_menu_search);
                searchDone = false;
                item.setChecked(false);
                return super.onOptionsItemSelected(item);
            }
            try {
                DateTimeFormatter fmtDay = DateTimeFormat.forPattern("yyyy-MM-dd");
                // Cargamos información del popup de búsqueda
                layoutFindRadio = LayoutInflater.from(getBaseContext());
                viewFindRadio = layoutFindRadio.inflate(R.layout.find_radio, null);
                txtFindRadio = viewFindRadio.findViewById(R.id.etTextoRadioFind);
                txtDesdeFindRadio = viewFindRadio.findViewById(R.id.etDesdeRadioFind);
                txtHastaFindRadio = viewFindRadio.findViewById(R.id.etHastaRadioFind);
                txtFindRadio.setText(txtBuscar);
                txtDesdeFindRadio.setText(fmtDay.print(dtDesde));
                txtHastaFindRadio.setText(fmtDay.print(dtHasta));

                // click en caja "fecha desde"
                txtDesdeFindRadio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        CustomHelper.showDialogDate(RadiosActivity.this,dfDesde,dtDesde,txtDesdeFindRadio);
                    }
                });

                // click en caja "fecha hasta"
                txtHastaFindRadio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CustomHelper.showDialogDate(RadiosActivity.this,dfHasta,dtHasta,txtHastaFindRadio);
                    }
                });

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(RadiosActivity.this);
                alertDialogBuilder.setView(viewFindRadio);
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
                                    consultaRadios(
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
        inicializarAdapter();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void removeItem(GenericObject o, int pos){
        rAdapter.removeItemFromFilter(pos);
        int index = rList.indexOf(o);
        rList.remove(index);
        rAdapter.notifyItemRemoved(index);
        rAdapter.notifyDataSetChanged();
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

        rv.setAdapter(null);
        rAdapter = null;
        rList = null;

        super.onDestroy();
    }
}
