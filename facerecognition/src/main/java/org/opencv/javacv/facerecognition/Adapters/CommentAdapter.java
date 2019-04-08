package org.opencv.javacv.facerecognition.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Strings;

import org.opencv.javacv.facerecognition.Database.DatabaseAdmin;
import org.opencv.javacv.facerecognition.Helpers.CustomHelper;
import org.opencv.javacv.facerecognition.Models.GenericObject;
import org.opencv.javacv.facerecognition.R;

import java.util.ArrayList;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.MyViewHolder>  {

    private List<GenericObject> list;
    private Context mContext;
    int modo_view = 0;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title,subtitle1, subtitle2;
        CardView cv;
        Context context;
        ImageButton opcionesBtn;
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
            opcionesBtn = itemView.findViewById(R.id.opciones);
        }


    }

    public CommentAdapter(Context mContext, ArrayList<GenericObject> l,int modo_view) {
        this.list = l;
        Log.i("app","Comentarios recibidos: "+l);
        this.mContext = mContext;
        this.modo_view = modo_view;
    }



    @Override
    public CommentAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comentario, parent, false);

        MyViewHolder vh = new MyViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        GenericObject r = list.get(position);
        String nombre = "";
        String apellido = "";
        if(r.getAsString("nombre_usuario") != null)
            nombre = r.getAsString("nombre_usuario").toUpperCase();
        if(r.getAsString("apellido_usuario") != null)
            apellido = r.getAsString("apellido_usuario").toUpperCase();

        holder.title.setText((!Strings.isNullOrEmpty(nombre)?nombre:"")+" "+(!Strings.isNullOrEmpty(apellido)?apellido:""));
        holder.subtitle1.setText(r.getAsString("comentario"));
        holder.subtitle2.setText(r.getAsString("datetime"));
        if(modo_view != CustomHelper.MODO_EDITAR)
            holder.opcionesBtn.setVisibility(View.GONE);
        list.get(position).set("cv_h",holder.cv.getHeight());
        holder.opcionesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: eliminar el item preguntando antes al usuario
                showDialog(view);
            }

            private boolean showDialog(View v){
                AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                dialog.setTitle("Eliminar");
                dialog.setMessage("¿Está seguro de eliminar este item?");
                dialog.setCancelable(false);

                dialog.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialoginterface, int i) {
                        // accion al presionar el boton OK haciendo switch a la variable action
                        GenericObject object = new GenericObject();
                        object = list.get(position);
                        DatabaseAdmin db = new DatabaseAdmin(mContext);
                        if(db.eliminar("comentario_bitacora",object.getAsInt("id"))) {
                            list.remove(object);
                            notifyItemRemoved(position);
                            notifyDataSetChanged();
                            Toast.makeText(mContext, "Comentario eliminado", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(mContext, "Comentario no se pudo eliminar", Toast.LENGTH_SHORT).show();

                        }
                        db.close();
                        //Bitacora2Activity.removeItem(listFilter.get(position),position);
                    }
                });

                dialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialoginterface, int i) {
                        // accion al presionar el boton cancelar

                    }

                });

                dialog.show();

                return true;
            }

        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    void removeItem(int i){
         list.remove(i);
         notifyItemRemoved(i);
         notifyDataSetChanged();
    }

    int getItemHeight(int position){
        return list.get(position).getAsInt("cv_h");
    }


}
