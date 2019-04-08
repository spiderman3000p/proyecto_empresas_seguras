package org.opencv.javacv.facerecognition.Activities;

import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.Lists;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.opencv.javacv.facerecognition.Database.DatabaseAdmin;
import org.opencv.javacv.facerecognition.Helpers.CustomHelper;
import org.opencv.javacv.facerecognition.Models.GenericObject;
import org.opencv.javacv.facerecognition.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class RegistrarUsuario extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    TextView etNombres,etApellidos,etCedula,etDireccion,etTelefono,etFechaNacimiento,etCiudad,etFechaIngreso;
    Spinner spCargo,spRol,spCompania;
    Button buttonFace;
    ArrayAdapter<GenericObject> rolAdapter,cargoAdapter,companiaAdapter;
    ArrayList<GenericObject> rolList = null, cargoList = null,companiaList = null;
    ContentValues userData;
    int modo_view = CustomHelper.MODO_NUEVO, idSelectedCompania = 0;
    Toolbar toolbar;
    ImageView ivFoto;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_registrar_usuario);
        etNombres = (TextView) findViewById(R.id.etNombres);
        etFechaIngreso = (TextView) findViewById(R.id.etFechaIngreso);
        etApellidos = (TextView) findViewById(R.id.etApellidos);
        etCedula = (TextView) findViewById(R.id.etCedula);
        etTelefono = (TextView) findViewById(R.id.etTelefono);
        etDireccion = (TextView) findViewById(R.id.etDireccion);
        etFechaNacimiento = (TextView) findViewById(R.id.etFechaNacimiento);
        etCiudad = (TextView) findViewById(R.id.etCiudad);
        etCiudad = (TextView) findViewById(R.id.etCiudad);
        spCargo = (Spinner) findViewById(R.id.spCargo);
        spRol = (Spinner) findViewById(R.id.spRol);
        spCompania = (Spinner) findViewById(R.id.spCompania);
        ImageView camera = (ImageView)findViewById(R.id.btCamera);
        Button scanButton = (Button)findViewById(R.id.button2);
        AppBarLayout appbar = (AppBarLayout) findViewById(R.id.appbar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar3);
        //toolbar.setNavigationIcon(R.drawable.user);
        /*toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });*/
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        ivFoto = (ImageView) findViewById(R.id.ivFoto);
        buttonFace = (Button) findViewById(R.id.button2);

        cargarParametros();


        cargarDatos();
        spCompania.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                DatabaseAdmin db = new DatabaseAdmin(getBaseContext());
                rolList = Lists.newArrayList();
                rolList = db.obtenerRoles();
                rolAdapter = new ArrayAdapter<GenericObject>(view.getContext(), android.R.layout.simple_spinner_dropdown_item, rolList);
                spRol.setAdapter(rolAdapter);
                rolAdapter.notifyDataSetChanged();
                db.close();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spCompania.setSelection(0);

        etFechaIngreso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogDate(v, "fechaIngreso");
            }
        });


        etFechaNacimiento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogDate(v, "fechaNacimiento");
            }
        });

        //toolbar.set
        //setSupportActionBar(toolbar);

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cedula = etCedula.getText().toString();
                if(cedula.isEmpty() || cedula.length() < 5){
                    Toast.makeText(RegistrarUsuario.this, "Verifique la cedula primero", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(getApplicationContext(), FdActivity.class);
                intent.putExtra("cedula",cedula);
                startActivity(intent);
            }
        });


        camera.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
