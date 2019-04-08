package org.opencv.javacv.facerecognition.Activities;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.opencv.javacv.facerecognition.Database.DatabaseAdmin;
import org.opencv.javacv.facerecognition.R;

public class PerfilUsuarioActivity extends AppCompatActivity {
    Toolbar toolbar;
    EditText password,newPassword,newPasswordConfirm;
    Button cancelButton,guardarButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_perfil_usuario);

        AppBarLayout appbar = (AppBarLayout) findViewById(R.id.appbar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar4);

        toolbar.setTitle("Cambiar Superclave");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        guardarButton = (Button)findViewById(R.id.btnGuardar);
        cancelButton = (Button)findViewById(R.id.btnCancelar);
        password = (EditText) findViewById(R.id.password);
        newPassword = (EditText) findViewById(R.id.new_password);
        newPasswordConfirm = (EditText) findViewById(R.id.new_password_confirm);

        guardarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseAdmin db = new DatabaseAdmin(PerfilUsuarioActivity.this);

                boolean upd = db.cambiarSuperclave(password.getText().toString(),newPassword.getText().toString(),newPasswordConfirm.getText().toString());
                if(upd){
                    Toast.makeText(PerfilUsuarioActivity.this, "Superclave cambiada con exito!", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(PerfilUsuarioActivity.this, "No se pudo cabiar la Superclave! verifique los datos", Toast.LENGTH_SHORT).show();
                }
                db.close();
            }
       });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        password.addTextChangedListener(new TextWatcher() {

            public void beforeTextChanged(CharSequence s,int start, int count, int after){}

            public void onTextChanged(CharSequence s,int start, int count, int after){}


            @Override
            public void afterTextChanged(Editable editable) {
                // si el dni es valido habilitamos el boton de login
                if(validInput(password)) {
                    guardarButton.setEnabled(true);
                }else{
                    guardarButton.setEnabled(false);
                }
            }

        });
        newPassword.addTextChangedListener(new TextWatcher() {

            public void beforeTextChanged(CharSequence s,int start, int count, int after){}

            public void onTextChanged(CharSequence s,int start, int count, int after){}


            @Override
            public void afterTextChanged(Editable editable) {
                // si el dni es valido habilitamos el boton de login
                if(validInput(newPassword) && equalsInput(newPasswordConfirm,newPassword)) {
                    guardarButton.setEnabled(true);
                }else{
                    guardarButton.setEnabled(false);
                }
            }


        });
        newPasswordConfirm.addTextChangedListener(new TextWatcher() {

            public void beforeTextChanged(CharSequence s,int start, int count, int after){}

            public void onTextChanged(CharSequence s,int start, int count, int after){}


            @Override
            public void afterTextChanged(Editable editable) {
                // si el dni es valido habilitamos el boton de login
                if(equalsInput(newPasswordConfirm,newPassword)) {
                    guardarButton.setEnabled(true);

                }else{
                    guardarButton.setEnabled(false);
                }
            }


        });
    }
    boolean validInput(EditText input){
        String dni = input.getText().toString();
        if(dni.length() < 6) {
            input.setError("Password muy corto");
            return false;
        }
        return true;
    }

    boolean equalsInput(EditText input1,EditText input2){
        String stInput1 = input1.getText().toString();
        String stInput2 = input1.getText().toString();
        if(!stInput1.equals(stInput2)) {
            if(!input2.getText().toString().isEmpty())
                input2.setError("Password no coinciden");
            return false;
        }
        return true;
    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle state){
        super.onSaveInstanceState(state);

     //   state.putParcelable("userData",userData);

    }

    @Override
    public void onDestroy(){

        super.onDestroy();
    }
}

