package org.opencv.javacv.facerecognition.Activities;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
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
import org.opencv.javacv.facerecognition.Adapters.MyListAdapter6;
import org.opencv.javacv.facerecognition.Database.DatabaseAdmin;
import org.opencv.javacv.facerecognition.Database.DatabaseHelper;
import org.opencv.javacv.facerecognition.Helpers.CustomHelper;
import org.opencv.javacv.facerecognition.Models.GenericObject;
import org.opencv.javacv.facerecognition.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

public class ImportDatabaseActivity extends AppCompatActivity {
    private static final int EMPTY_TABLES_ERROR = 0;
    private static final int JSON_PARSING_TABLES_ERROR = 1;
    private static final int FILE_DIR_ERROR = 2;
    private static final int IOEXCEPTION_ERROR = 3;
    MyListAdapter6 mAdapter;
    Button btnOk,btnCancel;
    ContentValues userData;
    RecyclerView rvModulos;
    List<GenericObject> modulos;
    List<Integer> selectedModulesIndexes;
    List<String> selectedTables;
    LinearLayoutManager llm;
    JSONObject mainObject;
    TextView tvMensaje;
    View loading_modal;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_export_database);
        cargarParametros();
        loading_modal = findViewById(R.id.loading_modal);
        btnOk = (Button) findViewById(R.id.btnExportar);
        tvMensaje = (TextView) findViewById(R.id.tvMensaje);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        rvModulos = (RecyclerView) findViewById(R.id.rvModulos);
        rvModulos.setHasFixedSize(true);
        llm = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        rvModulos.setLayoutManager(llm);
        //debemos obtener la lista de tablas o modulos a exportar.
        cargarArchivo();
        btnOk.setText(R.string.btn_import);
        tvMensaje.setText(tvMensaje.getText().toString().replace(getString(R.string.export_word),getString(R.string.import_word)));
       // btnOk.setBackgroundResource(android.R.drawable.stat_sys_download);

        btnOk.setCompoundDrawablesWithIntrinsicBounds(0,0,android.R.drawable.stat_sys_download,0);
        btnCancel.setCompoundDrawablesWithIntrinsicBounds(0,0,android.R.drawable.ic_menu_close_clear_cancel,0);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //iniciar tarea de importacion en segundo plano de preferencia
                if(!fillSelectedTables())//llenamos el array de tablas (contiene los nombres de las tablas a ser copiadas
                {
                    Toast.makeText(ImportDatabaseActivity.this, R.string.nothing_to_import_msg, Toast.LENGTH_SHORT).show();

                }else{

                    MyAsyncTask task = new MyAsyncTask();
                    loading_modal.setVisibility(View.VISIBLE);
                    task.execute();
                }
            }
        });
    }

    private boolean existeImport(String id_export){
        DatabaseAdmin db = new DatabaseAdmin(this);
        if(!Strings.isNullOrEmpty(id_export))
            return (db.existeImport(id_export));
        return false;
    }
    private static final int READ_REQUEST_CODE = 42;
    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    public void performFileSearch() {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("*/*");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.i("app", "Uri: " + uri.toString());

                String file_name =  uri.getLastPathSegment();
                Log.i("app", "--file name: " + file_name);
                if(!file_name.endsWith(".json")){
                    Toast.makeText(this, R.string.invalid_extension_msg, Toast.LENGTH_SHORT).show();
                    Log.e("app", "--Error: El archivo" + file_name+" no posee extension valida");
                    return;
                }else{
                    //obtenemos su contenido y buscamos si cumple con la estructura deseada
                    try {
                        String jsonContent = readTextFromUri(uri);
                        mainObject = new JSONObject(jsonContent);
                        fillModulosList();//se llena la lista a mostrar al usuario en el recyclerview y su adapter
                        inicializarAdapter();
                    } catch (JSONException e) {
                        Log.e("app", "JSONException: " + e.getMessage());
                    } catch (IOException e) {
                        Log.e("app", "IOException: " + e.getMessage());
                    }

                }

                }

            }
    }

    private String readTextFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        inputStream.close();
        reader.close();
        return stringBuilder.toString();
    }

    private void cargarArchivo() {
        //en este metodo solo mostraremos un dialogo informandole al usuario que lo primero que debe hacer es importar un archivo .json
        // este .json debe provenir del servidor (Exportando). Si el usuario presiona ok, se abre un filechooser para que este ubique
        //el archivo en su dispositivo.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setNeutralButton(R.string.btn_cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel(); finish();
                    }
                });
        builder.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Abrir aqui el DirChooser
                performFileSearch();
            }
        });
        CustomHelper.alert(getString(R.string.import_database),getString(R.string.import_database_msg),CustomHelper.SUCCESS,builder).show();
    }
    public void inicializarAdapter(){

        Log.e("app", "Inicializando adapter... ");
        if(modulos == null || modulos.isEmpty()){
            Log.e("app", "Error: Lista de modulos vacias");
        }

        Log.e("app", "Modulos: "+modulos);

        if(!modulos.isEmpty()){
            Log.i("app", "Configuring recyclerview...");
            mAdapter = new MyListAdapter6(CustomHelper.IMPORTAR,this,modulos);
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

    private boolean iniciarImportacion(){
        Log.i("app", "Iniciando importacion...");

        int proccess = -1;

            proccess = importJsonToDatabase(selectedTables);
            switch (proccess){
                case EMPTY_TABLES_ERROR:
                    Log.e("app", "Error: there's no exist any table to import in json file!");
                    Toast.makeText(getBaseContext(), R.string.error_import_no_tables,Toast.LENGTH_SHORT).show();
                    return false;
                case JSON_PARSING_TABLES_ERROR:
                    Log.i("app", "Error: couldn't create JSON tables parse!");
                    Toast.makeText(getBaseContext(), R.string.error_parsing_tables,Toast.LENGTH_SHORT).show();
                    return false;
                case FILE_DIR_ERROR:
                    Log.i("app", "Error: couldn't create the directory!");
                    Toast.makeText(getBaseContext(), R.string.error_creating_dir,Toast.LENGTH_SHORT).show();
                    return false;
                case IOEXCEPTION_ERROR:
                    Log.i("app", "Error: couldn't create file!");
                    Toast.makeText(getBaseContext(), R.string.error_creating_file,Toast.LENGTH_SHORT).show();
                    return false;
                case RESULT_OK:
                    Log.i("app", "importation succesfull!");
                    return true;
                default:
            }

            return true;
    }

    private int importJsonToDatabase(List<String> tables){

        DatabaseAdmin db = new DatabaseAdmin(this);
        SQLiteDatabase db1;

            Context c = getBaseContext();
            String dbName = c.getResources().getString(R.string.db_name);
            DatabaseHelper helper = new DatabaseHelper(getBaseContext(),dbName,null,1);
            if(!CustomHelper.isExternalStorageReadableAndWritable()){
                AlertDialog.Builder builder = new AlertDialog.Builder(c);
                builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Abrir aqui el DirChooser
                        return;
                    }
                });

                CustomHelper.alert("Error de Lectura/Escritura",
                        "El almacenamiento se encuentra en uso. Por favor, desactive el almacenamiento USB o cualquier" +
                                " otra operacion que mantenga ocupada la unidad de almacenamiento",CustomHelper.ERROR,builder).show();
            }

            db1 = helper.getWritableDatabase();

        Log.i("app", "Parsing JSON to SQL...");
        if(tables == null || tables.isEmpty()){
            Log.e("app", "Tables list empty");
            return EMPTY_TABLES_ERROR; // Error no existen tablas en la base de datos
        }else{
            Log.i("app","tablas a exportar: "+tables.toString());

            try {

                JSONObject tablas = mainObject.getJSONObject("tables");


                for (Iterator<String> iterator = tablas.keys(); iterator.hasNext(); ) {

                    String tableName = iterator.next();
                    //hacemos upgrade en la tabla encontrada
                    db.upgrade(tableName);
                    if (tablas.optJSONArray(tableName) != null) {
                        String sql;
                        String params;
                        String values;

                        db1.beginTransaction();
                        JSONArray tableArray = tablas.optJSONArray(tableName);//valores o registros de cada tabla
                        for (int i = 0; i < tableArray.length(); i++) { //por cada registro
                            sql = "INSERT INTO " + tableName+"(params) VALUES (values)";
                            params = "";
                            values = "";
                            JSONObject tableData = tableArray.getJSONObject(i);//obtenemos todo el registro
                            for (Iterator<String> iter = tableData.keys(); iter.hasNext(); ) { //iteramos en sus claves
                                String key = iter.next();
                                params += key;
                                Object valor = tableData.getString(key);
                                values += "'"+valor+"'";
                                if(iter.hasNext()) {
                                    params += ",";
                                    values += ",";
                                }
                            }
                            sql = sql.replace("params",params);
                            sql = sql.replace("values",values);
                            Log.i("app","sql a ejecutar: "+sql);

                            db1.execSQL(sql);
                        }
                        db1.setTransactionSuccessful();
                        db1.endTransaction();
                    }
                }
            } catch (Exception e) {
                Log.e("app","Ha ocurrido una excepcion al importar la BD web: "+e.getMessage()+""+e.getStackTrace().toString());
            }
        }
        if(db.db.isOpen())
            db.close();

        return RESULT_OK;
    }

    private boolean fillSelectedTables(){
        if(modulos == null || modulos.isEmpty())
            return false;
        selectedModulesIndexes = mAdapter.getSelectedItemsIndexes(); //lista con los indices seleccionados en la UI
        // lista de nombres de las tablas a ser parseadas a json (deben coincidir con el nombre real en la BD SQLite)
        selectedTables = Lists.newArrayList(); // Esto es asi, porque un modulo puede involucrar varias tablas

        //iniciamos un ciclo for para determinar las tablas a ser copiadas segun los modulos seleccionados
        for(int i:selectedModulesIndexes){

            selectedTables.add(modulos.get(i).getAsString("nombre_tabla"));
        }
        return true;
    }

    private void fillModulosList(){
        String nombre_modulo, table_name;
        JSONObject tablas;
        modulos = Lists.newArrayList();
        GenericObject modulo;
        Log.i("app","mainJSONObject..."+mainObject.toString());
        Log.i("app","Getting modules...");
        try {
            tablas = mainObject.getJSONObject("tables");
            for (Iterator<String> iterator = tablas.keys(); iterator.hasNext(); ) {

                table_name = iterator.next();
                nombre_modulo = CustomHelper.DatabaseTables.valueOf(table_name).getCodigo();
                JSONArray tableArray = tablas.optJSONArray(table_name);//valores o registros de cada tabla

                modulo = new GenericObject();
                modulo.set("nombre_tabla",table_name);
                modulo.set("nombre_modulo",nombre_modulo);
                modulo.set("migrated_last",mainObject.getString("timestamp"));
                modulo.set("migrated_count",tableArray.length());
                Log.i("app","--adding new module..."+modulo.getValues());
                modulos.add(modulo);
            }
        } catch (JSONException e) {
            Log.i("app","Excepcion al obtener JSON: "+e.getMessage());
        }
    }

    public class MyAsyncTask extends AsyncTask<String,Integer,Boolean>{
        Intent intent;
        @Override
        protected Boolean doInBackground(String... params) {

            return iniciarImportacion();

        }
        @Override
        protected void onProgressUpdate(Integer... progress){

        }
        @Override
        protected void onPostExecute(Boolean result){
            loading_modal.setVisibility(View.GONE);
            if(result){//si es exitoso el proceso
                btnCancel.setText(getString(R.string.volver));
                btnCancel.setEnabled(true);

                Toast.makeText(getBaseContext(), R.string.success_import_msg,Toast.LENGTH_SHORT).show();

            }else{
                //los mensajes de error se muestran en la funcion iniicarImportacion();
            }
        }

        @Override
        protected void onPreExecute(){
            //mostramos el progress
            View loading_modal = findViewById(R.id.loading_modal);
            loading_modal.setVisibility(View.VISIBLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                btnOk.setBackground(getResources().getDrawable(R.drawable.shape_button_disabled));
            }else{
                btnOk.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            }
            btnOk.setEnabled(false);
            btnCancel.setEnabled(false);
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

        mAdapter = null;
        rvModulos = null;
        super.onDestroy();
    }
}
