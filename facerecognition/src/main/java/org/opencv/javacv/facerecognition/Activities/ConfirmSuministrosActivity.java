package org.opencv.javacv.facerecognition.Activities;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.opencv.javacv.facerecognition.Adapters.SuministrosAdapter;
import org.opencv.javacv.facerecognition.Database.DatabaseAdmin;
import org.opencv.javacv.facerecognition.Helpers.CustomHelper;
import org.opencv.javacv.facerecognition.Models.GenericObject;
import org.opencv.javacv.facerecognition.R;

import java.util.ArrayList;

public class ConfirmSuministrosActivity extends AppCompatActivity {


    Button ok,cancel;
    String comentario;
    SuministrosAdapter suministrosAdapter;
    ArrayList<GenericObject> suministros;
    LinearLayoutManager llm;
    TextView tvFecha,tvComentario;
    RecyclerView listaInventario;
    Toolbar toolbar;
    ContentValues usuarioSaliente,usuarioEntrante;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_confirm_sumiinstros);

        listaInventario = (RecyclerView) findViewById(R.id.rvInventarioRelevo2);
        listaInventario.setHasFixedSize(true);
        llm = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        listaInventario.setLayoutManager(llm);
        AppBarLayout appbar = (AppBarLayout) findViewById(R.id.appbar);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar4);
        toolbar.setTitle(R.string.title_activity_confirmar_relevo);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setSupportActionBar(toolbar);
        appbar.setVisibility(View.VISIBLE);


        tvFecha = (TextView) findViewById(R.id.tvFecha);
        tvComentario = (TextView) findViewById(R.id.tvComentario);
        ok = (Button) findViewById(R.id.ok_btn);
        cancel = (Button) findViewById(R.id.cancel_btn);

        //tvComentario.setFocusable(false);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // retornar de la actividad con un OK
                Intent intent = new Intent ();
                if(usuarioEntrante != null)
                    intent.putExtra("usuarioEntrante",usuarioEntrante);
                if(usuarioSaliente != null)
                    intent.putExtra("usuarioSaliente",usuarioSaliente);
                if(suministros != null)
                    intent.putParcelableArrayListExtra("suministrosConfirmed",suministros);
                if(tvComentario.getText() != null)
                    intent.putExtra("comentarioConfirmed",tvComentario.getText().toString());
                setResult(RESULT_OK,intent);
                finish();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // retornar de la actividad con un OK
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        cargarDatos();
    }

    private void cargarDatos() {

        suministros = new ArrayList<GenericObject>();
        DatabaseAdmin db = new DatabaseAdmin(this);
        ArrayList<GenericObject> todosSuministros = new ArrayList<GenericObject>();//el inventario del puesto
        SharedPreferences preferences = getSharedPreferences("pref",MODE_PRIVATE);
        int idPuesto = preferences.getInt("id_puesto",0);
        todosSuministros = db.obtenerSuministrosByPuesto(idPuesto);
        db.close();
        cargarParametros();
        suministrosAdapter = new SuministrosAdapter(this, suministros,todosSuministros, CustomHelper.MODO_NUEVO);
        listaInventario.setAdapter(suministrosAdapter);
        tvComentario.setText(comentario);
        tvFecha.setText(CustomHelper.getDateTimeString());

    }


    void cargarParametros(){
        Bundle extras = getIntent().getExtras();
        Log.i("app","Entramos en cargar parametros...");

            if(extras.get("usuarioSaliente") != null){
                usuarioSaliente = (ContentValues)extras.get("usuarioSaliente");
                Log.i("app","parametro cargado -> usuarioSaliente: "+usuarioSaliente.toString());
            }

        if(extras.get("usuarioEntrante") != null){
            usuarioEntrante = (ContentValues)extras.get("usuarioEntrante");
            Log.i("app","parametro cargado -> usuarioEntrante: "+usuarioEntrante.toString());
        }

            if(extras.get("comentario") != null){
                comentario = extras.getString("comentario");
                Log.i("app","parametro cargado -> comentario: "+comentario);
            }

        }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle state){
        super.onSaveInstanceState(state);

        state.putParcelableArrayList("suministro",suministros);
        state.putParcelable("usuarioEntrante",usuarioEntrante);
        state.putParcelable("usuarioSaliente",usuarioSaliente);
        state.putString("comentario",comentario);
    }

    @Override
    public void onDestroy(){
        suministrosAdapter = null;
        listaInventario = null;
        super.onDestroy();
    }
}