// Show only images, no videos or anything else
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
// Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent, "Seleccione una imagen"), PICK_IMAGE_REQUEST);
            }
        });

    }


    void cargarParametros(){
        Bundle extras = getIntent().getExtras();
        Log.i("app","cargando parametros...");
        if (extras != null) {
            if(extras.get("userData") != null) {
                userData = (ContentValues) extras.get("userData");
                Log.i("app", "Parametros...userData: " + userData);
            }
            if(extras.get("modo_view") != null) {
                modo_view =  extras.getInt("modo_view");
                Log.i("app", "Parametros...modo_view: " + modo_view);
            }
        }
    }

    void cargarDatos(){
        cargoList = Lists.newArrayList();
        companiaList = Lists.newArrayList();

        DatabaseAdmin db = new DatabaseAdmin(this);

        companiaList = db.getCompanias();

        if(companiaList != null && !companiaList.isEmpty()) {
            companiaAdapter = new ArrayAdapter<GenericObject>(this, android.R.layout.simple_spinner_dropdown_item, companiaList);
            spCompania.setAdapter(companiaAdapter);
        }

        cargoList = db.obtenerCargos();

        if(cargoList != null && !cargoList.isEmpty()) {
            cargoAdapter = new ArrayAdapter<GenericObject>(this, android.R.layout.simple_spinner_dropdown_item, cargoList);
            spCargo.setAdapter(cargoAdapter);
        }
        db.close();
    }

    private int getSelectedCompania(){

        idSelectedCompania = companiaList.get(spCompania.getSelectedItemPosition()).getAsInt("id");
        return idSelectedCompania;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.radio_nuevo_menu, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        MenuItem item_send,item_edit;

        if(menu != null) {
            item_send = menu.getItem(0);
            item_edit = menu.getItem(1);
            if (modo_view == CustomHelper.MODO_EDITAR) {
                item_edit.setVisible(false);
                item_send.setVisible(true);
            } else if (modo_view == CustomHelper.MODO_VER) {
                item_edit.setVisible(true);
                item_send.setVisible(false);
            } else if (modo_view == CustomHelper.MODO_NUEVO) {
                item_edit.setVisible(false);
                item_send.setVisible(true);
            }
        }else{
            Log.e("app","Menu es NULL");
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        boolean errors = false;
        String cedula = etCedula.getText().toString();

        switch(item.getItemId()){
            case R.id.action_send:{
                DatabaseAdmin db = new DatabaseAdmin(getApplicationContext());
                if (cedula.isEmpty()) {
                    etCedula.setError("Ingrese una cedula");
                    errors = true;
                }else if(cedula.length() < 5){
                    etCedula.setError("Numero de cedula es muy corto");
                    errors = true;
                }else if(db.existeCedula(cedula)){
                    etCedula.setError("Esta cedula ya esta registrada");
                    errors = true;
                }
                if (etNombres.getText().toString().isEmpty()) {
                    etNombres.setError("Ingrese un nombre");
                    errors = true;
                }
                if (etApellidos.getText().toString().isEmpty()) {
                    etApellidos.setError("Ingrese un apellido");
                    errors = true;
                }
                if(db.getLabelKey(cedula) == 0){//comprobamos si tiene un rostro asociado

                    buttonFace.setError("Asocie un rostro");
                    errors = true;
                }
                db.close();
                if(errors)
                        return false;

                DateTime hoy = new DateTime(Calendar.getInstance().getTime());
                DateTimeFormatter fmtDay = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
                ContentValues object = new ContentValues();

                object.put("fecha_registro", fmtDay.print(hoy));
                object.put("fecha_ingreso", etFechaIngreso.getText().toString());
                object.put("dni", etCedula.getText().toString());
                object.put("nombre", etNombres.getText().toString());
                object.put("apellido", etApellidos.getText().toString());
                object.put("direccion", etDireccion.getText().toString());
                object.put("telefono", etTelefono.getText().toString());
                object.put("ciudad", etCiudad.getText().toString());
                object.put("fecha_nacimiento", etFechaNacimiento.getText().toString());
                object.put("avatar", CustomHelper.convertBitmapToBytes(bitmap));
                GenericObject rol = (GenericObject)spRol.getSelectedItem();
                GenericObject cargo = (GenericObject)spCargo.getSelectedItem();
                int rol_id = 0,cargo_id = 0;
                if(rol != null){
                    rol_id = rol.getAsInt("id");
                }
                if(cargo != null){
                    cargo_id = cargo.getAsInt("id");
                }
                object.put("rol_id", rol_id);
                object.put("cargo_id", cargo_id);
                object.put("compania_id", idSelectedCompania);

                    try {
                        if(modo_view == CustomHelper.MODO_EDITAR) {
                           /* if (db.update("radio", radio, id_r)) {
                                radio.put("id",peRadios.getAsInt("id"));
                                peRadios.setValues(radio);
                                Toast.makeText(getApplicationContext(), "Actualizacion exitosa!", Toast.LENGTH_SHORT).show();
                                modo_view = CustomHelper.MODO_VER;
                                toolbar.setTitle("Ver registro");
                                invalidateOptionsMenu();
                                setEnabled(false);
                            } else {
                                Toast.makeText(getApplicationContext(), "Actualizacion fallida!", Toast.LENGTH_SHORT).show();
                            }
                            */
                        }else if(modo_view == CustomHelper.MODO_NUEVO) {
                             db = new DatabaseAdmin(getApplicationContext());
                            long id_query = db.registrar("usuario", object);
                            db.close();
                            if (id_query > 0) {
                                //registrar los modulos
                                db = new DatabaseAdmin(getApplicationContext());
                                CheckBox bitacora,azucar,radio,informe,relevo,usuario;
                                ArrayList<Integer> modulosList = new ArrayList<Integer>();
                                bitacora = (CheckBox) findViewById(R.id.moduloBitacoras);
                                radio = (CheckBox) findViewById(R.id.moduloRadios);
                                azucar = (CheckBox) findViewById(R.id.moduloAzucar);
                                informe = (CheckBox) findViewById(R.id.moduloInformes);
                                relevo = (CheckBox) findViewById(R.id.moduloRelevos);
                                usuario = (CheckBox) findViewById(R.id.moduloUsuarios);

                                if(bitacora.isChecked())
                                    modulosList.add(3);
                                if(radio.isChecked())
                                    modulosList.add(1);
                                if(azucar.isChecked())
                                    modulosList.add(2);
                                if(informe.isChecked())
                                    modulosList.add(4);
                                if(relevo.isChecked())
                                    modulosList.add(5);
                                if(usuario.isChecked())
                                    modulosList.add(6);

                                db.registrarModulosUsuario(id_query,modulosList);

                                Toast.makeText(getApplicationContext(), "Registro exitoso!", Toast.LENGTH_SHORT).show();
                                db.close();
                            } else {
                                Toast.makeText(getApplicationContext(), "Registro fallido!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    finish();
                    //onBackPressed();

            }
            break;
            case R.id.action_edit:{

            }
            break;
            case R.id.home:{
                onBackPressed();
            }break;
            default:
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                // Log.d(TAG, String.valueOf(bitmap));

                ImageView imageView = (ImageView) findViewById(R.id.ivFoto);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void showDialogDate(View v, String fieldUsed) {
        DialogFragment fragment = new DatePickerFragment(fieldUsed);
        fragment.show(getFragmentManager(), "Elija una fecha");
    }

    protected void updateEditDate(DateTime dt, String fieldUsed) {
         if(fieldUsed.equalsIgnoreCase("fechaNacimiento")) {
            etFechaNacimiento.setText(CustomHelper.parseToString(dt, "yyyy-MM-dd"));
        } else if  (fieldUsed.equalsIgnoreCase("fechaIngreso")){
             etFechaIngreso.setText(CustomHelper.parseToString(dt, "yyyy-MM-dd"));
        }
    }

    public class DatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {

        private String fieldUsed;

        public DatePickerFragment(String fieldUsed){
            this.fieldUsed = fieldUsed;
        }

        @Override
        public android.app.Dialog onCreateDialog(Bundle savedInstanceState) {
            final DateTime dt = new DateTime(System.currentTimeMillis());
            Date date = dt.toDate();

            Calendar c = Calendar.getInstance();
            c.setTime(date);

            DatePickerDialog tpd = new DatePickerDialog(getActivity(), this,
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
            );

            TextView tvTitle = new TextView(getActivity());
            tvTitle.setText("Elija una fecha");
            tvTitle.setBackgroundColor(Color.parseColor("#EEE8AA"));
            tvTitle.setPadding(5, 3, 5, 3);
            tvTitle.setGravity(Gravity.CENTER_HORIZONTAL);
            tpd.setCustomTitle(tvTitle);

            return tpd;
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, month);
            c.set(Calendar.DAY_OF_MONTH, day);

            DateTime dt = new DateTime(c.getTime());
            updateEditDate(dt, fieldUsed);
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
