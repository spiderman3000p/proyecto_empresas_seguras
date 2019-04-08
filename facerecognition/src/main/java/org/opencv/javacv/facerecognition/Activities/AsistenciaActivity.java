package org.opencv.javacv.facerecognition.Activities;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Strings;

import org.opencv.javacv.facerecognition.Database.DatabaseAdmin;
import org.opencv.javacv.facerecognition.R;

import java.io.ByteArrayInputStream;

public class AsistenciaActivity extends AppCompatActivity {
    ContentValues userData;
    Bitmap bitmap;
    ImageView ivFoto;
    String action;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Button buttonEntrar = (Button) findViewById(R.id.button);
        Button buttonSalir = (Button) findViewById(R.id.button4);
        ivFoto = (ImageView) findViewById(R.id.ivFoto);
        TextView textView = (TextView) findViewById(R.id.textView);
        Bundle extras = new Bundle();
        String mensaje = "";
        extras = getIntent().getExtras();

        final boolean[] update = {false};
        if (extras != null) {

            if (extras.get("userData") != null) {
                userData = (ContentValues) extras.get("userData");
                cargarFoto();
            }
            if(extras.get("mensaje") != null){
             mensaje = extras.getString("mensaje");
                textView.setText(mensaje);
            }
            if(extras.get("action") != null){
                action = extras.getString("action");
            }
            if(userData == null || !userData.containsKey("id") || userData.getAsInteger("id") == null
                    || !userData.containsKey("puesto_id") || userData.getAsInteger("puesto_id") == null){
                //ha ocurrido un error grave y no se consiguen los datos importantes del usuario como su id o su puesto
                //retornar mosrtando un error
                Toast.makeText(getBaseContext(),"Ha ocurrido un error grave con los datos del usuario",Toast.LENGTH_SHORT).show();
                setResult(RESULT_CANCELED);
                finish();
            }
            DatabaseAdmin db = new DatabaseAdmin(getBaseContext());
            ContentValues user = db.getUserById(userData.getAsInteger("id"));
            if(user.getAsInteger("logged") == 1){
                buttonEntrar.setVisibility(View.GONE);
            }else{
                buttonSalir.setVisibility(View.GONE);
            }
            db.close();
            buttonEntrar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DatabaseAdmin db = new DatabaseAdmin(getBaseContext());
                        db.marcarAsistencia(userData.getAsInteger("id"),userData.getAsInteger("puesto_id"), 1);

                        Toast.makeText(getBaseContext(),"JORNADA INICIADA...",Toast.LENGTH_SHORT).show();

                        setResult(RESULT_OK);
                        db.close();
                        finish();
                    }
                });
            buttonSalir.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DatabaseAdmin db = new DatabaseAdmin(getBaseContext());
                    Log.i("app","marcando asistencia con salida para el usuario " + userData.getAsInteger("id"));

                    db.marcarAsistencia(userData.getAsInteger("id"),userData.getAsInteger("puesto_id"), 0);

                    if(action != null && action.equals("finalizar_jornada_sin_relevo")) {
                        Intent intent2 = new Intent(AsistenciaActivity.this, LoginActivity.class);
                        intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        Log.i("app","cerrando actividad asistencia e iniciando actividad login");
                        startActivity(intent2);
                        finish();
                    }

                    Toast.makeText(getBaseContext(),"JORNADA FINALIZADA...",Toast.LENGTH_SHORT).show();

                    setResult(RESULT_OK);

                    Log.i("app","Volviendo a actividad llamante");
                    db.close();
                    finish();
                }
            });
            }
        }

    public void cargarFoto(){
        if (!Strings.isNullOrEmpty(userData.getAsString("avatar"))){
            ByteArrayInputStream imageStream = new ByteArrayInputStream((byte[])userData.get("avatar"));
            bitmap = BitmapFactory.decodeStream(imageStream);
            ivFoto.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onDestroy(){


        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle state){
        super.onSaveInstanceState(state);

        state.putParcelable("userData",userData);

    }
}
