package org.opencv.javacv.facerecognition.Activities;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import org.opencv.javacv.facerecognition.Database.DatabaseAdmin;
import org.opencv.javacv.facerecognition.Helpers.CustomHelper;
import org.opencv.javacv.facerecognition.Models.GenericObject;
import org.opencv.javacv.facerecognition.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {
    static final String TAG = "app";
    public static final int SCAN_FACE_ACTIVITY = 1;
    private static final int CONFIRM_SUMINISTROS_ACTIVITY = 2;
    Button loginButton;
    EditText etCedula;
    String action = "login", mensaje = null, dni = "";
    View loading_modal;
    String cedula_saliente;
    String comentario;
    ArrayList<GenericObject> suministros;
    ContentValues userData = null,usuarioActivo = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginButton = (Button) findViewById(R.id.login_btn);
        etCedula = (EditText) findViewById(R.id.dni);
        loading_modal = findViewById(R.id.loading_modal);
        Button scanButton = (Button)findViewById(R.id.button3);
        TextView tvAndroidId = (TextView) findViewById(R.id.tvAndroidId);
        tvAndroidId.setText("Serie: " + CustomHelper.getAndroidId(getContentResolver()) + " - Marca: " +
                CustomHelper.getMarcaDispositivo()+" - Modelo: "+CustomHelper.getModeloDispositivo()+
        " - Sistema: "+CustomHelper.getSoDispositivo());
        cargarParametros(savedInstanceState);
        // Set up the login form.
        //desabilitamos el boton entrar mientras no haya un dni valido
        loginButton.setEnabled(false);

        if(action != null && action.equals("login_relevo_saliente")){
            if(!Strings.isNullOrEmpty(cedula_saliente)){
                etCedula.setText(cedula_saliente);
                etCedula.setFocusable(false);
                loginButton.setEnabled(true);
            }
        }
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //ocultar el teclado aqui
                Intent intent = new Intent(getApplicationContext(), FdActivity.class);
                startActivity(intent);
            }
        });
 ///////////////////////// LISTENERS  ///////////////////////////////////////////

        etCedula.addTextChangedListener(new TextWatcher() {

            public void beforeTextChanged(CharSequence s,int start, int count, int after){}

            public void onTextChanged(CharSequence s,int start, int count, int after){}


            @Override
            public void afterTextChanged(Editable editable) {
                // si el dni es valido habilitamos el boton de login
                if(validInput(etCedula)) {
                    loginButton.setEnabled(true);
                }else{
                    loginButton.setEnabled(false);
                }
            }

            boolean validInput(EditText input){
                String dni = input.getText().toString();
                if(dni.length() < 5) {
                    input.setError("Dni muy corto");
                    return false;
                }
                return true;
            }
        });

        loginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //cerramos el teclado
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                etCedula.dispatchWindowFocusChanged(false);
                etCedula.clearFocus();
                //Buscar el dni
                dni = etCedula.getText().toString();
                userData.put("dni", dni);
                DatabaseAdmin db = null;
                Log.i("app","LoginActivity->onCreate()->loginButton.setOnClickListener()->dni: "+dni);
                try {
                    db = new DatabaseAdmin(getBaseContext());
                } catch (Exception e) {
                    Log.e(TAG, "Excepcion al cargar database");
                }
                if (db == null) {
                    Log.e(TAG, "Excepcion al cargar database");
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });

                    CustomHelper.alert("Error de Lectura/Escritura",
                            "El almacenamiento se encuentra en uso. Por favor, desactive el almacenamiento USB o cualquier" +
                                    " otra operacion que mantenga ocupada la unidad de almacenamiento", CustomHelper.ERROR, builder).show();
                    return;
                }
                //verificamos si el numero ingresado corresponde al superpassword guardado en la BD
                if(db.isSuperPassword(dni)){//si coincide lanzamos el intent para importar archivo de BD,

                    // luego sera un menu de superusuario con mas opciones...
                    Log.i("app","LoginActivity->onCreate(): DNI ingresado es super password");
                    db.close();
                    Intent intent = new Intent(LoginActivity.this, ImportDatabaseActivity.class);
                    startActivity(intent);
                }
                Log.i("app","LoginActivity->onCreate()->loginButton.setOnClickListener()->usuarioActivo:"+usuarioActivo);
                //comprobamos si hay una sesion activa y comparamos el numero de cedula del usuario activo con el del usuario que se quiere loguear
                if(usuarioActivo != null && dni != null && !dni.equals(usuarioActivo.getAsString("dni")) && action.equals("finalizar_jornada_sin_relevo")){
                //si la accion es finalizar jornada sin relevo, la cedula ingresada debe ser igual a la cedula del usuario activo
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                        builder.setNeutralButton(R.string.entendido,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                        CustomHelper.alert(getString(R.string.informacion),getString(R.string.cuenta_abierta_msg),CustomHelper.INFO,builder).show();
                        return;
                }
                //comprobamos si hay una sesion activa y comparamos el numero de cedula del usuario activo con el del usuario que se quiere loguear
                if(usuarioActivo != null && dni != null && dni.equals(usuarioActivo.getAsString("dni")) && !action.equals("finalizar_jornada_sin_relevo")){
                    //si la accion es finalizar jornada sin relevo, la cedula ingresada debe ser igual a la cedula del usuario activo
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setNeutralButton(R.string.entendido,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    CustomHelper.alert(getString(R.string.informacion),getString(R.string.cuenta_abierta_msg2),CustomHelper.INFO,builder).show();
                    return;
                }

                if(!Strings.isNullOrEmpty(dni) && !Strings.isNullOrEmpty(action)){
                    //Si se trata de asistencia, antes de empezar el escaneo facial, comprobar que el usuario no tenga una sesion iniciada, o ya haya fnalizado su jornada

                    boolean capableToLoggin = true;
                    //boolean capableToLoggin = db.isUserCapableToLoggin(db.getUserByDni(dni).getAsString("id"),hoy);// esta funcion busca marcacion de salida
                    // en la tabla asistencia usando el id del usuario y la fecha enviada. Si existe al menos 1 registro en asistencia que tenga
                    // la salida marcada para el usuario en dicha fecha...el usuario no puede inciar sesion nuevamente
                    //db.close();
                    //para el caso de relevos
                    if(cedula_saliente != null && action != null && action.equals("login_relevo_entrante")){
                        //si la accion a ejecutar es el relevo de usuario entrante, hay que comprobar que ese usuario no sea el mismo que sale
                        if(dni.equals(cedula_saliente)){
                            //si son iguales...mostramos un toast y retornamos
                            Toast.makeText(LoginActivity.this, R.string.cedulas_iguales_msg, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    //para todos los casos (no se cumple por requerimiento. se dejo por si acaso)
                    if(!capableToLoggin){// si el usuario ha marcado su salida en la fecha establecida (hoy)
                        //se aborta el proceso enviando un mensaje al usuario
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);

                        builder.setNeutralButton(R.string.entendido,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                        if(!action.equals("asistencia"))
                                            finalizarLogin();
                                    }
                                });

                        String msg = "";
                        if(!action.equals("asistencia"))
                            msg = getString(R.string.jornada_finalizada_msg2);
                        else
                            msg = getString(R.string.jornada_finalizada_msg);

                        CustomHelper.alert(getString(R.string.informacion),msg,CustomHelper.ERROR,builder).show();

                    }else{
                            finalizarLogin();
                    }

                }

            }
        });

        this.userData = new ContentValues();
        this.usuarioActivo = new ContentValues();
    }

