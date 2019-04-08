package org.opencv.javacv.facerecognition.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import org.opencv.javacv.facerecognition.Helpers.CustomHelper;
import org.opencv.javacv.facerecognition.Models.GenericObject;
import org.opencv.javacv.facerecognition.R;

import java.util.ArrayList;

public class SuministrosAdapter extends RecyclerView.Adapter<SuministrosAdapter.MyViewHolder>  {

    private ArrayList<GenericObject> list;
    private ArrayList<GenericObject> suministros;
    private Context mContext;
    int modo_view = 0;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView subtitle1;
        EditText title;
        CardView cv;
        Context context;
        CheckBox checked;
        View view;
        boolean isChecked = true;

        private MyViewHolder(View itemView) {
            super(itemView);
            context = itemView.getContext();
            cv = itemView.findViewById(R.id.cv);
            view = itemView;
            final int position = getAdapterPosition();
            title = itemView.findViewById(R.id.title);
            subtitle1 = itemView.findViewById(R.id.subtitle1);
            checked = itemView.findViewById(R.id.checked);


        }


    }

    public SuministrosAdapter(Context mContext,ArrayList<GenericObject> l2,ArrayList<GenericObject> l, int modo_view) {
        list = l; //copia de todos los suministros asignados al puesto
        suministros = l2; //lista perteneciente a la lista de inventario de relevo
        this.mContext = mContext;
        this.modo_view = modo_view;
        setHasStableIds(true); // para evitar el bug de repetir views
        Log.i("app","constructor SuministrosAdapter: lista de inventario (suministros): "+suministros);
        Log.i("app","constructor SuministrosAdapter: lista de inventario (list): "+list);
    }



    @Override
    public SuministrosAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_suministro_relevo, parent, false);

        MyViewHolder vh = new MyViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        GenericObject r = list.get(position);

        Log.i("app","binding object...list: "+list);
        Log.i("app","binding object..."+r.getValues());

        holder.title.setText(r.getAsString("cantidad"));
        holder.title.addTextChangedListener(new TextWatcher(){

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //cambiar la cantidad en suministros si existe el item
                GenericObject r = list.get(position);
                if(suministros.contains(r)) {
                    if(holder.checked.isChecked()) {
                        suministros.remove(r);
                        r.set("cantidad", holder.title.getText().toString());
                        addItem(r);
                    }
                }
            }
        });
        holder.subtitle1.setText(r.getAsString("nombre"));
        holder.checked.setChecked(false);
        /*if(holder.isChecked){ // agregamos todos los items de la lista del puesto a la lista del inventario, que esten checkeados (estan checkeados todos por defecto)
            suministros.add(r);
        }*/
        if(modo_view == CustomHelper.MODO_VER){
            holder.checked.setVisibility(View.GONE);
            holder.title.setFocusable(false);
        }else
            holder.checked.setVisibility(View.VISIBLE);

        holder.checked.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                showDialog();
                GenericObject r = list.get(position);

                Log.i("app","onClick checked: lista de inventario (suministros): "+suministros);
                Log.i("app","onClick checked: lista de suministros (list): "+list);
                Log.i("app","holder.checked: "+holder.checked.isChecked());
                if(holder.checked.isChecked()){
                    r.set("cantidad",holder.title.getText().toString());
                    addItem(r);
                }else{
                    removeItem(r);
                }
                Log.i("app","onClick checked after: lista de inventario (suministros): "+suministros);
                Log.i("app","onClick checked after: lista de suministros (list): "+list);
            }


            private boolean showDialog(){
                AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                dialog.setTitle("Confirmación");
                String message = "";
                final boolean[] confirm = {false};
                if(holder.checked.isChecked()){
                    message = "Al marcar este item esta confirmando su existencia en el puesto.";
                }else{
                    message = "Al desmarcar este item esta indicando su ausencia o inexistencia en el puesto.";
                }
                dialog.setMessage(message);
                dialog.setCancelable(false);

                dialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialoginterface, int i) {
                        // accion al presionar el boton OK haciendo switch a la variable action
                       confirm[0] = true;
                    }
                });


                dialog.show();

                return confirm[0];
            }

        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private void addItem(GenericObject item){
        suministros.add(item);
    }

    private void removeItem(GenericObject item){
        suministros.remove(item);
    }

    @Override
    public long getItemId(int position) {
        return position;
    } //para evitar el bug de repetir views

    @Override
    public int getItemViewType(int position) {
        return position;
    }  //para evitar el bug de repetir views

}
