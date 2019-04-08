package org.opencv.javacv.facerecognition.Adapters;

import android.animation.ValueAnimator;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Strings;

import org.joda.time.DateTime;
import org.opencv.javacv.facerecognition.Activities.Bitacora2Activity;
import org.opencv.javacv.facerecognition.Activities.BitacoraNuevo;
import org.opencv.javacv.facerecognition.Database.DatabaseAdmin;
import org.opencv.javacv.facerecognition.Helpers.CustomHelper;
import org.opencv.javacv.facerecognition.Models.GenericObject;
import org.opencv.javacv.facerecognition.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class MyListAdapter2 extends RecyclerView.Adapter<MyListAdapter2.MyViewHolder> implements Filterable {

    private static List<GenericObject> list;
    private ContentValues userData;
    private static ArrayList<GenericObject> listFilter;
    private CustomFilter mFilter;
    private final WeakReference<Bitacora2Activity> bitacoraActivityWeakReference;

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    public void buscar(DateTime desde, DateTime hasta, String texto) {
        Log.i("app","MyLystAdapter.buscar(): Buscando "+ texto + "desde " + desde + " hasta " + hasta);
        mFilter.buscar(desde, hasta, texto);
        Toast.makeText(bitacoraActivityWeakReference.get(), listFilter.size()  + " registros encontrados", Toast.LENGTH_SHORT).show();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title,subtitle1, subtitle2;
        CardView cv;
        ArrayList<GenericObject> comentarios;
        RecyclerView listCommets;
        CommentAdapter commentAdapter;
        LinearLayoutManager llm;
        Context context;
        ImageButton opcionesBtn,btnExpand;
        ImageView imagen;
        View view;
        int minHeight;
        ContentValues userData;

        private MyViewHolder(View itemView, ContentValues u) {
            super(itemView);
            context = itemView.getContext();
            cv = itemView.findViewById(R.id.cv);
            view = itemView;
            userData = u;
            minHeight = 0;
            title = itemView.findViewById(R.id.title);
            subtitle1 = itemView.findViewById(R.id.subtitle1);
            subtitle2 = itemView.findViewById(R.id.subtitle2);
            imagen = itemView.findViewById(R.id.imagen);
            opcionesBtn = itemView.findViewById(R.id.opciones);
            btnExpand = itemView.findViewById(R.id.btnExpand);
            listCommets = itemView.findViewById(R.id.listComments);
            listCommets.setHasFixedSize(true);
            llm = new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false);
            listCommets.setLayoutManager(llm);
            comentarios = new ArrayList<>();
            commentAdapter = new CommentAdapter(context, comentarios, CustomHelper.MODO_VER);
            listCommets.setAdapter(commentAdapter);

            btnExpand.setVisibility(View.GONE);

            cv.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

                @Override
                public boolean onPreDraw() {
                    cv.getViewTreeObserver().removeOnPreDrawListener(this);
                    minHeight = cv.getHeight();
                    ViewGroup.LayoutParams layoutParams = cv.getLayoutParams();
                    layoutParams.height = minHeight;
                    cv.setLayoutParams(layoutParams);

                    return true;
                }
            });
        }
        public void dialogComentario(final View v, final GenericObject object, final int indexPadre){
            LayoutInflater layoutInflater = LayoutInflater.from(v.getContext());
            View promptView = layoutInflater.inflate(R.layout.add_comentario, null);
            final EditText input = promptView.findViewById(R.id.etComentarioAddBitacora);

            android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(v.getContext());
            alertDialogBuilder.setView(promptView);
            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ContentValues comentario = new ContentValues();

                            comentario.put("comentario",input.getText().toString());
                            comentario.put("datetime",CustomHelper.getDateTimeString());
                            comentario.put("bitacora_id",object.getAsInt("id"));
                            comentario.put("usuario_id",userData.getAsInteger("id"));
                            int linesNumber = input.getLineCount();
                            int lineHeight = input.getLineHeight();
                            int comentHeight = linesNumber * lineHeight;
                            Log.i("app","Comentario->lineas: "+linesNumber+" alto linea: "+lineHeight+" alto comentario: "+comentHeight);
                            //comentario.put("puesto_id",userData.getAsInteger("puesto_id"));
                            Log.i("app","comentario a insertar: "+comentario.toString());
                            DatabaseAdmin db = new DatabaseAdmin(v.getContext());

                            if(db.registrar("comentario_bitacora",comentario) > 0) {
                                Toast.makeText(v.getContext(),"Comentario ingresada correctamente!", Toast.LENGTH_SHORT).show();
                                comentario.put("nombre_usuario",userData.getAsString("nombre"));
                                comentario.put("apellido_usuario",userData.getAsInteger("apellido"));
                                addComentario(comentario,comentHeight,indexPadre);
                                listCommets.setVisibility(View.VISIBLE);
                            }else{
                                Toast.makeText(v.getContext(),"Comentario no ingresada!", Toast.LENGTH_SHORT).show();
                            }
                            db.close();
                        }
                    })
                    .setNegativeButton("Cancelar",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
            android.app.AlertDialog alertD = alertDialogBuilder.create();
            alertD.show();

        }

        private void toggleCardViewnHeight(int height) {

            if (cv.getHeight() == minHeight) {
                // expand
                expandView(height); //'height' is the height of screen which we have measured already.

            } else {
                //VACIAMOS LA LISTA DE COMENTARIOS
                comentarios.clear();
                commentAdapter.notifyDataSetChanged();
                listCommets.setVisibility(View.GONE);
                // collapse
                collapseView();

            }
        }

        public void collapseView() {

            ValueAnimator anim = ValueAnimator.ofInt(cv.getMeasuredHeightAndState(),
                    minHeight);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int val = (Integer) valueAnimator.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = cv.getLayoutParams();
                    layoutParams.height = val;
                    cv.setLayoutParams(layoutParams);

                }
            });
            anim.start();
        }
        public void expandView(int height) {

            ValueAnimator anim = ValueAnimator.ofInt(cv.getMeasuredHeightAndState(),
                    height);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int val = (Integer) valueAnimator.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = cv.getLayoutParams();
                    layoutParams.height = val;
                    cv.setLayoutParams(layoutParams);
                }
            });
            anim.start();

        }
        public void addComentario(ContentValues r,int alto,int indexPadre){
            Log.i("app","agregando comentario: "+r.toString());
            //TODO: aqui se toma solo el campo comentario(String) del objeto comentario, pero deberia ser todo completo, para mostrar mas detalles en la lista de comentarios
            GenericObject object = new GenericObject();
            object.set("comentario",r.getAsString("comentario"));
            object.set("datetime",r.getAsString("datetime"));
            object.set("nombre_usuario",r.getAsString("nombre_usuario"));
            object.set("apellido_usuario",r.getAsString("apellido_usuario"));
            int height = minHeight+listCommets.getHeight();
            Log.i("app","pre comment listH: "+listCommets.getHeight()+" cvH: "+cv.getHeight()+" minHeight: "+minHeight+" newH: "+height);
            listCommets.setVisibility(View.VISIBLE);
            comentarios.add(object);
            commentAdapter = new CommentAdapter(context, comentarios, CustomHelper.MODO_VER);
            listCommets.setAdapter(commentAdapter);
            commentAdapter.notifyDataSetChanged();
            height = minHeight+listCommets.getHeight();
            Log.i("app","post comment-> listH: "+listCommets.getHeight()+" cvH: "+cv.getHeight()+" minHeight: "+minHeight+" newH: "+height);
            expandView(cv.getHeight()+alto+80);

        }

           }

    public MyListAdapter2(Bitacora2Activity activity, List<GenericObject> l, ContentValues u) {
        list = l;
        listFilter = new ArrayList<>();
        listFilter.addAll(list);
        mFilter = new CustomFilter(activity,MyListAdapter2.this);
        userData = u;
        setHasStableIds(true);
         this.bitacoraActivityWeakReference = new WeakReference<>(activity);
    }



    @Override
    public MyListAdapter2.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(getContext().get()).inflate(R.layout.item_radios, parent, false);

        return new MyViewHolder(v,userData);
    }


    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final GenericObject object = listFilter.get(position);

        Log.i("app","object: "+object.toString());
        String nombre = object.getAsString("nombre_usuario").toUpperCase();
        String apellido = object.getAsString("apellido_usuario");
        holder.title.setText((!Strings.isNullOrEmpty(nombre)?nombre:"")+" "+(!Strings.isNullOrEmpty(apellido)?apellido:""));
        String substring = object.getAsString("observacion");
        holder.subtitle1.setText(substring);
        holder.subtitle2.setText(object.getAsString("timestamp"));

        holder.imagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //aqui iniciar un activity para un nuevo registro en la tabla comentarios
                showDialog(view,holder.getAdapterPosition());
            }
            public void showDialog(View v,int pos){
                holder.dialogComentario(v,object,pos);
            }
        });
        if(object.getAsInt("num_comentarios") > 0) {
            //holder.btnExpand.setVisibility(View.VISIBLE);
            holder.listCommets.setVisibility(View.VISIBLE);
            DatabaseAdmin db = new DatabaseAdmin(bitacoraActivityWeakReference.get());
            holder.comentarios.clear();

            holder.comentarios.addAll(db.obtenerComentariosBitacora2(object.getAsInt("id")));

            Log.i("app", "num de comentarios: " + holder.comentarios.size());
            if (holder.comentarios.size() > 0) {
                //crear items para el listview

                Log.i("app", "comentarios: " + holder.comentarios.toString());
                //holder.listCommets.setAdapter(holder.commentAdapter);
                holder.commentAdapter.notifyDataSetChanged();
                holder.listCommets.setVisibility(View.VISIBLE);
            } else {
                Log.i("app", "no hay comentarios");
            }
            db.close();
        }
            holder.btnExpand.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    //recuperar comentarios desde la BD, insertarlos en el CV y luego expandirlo
                    DatabaseAdmin db = new DatabaseAdmin(bitacoraActivityWeakReference.get());
                    holder.comentarios.clear();
                    holder.comentarios.addAll(db.obtenerComentariosBitacora2(object.getAsInt("id")));
                    Log.i("app", "num de comentarios: " + holder.comentarios.size());
                    if (holder.comentarios.size() > 0) {
                        //crear items para el listview

                        Log.i("app", "comentarios: " + holder.comentarios.toString());
                        //holder.listCommets.setAdapter(holder.commentAdapter);
                        holder.commentAdapter.notifyDataSetChanged();
                        holder.listCommets.setVisibility(View.VISIBLE);
                        //establecemos un tamaño maximo del cv expandido, por ejemplo: 400dp
                        holder.toggleCardViewnHeight(holder.cv.getHeight() + (holder.comentarios.size() * 60));//expandir cardview
                    } else {
                        Log.i("app", "no hay comentarios");
                    }
                db.close();
                }
            });

        holder.imagen.setImageResource(android.R.drawable.stat_notify_chat);
        if(object.getAsInt("tipo") == 0) {
            holder.imagen.setBackgroundResource(R.drawable.boton_redondo_azul);

        }else{
            holder.imagen.setBackgroundResource(R.drawable.boton_redondo_rojo);
        }
        holder.opcionesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(view,position);

            }

            private void showPopupMenu(final View v, int pos){
                PopupMenu menu = new PopupMenu(bitacoraActivityWeakReference.get(),v);

                MenuInflater inflater = menu.getMenuInflater();
                inflater.inflate(R.menu.popup_menu,menu.getMenu());
                final AlertDialog.Builder[] dialog = new AlertDialog.Builder[1];
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {


                            case R.id.opcion1: // ver

                                Intent intent = new Intent(v.getContext(), BitacoraNuevo.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("id_objeto", object.getAsInt("id"));
                                intent.putExtra("modo_view", CustomHelper.MODO_VER);
                                bitacoraActivityWeakReference.get().startActivity(intent);
                                break;
                            case R.id.opcion2: // editar

                                //LLamada a editar
                                intent = new Intent(bitacoraActivityWeakReference.get().getApplicationContext(), BitacoraNuevo.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("id_objeto", object.getAsInt("id"));
                                intent.putExtra("modo_view", CustomHelper.MODO_EDITAR);
                                bitacoraActivityWeakReference.get().startActivity(intent);
                                break;

                            case R.id.opcion3: //Eliminar
                                dialog[0] = new AlertDialog.Builder(bitacoraActivityWeakReference.get());
                                dialog[0].setTitle("Eliminar");
                                dialog[0].setMessage("¿Está seguro de eliminar este item?");
                                dialog[0].setCancelable(false);

                                dialog[0].setPositiveButton("SI", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialoginterface, int i) {
                                        // accion al presionar el boton OK haciendo switch a la variable action

                                        DatabaseAdmin db = new DatabaseAdmin(bitacoraActivityWeakReference.get());
                                        db.eliminar("bitacora",object.getAsInt("id"));
                                        listFilter.remove(object);
                                        list.remove(object);
                                        notifyItemRemoved(position);
                                        notifyDataSetChanged();
                                        //Bitacora2Activity.removeItem(listFilter.get(position),position);
                                        db.close();

                                    }
                                });

                                dialog[0].setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialoginterface, int i) {
                                        // accion al presionar el boton cancelar

                                    }
                                });

                                dialog[0].show();
                                break;
                            default:
                        }
                        return false;
                    }
                });


                menu.show();
            }
        });
    }



    @Override
    public int getItemCount() {
        return listFilter.size();
    }

    public  void removeItemFromFilter(int i){
        mFilter.removeItem(i);

    }

    public static class CustomFilter extends Filter {
        private MyListAdapter2 listAdapter;
        private WeakReference<Bitacora2Activity> activity;

        private CustomFilter(Bitacora2Activity activity,MyListAdapter2 listAdapter) {
            super();
            this.listAdapter = listAdapter;
            this.activity = new WeakReference<Bitacora2Activity>(activity);
        }

        void removeItem(int i){
            listFilter.remove(i);
            listAdapter.notifyItemRemoved(i);
            listAdapter.notifyDataSetChanged();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            listFilter.clear();
            final FilterResults results = new FilterResults();

            if (constraint.length() == 0) {
                listFilter.addAll(list);

            } else {

                final String filterPattern = constraint.toString().toLowerCase();
                for (final GenericObject b : list) {
                    if (    b.getAsString("nombre_usuario").toLowerCase().contains(filterPattern) ||
                            b.getAsString("apellido_usuario").toLowerCase().contains(filterPattern) ||
                            b.getAsString("timestamp").toLowerCase().contains(filterPattern) ||
                            b.getAsString("observacion").toLowerCase().contains(filterPattern)) {

                        listFilter.add(b);

                    }
                }
            }
            results.values = listFilter;
            results.count = listFilter.size();
            return results;
        }

        public void buscar(DateTime fechaDesde, DateTime fechaHasta, String texto) {
            //limpiamos el filtro
            listFilter.clear();
            final FilterResults results = new FilterResults();
            //para hacer busquedas por rango de fechas y texto se usa el formato: search; [fechaDesde]; [fechaHasta]; [texto]
            Log.i("app","MyLystAdapter.CustomFilter.buscar(): Buscando "+ texto + "desde " + fechaDesde + " hasta " + fechaHasta);

            //sino, obtenemos el texto y filtramos
            final String filterPattern = texto.toLowerCase();
            String fecha;

            for (final GenericObject bitacora : list) {
                fecha = bitacora.getAsString("timestamp");
                //transformamos del formato yyyy-MM-dd HH:mm:ss al formato yyyy-MM-dd
                DateTime date = DateTime.parse(fecha.substring(0,10));


                if (filterPattern.isEmpty() || (bitacora.getAsString("nombre_usuario").toLowerCase().contains(filterPattern) ||
                    bitacora.getAsString("apellido_usuario").toLowerCase().contains(filterPattern) ||
                    bitacora.getAsString("observacion").toLowerCase().contains(filterPattern) || CustomHelper.existsInComments(activity.get(),bitacora.getAsInt("id"),filterPattern)) &&
                    CustomHelper.isInIntervaloFechas(fechaDesde,fechaHasta,date)){
                    Log.i("app","MyLystAdapter.buscar():  1 encontrado");
                    listFilter.add(bitacora);
                }
            }

            results.values = listFilter;
            results.count = listFilter.size();
            publishResults("",results);
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
            this.listAdapter.notifyDataSetChanged();
    }

    }

    private WeakReference<Bitacora2Activity> getContext() {
        return bitacoraActivityWeakReference;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