/*
    void finalizarLogin(){


        MyAsyncTask task = new MyAsyncTask(this);
        loading_modal.setVisibility(View.VISIBLE);
        if(dni != null){
            task.execute(dni);
        }else {
            Log.i("app","LoginActivity->finalizarLogin(): dni es nulo");
            task.execute(etCedula.getText().toString());
        }

    }
*/

    void finalizarLogin(){

        //Log.i("app", "obteniendo userData...userData: " + userData);
        //Log.i("app", "DNI sent: " + params[0]);
        String dispositivo = CustomHelper.getAndroidId(getContentResolver());
        final int[] id_puesto = {CustomHelper.getIdPuesto(this, dispositivo)};
        loading_modal.setVisibility(View.GONE);

        Log.i("app", "...id_puesto = " + id_puesto[0]);
        userData.put("puesto_id", id_puesto[0]);
        userData.put("serieDispositivo", dispositivo);
        userData.put("infoDispositivo", CustomHelper.getDeviceInfo());
        Log.i("app", "LoginActivity.userData: " + userData);

        if (id_puesto[0] == 0) {//si no existe un id de suministro asociado al id del dispositivo

            final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            builder.setTitle("Información");
            builder.setMessage("El dispositivo " + userData.getAsString("infoDispositivo")
                    + " (" + userData.getAsString("serieDispositivo") +
                    ")no está dado de alta en la base de datos");
            builder.setCancelable(true);
            builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();

        } else if (!existeFaceDirectory()) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            builder.setTitle("Información");
            builder.setMessage("En el dispositivo NO se encuentra el directorio de fotos de empleados. Por favor asocie los rostros a los usuarios registrados");

            builder.setCancelable(true);
            builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                }
            });
            AlertDialog alert = builder.create();
            alert.show();


        } else if (!existeFaceRecord(userData.getAsString("dni"))) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            builder.setTitle("Información");
            builder.setMessage("El actual dispositivo NO cuenta con las fotos del empleado, " +
                    "favor cargar sus fotos desde Windows o entrenar desde el dispositivo");
            builder.setCancelable(true);
            builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        } else {

            startScanActivity();
        }
    }

    void cargarParametros(Bundle savedState){
        Bundle extras = getIntent().getExtras();

        if(extras != null){
            if(extras.get("action") != null){//tipo de accion a ejecutar por el login (Si es para acceder al dashboard o por solo comprobar identidad)
                action = extras.getString("action");
                Log.i("app","LoginActivity->cargarParametros()->action = " + action);
            }else{
                Log.i("app","LoginActivity->cargarParametros()->No se recibio action");
            }

            if(extras.get("userData") != null){//tipo de accion a ejecutar por el login (Si es para acceder al dashboard o por solo comprobar identidad)
                userData = (ContentValues)extras.get("userData");
                usuarioActivo = userData;
                Log.i("app","LoginActivity->cargarParametros()->usuarioActivo = " + usuarioActivo);
                Log.i("app","LoginActivity->cargarParametros()->userData = " + userData);
            }else{
                Log.i("app","LoginActivity->cargarParametros()->No se recibio userData");
            }

            if(extras.get("mensaje") != null){
                mensaje = extras.getString("mensaje");
                Log.i("app","LoginActivity->cargarParametros()->mensaje = " + mensaje);
            }else{
                Log.i("app","LoginActivity->cargarParametros()->No se recibio mensaje");
            }

            if(extras.get("cedula_saliente") != null){
                cedula_saliente = extras.getString("cedula_saliente");
                Log.i("app","LoginActivity->cargarParametros()->cedula_saliente = " + cedula_saliente);
            }else{
                Log.i("app","LoginActivity->cargarParametros()->No se recibio cedula_saliente");
            }

            if(extras.get("comentario") != null){
                comentario = extras.getString("comentario");
                Log.i("app","LoginActivity->cargarParametros()->comentario = " + comentario);
            }else{
                Log.i("app","LoginActivity->cargarParametros()->No se recibio comentario");
            }

            if(extras.get("dni") != null){
                dni = extras.getString("dni");
                Log.i("app","LoginActivity->cargarParametros()->dni = " + dni);
            }else{
                Log.i("app","LoginActivity->cargarParametros()->No se recibio dni");
            }

            if(extras.getParcelableArrayList("suministros") != null){
                Bundle bdl = getIntent().getExtras();
                suministros = bdl.getParcelableArrayList("suministros");
                ///suministros = (ArrayList<GenericObject>) o;
                Log.i("app","LoginActivity->cargarParametros()->suministros = " + suministros);
            }else{

                    Log.i("app","LoginActivity->cargarParametros()->No se recibio suministros");

                suministros = new ArrayList<GenericObject>();
            }
            if(mensaje != null) {//Si se recibe un mensaje desde el intent, ponerlo como mensaje en la antalla de login
                TextView msg = (TextView) findViewById(R.id.txtMensaje);
                msg.setText(mensaje);
                msg.setVisibility(View.VISIBLE);
            }
        }else if(savedState != null){
            if(savedState.getString("dni") != null){
                dni = savedState.getString("dni");
            }
            if(savedState.getString("comentario") != null){
                comentario = savedState.getString("comentario");
            }
            if(savedState.getString("mensaje") != null){
                mensaje = savedState.getString("mensaje");
            }
            if(savedState.getParcelable("userData") != null){
                userData = (ContentValues) savedState.getParcelable("userData");
            }
            if(savedState.getParcelableArrayList("suministros") != null){
                suministros = savedState.getParcelableArrayList("suministros");
            }
            if(savedState.getString("action") != null){
                action = savedState.getString("action");
            }
            if(savedState.getParcelable("usuarioActivo") != null){
                usuarioActivo = (ContentValues) savedState.getParcelable("usuarioActivo");
            }
            if(savedState.getString("cedula_saliente") != null){
                cedula_saliente = savedState.getString("cedula_saliente");
            }
        }
    }


    public void startScanActivity(){
        Intent intent = new Intent(LoginActivity.this, ScanActivity.class);
        intent.putExtra("dni", dni);

        startActivityForResult(intent, SCAN_FACE_ACTIVITY);
    }


    public boolean existeFaceRecord(final String strCedula){
        String strFolderFaces = Environment.getExternalStorageDirectory() + "/databases/facerecogOCV";
        File folderFaces = new File(strFolderFaces);
        if (folderFaces.isDirectory()) {
            Log.i("app","existe directorio: "+strFolderFaces);
            Log.i("app", "existe strCedula: " + strCedula);
            List<String> rostros = Lists.newArrayList(folderFaces.list());
            Predicate<String> P = new Predicate<String>() {
                @Override
                public boolean apply(String input) {
                    if (input == null)
                        return false;

                    Log.i("app","existe input: "+input);

                    boolean existe;

                    try {
                        existe = input.contains(strCedula);
                    } catch (Exception e) {
                        Log.e("app", "Exception: " + e.getMessage());
                        existe = false;
                    }
                    return existe;
                }
            };
            int cont = Collections2.filter(rostros, P).size();
            Log.i("app","contador: "+cont);
            if (cont > 0) {
                Log.i("app","Existen "+cont+" imagenes con cedula: "+strCedula);
                return true;
            }
        }
        Log.i("app","No existen imagenes con cedula: "+strCedula);
        return false;
    }

    public boolean existeFaceDirectory(){
        String strFolderFaces = Environment.getExternalStorageDirectory() + "/databases/facerecogOCV";
        File folderFaces = new File(strFolderFaces);

        return folderFaces.isDirectory();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        try {
            Log.i("app","obteniendo resultado de scanActivity: requestCode"+requestCode+" resultCOde:"+resultCode);

            if(requestCode == SCAN_FACE_ACTIVITY) {

                DatabaseAdmin db = new DatabaseAdmin(getBaseContext());
                if (resultCode == RESULT_OK) {
                    if(data!= null && data.hasExtra("dni"))
                        dni = data.getExtras().getString("dni");
                    Log.i("app","LoginActivity->onActivityResult(): userData->"+userData+", dni: "+dni);

                    if(dni != null)
                        userData = db.getUserByDni(dni);
                    else {
                        Toast.makeText(this, "LoginActivity.onActivityResult(): No existe dni", Toast.LENGTH_SHORT).show();
                        Log.i("app","LoginActivity->onActivityResult(): no existe dni");
                        return;
                    }
                    if(userData == null){
                        //Toast.makeText(this, "LoginActivity.onActivityResult(): userData es null", Toast.LENGTH_SHORT).show();
                        Log.i("app","LoginActivity->onActivityResult(): userData es null. dni = "+dni);
                        return;
                    }
                    Log.i("app", "resultados obtenidos...userData: " + userData);

                    String dispositivo = CustomHelper.getAndroidId(getContentResolver());
                    SharedPreferences preferences = getSharedPreferences("pref",MODE_PRIVATE);
                    int id_puesto[] = {0};
                    if(!preferences.contains("id_puesto"))
                        CustomHelper.getIdPuesto(getBaseContext(), dispositivo);

                    userData.put("puesto_id",id_puesto[0]);
                    userData.put("serieDispositivo",dispositivo);
                    userData.put("infoDispositivo",CustomHelper.getDeviceInfo());
                    Log.i("app", "LoginActivity.userData: " + userData);
                    //mostrar pantalla de bienvenida al usuario
                    Log.i("app","result_code is OK");
                     //iniciar dashhboard o retornar resultado a la actividad llamante
                    if(action.equals("login")){//se inicia el dashboard y se finaliza
                        data = new Intent(this,DashboardActivity.class);
                        Log.i("app","Excecuting action..."+action);
                        //aqui iniciamos la sesion en Android y guardamos los datos en la BD
                        //db = new DatabaseAdmin(this);
                        long sesion_id = db.iniciarSesion(userData);
                        userData.put("sesion_id",sesion_id);
                        data.putExtra("userData", userData);
                        Log.i("app","userData: "+userData);
                        db.close();
                        startActivity(data);
                        finish();
                    }else if(action.equals("asistencia")){//se registra la asistencia del usuario y se finaliza
                        Intent intent = new Intent();
                        Log.i("app","Excecuting action..."+action);
                        //TODO: se hacen las operaciones correspondientes en la BD
                        //modificar el campo logged del usuario
                        //DatabaseAdmin db = new DatabaseAdmin(this);
                        intent.putExtra("userData", userData);
                        setResult(resultCode,intent);
                        db.close();
                        finish();
                    }else if(action.equals("login_relevo_entrante")){//se registra la asistencia del usuario y se finaliza
                        Log.i("app","Excecuting action..."+action);
                        //antes que nada se le pregunta al usuario entrante si esta conforme con los suministros declarados por el usuario saliente
                        Intent confirmSuministros = new Intent(LoginActivity.this,ConfirmSuministrosActivity.class);

                        confirmSuministros.putExtra("usuarioEntrante",userData);
                        confirmSuministros.putExtra("comentario",comentario);
                        //confirmSuministros.putParcelableArrayListExtra("suministros", suministros);
                        db.close();
                        startActivityForResult(confirmSuministros,CONFIRM_SUMINISTROS_ACTIVITY);

                    }else if(action.equals("login_relevo_saliente")){//se registra la asistencia del usuario y se finaliza
                        Log.i("app","Excecuting action..."+action);
                        //TODO: se hacen las operaciones correspondientes en la BD y se envian datos de vuelta

                        Intent intent = new Intent();

                        intent.putExtra("usuarioSaliente",userData);
                        setResult(resultCode,intent);
                        db.close();
                        finish();
                    }else if(action.equals("finalizar_jornada_sin_relevo")){
                        Intent intent = new Intent();
                        Log.i("app","Excecuting action..."+action);
                        //TODO: se hacen las operaciones correspondientes en la BD
                        //modificar el campo logged del usuario
                        //DatabaseAdmin db = new DatabaseAdmin(this);
                        intent.putExtra("userData", userData);
                        setResult(resultCode,intent);
                        db.close();
                        finish();
                    }

                }else if( resultCode == RESULT_CANCELED){
                    Toast.makeText(this, "Rostro no detectado, intente nuevamente", Toast.LENGTH_SHORT).show();

                }
            }else if(requestCode == CONFIRM_SUMINISTROS_ACTIVITY) {//al regresar de la confirmacion de los suministros
                if (resultCode == RESULT_OK) {// si se presiono el boton confirmar
                    //TODO: se hacen las operaciones correspondientes en la BD
                    Intent intent = new Intent();
                    //intent.putExtra("usuarioSaliente",userData);
                    intent.putExtra("usuarioEntrante",(ContentValues)data.getExtras().get("usuarioEntrante"));


                        Bundle bdl = data.getExtras();
                    if(bdl.get("suministrosConfirmed") != null){
                        suministros = bdl.getParcelableArrayList("suministrosConfirmed");
                        Log.i("app","regresando a LoginActivity desde ConfirmActivity->suministros confirmados: "+suministros);
                        ///suministros = (ArrayList<GenericObject>) o;
                        intent.putParcelableArrayListExtra("suministros",suministros);
                    }else{
                        Log.i("app","regresando a LoginActivity desde ConfirmActivity->suministros confirmados: ninguno");
                        suministros = new ArrayList<GenericObject>(); // un array vacio porque no se confirmaron suministros
                        intent.putParcelableArrayListExtra("suministros",suministros);
                    }
                    if(bdl.get("comentarioConfirmed") != null){
                        comentario = bdl.getString("comentarioConfirmed");
                        Log.i("app","regresando a LoginActivity desde ConfirmActivity->suministros confirmados: "+suministros);
                        ///suministros = (ArrayList<GenericObject>) o;
                        intent.putExtra("comentario",comentario);
                    }

                    setResult(resultCode, intent);

                    finish();
                }
            }
        } catch (Exception e){
            Log.e("app", e.getMessage(), e);
        }
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle state){
        super.onSaveInstanceState(state);

        state.putString("dni",dni);
        state.putParcelable("userData",userData);
        state.putParcelable("usuarioActivo",usuarioActivo);
        state.putString("comentario",comentario);
        state.putString("cedula_saliente",cedula_saliente);
        state.putString("action",action);
        state.putString("mensaje",mensaje);
        state.putString("dni",dni);
        state.putParcelableArrayList("suministros",suministros);
    }


    @Override
    public void onDestroy(){

        super.onDestroy();
    }
}

