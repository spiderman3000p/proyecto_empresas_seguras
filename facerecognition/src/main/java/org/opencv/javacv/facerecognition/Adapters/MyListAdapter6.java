package org.opencv.javacv.facerecognition.Adapters;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.collect.Lists;

import org.opencv.javacv.facerecognition.Helpers.CustomHelper;
import org.opencv.javacv.facerecognition.Models.GenericObject;
import org.opencv.javacv.facerecognition.R;

import java.util.List;

public class MyListAdapter6 extends RecyclerView.Adapter<MyListAdapter6.MyViewHolder>  {

    private static final int IMPORTAR = CustomHelper.IMPORTAR;
    private static final int EXPORTAR = CustomHelper.EXPORTAR;
    private static List<GenericObject> list;
    Context mContext;
    int selectedCounter = 0;
    List<Integer> selectedItemsIndexes;
    private int modo_view = -1;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title,subtitle1, subtitle2;
        CardView cv;
        Context context;
        CheckBox checkBox;
        ImageView imagen;
        View view;

        private MyViewHolder(View itemView) {
            super(itemView);

            context = itemView.getContext();

            cv = itemView.findViewById(R.id.cv);

            view = itemView;
            final int position = getAdapterPosition();
            title = itemView.findViewById(R.id.title);
            subtitle1 = itemView.findViewById(R.id.subtitle1);
            subtitle2 = itemView.findViewById(R.id.subtitle2);
            imagen = itemView.findViewById(R.id.imagen);
            checkBox = itemView.findViewById(R.id.checkBox);

        }


    }

    public MyListAdapter6(int modo,Context context, List<GenericObject> l) {
        mContext = context;
        //this.inflate = LayoutInflater.from(mContext);
        Log.i("app", "Setting adapter...list:"+l.toString());
        Log.i("app", "Modo view: "+ modo);
        selectedItemsIndexes = Lists.newArrayList();
        list = l;
        this.modo_view = modo;
    }



    @Override
    public MyListAdapter6.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_modulos_export, parent, false);

        MyViewHolder vh = new MyViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        GenericObject object = list.get(position);
        //holder.title.setText(this.list.get(position).getAsString("codigo_modulo"));
        holder.checkBox.setChecked(true);
        holder.title.setText(object.getAsString("nombre_modulo"));
        String s1 = mContext.getString(R.string.registros_nuevos)+ object.getAsString("migrated_count");
        holder.subtitle1.setText(s1);
        holder.subtitle2.setText("");

/*
        if(Strings.isNullOrEmpty(object.getAsString("migrated_last"))){
            if(modo_view == IMPORTAR)
                holder.subtitle2.setText(R.string.ultima_importacion_ningun);
            if(modo_view == EXPORTAR)
                holder.subtitle2.setText("");
        }else {
            if(modo_view == IMPORTAR) {
                s1 = "Fecha: " + object.getAsString("migrated_last");
                holder.subtitle2.setText();
            }
            if(modo_view == EXPORTAR)
                holder.subtitle2.setText("Ultima exportación: " + object.getAsString("migrated_last"));
        }*/

        Resources r = mContext.getResources();
        TypedArray imagenes = r.obtainTypedArray(R.array.opciones);
        Drawable drawable = imagenes.getDrawable(position);
        imagenes.recycle();
        ScaleDrawable sd = new ScaleDrawable(drawable, 0, 0.1f, 0.1f);

        //holder.btAcceder.setCompoundDrawablesWithIntrinsicBounds(sd.getDrawable(), null, null, null);

        //holder.title.setText(r.getAsString("nombre_compania"));
        holder.imagen.setImageDrawable(sd.getDrawable());

        if(holder.checkBox.isChecked()){
            selectedItemsIndexes.add(position);
        }
        holder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.checkBox.setChecked(!holder.checkBox.isChecked()); // checkea o descheckea dependiendo del estado
                selectedCounter += holder.checkBox.isChecked()?1:-1; // suma o resta una unidad dependiendo si esta checkeado o no
                if (holder.checkBox.isChecked()) {
                    selectedItemsIndexes.add(position);
                    Log.i("app", "adding index: " + position);
                } else {
                    Log.i("app", "removing index: " + position);
                    selectedItemsIndexes.remove((Integer) position);
                }
                Log.i("app", "selectedItemsIndexes: " + selectedItemsIndexes);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public int getSelectedCount() { return selectedCounter; }

    public List<Integer> getSelectedItemsIndexes(){
        return selectedItemsIndexes;
    }
}
