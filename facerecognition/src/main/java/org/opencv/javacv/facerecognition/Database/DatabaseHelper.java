package org.opencv.javacv.facerecognition.Database;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.opencv.javacv.facerecognition.Helpers.CustomHelper;
import org.opencv.javacv.facerecognition.R;

/**
 * Created by RAFAEL on 27/11/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper{

    Context context;
    public DatabaseHelper(Context c, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(c, CustomHelper.DATABASE_PATH+CustomHelper.DATABASE_NAME, factory, version);
        context = c;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //eliminarTablas(db);
        crearTablas(db);
        llenarTablas(db);
    }

    private void llenarTablas(SQLiteDatabase db){
        /////////////////    Insertamos los datos por defecto   ////////////////////////////////////
        //Insertamos los modulos habilitados por defecto para cada usuario
        //db.execSQL(context.getResources().getString(R.string.insert_default_compania));
        //Isertamos los cargos por defecto
        //db.execSQL(context.getResources().getString(R.string.insert_default_cargo));
        //Insertamos los roles de usuario por defecto
        //db.execSQL(context.getResources().getString(R.string.insert_default_role));
        //Insertamos los usuarios por defecto del sistema (son 3)
        //db.execSQL(context.getResources().getString(R.string.insert_default_users));
        //Insertamos los tipos de permisos por defecto del sistema (son 24 acciones en total)
        //db.execSQL(context.getResources().getString(R.string.insert_default_permissions));
        //Insertamos los permisos por defecto del usuario admin (son 24 acciones en total permitidas)
        //db.execSQL(context.getResources().getString(R.string.insert_default_permissions_rol_admin));
        //Insertamos los permisos por defecto del usuario user (son 18 acciones en total permitidas, excepto eliminar registros)
        //db.execSQL(context.getResources().getString(R.string.insert_default_permissions_rol_user));
        //Insertamos los permisos por defecto del usuario visitante (son 6 acciones en total permitidas, solo visualizar el menu principal)
        //db.execSQL(context.getResources().getString(R.string.insert_default_permissions_rol_visit));
        //Insertamos los modulos disponibles
        //db.execSQL(context.getResources().getString(R.string.insert_default_modules));
        //Insertamos los modulos habilitados por defecto para cada usuario
        //db.execSQL(context.getResources().getString(R.string.insert_default_modules_user));
        //Insertamos los puestos por defecto
        //db.execSQL(context.getResources().getString(R.string.insert_default_puestos));
        //Insertamos los suministros por defecto
        //db.execSQL(context.getResources().getString(R.string.insert_default_suministros));
        //Insertamos los dispositivos por defecto
        //db.execSQL(context.getResources().getString(R.string.insert_default_dispositivo),new String[]{CustomHelper.getMarcaDispositivo(),CustomHelper.getModeloDispositivo(),CustomHelper.getSoDispositivo(),CustomHelper.getAndroidId(context.getContentResolver())});
        //Insertamos los suministros por puesto por defecto
        //db.execSQL(context.getResources().getString(R.string.insert_default_suministros_puesto));
        //Insertamos los datos del sistema
        db.execSQL(context.getResources().getString(R.string.insert_default_system));

    }

    private void crearTablas(SQLiteDatabase db){
        ////////////////////////////    Creamos las tablas    ////////////////////////////////////
        //creamos la tabla de compañia
        db.execSQL(context.getResources().getString(R.string.create_compania_table));
        //creamos la tabla de usuarios
        db.execSQL(context.getResources().getString(R.string.create_user_table));
        //creamos la tabla asistencia
        db.execSQL(context.getResources().getString(R.string.create_asistance_table));
        //creamos la tabla de dispositivos
        db.execSQL(context.getResources().getString(R.string.create_dispositivo_table));
        //creamos la tabla de tipos de usuario (roles)
        db.execSQL(context.getResources().getString(R.string.create_role_table));
        //creamos la tabla de tipos de cargos
        db.execSQL(context.getResources().getString(R.string.create_cargo_table));
        //creamos la tabla de labels
        db.execSQL(context.getResources().getString(R.string.create_label_table));
        //creamos la tabla de modulos
        db.execSQL(context.getResources().getString(R.string.create_modulo_table));
        //creamos la tabla de relacion modulo_usuario
        db.execSQL(context.getResources().getString(R.string.create_modulo_user_table));
        //creamos la tabla de puestos
        db.execSQL(context.getResources().getString(R.string.create_puesto_table));
        //creamos la tabla de sesiones
        db.execSQL(context.getResources().getString(R.string.create_session_table));
        //creamos la tabla de informe
        db.execSQL(context.getResources().getString(R.string.create_informe_table));
        //creamos la tabla de suministros
        db.execSQL(context.getResources().getString(R.string.create_dispositivo_table));
        //creamos la tabla de bitacora
        db.execSQL(context.getResources().getString(R.string.create_bitacora_table));
        //creamos la tabla de llamadas de radio
        db.execSQL(context.getResources().getString(R.string.create_radio_table));
         //creamos la tabla de systema
        db.execSQL(context.getResources().getString(R.string.create_system_table));
        //creamos la tabla de comentarios de bitacora
        db.execSQL(context.getResources().getString(R.string.create_comment_bitacora_table));
        //creamos la tabla de control de azucar
        db.execSQL(context.getResources().getString(R.string.create_control_azucar_table));
        //creamos la tabla de relevos
        db.execSQL(context.getResources().getString(R.string.create_relevo_table));
        //creamos la tabla de suministros
        db.execSQL(context.getResources().getString(R.string.create_suministro_table));
        //creamos la tabla de relacion entre relevo y suministros (inventario en el relevo)
        db.execSQL(context.getResources().getString(R.string.create_relevo_suministro_table));
        //creamos la tabla de relacion de suministros por puesto (inventario asignado a un puesto)
        db.execSQL(context.getResources().getString(R.string.create_puesto_suministro_table));

    }

    private void eliminarTablas(SQLiteDatabase db){
        db.execSQL(context.getResources().getString(R.string.drop_compania_table));

        db.execSQL(context.getResources().getString(R.string.drop_asistance_table));

        db.execSQL(context.getResources().getString(R.string.drop_role_table));

        db.execSQL(context.getResources().getString(R.string.drop_cargo_table));

        db.execSQL(context.getResources().getString(R.string.drop_label_table));

        db.execSQL(context.getResources().getString(R.string.drop_permissions_table));

        db.execSQL(context.getResources().getString(R.string.drop_role_permission_table));

        db.execSQL(context.getResources().getString(R.string.drop_modulo_table));

        db.execSQL(context.getResources().getString(R.string.drop_modulo_user_table));

        db.execSQL(context.getResources().getString(R.string.drop_puesto_table));

        db.execSQL(context.getResources().getString(R.string.drop_suministro_table));

        db.execSQL(context.getResources().getString(R.string.drop_bitacora_table));

        db.execSQL(context.getResources().getString(R.string.drop_informe_table));

        db.execSQL(context.getResources().getString(R.string.drop_session_table));

        db.execSQL(context.getResources().getString(R.string.drop_radio_table));

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        eliminarTablas(db);
        crearTablas(db);
        llenarTablas(db);
    }

    public void upgrade(SQLiteDatabase db){
        eliminarTablasPadres(db);
        crearTablasPadres(db);
    }

    private void eliminarTablasPadres(SQLiteDatabase db) {
        db.execSQL(context.getResources().getString(R.string.drop_compania_table));
        db.execSQL(context.getResources().getString(R.string.drop_puesto_table));
        db.execSQL(context.getResources().getString(R.string.drop_modulo_table));
        db.execSQL(context.getResources().getString(R.string.drop_modulo_user_table));
        db.execSQL(context.getResources().getString(R.string.drop_role_table));
        db.execSQL(context.getResources().getString(R.string.drop_cargo_table));
        db.execSQL(context.getResources().getString(R.string.drop_suministro_table));
        db.execSQL(context.getResources().getString(R.string.drop_puesto_suministro_table));
        db.execSQL(context.getResources().getString(R.string.drop_dispositivo_table));
        db.execSQL(context.getResources().getString(R.string.drop_usuario_table));
    }

    private void crearTablasPadres(SQLiteDatabase db) {
        db.execSQL(context.getResources().getString(R.string.create_compania_table));
        //creamos la tabla de usuarios
        db.execSQL(context.getResources().getString(R.string.create_user_table));
        //creamos la tabla de dispositivos
        db.execSQL(context.getResources().getString(R.string.create_dispositivo_table));
        //creamos la tabla de tipos de usuario (roles)
        db.execSQL(context.getResources().getString(R.string.create_role_table));
        //creamos la tabla de tipos de cargos
        db.execSQL(context.getResources().getString(R.string.create_cargo_table));
        //creamos la tabla de modulos
        db.execSQL(context.getResources().getString(R.string.create_modulo_table));
        //creamos la tabla de relacion modulo_usuario
        db.execSQL(context.getResources().getString(R.string.create_modulo_user_table));
        //creamos la tabla de puestos
        db.execSQL(context.getResources().getString(R.string.create_puesto_table));
        //creamos la tabla de suministros
        db.execSQL(context.getResources().getString(R.string.create_suministro_table));
        //creamos la tabla de puestos_suministro
        db.execSQL(context.getResources().getString(R.string.create_puesto_suministro_table));
    }
}
