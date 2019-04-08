package org.opencv.javacv.facerecognition;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.util.List;

public class ExportDatabaseActivity extends AppCompatActivity {
    private static final int EMPTY_DATABASE_ERROR = 0;
    private static final int NOT_SELECTED_TABLES_ERROR = 4;
    private static final int JSON_PARSING_TABLES_ERROR = 1;
    private static final int FILE_DIR_ERROR = 2;
    private static final int IOEXCEPTION_ERROR = 3;
    private static final int EXPORT_ALL_RECORDS = 5;
    private static final int EXPORT_NEW_RECORDS_ONLY = 6;
    private static final int EXPORT_MIGRATED_RECORDS_ONLY = 7;
    JSONObject jsonObjectMain;
    MyListAdapter6 mAdapter;
    Button btnOk,btnCancel;
    ContentValues userData;
    RecyclerView rvModulos;
    List<GenericObject> modulos;
    List<Integer> selectedModulesIndexes;
    List<String> selectedTables;
    LinearLayoutManager llm;
    TextView tvMensaje;
    String migrated_timestamp;
    int tipoExportacion = EXPORT_ALL_RECORDS;
    String exportWhereClause = "migrated = 0";//0 solo los registros nuevos, 1 solo los migrados anteriormente, sin clausula where, son todos los registros
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_database);
        cargarParametros();
        btnOk = (Button) findViewById(R.id.btnExportar);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        tvMensaje = (TextView) findViewById(R.id.tvMensaje);
        rvModulos = (RecyclerView) findViewById(R.id.rvModulos);
        rvModulos.setHasFixedSize(true);
        llm = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        rvModulos.setLayoutManager(llm);

        //debemos obtener la lista de tablas o modulos a exportar.
        inicializarAdapter();

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //iniciar tarea de exportacion en segundo plano de preferencia
                //iniciarExportacion();
                if(!fillSelectedTables())//llenamos el array de tablas (contiene los nombres de las tablas a ser copiadas))
                {
                    Toast.makeText(ExportDatabaseActivity.this, "No hay nada para exportar", Toast.LENGTH_SHORT).show();
                    return;
                }
                String fecha = CustomHelper.getDateTimeString();
                fecha = fecha.replace(":","_");
                fecha = fecha.replace("-","_");
                fecha = fecha.replace(" ","_");
                int idPuesto = CustomHelper.getIdPuesto(getBaseContext(),CustomHelper.getAndroidId(getContentResolver()));
                String file_name = idPuesto+"_"+"android_export_"+fecha+".json"; // el nombre del archivo generado seria p.ejemplo
                // 12_android_2018_10_08_05_22_33.json (el primer numero es el numero del puesto)

                createFile("*/*", file_name);
            }
        });
    }

    public void inicializarAdapter(){
        DatabaseAdmin db = new DatabaseAdmin(this);
        modulos = db.getExportModulos(userData.getAsString("dni"));
        db.close();
        if(!modulos.isEmpty()){
            Log.i("app", "Configuring recyclerview...");
            mAdapter = new MyListAdapter6(CustomHelper.EXPORTAR,this,modulos);
            rvModulos.setAdapter(mAdapter);
        }
    }

    private void cargarParametros(){
        Bundle extras = getIntent().getExtras();
        Log.i("app","cargando parametros...");
        if (extras != null) {
            if(extras.get("userData") != null) {
                userData = (ContentValues) extras.get("userData");
                //Aqui se debe marcar el campo logged de la tabla usuario

                Log.i("app", "Parametros...userData: " + userData);
            }else{
                Log.i("app", "Parametros...userData no recibido!");
            }
        }
    }

    private void iniciarExportacion(){
        Log.i("app", "Iniciando exportacion...");

        int proccess = -1;
        if(CustomHelper.isExternalStorageReadableAndWritable()){

            //preguntamos al usuario qué tipo de exportacion desea: todos o solo los registros nuevos
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("Exportar Todos los registros");
            dialog.setMessage("Elija cuáles registros desea exportar");
            dialog.setCancelable(false);

            dialog.setPositiveButton("Exportar Todos", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialoginterface, int i) {
                    tipoExportacion = EXPORT_ALL_RECORDS;
                    exportWhereClause = "";
                }
            });

            dialog.setNeutralButton("Exportar Migrados", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialoginterface, int i) {
                    tipoExportacion = EXPORT_MIGRATED_RECORDS_ONLY;
                    exportWhereClause = "migrated = 1";
                }
            });

            dialog.setNegativeButton("Exportar Nuevos", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialoginterface, int i) {
                    tipoExportacion = EXPORT_NEW_RECORDS_ONLY;
                    exportWhereClause = "migrated = 0";
                }
            });

            dialog.show();

            proccess = exportDatabaseToJsonFile(selectedTables,exportWhereClause);
            switch (proccess){
                case EMPTY_DATABASE_ERROR:
                    Log.e("app", "Error: there's no exist any table in database!");
                    Toast.makeText(getBaseContext(),"Error! Base de datos vacia",Toast.LENGTH_SHORT).show();
                    break;
                case JSON_PARSING_TABLES_ERROR:
                    Log.e("app", "Error: couldn't create JSON tables parse!");
                    Toast.makeText(getBaseContext(),"Error! No se pudo parsear las tablas",Toast.LENGTH_SHORT).show();
                    break;
                case FILE_DIR_ERROR:
                    Log.e("app", "Error: couldn't create the directory!");
                    Toast.makeText(getBaseContext(),"Error! No se pudo crear el directorio",Toast.LENGTH_SHORT).show();
                    break;
                case IOEXCEPTION_ERROR:
                    Log.e("app", "Error: couldn't create file!");
                    Toast.makeText(getBaseContext(),"Error! No se pudo crear el archivo",Toast.LENGTH_SHORT).show();
                    break;
                case NOT_SELECTED_TABLES_ERROR:
                    Log.e("app", "Error: No hay tablas seleccionadas!");
                    Toast.makeText(getBaseContext(),"Seleccione al menos un modulo",Toast.LENGTH_SHORT).show();
                    break;
                case RESULT_OK:
                    Log.i("app", "exportation succesfull!");
                    Toast.makeText(getBaseContext(),"Exito! Busque el archivo en la carpeta seleccionada",Toast.LENGTH_SHORT).show();
                    //mostrar aqui el layou resultado export
                    break;
                default:
            }
        }else{
            Log.e("app", "Error: External storage not readable or writeable!");
            Toast.makeText(getBaseContext(),"Error! No se puede acceder al almacenamiento",Toast.LENGTH_SHORT).show();
        }
    }

    private int exportDatabaseToJsonFile(List<String> tables,String where_clause){
        migrated_timestamp = CustomHelper.getDateTimeString();
        Log.i("app", "Parsing DB to JSON...");
        if(tables == null){
            Log.w("app", "Selected Tables list empty, please select one at least...");
            //se exportan todas las tablas
            return NOT_SELECTED_TABLES_ERROR;

        }else{
            Log.i("app","tablas a exportar: "+tables.toString());
        }

        //Comenzamos la creacion del archivo de texto
        jsonObjectMain = new JSONObject(); //objeto json principal para todo el archivo
        JSONArray jsonTables = new JSONArray();
        String dispositivo = CustomHelper.getAndroidId(getContentResolver());

        //se insertan meta datos al objeto JSON principal, para usarlos en el servidor al importar el JSON
        try {
            jsonObjectMain.put("dispositivo", dispositivo);
            jsonObjectMain.put("timestamp", migrated_timestamp);
            jsonObjectMain.put("user_id", userData.getAsInteger("id"));
            Log.i("app", "1. JSON main object first values: " + jsonObjectMain.toString());
            jsonTables = tablesToJson(tables, where_clause, tipoExportacion);
            Log.i("app", "JSON tables array values: " + jsonTables.toString());
            if (jsonTables.length() == 0)
                return JSON_PARSING_TABLES_ERROR; //error al pasar las tablas a JSON

            jsonObjectMain.put("tables", jsonTables);
            Log.e("app", "2. JSON main object values: " + jsonObjectMain.toString());

            //ahora guardaremos el objeto jsonTables a un archivo .json a un directorio selecionado por el usuario (json_destiny_path)
            // esto se hace en onActivityResult();

        }catch (JSONException e) {
            e.printStackTrace();
        }

        return RESULT_OK;
    }

    public JSONArray tablesToJson(List<String> nombres_tablas,String where_clause, int tipoExportacion){ // el retorno es entero porque retornara un codigo de status...identificados en CustomHelper
        Log.i("app", "Attempting parsing tables to JSON...");
        if(nombres_tablas == null || nombres_tablas.size() == 0){
            Log.e("app", "Error, no tables receibed");
            return null;
        }

        JSONObject jsonObjectTabla = new JSONObject(); //objeto json para cada tabla
        JSONArray arrayTablas = new JSONArray(); // array json para todas las tablas
        JSONArray arrayValores = new JSONArray(); // array json para los registros o filas de cada tabla
        DatabaseAdmin db = new DatabaseAdmin(this);


        try {
            for (int i = 0; i < nombres_tablas.size(); i++) {
                String nombre_tabla = nombres_tablas.get(i); // obtenemos el nombre de la tabla a generar en formato JSON
                Log.i("app", "Parsing table "+ nombre_tabla+" to JSON");
                jsonObjectTabla = new JSONObject();
                jsonObjectTabla.put("nombre_tabla", nombre_tabla); // establecemos el nombre de la tabla
                //ejecutamos una query ara obtener todos los registros de la tabla a exportar en JSON
                arrayValores = new JSONArray();
                arrayValores = db.getRowsJSON(nombre_tabla,where_clause,migrated_timestamp);

                jsonObjectTabla.put("valores",arrayValores); // insertamos en el objeto JSON de la tabla los valores o registros de la misma

                arrayTablas.put(jsonObjectTabla); // insertamos en el array de tablas JSON, la tabla obtenida
                db.close();
            }
        }catch(JSONException e){
            Log.i("app","Excepcion al generar JSON");
        }
        Log.i("app", "generated tables: "+arrayTablas.toString());
        return arrayTablas;
    }


    private boolean fillSelectedTables(){
        Log.i("app", "Attempting filling tables...");
        if(modulos == null || modulos.isEmpty() || mAdapter == null){
            Log.i("app", "Error. Couldnt fill tables, exiting...");
            return false;
        }
        selectedModulesIndexes = mAdapter.getSelectedItemsIndexes(); //lista con los indices seleccionados en la UI
        // lista de nombres de las tablas a ser parseadas a json (deben coincidir con el nombre real en la BD SQLite)
        selectedTables = Lists.newArrayList(); // Esto es asi, porque un modulo puede involucrar varias tablas

        //iniciamos un ciclo for para determinar las tablas a ser copiadas
        for(int i:selectedModulesIndexes){
            Log.i("app","modulo seleccionado: "+modulos.get(i).getAsString("codigo_modulo"));
            if(modulos.get(i).getAsString("codigo_modulo").equalsIgnoreCase(CustomHelper.Opciones.CONTROL_AZUCAR.getCodigo())){
                selectedTables.add("controlsalida");//esta tabla tiene 2 claves foraneas, id_usuario e id_puesto, pero ambas provienen de la BD externa
            }
            if(modulos.get(i).getAsString("codigo_modulo").equalsIgnoreCase(CustomHelper.Opciones.CONTROL_RADIOS.getCodigo())){
                selectedTables.add("radio");//esta tabla tiene 2 claves foraneas, id_usuario e id_puesto, pero ambas provienen de la BD externa
            }
            if(modulos.get(i).getAsString("codigo_modulo").equalsIgnoreCase(CustomHelper.Opciones.BITACORA.getCodigo())){
                selectedTables.add("bitacora");//esta tabla tiene 2 claves foraneas, id_usuario e id_puesto, pero ambas provienen de la BD externa
                selectedTables.add("comentario_bitacora");//esta tabla tiene 2 claves foraneas, id_usuario e id_bitacora,
                // el id_usuario proviene de la BD externa, pero el id_bitacora es generado en la BD local, por lo cual, al subirlo a la BD externa
                //la referencia bitacora(id) cambiara y habria que aplicar un algoritmo en el servidor que mantenga las referencias de esta tabla con bitacora
            }
            if(modulos.get(i).getAsString("codigo_modulo").equalsIgnoreCase(CustomHelper.Opciones.RELEVO_GUARDIA.getCodigo())){
                selectedTables.add("relevo");//esta tabla tiene 2 claves foraneas, id_usuario e id_puesto, pero ambas provienen de la BD externa
                selectedTables.add("relevo_suministro");//esta tabla tiene 2 claves foraneas, id_relevo e id_suministro,
                // el id_suministro proviene de la BD externa, pero el id_relevo es generado en la BD local, por lo cual, al subirlo a la BD externa
                //la referencia relevo(id) cambiara y habria que aplicar un algoritmo en el servidor que mantenga las referencias de esta tabla con relevo
            }
            if(modulos.get(i).getAsString("codigo_modulo").equalsIgnoreCase(CustomHelper.Opciones.INFORME_ESPECIAL.getCodigo())){
                selectedTables.add("informe");//esta tabla tiene 2 claves foraneas, id_usuario e id_puesto, pero ambas provienen de la BD externa
            }
            if(modulos.get(i).getAsString("codigo_modulo").equalsIgnoreCase(CustomHelper.Opciones.REGISTRO_USUARIOS.getCodigo())){
                /* por los momentos no se enviaran usuarios creados localmente, ya que este modulo estara deshabilitado por defecto
                selectedTables.add("usuario");//esta tabla tiene 3 claves foraneas, id_rol, id_cargo e id_compania,

                */
            }
            if(modulos.get(i).getAsString("codigo_modulo").equalsIgnoreCase(CustomHelper.Opciones.ASISTENCIA_EMPLEADOS.getCodigo())){
                selectedTables.add("asistencia");//esta tabla tiene 2 claves foraneas, id_puesto, id_usuario
            }
            if(modulos.get(i).getAsString("codigo_modulo").equalsIgnoreCase(CustomHelper.Opciones.SESIONES_USUARIOS.getCodigo())){
                selectedTables.add("session");//esta tabla tiene 2 claves foraneas, id_puesto, id_usuario
            }
        }
        Log.i("app","selectedTables:"+selectedTables);
        Log.i("app","selectedTables.isEmpty():"+selectedTables.isEmpty());

        return !selectedTables.isEmpty();

    }

    private static final int WRITE_REQUEST_CODE = 43;

    private void createFile(String mimeType, String fileName) {
        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

            // Filter to only show results that can be "opened", such as
            // a file (as opposed to a list of contacts or timezones).
            intent.addCategory(Intent.CATEGORY_OPENABLE);

            // Create a file with the requested MIME type.
            intent.setType(mimeType);
            intent.putExtra(Intent.EXTRA_TITLE, fileName);
            startActivityForResult(intent, WRITE_REQUEST_CODE);
        }else{

        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == WRITE_REQUEST_CODE && resultCode == RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.i("app", "Uri: " + uri.toString());
                try {
                    ParcelFileDescriptor pfd = getContentResolver().
                            openFileDescriptor(uri, "w");

                    FileOutputStream fileOutputStream =
                            null;
                    if (pfd != null) {
                        fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());

                        iniciarExportacion();
                        fileOutputStream.write(jsonObjectMain.toString().getBytes());
                        // Let the document provider know you're done by closing the stream.
                        fileOutputStream.close();
                        pfd.close();
                        //ahora marcamos todas las tablas migradas en la DB
                        ContentValues values = null;
                        DatabaseAdmin db = new DatabaseAdmin(this);
                        Log.i("app", "Attempting to update migrated flag in tables records");
                        for (String tabla : selectedTables) {
                            values = new ContentValues();
                            values.put("migrated", 1);
                            values.put("migrated_timestamp", migrated_timestamp);
                            if (db.update(tabla, values, "migrated = 0")) {
                                Log.i("app", "actualizacion realizada");
                            } else {
                                Log.e("app", "no se pudieron actualizar las tablas a migrated = 1");
                            }
                        }
                        db.close();
                    }

                } catch (Exception e) {
                    Log.e("app", "Excepcion: "+e.getMessage());
                    e.printStackTrace();
                }

            }
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

        state.putParcelable("userData",userData);

    }

    @Override
    public void onDestroy(){

        super.onDestroy();
    }
}
