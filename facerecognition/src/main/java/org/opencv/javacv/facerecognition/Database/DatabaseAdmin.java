package org.opencv.javacv.facerecognition.Database;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;
import android.util.Log;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opencv.javacv.facerecognition.Helpers.CustomHelper;
import org.opencv.javacv.facerecognition.Models.GenericObject;
import org.opencv.javacv.facerecognition.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by RAFAEL on 27/11/2017.
 */
public class DatabaseAdmin {

    Context context;
    private String dbName;
    DatabaseHelper helper;
    public SQLiteDatabase db;
    private static final String TAG = "DatabaseAdmin";

    public DatabaseAdmin(Context c) {
        context = c;
        dbName = context.getResources().getString(R.string.db_name);
        try {
            helper = new DatabaseHelper(c, dbName, null, 1);
        } catch (Exception e) {
            Log.e(TAG, "Excepcion al cargar database");
        }
        if(!CustomHelper.isExternalStorageReadableAndWritable()){
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });

            CustomHelper.alert("Error de Lectura/Escritura",
                    "El almacenamiento se encuentra en uso. Por favor, desactive el almacenamiento USB o cualquier" +
                    " otra operacion que mantenga ocupada la unidad de almacenamiento",CustomHelper.ERROR,builder).show();
        }
    }

    public long addLabel(String descripcion) {
        ContentValues values = new ContentValues();
        try {
                db = helper.getWritableDatabase();

        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        long id_count = 0;
        values.put("descripcion", descripcion);

            //insertar en la BD
            id_count = db.insert("label", null, values);

            if (id_count < 0) {
                Log.i("app","No se pudo insertar este label");
            }

        return id_count;
    }


    public boolean consultarUsuarioPorDni(String dni) {
        try {

            db = helper.getWritableDatabase();
        }catch (Exception e){

            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        Cursor fila = null;
        boolean exist = false;
        try {
            fila = db.rawQuery(context.getResources().getString(R.string.get_user_where),new String[]{dni});

            if (fila != null && fila.moveToFirst()) {
                exist = true;
            }
        }catch (Exception e){
            Log.i("app","Exception: "+e.getMessage());
        }

        if (fila != null) {
                fila.close();
        }
        return exist;
    }

    public boolean consultarUsuarioPorDniClave(String dni,String clave) {
        try {

            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
        }
        Cursor fila = null;
        boolean exist = false;
        try {
            fila = db.rawQuery(context.getResources().getString(R.string.get_user_where_dni_clave),new String[]{dni, clave});

            if (fila != null && fila.moveToFirst()) {
                exist = true;
            }
        }catch (Exception e){
            Log.i("app","Exception: "+e.getMessage());
        }
        if(fila != null && !fila.isClosed())
            fila.close();

        return exist;
    }

    public int countLabels() {
        try {

            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        Cursor fila = null;
        int counter = 0;
        try {
            fila = db.rawQuery(context.getResources().getString(R.string.count_labels), null);

            if (fila != null && fila.moveToFirst()) {
                counter = fila.getInt(0);
                fila.close();
            }
        }catch(Exception e) {
            Log.i("app", "No se pudo abrir el archivo de base de datos! Exception: " + e.getMessage());
        }
        if(fila != null && !fila.isClosed())
            fila.close();
        return counter;
    }
    public ContentValues getUserById(Integer id) {
        Log.i("app","DatabaseAdmin->getUserById(): obteniendo usuario por id para el id: "+id);
        try {
            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            

        }
        ContentValues values = new ContentValues();
        GenericObject rol;
        Cursor fila = db.rawQuery(context.getResources().getString(R.string.get_user_where_id), new String[]{ id.toString()});

        if (fila != null && fila.moveToFirst()) {

            values.put("id", fila.getString(fila.getColumnIndex("id")));
            values.put("nombre", fila.getString(fila.getColumnIndex("nombre")));
            values.put("apellido", fila.getString(fila.getColumnIndex("apellido")));
            values.put("nombre_rol", fila.getString(fila.getColumnIndex("nombre_rol")));
            values.put("nombre_cargo", fila.getString(fila.getColumnIndex("nombre_cargo")));
            values.put("compania_id", fila.getString(fila.getColumnIndex("compania_id")));
            values.put("locale", fila.getString(fila.getColumnIndex("locale")));
            values.put("dni", fila.getString(fila.getColumnIndex("dni")));
            values.put("logged", fila.getString(fila.getColumnIndex("logged")));
            values.put("avatar", fila.getBlob(fila.getColumnIndex("avatar")));

            //ahora recuperamos el rol, para las permisiones
            rol = getRolById(fila.getInt(fila.getColumnIndex("rol_id")));
            values.put("ver",rol.getAsBoolean("ver"));
            values.put("registrar",rol.getAsBoolean("registrar"));
            values.put("editar",rol.getAsBoolean("editar"));
            values.put("borrar",rol.getAsBoolean("borrar"));

            Log.i("app", "getUserById(): " + values.toString());
            fila.close();
        }else{
            Log.i("app","DatabaseAdmin->getUserById()->No existe usuario con id"+id);
            return null;
        }
        if(!fila.isClosed())
            fila.close();
        return values;
    }
    public ContentValues getUserByDni(String dni) {
        Log.i("app","DatabaseAdmin->getUserByDni(): obteniendo usuario por dni para el dni: "+dni);
        try {

            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        ContentValues values = new ContentValues();
        GenericObject rol;
        Cursor fila = db.rawQuery(context.getResources().getString(R.string.get_user_where), new String[]{dni});

        if (fila != null && fila.moveToFirst()) {

            values.put("id", fila.getString(fila.getColumnIndex("id")));
            values.put("nombre", fila.getString(fila.getColumnIndex("nombre")));
            values.put("apellido", fila.getString(fila.getColumnIndex("apellido")));
            values.put("nombre_rol", fila.getString(fila.getColumnIndex("nombre_rol")));
            values.put("nombre_cargo", fila.getString(fila.getColumnIndex("nombre_cargo")));
            values.put("compania_id", fila.getString(fila.getColumnIndex("compania_id")));
            values.put("locale", fila.getString(fila.getColumnIndex("locale")));
            values.put("dni", fila.getString(fila.getColumnIndex("dni")));
            values.put("logged", fila.getString(fila.getColumnIndex("logged")));
            values.put("avatar", fila.getBlob(fila.getColumnIndex("avatar")));

            //ahora recuperamos el rol, para las permisiones
            rol = getRolById(fila.getInt(fila.getColumnIndex("rol_id")));
            values.put("ver",rol.getAsBoolean("ver"));
            values.put("registrar",rol.getAsBoolean("registrar"));
            values.put("editar",rol.getAsBoolean("editar"));
            values.put("borrar",rol.getAsBoolean("borrar"));

            Log.i("app", "getUserByDni(): " + values.toString());
            fila.close();
        }else{
            Log.i("app","DatabaseAdmin->getUserByDni()->No existe usuario con dni "+dni);
            return null;
        }
        if(!fila.isClosed())
            fila.close();
        return values;
    }

    public List<GenericObject> getRadiosByFecha( String radio, String desde, String hasta) {
        try {

            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        List<GenericObject> L = Lists.newArrayList();
        String strLike = "%" + radio + "%";
        String sql = "SELECT R.id,R.usuario_id, R.puesto_id, C.id AS compania_id, U.nombre AS nombre_usuario, U.apellido AS apellido_usuario, R.timestamp,C.nombres AS nombre_compania,P.nominativo,P.nombre AS nombre_puesto,R.responde,date(R.timestamp) AS fecha FROM radio AS R, usuario AS U, puesto AS P, compania AS C WHERE R.puesto_id = P.id AND U.id = R.usuario_id AND C.id = p.compania_id AND date(R.timestamp) >= date('?') AND date(R.timestamp) <= date('?') AND (P.nominativo LIKE '%?%' OR nombre_usuario LIKE '%?%' OR apellido_usuario LIKE '%?%' OR nombre_puesto LIKE '%?%')";
        Cursor cursor = db.rawQuery(sql, new String[]{desde, hasta, strLike, strLike, strLike, strLike, strLike});
        GenericObject radiosBean;
        int index = 1;
        if (cursor != null && cursor.moveToFirst()) {
            radiosBean = new GenericObject();
            radiosBean.set("id", cursor.getInt(0));
            //TODO: todos los demas
            L.add(radiosBean);
            cursor.close();
        }

        return L;
    }

    public int getLabelKey(String label) {
        try {
                db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        int key = 0;
        Cursor fila = db.rawQuery(context.getResources().getString(R.string.get_labelkey_where)+"'"+label+"'", null);

        if (fila != null && fila.moveToFirst()) {
            key = fila.getInt(0);
            fila.close();
        }
        if(!fila.isClosed())
            fila.close();

        return key;
    }

    public String getLabelByKey(int key) {
        try {

            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        String label = "";
        Cursor fila = db.rawQuery(context.getResources().getString(R.string.get_label_where) + key, null);

        if (fila != null && fila.moveToFirst()) {
            label = fila.getString(0);
            fila.close();
        }
        if(fila != null && !fila.isClosed())
            fila.close();

        return label;
    }

    public boolean update(String table, ContentValues data, String where_clause) {
        try {
            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        int r = db.update(table, data, where_clause, null);
        Log.i("app", "setting " + where_clause + " where " + where_clause + " en la tabla " + table);
        if (r > 0)
            Log.i("app", "actualizacion exitosa!");
        else
            Log.i("app", "actualizacion fallida!");

        return (r > 0);
    }


    public ArrayList<ContentValues> getPermisosUsuario(String cedula) {
        //este metodo retornara todos los datos del usuario consultado, excepto sus permisos
        try {

            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
        }
        ArrayList<ContentValues> listaPermisosUsuario = new ArrayList<ContentValues>();
        Cursor fila = db.rawQuery(context.getResources().getString(R.string.get_user_permissions_where), new String[]{cedula});
        if(fila != null && fila.moveToFirst()) {
            do {
                ContentValues values = new ContentValues();
                values.put("nombre_permiso", fila.getString(fila.getColumnIndex("nombre_permiso")));
                listaPermisosUsuario.add(values);
            } while (fila.moveToNext());
            fila.close();
        }
        if(fila != null && !fila.isClosed())
            fila.close();

        return listaPermisosUsuario;
    }

    public List<GenericObject> getModulosUsuario(String cedula) {//este metodo retornara todos los datos del usuario consultado, excepto sus permisos
        try {
            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        Log.i("app","Obteniendo modulos para cedula "+cedula);
        ArrayList<GenericObject> listaPermisosUsuario = Lists.newArrayList();
        GenericObject modulo = new GenericObject();
        String sql = context.getResources().getString(R.string.get_user_modules_where);
        //String sql = "SELECT nombre,apellido FROM usuario";
        Cursor fila = db.rawQuery(sql, new String[]{cedula});
        //int rows = fila.getCount();
        Log.i("app", "sql: " + sql);
        if(fila != null && fila.moveToFirst()) {
            do {
//con esta condicion evitamos incluir los modulos: cambio de clave, registro de usuarios,sesione y asistencia de empleados al menu principal en el dashboard.
// Si desea mostrar estas 4 opciones en el Dashboard, quite estas condiciones a consideracion
                if(!fila.getString(1).equals(CustomHelper.Opciones.SESIONES_USUARIOS.getCodigo())
                && !fila.getString(1).equals(CustomHelper.Opciones.ASISTENCIA_EMPLEADOS.getCodigo())
                && !fila.getString(1).equals(CustomHelper.Opciones.CAMBIO_CLAVE.getCodigo())
                && !fila.getString(1).equals(CustomHelper.Opciones.REGISTRO_USUARIOS.getCodigo())) {

                    modulo = new GenericObject();
                    modulo.set("nombre_modulo", fila.getString(0));
                    modulo.set("codigo_modulo", fila.getString(1));
                    Log.i("app", "Modulo: " + fila.getString(0) + "," + fila.getString(1));
                    listaPermisosUsuario.add(modulo);
                }
            } while (fila.moveToNext());
            fila.close();
        }

        Log.i("app","modulos obtenidos: "+ listaPermisosUsuario.toString());
        if(fila != null && !fila.isClosed())
            fila.close();

        return listaPermisosUsuario;
    }

    public List<GenericObject> getExportModulos(String cedula) {//este metodo retornara todos los datos del usuario consultado, excepto sus permisos
        try {
            db = helper.getWritableDatabase();

        } catch (Exception e) {
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        Log.i("app", "Obteniendo modulos para cedula " + cedula);
        ArrayList<GenericObject> listaPermisosUsuario = Lists.newArrayList();
        GenericObject modulo = new GenericObject();
        String sql = context.getResources().getString(R.string.get_user_modules_where);
        //String sql = "SELECT nombre,apellido FROM usuario";
        Cursor fila = db.rawQuery(sql, new String[]{cedula});
        //int rows = fila.getCount();
        Log.i("app", "sql: " + sql);
        //Log.i("app", "Num rows: " + rows);
        String table_name = "";
        if (fila != null && fila.moveToFirst()){
            do {
                if(!fila.getString(1).equals(CustomHelper.Opciones.CAMBIO_CLAVE.getCodigo())
                   && !fila.getString(1).equals(CustomHelper.Opciones.REGISTRO_USUARIOS.getCodigo())
                        && !fila.getString(1).equals(CustomHelper.Opciones.ASOCIAR_ROSTTRO.getCodigo())) {
                    modulo = new GenericObject();
                    modulo.set("nombre_modulo", fila.getString(0));
                    modulo.set("codigo_modulo", fila.getString(1));

                    if (fila.getString(1).equalsIgnoreCase(CustomHelper.Opciones.ASISTENCIA_EMPLEADOS.getCodigo())) {
                        table_name = "asistencia";
                    }
                    if (fila.getString(1).equalsIgnoreCase(CustomHelper.Opciones.INFORME_ESPECIAL.getCodigo())) {
                        table_name = "informe";
                    }
                    if (fila.getString(1).equalsIgnoreCase(CustomHelper.Opciones.RELEVO_GUARDIA.getCodigo())) {
                        table_name = "relevo";
                    }
                    if (fila.getString(1).equalsIgnoreCase(CustomHelper.Opciones.BITACORA.getCodigo())) {
                        table_name = "bitacora";
                    }
                    if (fila.getString(1).equalsIgnoreCase(CustomHelper.Opciones.CONTROL_RADIOS.getCodigo())) {
                        table_name = "radio";
                    }
                    if (fila.getString(1).equalsIgnoreCase(CustomHelper.Opciones.CONTROL_AZUCAR.getCodigo())) {
                        table_name = "controlsalida";
                    }
                    if (fila.getString(1).equalsIgnoreCase(CustomHelper.Opciones.SESIONES_USUARIOS.getCodigo())) {
                        table_name = "session";
                    }
                    modulo.set("migrated_count", getMigratedCount(table_name, "migrated = 0"));
                    modulo.set("migrated_last", getLastMigratedTimestamp(table_name, "migrated = 0"));

                    Log.i("app", "Modulo: " + fila.getString(0) + "," + fila.getString(1));
                    listaPermisosUsuario.add(modulo);
                }
            }while (fila.moveToNext());
            fila.close();
        }
        Log.i("app","modulos obtenidos: "+ listaPermisosUsuario.toString());
        if(fila != null && !fila.isClosed())
            fila.close();
        return listaPermisosUsuario;
    }



    public int getMigratedCount(String table_name, String where_clause){

            try {

                db = helper.getWritableDatabase();
            }catch (Exception e){
                Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
                
            }

        String sql = context.getResources().getString(R.string.get_migrated_rows_count)+" "+where_clause;
        sql = sql.replace("?",table_name);
        Log.i("app", "sql:" + sql);
        int count = 0;
        Cursor cursor = db.rawQuery(sql, null);
        if(cursor != null && cursor.moveToFirst()){
            count = cursor.getInt(0);
            cursor.close();
        }

        return count;
    }

    public String getLastMigratedTimestamp(String table_name, String where_clause) {

            try {

                db = helper.getWritableDatabase();
            }catch (Exception e){
                Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
                
            }

        String sql = context.getResources().getString(R.string.get_migrated_last_timestamp)+" "+where_clause;
        sql = sql.replace("?",table_name);
        Log.i("app", "sql:" + sql);
        String timestamp = "";
        Cursor cursor = db.rawQuery(sql, null);
        if(cursor != null && cursor.moveToFirst()){
            timestamp = cursor.getString(0);
            cursor.close();
        }

        return timestamp;
    }

    public GenericObject buscarRadioPorId(int id) {
        try {

            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        GenericObject radiosBean = null;
        Cursor cursor = null;

        Log.i("app","buscandoRadio...id:"+id);
        try {
            String sql = context.getResources().getString(R.string.get_radios_where_id) + id;
            Log.i("app", "sql:" + sql);
            cursor = db.rawQuery(sql, null);

            if (cursor != null && cursor.moveToFirst()) {
                radiosBean = new GenericObject();
                radiosBean.set("id", cursor.getInt(0));
                radiosBean.set("compania_id", cursor.getInt(3));
                radiosBean.set("puesto_id", cursor.getInt(2));
                radiosBean.set("usuario_id", cursor.getInt(1));
                radiosBean.set("nombre_usuario", cursor.getString(4) + " " + cursor.getString(5));
                radiosBean.set("timestamp", cursor.getString(6));
                radiosBean.set("nombre_compania", cursor.getString(7));
                radiosBean.set("nombre_nominativo", cursor.getString(8));
                radiosBean.set("nombre_puesto", cursor.getString(9));
                radiosBean.set("responde", cursor.getInt(10));
                Log.i("app","Puesto encontrado! : " + radiosBean.toString());
                cursor.close();
            }

        } catch (SQLException e) {
            Log.i("app", e.getMessage(), e);
        }
        if(cursor != null && !cursor.isClosed())
            cursor.close();

        return radiosBean;
    }

    public GenericObject getControlAzucar(int id) {
        try {

            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        GenericObject control = null;
        Cursor cursor = null;

        //Log.i("app","buscandoRadio...id:"+id);
        try {
            String sql = context.getResources().getString(R.string.get_control_azucar_where);

            cursor = db.rawQuery(sql, new String[]{String.valueOf(id)});
            //SELECT A.id,A.usuario_id,A.compania_id,A.orden_trabajo, A.comentario,A.timestamp,A.foto,A.ultima_modificacion FROM controlsalida AS A WHERE id = ? ORDER BY A.timestamp DESC
            if(cursor != null && cursor.moveToFirst()){

                control = new GenericObject();
                control.set("id", cursor.getInt(0));
                control.set("usuario_id", cursor.getInt(1));
                control.set("orden_trabajo", cursor.getString(2));
                control.set("comentario", cursor.getString(3));
                control.set("timestamp", cursor.getString(4));
                control.set("foto", cursor.getBlob(5));
                control.set("puesto_id", cursor.getInt(6));

                cursor.close();
            }

        } catch (SQLException e) {
            Log.i("app", e.getMessage(), e);

        }

        if(cursor != null && !cursor.isClosed())
            cursor.close();

        return control;
    }

    public ArrayList<GenericObject> obtenerRegistrosRadios() {
        try {

            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        ArrayList<GenericObject> list = Lists.newArrayList();

        GenericObject radio;
        String sql = context.getResources().getString(R.string.get_radios);
        Log.i("app", "sql: " + sql);
        Cursor fila = db.rawQuery(sql, null);

        if(fila != null && fila.moveToFirst()){
            do {
                radio = new GenericObject();
                radio.set("id", fila.getInt(0));
                radio.set("compania_id", fila.getInt(3));
                radio.set("puesto_id", fila.getInt(2));
                radio.set("usuario_id", fila.getInt(1));
                radio.set("nombre_usuario", fila.getString(4) + " " + fila.getString(5));
                radio.set("timestamp", fila.getString(6));
                radio.set("nombre_compania", fila.getString(7));
                radio.set("nombre_nominativo", fila.getString(8));
                radio.set("nombre_puesto", fila.getString(9));
                radio.set("responde", fila.getInt(10));

                Log.i("app", "radio: id:" + fila.getString(0) + ",idusuario:" + fila.getString(1) + ",idpuesto:" + fila.getString(2)
                        + ",idcompania:" + fila.getString(3) + ",timestamp:" + fila.getString(6));
                //Log.i("app","object radio.timestamp: "+radio.getTimestamp());
                list.add(radio);
                //Log.i("app","object list.radio.timestamp: "+list.get(list.size()-1).getTimestamp());
            } while (fila.moveToNext());
            fila.close();
        }
        if(fila != null && !fila.isClosed())
            fila.close();


        return list;
    }

    public ArrayList<GenericObject> obtenerRegistrosBitacora() {
        try {

            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        ArrayList<GenericObject> list = Lists.newArrayList();

        GenericObject bitacora;
        String sql = context.getResources().getString(R.string.get_bitacoras);
        Log.i("app", "sql: " + sql);
        Cursor fila = db.rawQuery(sql, null);

        if(fila != null && fila.moveToFirst()){
            do {

                bitacora = new GenericObject();
                bitacora.set("id", fila.getInt(0));
                bitacora.set("puesto_id", fila.getInt(1));
                bitacora.set("usuario_id", fila.getInt(2));
                bitacora.set("timestamp", fila.getString(4));
                bitacora.set("tipo", fila.getInt(5));
                bitacora.set("observacion", fila.getString(3));
                bitacora.set("nombre_usuario", fila.getString(6));
                bitacora.set("apellido_usuario", fila.getString(7));
                bitacora.set("nombre_tipo", fila.getInt(5) == 0 ? "normal" : "urgente");
                bitacora.set("num_comentarios", fila.getInt(8));

                //Log.i("app","object radio.timestamp: "+radio.getTimestamp());
                list.add(bitacora);
                //Log.i("app","object list.radio.timestamp: "+list.get(list.size()-1).getTimestamp());
            } while (fila.moveToNext());
            fila.close();
        }
        if(fila != null && !fila.isClosed())
            fila.close();
        return list;
    }

    public int getIdPuestoByDispositivo(String dispositivo) {
        try {

            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        int i = 0;

        String SQL = context.getResources().getString(R.string.get_puesto_id_from_disp)+"'"+dispositivo+"'";
        Log.i("app", "sql: " + SQL);
        Cursor cursor = db.rawQuery(SQL, null);
        if(cursor != null && cursor.moveToFirst()) {

                Log.i("app", "cursor string: " + cursor.getString(0));
                Log.i("app", "cursor int: " + cursor.getInt(0));
                i = cursor.getInt(0);

                cursor.close();
        }
        if(cursor != null && !cursor.isClosed())
            cursor.close();

        return i;
    }

    public ArrayList<GenericObject> getPuestosPorCompania(int idCompania) {
        try {

            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        ArrayList<GenericObject> list = Lists.newArrayList();

        GenericObject puesto = new GenericObject();
        String sql = context.getResources().getString(R.string.get_puestos_where_compania) + idCompania;
        //Log.i("app", "Compañia seleccionada: " + idCompania);
        Log.i("app", "Sql: " + sql);

        Cursor fila = db.rawQuery(sql, null);
        if(fila != null && fila.moveToFirst()) {
            do {
                puesto = new GenericObject();
                puesto.set("id", fila.getInt(0));
                puesto.set("nombre", fila.getString(4));
                puesto.set("descripcion", fila.getString(2));
                puesto.set("compania_id", fila.getInt(3));
                puesto.set("nominativo", fila.getString(4));
                puesto.set("trueName", fila.getString(1));
                //Log.i("app", "Puesto: " + fila.getString(0) + "," + fila.getString(1) + ","+fila.getString(2)
                //       + ","+fila.getString(3)+ ","+fila.getString(4));
                list.add(puesto);
            }while (fila.moveToNext());
            fila.close();
        }
        if(fila != null && !fila.isClosed())
            fila.close();

        return list;
    }

    public ArrayList<GenericObject> getCompanias() {
        try {

            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        ArrayList<GenericObject> list = Lists.newArrayList();

        GenericObject object = new GenericObject();
        String sql = context.getResources().getString(R.string.get_companias);
        //Log.i("app", "Compañia seleccionada: " + idCompania);
        Log.i("app", "Sql: " + sql);

        Cursor fila = db.rawQuery(sql, null);
        if(fila != null && fila.moveToFirst()) {
           do {

                object = new GenericObject();
                object.set("id", fila.getInt(0));
                object.set("codigo", fila.getString(1));
                object.set("nombre", fila.getString(2));
                object.set("ruc", fila.getString(3));
                object.set("representante", fila.getString(4));
                object.set("estado", fila.getInt(5));
                //Log.i("app", "Puesto: " + fila.getString(0) + "," + fila.getString(1) + ","+fila.getString(2)
                //       + ","+fila.getString(3)+ ","+fila.getString(4));
                list.add(object);
            } while (fila.moveToNext());
            fila.close();
        }
        if(fila != null && !fila.isClosed())
            fila.close();

        return list;
    }

    public boolean eliminar(String table, int id) {
        try {

            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        long result = 0;

        try {
            result = db.delete(table, "id=" + id, null);
        } catch (SQLException e) {
            Log.i("app", "SQLExcepcion al eliminar " + table + ":" + e.getMessage());
        }
        return (result > 0);
    }

    public ArrayList<String> obtenerComentariosBitacora(int bitacora_id) {
        try {

            db = helper.getWritableDatabase();
        } catch (Exception e) {
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        ArrayList<String> list = Lists.newArrayList();

        Log.i("app", "Obteniendo comentarios...");
        String sql = context.getResources().getString(R.string.get_comentarios) + bitacora_id;
        //Log.i("app","Obteniendo compañias...");
        //String sql = "SELECT nombre,apellido FROM usuario";
        Cursor fila = db.rawQuery(sql, null);
        Log.i("app", "sql: " + sql);
        if (fila != null && fila.moveToFirst()) {
            do {
                list.add(fila.getString(1));
            } while (fila.moveToNext());
            Log.i("app", "Comentarios obtenidos..." + list);
            fila.close();
        }
        if(fila != null && !fila.isClosed())
            fila.close();

        return list;
    }

    public ArrayList<GenericObject> obtenerComentariosBitacora2(int bitacora_id) {
        try {

            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        ArrayList<GenericObject> list = new ArrayList<>();
        String sql = context.getResources().getString(R.string.get_comentarios) + bitacora_id;
        Log.i("app", "Obteniendo comentarios...");
        //String sql = "SELECT nombre,apellido FROM usuario";
        Cursor fila = db.rawQuery(sql, null);
        Log.i("app", "sql: " + sql);

        if(fila != null && fila.moveToFirst()) {
            do {
                GenericObject c = new GenericObject();
                c.set("id", fila.getInt(0));
                c.set("comentario", fila.getString(1));
                c.set("bitacora_id", fila.getInt(2));
                c.set("nombre_usuario", fila.getString(3));
                c.set("apellido_usuario", fila.getString(4));
                c.set("usuario_id", fila.getInt(5));
                c.set("datetime", fila.getString(6));

                list.add(c);
            } while (fila.moveToNext());
            Log.i("app", "Comentarios obtenidos..." + list);
            fila.close();
        }
        if(fila != null && !fila.isClosed())
            fila.close();
        return list;
    }

    public ArrayList<GenericObject> obtenerRegistrosAzucars() {
        try {

            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        ArrayList<GenericObject> list = Lists.newArrayList();

        GenericObject radio;
        String sql = context.getResources().getString(R.string.get_azucars);
        Log.i("app", "sql: " + sql);
        Cursor fila = db.rawQuery(sql, null);
        if(fila != null && fila.moveToFirst()){
            do {

                radio = new GenericObject();
                radio.set("id", fila.getInt(0));
                radio.set("usuario_id", fila.getInt(1));
                radio.set("puesto_id", fila.getInt(2));
                radio.set("orden_trabajo", fila.getString(3));
                radio.set("comentario", fila.getString(4));
                radio.set("timestamp", fila.getString(5));
                radio.set("foto", fila.getBlob(6));
                Log.i("app", "azucar: id:" + fila.getString(0) + ",idusuario:" + fila.getString(1) + ",idpuesto:" + fila.getString(2)
                        + ",orden_trabajo:" + fila.getString(3) + ",timestamp:" + fila.getString(5));
                //Log.i("app","object radio.timestamp: "+radio.getTimestamp());
                list.add(radio);
                //Log.i("app","object list.radio.timestamp: "+list.get(list.size()-1).getTimestamp());
            } while (fila.moveToNext());
            fila.close();
        }
        if(fila != null && !fila.isClosed())
            fila.close();
        return list;
    }

    public long registrar(String table, ContentValues data) {
        try {

            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        long id = 0;
        //Log.i("app", "Registro a ingresar: " + radio.getId()+","+radio.getIdPuesto()+","+radio.getIdUsuario()+","+
        //      radio.getNominativo()+","+radio.getNombrePuesto()+","+radio.getNovedad()+","+radio.getResponde());
        try {

            id = db.insert(table, null, data);

            if (id > 0) {
                Log.i("app", "Registro exitoso!");
            } else {
                Log.i("app", "Registro fallido!");
            }

        } catch (SQLException e) {
            Log.i("app", e.getMessage(), e);
            throw new SQLException("Error: No se pudo insertar en la BD. desconecte el dispositivo del USB o reinicielo.");
        }
        return id;
    }

    public GenericObject obtenerBitacora(int id) {
        try {

            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        GenericObject bitacora = new GenericObject();
        String sql = context.getResources().getString(R.string.get_bitacora_where) + id;
        Log.i("app", "sql: " + sql);
        Cursor fila = db.rawQuery(sql, null);
        if (fila != null && fila.moveToFirst()) {

            bitacora = new GenericObject();
            bitacora.set("id", fila.getInt(0));
            bitacora.set("puesto_id", fila.getInt(1));
            bitacora.set("usuario_id", fila.getInt(2));
            bitacora.set("observacion", fila.getString(3));
            bitacora.set("timestamp", fila.getString(4));
            bitacora.set("tipo", fila.getInt(5));
            bitacora.set("nombre_usuario", fila.getString(6));
            bitacora.set("nombre_usuario", fila.getString(7));
            bitacora.set("tipo_bitacora", fila.getInt(5) == 0 ? "normal" : "urgente");
            bitacora.set("num_comentarios", fila.getInt(8));

            Log.i("app", "object bitacora: " + bitacora);
            fila.close();
        }
        if(fila != null && !fila.isClosed())
            fila.close();
        return bitacora;
    }

    public ArrayList<GenericObject> obtenerRegistrosInformes() {
        try {

            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        ArrayList<GenericObject> list = Lists.newArrayList();

        GenericObject radio;
        String sql = context.getResources().getString(R.string.get_informes);
        Log.i("app", "sql: " + sql);
        Cursor fila = db.rawQuery(sql, null);
        if(fila != null && fila.moveToFirst()){
            do {

                radio = new GenericObject();
                radio.set("id", fila.getInt(0));
                radio.set("puesto_id", fila.getInt(1));
                radio.set("usuario_id", fila.getInt(2));
                radio.set("nombre_usuario", fila.getString(3) != null ? fila.getString(3) : " ");
                radio.set("apellido_usuario", fila.getString(4) != null ? fila.getString(4) : " ");
                radio.set("titulo", fila.getString(5));
                radio.set("observacion", fila.getString(6));
                radio.set("timestamp", fila.getString(7));
                radio.set("foto1", fila.getBlob(8));
                radio.set("foto2", fila.getBlob(9));
                radio.set("foto3", fila.getBlob(10));

                list.add(radio);
                //Log.i("app","object list.radio.timestamp: "+list.get(list.size()-1).getTimestamp());
            } while (fila.moveToNext());
            fila.close();
        }
        if(fila != null && !fila.isClosed())
            fila.close();
        return list;
    }

    public GenericObject getInformeEspecial(int id) {
        try {

            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        GenericObject object = new GenericObject();
        String sql = context.getResources().getString(R.string.get_informe_where) + id;
        Log.i("app", "sql: " + sql);
        Cursor fila = db.rawQuery(sql, null);
        if (fila != null && fila.moveToFirst()) {

            object = new GenericObject();
            object.set("id", fila.getInt(0));
            object.set("puesto_id", fila.getInt(1));
            object.set("usuario_id", fila.getInt(2));
            object.set("nombre_usuario", fila.getString(3) != null ? fila.getString(3) : " ");
            object.set("apellido_usuario", fila.getString(4) != null ? fila.getString(4) : " ");
            object.set("titulo", fila.getString(5));
            object.set("observacion", fila.getString(6));
            object.set("timestamp", fila.getString(7));
            object.set("foto1", fila.getBlob(8));
            object.set("foto2", fila.getBlob(9));
            object.set("foto3", fila.getBlob(10));

            Log.i("app", "object informe: " + object);
            fila.close();
        }
        if(fila != null && !fila.isClosed())
            fila.close();
        return object;
    }

    public boolean existeSuministro(GenericObject s, List<GenericObject> relevoDetalle) {
        return false;
    }

    public long registrarAsistenciaEmpleado(int usuario_id,int puesto_id, String descripcion) {
        try {

            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        Log.i("app",getClass().getName() + ": registrarAsistenciaEmpleado()");
        ContentValues asistencia = new ContentValues();
        String fecha = CustomHelper.getDateTimeString();
        asistencia.put("fecha", fecha);
        asistencia.put("usuario_id", usuario_id);
        asistencia.put("puesto_id", puesto_id);
        asistencia.put("descripcion", descripcion);

        Log.i("app", "- Intentando registrar asistencia: " + asistencia);
        long id = db.insert("asistencia", null,asistencia);
        if(id > 0){
            Log.i("app", "- asistencia registrada con id = " + id);
        }else{
            Log.i("app", "- No se pudo registrar asistencia");
        }
         return id;
    }

    public ArrayList<GenericObject> obtenerSuministros() {
        try {

            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        ArrayList<GenericObject> list = Lists.newArrayList();

        GenericObject object;
        String sql = context.getResources().getString(R.string.get_suministros_where);
        Log.i("app", "sql: " + sql);
        Cursor fila = db.rawQuery(sql, null);
        if(fila != null && fila.moveToFirst()) {
            do {

                object = new GenericObject();
                object.set("id", fila.getInt(0));
                object.set("codigo", fila.getString(1));
                object.set("nombre", fila.getString(2));
                object.set("serial", fila.getString(3));
                object.set("estado", fila.getInt(4));

                list.add(object);
                //Log.i("app","object list.radio.timestamp: "+list.get(list.size()-1).getTimestamp());
            } while (fila.moveToNext());
            fila.close();
        }

        Log.i("app", "object list Todos los Suministros: " + list);
        if(fila != null && !fila.isClosed())
            fila.close();
        return list;
    }

    public List<GenericObject> obtenerRelevosByPuesto(int puesto_id) {
        try {

            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        ArrayList<GenericObject> list = Lists.newArrayList();

        GenericObject object;
        String sql = context.getResources().getString(R.string.get_relevos_where);
        Log.i("app", "sql: " + sql);
        Cursor fila = db.rawQuery(sql, new String[]{String.valueOf(puesto_id)});
        if(fila != null && fila.moveToFirst()){
            do {

                object = new GenericObject();
                object.set("id", fila.getInt(0));
                object.set("puesto_id", fila.getInt(1));
                object.set("comentario", fila.getString(2));
                object.set("timestamp", fila.getString(3));
                object.set("usuario_id_saliente", fila.getInt(4));
                object.set("usuario_id_entrante", fila.getInt(5));
                object.set("num_suministros", fila.getInt(6));
                object.set("nombre_usuario_saliente", fila.getString(7));
                object.set("nombre_usuario_entrante", fila.getString(8));
                object.set("nominativo_puesto", fila.getString(9));

                list.add(object);
                //Log.i("app","object list.radio.timestamp: "+list.get(list.size()-1).getTimestamp());
            } while (fila.moveToNext());

            fila.close();
        }
        if(fila != null && !fila.isClosed())
            fila.close();
        return list;
    }

    public List<GenericObject> obtenerInventarioRelevo(int relevo_id) {
        try {

            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        ArrayList<GenericObject> list = Lists.newArrayList();

        GenericObject object;
        String sql = context.getResources().getString(R.string.get_inventario_relevo_where) + relevo_id;
        Log.i("app", "sql: " + sql);
        Cursor fila = db.rawQuery(sql, null);
        if(fila != null && fila.moveToFirst()){
            do {

                object = new GenericObject();
                object.set("relevo_id", fila.getInt(0));
                object.set("suministro_id", fila.getInt(1));
                object.set("nombre", fila.getString(2));
                object.set("cantidad", fila.getInt(3));
                Log.i("app", "-cursor fila: " + fila.toString());
                Log.i("app", "-object: " + object.getValues());
                list.add(object);

            } while (fila.moveToNext());
        }
        Log.i("app", "Inventario Relevo object: " + list);
        if(fila != null && !fila.isClosed())
            fila.close();
        return list;
    }

    public List<String> obtenerInventarioRelevoAsString(int relevo_id) {
        try {

            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        ArrayList<String> list = Lists.newArrayList();

        Log.i("app", "Obteniendo inventario...");
        String sql = context.getResources().getString(R.string.get_inventario_relevo_where) + relevo_id;
        //Log.i("app","Obteniendo compañias...");
        //String sql = "SELECT nombre,apellido FROM usuario";
        Cursor fila = db.rawQuery(sql, null);
        Log.i("app", "sql: " + sql);
        if(fila != null && fila.moveToFirst()) {
            do {

                list.add(fila.getString(3) + " " + fila.getString(2));
            } while (fila.moveToNext());
            fila.close();
        }
        Log.i("app", "inventario obtenido..." + list);
        if(fila != null && !fila.isClosed())
            fila.close();
        return list;
    }

    public GenericObject obtenerRelevo(int id) {
        try {

            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        GenericObject object = null;
        String sql = context.getResources().getString(R.string.get_relevo_where) + id;
        Log.i("app", "sql: " + sql);
        Cursor fila = db.rawQuery(sql, null);
        if(fila != null && fila.moveToFirst()) {

            object = new GenericObject();
            object.set("id", fila.getInt(0));
            object.set("puesto_id", fila.getInt(1));
            object.set("comentario", fila.getString(2));
            object.set("timestamp", fila.getString(3));
            object.set("usuario_id_saliente", fila.getInt(4));
            object.set("usuario_id_entrante", fila.getInt(5));
            object.set("num_suministros", fila.getInt(6));
            object.set("nombre_usuario_saliente", fila.getString(7));
            object.set("nombre_usuario_entrante", fila.getString(8));
            object.set("nominativo_puesto", fila.getString(9));
            Log.i("app", "object relevo: " + object);
            fila.close();
        }
        if(fila != null && !fila.isClosed())
            fila.close();
        return object;
    }

    public long iniciarSesion(ContentValues userData) {
        try {

            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        Log.i("app",getClass().getName() + ": iniciarSesion()");
        long sesion_id = -1;
        try {

            ContentValues session = new ContentValues();

            session.put("puesto_id", getIdPuestoByDispositivo(userData.getAsString("serieDispositivo")));
            session.put("usuario_id", userData.getAsInteger("id"));
            session.put("serieDispositivo", userData.getAsString("serieDispositivo"));
            session.put("inicio", CustomHelper.getDateTimeString());
            session.put("dispositivo", CustomHelper.getDeviceInfo());
            Log.i("app","- Trying to insert in session DB table...session:"+session.toString());

            sesion_id = db.insert("session", null, session);

            Log.i("app","- insert result: "+sesion_id);

            if (sesion_id > 0) {
                //comprobamos si el usuario puede marcar su asistencia(si no ha marcado hoy)
                Log.i("app","Sesion iniciada "+sesion_id);
                //modificamos el campo logged del usuario si
                Log.i("app","Attempting to marcarAsistencia...sesion_id = "+sesion_id+" session: "+session);

                    this.marcarAsistencia(userData.getAsInteger("id"),userData.getAsInteger("puesto_id"), 1);
            }else{
                Log.i("app","Sesion no iniciada. sesion_id = "+sesion_id);
            }
        } catch (Exception e) {
            Log.i("app","Sesion no iniciada. Exception: "+e.getMessage());
        }
        return sesion_id;
    }

    public long cerrarSesion(ContentValues userData) {
        try {

            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        long update = -1;
        Log.i("app",getClass().getName() + ": cerrarCesion()");
        try {

            ContentValues session = new ContentValues();
            session.put("fin", CustomHelper.getDateTimeString());
           // Log.i("app","marcando fin session de userData: "+userData);
            Log.i("app","- marcando fin session de userData: "+userData);
            int sesion_id = userData.getAsInteger("sesion_id");

            if(sesion_id > 0)
                update = db.update("session", session,"id="+sesion_id,null);
            else
                Log.i("app","- No existe id_sesion en userData. userData['sesion_id'] =  " + userData.getAsInteger("sesion_id"));

            if (update > 0) {
                //modificamos el campo logged del usuario
                Log.i("app","- update de fin de sesion satisfactorio...");
                Log.i("app","- Intentando marcar asistencia salida...");
                this.marcarAsistencia(userData.getAsInteger("id"),userData.getAsInteger("puesto_id"), 0);
            }else{
                Log.i("app","no se pudo marcar fin de sesion, update fallido...");
            }
        } catch (Exception e) {
            Log.i("app","Exception: "+e.getMessage());
         
        }
        return update;
    }

    public void marcarAsistencia(int usuario_id,int puesto_id, int value) {
        try {

            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        long logged = -1,asistencia;
        Log.i("app",getClass().getName() + ":marcarAsistencia()");
        Log.i("app","- marcarndo asistencia con value = "+value);

        ContentValues update = new ContentValues();

        update.put("logged", value);

        asistencia = registrarAsistenciaEmpleado(usuario_id,puesto_id,value == 1?"ENTRADA":"SALIDA");
        if(asistencia > 0) {
                logged = db.update("usuario", update, "id =" + usuario_id, null);
                Log.i("app", getClass().getName() + ": marcarAsistencia(): marcando usuario.logged = " + logged);
        }else{
                Log.i("app", getClass().getName() + ": marcarAsistencia(): Asistencia no registrada");
        }

    }

    public int getIdCompaniaByPuesto(int id_p) {
        try {

            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }

        String SQL = context.getResources().getString(R.string.get_compania_id_by_puesto) + id_p;
        Log.i("app", "sql: " + SQL);
        Cursor cursor = db.rawQuery(SQL, null);
        int id = 0;
        try {
            if (cursor != null && cursor.moveToFirst()) {
                Log.i("app", "cursor string: " + cursor.getString(0));
                Log.i("app", "cursor int: " + cursor.getInt(0));
                id = cursor.getInt(0);
                cursor.close();
            }
        }catch(Exception e){

        }
        return id;
    }

    public void registrarInventarioRelevo(long relevo_id, ArrayList<GenericObject> suministros) {
        try {

            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        ContentValues values;
        Log.i("app", "registrando inventario: relevo_id:" + relevo_id + " suministros:" + suministros);
        try {
            if (relevo_id > 0 && !suministros.isEmpty()) {

                for (final GenericObject suministro : suministros) {
                    values = new ContentValues();
                    Log.i("app", "suministro a registrar:" + values);
                    values.put("relevo_id", relevo_id);
                    values.put("suministro_id", suministro.getAsInt("id"));
                    values.put("cantidad", suministro.getAsInt("cantidad"));

                    db.insert("relevo_suministro", null, values);
                }

            }
        }catch(Exception e){
            Log.i("app","Exception: "+e.getMessage());
            
        }

    }

    public ArrayList<GenericObject> obtenerRoles() {
        try {

            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        ArrayList<GenericObject> list = Lists.newArrayList();

        GenericObject object;
        Log.i("app", "Obteniendo roles...");
        String sql = context.getResources().getString(R.string.get_roles_where);
        //Log.i("app","Obteniendo compañias...");
        //String sql = "SELECT nombre,apellido FROM usuario";
        Cursor fila = db.rawQuery(sql, null);
        Log.i("app", "sql: " + sql);
        if(fila != null && fila.moveToFirst()) {
            do {
                object = new GenericObject();
                object.set("id", fila.getString(0));
                object.set("nombre", fila.getString(1));
                object.set("responsabilidades", fila.getString(2));
                object.set("estado", fila.getString(3));
                list.add(object);
            } while (fila.moveToNext());
            fila.close();
        }
        Log.i("app", "roles obtenidos..." + list);
        if(fila != null && !fila.isClosed())
            fila.close();
        return list;
    }

    public ArrayList<GenericObject> obtenerCargos() {
        try {

            db = helper.getReadableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        ArrayList<GenericObject> list = Lists.newArrayList();

        GenericObject object;
        Log.i("app", "Obteniendo roles...");
        String sql = context.getResources().getString(R.string.get_cargos_where);
        //Log.i("app","Obteniendo compañias...");
        //String sql = "SELECT nombre,apellido FROM usuario";
        Cursor fila = db.rawQuery(sql, null);
        Log.i("app", "sql: " + sql);
        if(fila != null && fila.moveToFirst()) {
            do {
                object = new GenericObject();
                object.set("id", fila.getString(0));
                object.set("nombre", fila.getString(1));
                object.set("responsabilidades", fila.getString(2));
                object.set("estado", fila.getString(3));
                list.add(object);
            } while (fila.moveToNext());
            fila.close();
        }
        Log.i("app", "cargos obtenidos..." + list);
        if(fila != null && !fila.isClosed())
            fila.close();
        return list;
    }

    public boolean existeCedula(String cedula) {
        try {

            db = helper.getReadableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        Log.i("app", "Verificando cedula en la bd...");
        String sql = context.getResources().getString(R.string.get_cedula);
        Cursor fila = null;
        boolean existe = false;
        try {
            fila = db.rawQuery(sql,new String[]{cedula});

            if (fila != null && fila.moveToFirst()) {
                if (fila.getInt(0) > 0)
                    existe =  true;
                fila.close();
            }
        }catch (Exception e){
            Log.i("app","Error, Excepcional ejecutar sql en existeCedula()");

        }
        if(fila != null && !fila.isClosed())
            fila.close();
        return existe;
    }

    public void registrarModulosUsuario(long usuario_id, ArrayList<Integer> modulosList) {
        try {

            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        ContentValues values = new ContentValues();

        for(Integer modulo_id : modulosList){
            values.put("modulo_id",modulo_id);
            values.put("usuario_id",usuario_id);
            db.insert("modulousuario",null,values);
        }
    }

    public List<String> getTablesList() {
        try {

            db = helper.getReadableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        List<String> listTableNames = Lists.newArrayList();
        Cursor cursor = null;
        GenericObject tableData;
        try{
            cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table AND name != 'android_metadata'",null);
            if (cursor != null && cursor.moveToFirst()) {
                do{
                    listTableNames.add(cursor.getString(0));
                }while(cursor.moveToNext());
                cursor.close();
            }

        }catch(Exception e){
            Log.i("app","Error al obtenerº lista de tablas de la DB");
         
        }
        if(cursor != null && !cursor.isClosed())
            cursor.close();

        return listTableNames;
    }

    public JSONArray getRowsJSON(String nombre_tabla, String where_clause,String timestamp) {

        try {
            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        Log.i("app","attempting to obtain cursor of "+nombre_tabla);

        Cursor cursor = null;
        JSONArray arrayValores = new JSONArray();

        String sql = "SELECT * FROM " + nombre_tabla;
        if(!Strings.isNullOrEmpty(where_clause)){
            sql = sql.concat(" WHERE "+where_clause);
        }

        Log.i("app","--sql: "+sql);
        try{
            cursor = db.rawQuery(sql,null);

            if(cursor != null && cursor.moveToFirst()) {
                do { // si se retornan registros del query...

                    JSONObject jsonObjectRow = new JSONObject(); // creamos un nuevo objeto para cada registro o fila

                    jsonObjectRow = cursorToJson(cursor,timestamp); // agregamos el registro o fila al objeto JSON

                    arrayValores.put(jsonObjectRow); // agregamos el objeto JSON de la fila al array de objetos JSON
                } while (cursor.moveToNext());
                cursor.close();

            }else{
                Log.w("app", "table "+nombre_tabla+" has no records!");
            }
            //cursor.close();
        }catch(Exception e){
            throw new SQLException("Error al obtener lista de tablas de la DB");
        }
        if(cursor != null && !cursor.isClosed())
            cursor.close();

        return arrayValores;
    }

    public JSONObject cursorToJson(Cursor c,String timestamp) {
        JSONObject retVal = new JSONObject();
        Log.i("app", "parsing cursor (record) to JSON!");

        for(int i=0; i<c.getColumnCount(); i++) {
            String cName = c.getColumnName(i);
            if(cName.equals("migrated")){//ignorar estas columnas para exportar
                continue;//saltamos el ciclo
            }
            try {
                switch (c.getType(i)) {
                    case Cursor.FIELD_TYPE_INTEGER:

                        retVal.put(cName, c.getInt(i));

                        break;
                    case Cursor.FIELD_TYPE_FLOAT:
                        retVal.put(cName, c.getFloat(i));
                        break;
                    case Cursor.FIELD_TYPE_STRING:
                        if(cName.equals("migrated_timestamp")){
                            retVal.put(cName, timestamp);
                        }else {
                            retVal.put(cName, c.getString(i));
                        }
                        break;
                    case Cursor.FIELD_TYPE_BLOB:
                        if(c.getBlob(i) != null) {
                            byte[] bdata = c.getBlob(i);
                            String encodedImage;
                            encodedImage = Base64.encodeToString(bdata, Base64.DEFAULT);
                            Log.w("app","ecodedImage:"+encodedImage);
                            retVal.put(cName, encodedImage);
                        }else{
                            retVal.put(cName, c.getString(i));
                        }
                        break;
                }
            }
            catch(Exception ex) {
                Log.i("app", "Exception converting cursor column to json field: " + cName+". Exception: "+ex.getMessage());
            }
        }

        Log.i("app", "JSON: "+retVal.toString());
        return retVal;
    }

    public ArrayList<GenericObject> getPuestos() {
        try {
            db = helper.getReadableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            
        }
        ArrayList<GenericObject> list = Lists.newArrayList();

        GenericObject puesto = new GenericObject();
        String sql = context.getResources().getString(R.string.get_puestos);
        //Log.i("app", "Compañia seleccionada: " + idCompania);
        Log.i("app", "Sql: " + sql);

        Cursor fila = db.rawQuery(sql, null);
        if(fila != null && fila.moveToFirst()) {
            do{
                puesto = new GenericObject();
                puesto.set("id", fila.getInt(0));
                puesto.set("nombre", fila.getString(4));
                puesto.set("descripcion", fila.getString(2));
                puesto.set("compania_id", fila.getInt(3));
                puesto.set("nominativo", fila.getString(4));
                puesto.set("trueName", fila.getString(1));
                //Log.i("app", "Puesto: " + fila.getString(0) + "," + fila.getString(1) + ","+fila.getString(2)
                //       + ","+fila.getString(3)+ ","+fila.getString(4));
                list.add(puesto);
            }while (fila.moveToNext());
            fila.close();
        }
        if(fila != null && !fila.isClosed())
            fila.close();
        return list;
    }

    public void close(){
        if(db != null && db.isOpen())
            db.close();
        if(helper != null)
            helper.close();

        context = null;
        dbName = null;
        helper = null;
        db = null;
    }

    public boolean isSuperPassword(String pass) {
        try {

            db = helper.getReadableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
        }
        Log.i("app", "Verificando si "+pass+" es super password...");
        String sql = context.getResources().getString(R.string.get_superpassword);
        Cursor fila = null;
        boolean existe = false;
        try {
            fila = db.rawQuery(sql,null);

            if (fila != null && fila.moveToFirst()) {
                existe =  fila.getString(0).equals(pass);

                fila.close();
            }
        }catch (Exception e){
            Log.i("app", "Excepcion al obtener super password..."+e.getMessage());
        }
        if(fila != null && !fila.isClosed())
            fila.close();
        return existe;
    }

    public GenericObject getRolById(int id_rol) {
        try {
            db = helper.getReadableDatabase();

        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
        }
        int key = 0;
        Cursor fila = db.rawQuery(context.getResources().getString(R.string.get_rol_where_id)+id_rol, null);
        GenericObject rol = new GenericObject();
        int id,estado;
        boolean ver,registrar,editar,borrar;
        String descripcion,responsabilidades;
        if (fila != null && fila.moveToFirst()) {
            rol = new GenericObject();
            id = fila.getInt(0);
            descripcion = fila.getString(1);
            responsabilidades = fila.getString(2);
            estado = fila.getInt(3);
            ver = (fila.getInt(4) > 0);
            registrar = fila.getInt(5) > 0;
            editar = fila.getInt(6) > 0;
            borrar = fila.getInt(7) > 0;
            rol.set("id",id);
            rol.set("descripcion",descripcion);
            rol.set("responsabilidades",responsabilidades);
            rol.set("estado",estado);
            rol.set("ver",ver);
            rol.set("registrar",registrar);
            rol.set("editar",editar);
            rol.set("borrar",borrar);

            fila.close();
        }
        if(fila != null && !fila.isClosed())
            fila.close();
        return rol;
    }

    public boolean existeImport(String id_export) {
        try {

            db = helper.getReadableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
        }
        Log.i("app", "Verificando id_export '"+id_export+"' en la bd...");
        String sql = context.getResources().getString(R.string.get_cedula);
        Cursor fila = null;
        boolean existe = false;
        try {
            fila = db.rawQuery(sql,new String[]{id_export});

            if (fila != null && fila.moveToFirst()) {
                if (fila.getInt(0) > 0)
                    existe =  true;
                fila.close();
            }
        }catch (Exception e){
            Log.i("app", "Ha ocurrido una excepcion: "+e.getMessage());
        }
        if(fila != null && !fila.isClosed())
            fila.close();
        return existe;
    }

    public boolean isUserCapableToLoggin(String usuario_id, String hoy) {
        try {

            db = helper.getReadableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
        }
        Log.i("app", "Verificando si usuario '"+usuario_id+"' tiene SALIDA en asistencia...");
        //se debe verificar primero que no exista una salida registrada para la fecha recibida,
        // si existe una salida se retora false, el usuario no puede volver a marcar su entrada

        String sql = context.getResources().getString(R.string.get_asistencia_where);
        Log.i("app","sql: "+sql);
        Cursor  fila = null;
        boolean existe = true;
        try {
            fila = db.rawQuery(sql,new String[]{hoy+"%",usuario_id,"SALIDA"});

            if (fila != null && fila.moveToFirst()) {
                Log.i("app","COUNT(*): "+fila.getInt(0));
                if (fila.getInt(0) > 0)
                    existe =  false;
                fila.close();
            }
        }catch (Exception e){
            Log.i("app", "Ha ocurrido una excepcion: "+e.getMessage());
        }
        if(fila != null && !fila.isClosed())
            fila.close();
        return existe;
    }

    public int isUserLogged(String id, String hoy) {
        try {

            db = helper.getReadableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
        }
        Log.i("app", "Verificando el logged de usuario '"+id+"'...");
        //se debe verificar primero que no exista una salida registrada para la fecha recibida,
        // si existe una salida se retora false, el usuario no puede volver a marcar su entrada

        String sql = context.getResources().getString(R.string.get_user_where);
        Log.i("app","sql: "+sql);
        Cursor fila = null;
        int logged = 0;
        try {
            fila = db.rawQuery(sql,new String[]{id});

            if (fila != null && fila.moveToFirst()) {
                logged = fila.getInt(16);

                fila.close();
            }
        }catch (Exception e){
            Log.i("app", "Ha ocurrido una excepcion: "+e.getMessage());
        }
        Log.i("app","logged es :"+logged);
        if(fila != null && !fila.isClosed())
            fila.close();
        return logged;
    }

    public boolean limpiarRegistrosWhere(String where_clause){

        try {

            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
        }


        String tablas[] = {"bitacora","controlsalida","radio","comentario_bitacora","relevo","informe","session","asistencia"};

        boolean exito = true;
        String dateFieldName = "";
        String where_clause_aux = "";
        for(int i = 0; i < tablas.length; i++) {

        if(tablas[i].equals("asistencia")){

            dateFieldName = "fecha";

        }else if(tablas[i].equals("bitacora") || tablas[i].equals("radio") || tablas[i].equals("relevo")
            || tablas[i].equals("controlsalida") || tablas[i].equals("informe") ){

            dateFieldName = "timestamp";

        }else if(tablas[i].equals("comentario_bitacora")){

            dateFieldName = "datetime";

        }else if(tablas[i].equals("session")){

            dateFieldName = "inicio";
        }

        //armamos where_clause
            DateTimeZone tz = DateTimeZone.getDefault();
            DateTime today = DateTime.now(tz);

            DateTimeFormatter fmtDay = DateTimeFormat.forPattern("yyyy-MM-dd");

            String hoy = fmtDay.print(today);
            where_clause_aux = where_clause+" "+"AND "+dateFieldName+" < '"+hoy+"'";

            Log.i("app", "Eliminando registros donde de"+tablas[i]+" "+where_clause+" ...");
            try {
                 db.delete(tablas[i], where_clause_aux,null);

            } catch (Exception e) {
                exito = false;
                Log.i("app", "Ha ocurrido una excepcion: " + e.getMessage());

            }
        }
        return exito;
    }

    public boolean cambiarSuperclave(String s, String s1, String s2) {


        try {
            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
        }
        Log.i("app", "Cambiando superclave ...");

        boolean exito = true;
        ContentValues values = new ContentValues();

        if(!Strings.isNullOrEmpty(s) && Strings.isNullOrEmpty(s1) && Strings.isNullOrEmpty(s2)){
            return false;
        }
        if(s1.length() < 5 || s2.length() < 5 || s.length() < 5)
            return false;
        if(!s1.equals(s2))
            return false;

        if(!isSuperPassword(s)){
            return false;
        }

        values.put("password",s1);

            try {
                db.update("system", values,"password="+s,null);

            } catch (Exception e) {
                exito = false;
                Log.i("app", "Ha ocurrido una excepcion: " + e.getMessage());
            }

            return exito;
    }

    public void upgrade() {
        try {

            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            return;
        }

        helper.upgrade(db); // se eliminan las tablas padres completas y se vuelven a crear
    }

    public void upgrade(String table){
        try {

            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
            return;
        }
        String sql = "";
        switch(table){
            case "usuario":
                db.execSQL(context.getResources().getString(R.string.drop_usuario_table));
                db.execSQL(context.getResources().getString(R.string.create_user_table));
                break;
            case "compania":
                db.execSQL(context.getResources().getString(R.string.drop_compania_table));
                db.execSQL(context.getResources().getString(R.string.create_compania_table));
                break;
            case "rol":
                db.execSQL(context.getResources().getString(R.string.drop_role_table));
                db.execSQL(context.getResources().getString(R.string.create_role_table));
                break;
            case "cargo":
                db.execSQL(context.getResources().getString(R.string.drop_cargo_table));
                db.execSQL(context.getResources().getString(R.string.create_cargo_table));
                break;
            case "suministro":
                db.execSQL(context.getResources().getString(R.string.drop_suministro_table));
                db.execSQL(context.getResources().getString(R.string.create_suministro_table));
                break;
            case "puesto":
                db.execSQL(context.getResources().getString(R.string.drop_puesto_table));
                db.execSQL(context.getResources().getString(R.string.create_puesto_table));
                break;
            case "puesto_suministro":
                db.execSQL(context.getResources().getString(R.string.drop_puesto_suministro_table));
                db.execSQL(context.getResources().getString(R.string.create_puesto_suministro_table));
                break;
            case "dispositivo":
                db.execSQL(context.getResources().getString(R.string.drop_dispositivo_table));
                db.execSQL(context.getResources().getString(R.string.create_dispositivo_table));
                break;
            case "modulo":
                    db.execSQL(context.getResources().getString(R.string.drop_modulo_table));
                db.execSQL(context.getResources().getString(R.string.create_modulo_table));
                break;
            case "modulousuario":
                db.execSQL(context.getResources().getString(R.string.drop_modulo_user_table));
                db.execSQL(context.getResources().getString(R.string.create_modulo_user_table));
                break;
        }
    }

    public ArrayList<GenericObject> obtenerSuministrosByPuesto(int idPuesto) {
        try {

            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
        }
        ArrayList<GenericObject> list = Lists.newArrayList();

        GenericObject object;
        String sql = context.getResources().getString(R.string.get_suministros_where_puesto);
        Log.i("app", "sql: " + sql);
        Cursor  fila = db.rawQuery(sql, new String[]{String.valueOf(idPuesto)});
        if(fila != null && fila.moveToFirst()) {
            do {

                object = new GenericObject();
                object.set("id", fila.getInt(0));
                object.set("codigo", fila.getString(1));
                object.set("nombre", fila.getString(2));
                object.set("serial", fila.getString(3));
                object.set("estado", fila.getInt(4));
                object.set("cantidad", fila.getInt(5));

                list.add(object);
                //Log.i("app","object list.radio.timestamp: "+list.get(list.size()-1).getTimestamp());
            } while (fila.moveToNext());
            fila.close();
        }

        Log.i("app", "object list Todos los Suministros: " + list);
        if(fila != null && !fila.isClosed())
            fila.close();
        return list;

    }

    public void exec(String query) {
        db.execSQL(query);
    }

    public void dropTabla(String tabla){
        try {

            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
        }

         db.execSQL(context.getResources().getString(R.string.drop_table)+tabla);
    }

    public void crearTabla(String tabla){
        try {

            db = helper.getWritableDatabase();
        }catch (Exception e){
            Log.i("app","No se pudo abrir el archivo de base de datos! Exception: "+e.getMessage());
        }
        if(tabla.equals("compania"))
            db.execSQL(context.getResources().getString(R.string.create_compania_table));
        if(tabla.equals("puesto"))
            db.execSQL(context.getResources().getString(R.string.create_puesto_table));
        if(tabla.equals("cargo"))
            db.execSQL(context.getResources().getString(R.string.create_cargo_table));
        if(tabla.equals("rol"))
            db.execSQL(context.getResources().getString(R.string.create_role_table));
        if(tabla.equals("modulo"))
            db.execSQL(context.getResources().getString(R.string.create_modulo_table));
        if(tabla.equals("usuario"))
            db.execSQL(context.getResources().getString(R.string.create_user_table));
        if(tabla.equals("suministro"))
            db.execSQL(context.getResources().getString(R.string.create_suministro_table));
        if(tabla.equals("dispositivo"))
            db.execSQL(context.getResources().getString(R.string.create_dispositivo_table));
        if(tabla.equals("modulousuario"))
            db.execSQL(context.getResources().getString(R.string.create_modulo_user_table));
        if(tabla.equals("puesto_suministro"))
            db.execSQL(context.getResources().getString(R.string.create_puesto_suministro_table));

    }


}