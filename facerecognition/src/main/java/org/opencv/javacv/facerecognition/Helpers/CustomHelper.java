package org.opencv.javacv.facerecognition.Helpers;
/**
 * Created by Angel on 31/01/2016.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.widget.EditText;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.opencv.javacv.facerecognition.Database.DatabaseAdmin;
import org.opencv.javacv.facerecognition.DatePickerFragment;
import org.opencv.javacv.facerecognition.Models.GenericObject;
import org.opencv.javacv.facerecognition.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CustomHelper {
    public static final String BITACORA_IMPORTANTE = "IMPORTANTE";
    public static final int INFO = 3;
    public static final int ERROR = 1;
    public static final int SUCCESS = 2;
    public static final int MODO_VER = 1;
    public static final int MODO_EDITAR = 0;
    public static final int MODO_NUEVO = 2;
    public static final int IMPORTAR = 4;
    public static final int EXPORTAR = 5;
    public static final String DATABASE_PATH = Environment.getExternalStorageDirectory() +
            "/databases/";
    public static final String DATABASE_NAME = "compured.db";

    public static String getMarcaDispositivo() {
        return  Build.MANUFACTURER;
    }

    public static String getModeloDispositivo() {
        return android.os.Build.MODEL;
    }

    public static String getSoDispositivo() {
        return android.os.Build.VERSION.RELEASE + "(API "+android.os.Build.VERSION.SDK_INT+")";
    }

    public static String getDateString() {
        DateTimeZone tz = DateTimeZone.getDefault();
        DateTime hoy = DateTime.now(tz);

        DateTimeFormatter fmtDay = DateTimeFormat.forPattern("yyyy-MM-dd");

        return fmtDay.print(hoy);
    }

    public static boolean isInIntervaloFechas(DateTime fechaDesde, DateTime fechaHasta, DateTime timestamp) {
        //transformamos los parametros a fechas y usamos la funcion de Date

        boolean result = timestamp.isAfter(fechaDesde.minusDays(1)) && timestamp.isBefore(fechaHasta.plusDays(1));
        Log.i("app","fecha: " + timestamp +     " desde : " + fechaDesde + " hasta " + fechaHasta + "");
        Log.i("app","esta entre fechas? " + (result? "SI" : "NO"));

        return result;
    }

    public enum Opciones {
        CONTROL_RADIOS("OP001"),
        CONTROL_AZUCAR("OP002"),
        BITACORA("OP003"),
        INFORME_ESPECIAL("OP004"),
        RELEVO_GUARDIA("OP005"),
        REGISTRO_USUARIOS("OP006"),
        CAMBIO_CLAVE("OP007"),
        ASISTENCIA_EMPLEADOS("OP008"),
        SESIONES_USUARIOS("OP009"),
        ASOCIAR_ROSTTRO("OP010");

        private String codigo;

        Opciones(String codigo) {
            this.codigo = codigo;
        }

        public String getCodigo() {
            return codigo;
        }

    }

    public enum DatabaseTables {
        radio("Controles de Radios"),
        controlsalida("Controles de Azucar"),
        bitacora("Bitacoras"),
        informe("Informes Especiales"),
        relevo("Relevos de Guardias"),
        relevo_suministro("Inventario de Relevos"),
        cargo("Cargos"),
        compania("Companias"),
        comentario_bitacora("Comentarios de Bitacoras"),
        dispositivo("Inventario de Dispositivos"),
        modulo("Modulos del Sistema"),
        modulousuario("Modulos de Usuarios"),
        permiso("Permisos del Sistema"),
        rol("Roles del Sistema"),
        rolpermiso("Permisos de Roles"),
        suministro("Inventario del Sistema"),
        usuario("Usuarios/Empleados"),
        puesto("Puestos"),
        asistencia("Asistencias de Empleados"),
        puesto_suministro("Inventario de Puestos"),
        session("Sesiones de Usuarios");

        private String codigo;

        DatabaseTables(String codigo) {
            this.codigo = codigo;
        }

        public String getCodigo() {
            return codigo;
        }

    }

    private static boolean externalStorageReadable, externalStorageWritable;

    public static boolean isExternalStorageReadable() {
        checkStorage();
        return externalStorageReadable;
    }

    public static boolean isExternalStorageWritable() {
        checkStorage();
        return externalStorageWritable;
    }

    public static boolean isExternalStorageReadableAndWritable() {
        checkStorage();
        return externalStorageReadable && externalStorageWritable;
    }

    private static void checkStorage() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            externalStorageReadable = externalStorageWritable = true;
        } else if (state.equals(Environment.MEDIA_MOUNTED) || state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            externalStorageReadable = true;
            externalStorageWritable = false;
        } else {
            externalStorageReadable = externalStorageWritable = false;
        }
    }

    public static String getDateTimeString(){
        DateTimeZone tz = DateTimeZone.getDefault();
        DateTime hoy = DateTime.now(tz);

        DateTimeFormatter fmtDay = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

        return fmtDay.print(hoy);
    }

    public static final byte[] convertBitmapToBytes(Bitmap bitmap) {
        byte[] result = null;
        if (bitmap != null) {
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                result = bos.toByteArray();

            } catch (Exception e) {
                Log.e(CustomHelper.class.getName(), e.getMessage(), e);
            }
        } else {
            Log.i(CustomHelper.class.getName(), "Bitmap enviado es nulo!");
        }
        return result;
    }

    public static boolean existsInComments(Context c,int id, String filterPattern) {
        DatabaseAdmin db = new DatabaseAdmin(c);
        ArrayList<GenericObject> comentarios = db.obtenerComentariosBitacora2(id);

        if (comentarios.size() > 0) {
            //Buscar dentro de los comentarios
            for(GenericObject comentario: comentarios){
                if(comentario.getAsString("comentario").toLowerCase().contains(filterPattern) ||
                        comentario.getAsString("nombre_usuario").toLowerCase().contains(filterPattern) ||
                        comentario.getAsString("apellido_usuario").toLowerCase().contains(filterPattern) ||
                        comentario.getAsString("datetime").toLowerCase().contains(filterPattern)){
                    return true;
                }
            }

        }
        return false;
    }


    public static void showDialogDate( Activity activity,DialogFragment fragment, DateTime dtDate,
                               EditText txtDate) {
        fragment = new DatePickerFragment(dtDate,txtDate);

        fragment.show(activity.getFragmentManager(), "Elija una fecha");
    }

    public static String parseToString(DateTime dt, String format) {
        return DateTimeFormat.forPattern(format).print(dt);
    }

    public static final String getAndroidId(ContentResolver content) {
        return Settings.Secure.getString(content, Settings.Secure.ANDROID_ID);
    }

    public static final int getIdPuesto(Context c, String dispositivo) {
        DatabaseAdmin db = new DatabaseAdmin(c);

        int id = db.getIdPuestoByDispositivo(dispositivo);

        db.close();
        return id;
    }

    public static String getDeviceInfo(){
        String s="(" + Build.MANUFACTURER+" ";
        s += android.os.Build.MODEL + " "+ android.os.Build.PRODUCT + ") Android ";
        s += android.os.Build.VERSION.RELEASE + "("+android.os.Build.VERSION.SDK_INT+")";

        return s;
    }

    public String getStringFromBitmap(Bitmap bitmapPicture) {
 /*
 * This functions converts Bitmap picture to a string which can be
 * JSONified.
 * */
        final int COMPRESSION_QUALITY = 100;
        String encodedImage;
        ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
        bitmapPicture.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY,byteArrayBitmapStream);
        byte[] b = byteArrayBitmapStream.toByteArray();
        encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
        return encodedImage;
    }

    public static AlertDialog alert(String title, String texto, int tipoIcon, AlertDialog.Builder builder) {
        if (tipoIcon == ERROR) {
            builder.setIcon(R.drawable.errorp);
        } else if (tipoIcon == SUCCESS) {
            builder.setIcon(R.drawable.okp);
        } else {
            builder.setIcon(R.drawable.infop);
        }

        builder.setMessage(texto)
                .setTitle(title)
                .setCancelable(false);


        return builder.create();
    }

    public boolean existeFaceRecord(Context c,final String strCedula){
        String strFolderFaces = Environment.getExternalStorageDirectory() + "/databases/facerecogOCV";
        File folderFaces = new File(strFolderFaces);
        if (folderFaces.isDirectory()) {

            List<String> rostros = Lists.newArrayList(folderFaces.list());
            Predicate<String> P = new Predicate<String>() {
                @Override
                public boolean apply(String input) {
                    return input.contains(strCedula);
                }
            };

            if (Collections2.filter(rostros, P).size() <= 0) {
                return false;
            }
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(c);
            builder.setTitle("Información");
            builder.setMessage("En el dispositivo NO se encuentra el directorio de fotos de empleados, " +
                    "favor migrar las fotos desde el sistema de Windows");
            builder.setCancelable(true);
            builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();

        }
        return false;
    }

    public static final void closeCursor(Cursor cursor) {
        try {
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLException e) {
            Log.e(CustomHelper.class.getName(), e.getMessage(), e);
        }
    }

    public static final void closeDatabase(SQLiteDatabase db) {
        try {
            if (db != null) {
                db.close();
            }
        } catch (SQLException e) {
            Log.e(CustomHelper.class.getName(), e.getMessage(), e);
        }
    }


}