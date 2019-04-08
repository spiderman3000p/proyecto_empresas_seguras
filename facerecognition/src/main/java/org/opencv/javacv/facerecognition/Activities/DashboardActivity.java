package org.opencv.javacv.facerecognition.Activities;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.Lists;

import org.opencv.javacv.facerecognition.Adapters.OpcionAdapter;
import org.opencv.javacv.facerecognition.Database.DatabaseAdmin;
import org.opencv.javacv.facerecognition.Helpers.CustomHelper;
import org.opencv.javacv.facerecognition.Models.GenericObject;
import org.opencv.javacv.facerecognition.R;

import java.util.List;
public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final int LOGIN_ASISTENCIA = 1;
    private static final int SELECCION_ASISTENCIA = 2;
    private static final int EXPORT_DATABASE_ACTION = 5;
    private static final int IMPORT_DATABASE_ACTION = 6;
    private static final int FINALIZAR_JORNADA_SIN_RELEVO = 7;
    ContentValues userData;
    List<GenericObject> moduloList;
    GridView lvOpciones;
    TextView fecha;
    OpcionAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main2);

        cargarParametros();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        lvOpciones = (GridView) findViewById(R.id.lvOpciones);
        TextView nombreEmpleado = (TextView) findViewById(R.id.tvNombresI);
        fecha = (TextView) findViewById(R.id.tvFechaI);
        Log.i("app","DashboardActivity->onCreate()->userData:"+userData);
        if(userData != null) {
            nombreEmpleado.setText(userData.getAsString("nombre").toUpperCase());
        }else{

        }
        fecha.setText(CustomHelper.getDateString());

        cargarOpciones();

    }

    public DashboardActivity(){
        moduloList = Lists.newArrayList();
        userData = null; //hacemos userData null por defecto para saber cuando no se han recuperado los datos (cuando sea igual a null)
    }

    @Override
    public void onBackPressed() {

    }


    //Metodo que recupera parametros enviados desde la activity padre (Scan) con los datos del usuario (completos)
    private void cargarParametros() {
        Bundle extras = getIntent().getExtras();
        Log.i("app","cargando parametros...");
        if (extras != null) {
            if(extras.get("finalizar_jornada_sin_relevo") != null) {
                Intent intent2 = new Intent(DashboardActivity.this, LoginActivity.class);
                intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Log.i("app","cerrando dashboard e iniciando login");
                startActivity(intent2);
                finish();
            }

            if(extras.get("userData") != null) {
                userData = (ContentValues) extras.get("userData");

                Log.i("app", "DashboardActivity->cargarParametros()->userData: " + userData);
            }else{
                Log.i("app", "DashboardActivity->cargarParametros()->userData no recibido!");
            }
        }
    }

    private void cargarOpciones() {

        //TODO: se debe llenar la lista permisoList en la actividad anterior y hacer que forme parte de empleados como un campo mas de usuario
        DatabaseAdmin db = new DatabaseAdmin(this);
        String nombre = userData.getAsString("nombre");
        String cedula = userData.getAsString("dni");
        //ArrayList<ContentValues> userPermissions = db.getPermisosUsuario(cedula);
        //TODO: quitar este toast en produccion
        //Toast.makeText(this,"Bienvenido "+nombre,Toast.LENGTH_SHORT).show();
        Log.i("app","Usuario entrante: "+nombre);
        moduloList.addAll(db.getModulosUsuario(cedula));
        db.close();
        //agregamos los datos del usuario al drawermenu
        NavigationView nav_view = (NavigationView) findViewById(R.id.nav_view);
        TextView nombreUsuario = nav_view.getHeaderView(0).findViewById(R.id.username);
        TextView cargoUsuario = nav_view.getHeaderView(0).findViewById(R.id.cargouser);
        nombreUsuario.setText(nombre+" ("+userData.getAsString("nombre_cargo")+")");
        cargoUsuario.setText("Rol: "+userData.getAsString("nombre_rol"));
        Log.i("app","moduloList: "+moduloList.toString());

        if (!moduloList.isEmpty()) {

            mAdapter = new OpcionAdapter(getBaseContext(), moduloList,userData);
            lvOpciones.setAdapter(mAdapter);
        }else{
            Log.i("app","moduloList vacio!");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id){
            case R.id.logout:
                android.app.AlertDialog.Builder bDialog = new android.app.AlertDialog.Builder(DashboardActivity.this);
                confirmation("Advertencia!", "¿Esta seguro q desea salir de la aplicación?", bDialog, "alert1")
                        .show();
                //TODO: preguntar por salida del sistema haciendo identificacion facial
                break;
            case R.id.asistencia:

                Intent intent = new Intent(this, LoginActivity.class);
                intent.putExtra("userData", userData);
                intent.putExtra("action", "asistencia");
                intent.putExtra("mensaje", "Marcar Entrada/Salida");
                startActivityForResult(intent,LOGIN_ASISTENCIA);
                break;
            default:

        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        
        if (id == R.id.nav_cambiar_clave) {
            // Handle the camera action
            if(!userData.getAsBoolean("editar")){
                Toast.makeText(this, "Ud. No posee los permisos suficientes para ejecutar esta acción", Toast.LENGTH_SHORT).show();
                return false;
            }
                Intent intent = new Intent(this, PerfilUsuarioActivity.class);
                startActivity(intent);

        } else if (id == R.id.nav_import_database) {
            Intent intent = new Intent(this,ImportDatabaseActivity.class);
            intent.putExtra("userData",userData);

            startActivityForResult(intent,IMPORT_DATABASE_ACTION);
        } else if (id == R.id.nav_export_database) {
            //llamar activity para exportar
            Intent intent = new Intent(this,ExportDatabaseActivity.class);
            intent.putExtra("userData",userData);

            startActivityForResult(intent,EXPORT_DATABASE_ACTION);


        } else if (id == R.id.nav_clear_database){
            if(!userData.getAsBoolean("borrar")){
                Toast.makeText(this, "Ud. No posee los permisos suficientes para ejecutar esta acción", Toast.LENGTH_SHORT).show();
                return false;
            }
            //ejecutar funcion de limpiar base de datos y mostrar un aviso
            //mostramos un aviso primero para confirmar
            AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this);
            builder.setNeutralButton(R.string.no,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            builder.setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    DatabaseAdmin db = new DatabaseAdmin(DashboardActivity.this);

                    if(db.limpiarRegistrosWhere("migrated = 1")){
                        Toast.makeText(DashboardActivity.this, "Base de datos limpiada exitosamente!", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(DashboardActivity.this, "Han ocurrio errores!", Toast.LENGTH_SHORT).show();
                    }
                    db.close();
                }
            });

            CustomHelper.alert(getString(R.string.advertencia),getString(R.string.borrar_registros_msg),CustomHelper.INFO,builder).show();
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (id == R.id.nav_finalizar_jornada) {
        //llamar activity para marcar asistencia
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra("userData", userData);
            intent.putExtra("action", "finalizar_jornada_sin_relevo");
            intent.putExtra("mensaje", "Finalizar jornada sin relevo");
            startActivityForResult(intent,FINALIZAR_JORNADA_SIN_RELEVO);
    }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode,int result,Intent intent){
        if(requestCode == LOGIN_ASISTENCIA){
            if(result == RESULT_OK){
                //Preguntar en la UI si registrara su entrada o salida

                if(intent.getExtras().get("userData") != null){
                    ContentValues user = (ContentValues)intent.getExtras().get("userData");
                    String mensaje = "Hola " + (user != null ? user.getAsString("nombre") : "usuario") +"! Elige que deseas hacer";
                //hacer las operaciones en la base de datos para registrar la asistencia del usuario
                    Intent splash = new Intent(this, AsistenciaActivity.class);
                    splash.putExtra("userData",user);
                    splash.putExtra("mensaje",mensaje);

                    startActivityForResult(splash,SELECCION_ASISTENCIA);
                }
            }else if (result == RESULT_CANCELED){

            }
        }else if(requestCode == SELECCION_ASISTENCIA){
            Log.i("app","Volviendo de SELECCION_ASISTENCIA");

            if(intent == null)
                Log.i("app","intent es null");
            else
                Log.i("app","intent NO es null " + intent.toString());

            if(result == RESULT_OK) {
                Log.i("app","RESUL_OK");

                if (intent != null && intent.hasExtra("userData") && intent.getExtras().get("userData") != null) {

                    ContentValues user = (ContentValues) intent.getExtras().get("userData");

                    Log.i("app","intent.user es " + user +  " y userData es " + userData);

                    if (user != null && user.containsKey("dni") && user.getAsString("dni").equals(userData.getAsString("dni"))) {
                        //si la cedula del que marco su asistencia, es la misma que la del usuario actual (userData), se cierra el dashboard

                        Log.i("app","userData es igual a user");

                        Intent intent2 = new Intent(DashboardActivity.this, LoginActivity.class);
                        intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        Log.i("app","cerrando dashboard e iniciando login");
                        startActivity(intent2);
                        finish();
                    }else{
                        Log.i("app","userData NO es igual a user");
                    }
                }else{
                    Log.i("app","intent NO contiene userData");
                }
            }else{
                Log.i("app","result NO es OK");
            }
            if(result == RESULT_CANCELED){
                Log.i("app","RESULT_CANCELED");
                Toast.makeText(this, "Accion cancelada", Toast.LENGTH_SHORT).show();
            }


        }else if(requestCode == FINALIZAR_JORNADA_SIN_RELEVO){
            Log.i("app","Volviendo de FINALIZAR_JORNADA_SIN_RELEVO");

            if(result == RESULT_OK) {
                Log.i("app","RESUL_OK");

                if(intent.getExtras().get("userData") != null){

                    ContentValues user = (ContentValues)intent.getExtras().get("userData");

                    Log.i("app","intent.user es " + user +  " y userData es " + userData);

                    String mensaje = "Hola " + (user != null ? user.getAsString("nombre") : "usuario") +"! Elige que deseas hacer";
                    //hacer las operaciones en la base de datos para registrar la asistencia del usuario
                    Intent splash = new Intent(this, AsistenciaActivity.class);
                    splash.putExtra("userData",user);
                    splash.putExtra("mensaje",mensaje);
                    splash.putExtra("action","finalizar_jornada_sin_relevo");
                    Log.i("app","iniciando Asistencia con SELECCION_ASISTENCIA");
                    startActivityForResult(splash,SELECCION_ASISTENCIA);
                }else{
                    Log.i("app","intent no tiene userData");
                }
            }else{
                Log.i("app","result NO es OK");
            }
            if(result == RESULT_CANCELED) {
                Log.i("app","RESULT_CANCELED");
                Toast.makeText(this, "Accion cancelada", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private android.app.AlertDialog confirmation(String title, String texto, android.app.AlertDialog.Builder builder, String tipo) {
        builder.setIcon(R.drawable.infop);

        builder.setMessage(texto);
        builder.setTitle(title);

        if(tipo.equalsIgnoreCase("alert1")) {
            builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    android.app.AlertDialog.Builder bDialog = new android.app.AlertDialog.Builder(DashboardActivity.this);
                    confirmation("Advertencia!", "¿Confirma que desea salir?", bDialog, "alert2")
                            .show();

                    dialog.dismiss();
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Do nothing
                    dialog.dismiss();
                }
            });
        } else if(tipo.equalsIgnoreCase("alert2")) {
            builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    android.app.AlertDialog.Builder bDialog = new android.app.AlertDialog.Builder(DashboardActivity.this);
                    confirmation("Advertencia!", "La aplicación no debe ser cerrada, ¿esta seguro de querer salir?", bDialog, "alert3")
                            .show();

                    dialog.dismiss();
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Do nothing
                    dialog.dismiss();
                }
            });
        } else {
            builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    logout();
                    Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    dialog.dismiss();
                    finish();
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Do nothing
                    dialog.dismiss();
                }
            });
        }


        return builder.create();
    }

    void logout(){
        DatabaseAdmin db = new DatabaseAdmin(this);
        db.cerrarSesion(userData);
        db.close();
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

        super.onDestroy();
    }
}

