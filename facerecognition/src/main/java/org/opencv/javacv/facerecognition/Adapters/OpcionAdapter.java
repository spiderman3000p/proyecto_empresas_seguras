package org.opencv.javacv.facerecognition.Adapters;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.javacv.facerecognition.Activities.Bitacora2Activity;
import org.opencv.javacv.facerecognition.Activities.ControlAzucarActivity;
import org.opencv.javacv.facerecognition.Activities.FdActivity;
import org.opencv.javacv.facerecognition.Activities.InformeActivity;
import org.opencv.javacv.facerecognition.Activities.RadiosActivity;
import org.opencv.javacv.facerecognition.Activities.RegistrarUsuario;
import org.opencv.javacv.facerecognition.Activities.RelevoActivity;
import org.opencv.javacv.facerecognition.Helpers.CustomHelper;
import org.opencv.javacv.facerecognition.Models.GenericObject;
import org.opencv.javacv.facerecognition.R;

import java.util.List;

/**
 * Created by AlexManuel on 15/09/2015.
 */
public class OpcionAdapter extends BaseAdapter {
    private Context mContext, context2;
    private LayoutInflater inflate;
    private List<GenericObject> moduloList;
    private ContentValues userData;

    public OpcionAdapter(Context mContext, List<GenericObject> moduloList, ContentValues userData) {
        this.mContext = mContext;
        this.inflate = LayoutInflater.from(mContext);
        this.moduloList = moduloList;
        this.userData = userData;
    }

    @Override
    public int getCount() {
        return moduloList.size();
    }

    @Override
    public Object getItem(int position) {
        if (moduloList != null) {
            return this.moduloList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder {
        TextView tvCodigo;
        Button btAcceder;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        context2 = parent.getContext();
            final ViewHolder holder;
            try {
                if (convertView == null) {
                    holder = new ViewHolder();
                    convertView = inflate.inflate(R.layout.item_opciones, null);
                    holder.tvCodigo = convertView.findViewById(R.id.tvCodigoOp);
                    holder.btAcceder = convertView.findViewById(R.id.btAccederOp);

                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                holder.tvCodigo.setText(this.moduloList.get(position).getAsString("codigo_modulo"));
                holder.btAcceder.setText(this.moduloList.get(position).getAsString("nombre_modulo"));

                Resources r = mContext.getResources();
                TypedArray imagenes = r.obtainTypedArray(R.array.opciones);
                Drawable drawable = imagenes.getDrawable(position);

                ScaleDrawable sd = new ScaleDrawable(drawable, 0, 0.1f, 0.1f);

                holder.btAcceder.setCompoundDrawablesWithIntrinsicBounds(sd.getDrawable(), null, null, null);

                holder.btAcceder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Log.i("app", "Click: " + holder.tvCodigo.getText().toString() + holder.btAcceder.getText().toString());
                            //LLamada a editar
                            if (holder.tvCodigo.getText().toString()
                                    .equalsIgnoreCase(CustomHelper.Opciones.CONTROL_RADIOS.getCodigo())) {
                                //Toast.makeText(mContext,holder.btAcceder.getText().toString(),Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(mContext, RadiosActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("userData", userData);
                                mContext.startActivity(intent);

                            } else if (holder.tvCodigo.getText().toString()
                                    .equalsIgnoreCase(CustomHelper.Opciones.CONTROL_AZUCAR.getCodigo())) {
                                Intent intent = new Intent(mContext.getApplicationContext(),
                                        ControlAzucarActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("userData", userData);
                                mContext.startActivity(intent);
                            } else if (holder.tvCodigo.getText().toString()
                                    .equalsIgnoreCase(CustomHelper.Opciones.BITACORA.getCodigo())) {
                                Intent intent = new Intent(mContext, Bitacora2Activity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("userData", userData);
                                mContext.startActivity(intent);

                            } else if (holder.tvCodigo.getText().toString()
                                    .equalsIgnoreCase(CustomHelper.Opciones.INFORME_ESPECIAL.getCodigo())) {
                                Intent intent = new Intent(mContext, InformeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("userData", userData);
                                mContext.startActivity(intent);
                            } else if (holder.tvCodigo.getText().toString()
                                    .equalsIgnoreCase(CustomHelper.Opciones.RELEVO_GUARDIA.getCodigo())) {
                                Intent intent = new Intent(mContext.getApplicationContext(), RelevoActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("userData", userData);
                                mContext.startActivity(intent);

                            } else if (holder.tvCodigo.getText().toString()
                                    .equalsIgnoreCase(CustomHelper.Opciones.REGISTRO_USUARIOS.getCodigo())) {
                                Intent intent = new Intent(mContext.getApplicationContext(), RegistrarUsuario.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("userData", userData);
                                mContext.startActivity(intent);

                            } else if (holder.tvCodigo.getText().toString()
                                    .equalsIgnoreCase(CustomHelper.Opciones.REGISTRO_USUARIOS.getCodigo())) {

                            } else if (holder.tvCodigo.getText().toString()
                                .equalsIgnoreCase(CustomHelper.Opciones.ASOCIAR_ROSTTRO.getCodigo())) {
                            Intent intent = new Intent(mContext.getApplicationContext(),
                                    FdActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(intent);

                        } else {
                                Toast.makeText(null, "No esta definido la opción para el acceso!", Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Log.i("Debug", "Error grid" + e.getMessage());
                        }
                    }
                });


            } catch (Exception e) {
                e.printStackTrace();
            }

        return convertView;
    }


}
