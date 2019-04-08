package org.opencv.javacv.facerecognition.Adapters;

import android.animation.ValueAnimator;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.Lists;

import org.joda.time.DateTime;
import org.opencv.javacv.facerecognition.Activities.RelevoActivity;
import org.opencv.javacv.facerecognition.Activities.RelevoNuevo;
import org.opencv.javacv.facerecognition.Database.DatabaseAdmin;
import org.opencv.javacv.facerecognition.Helpers.CustomHelper;
import org.opencv.javacv.facerecognition.Models.GenericObject;
import org.opencv.javacv.facerecognition.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;


public class MyListAdapter5 extends RecyclerView.Adapter<MyListAdapter5.MyViewHolder> implements Filterable {

    private static List<GenericObject> list;
    private ContentValues userData;
    private static ArrayList<GenericObject> listFilter;
    private CustomFilter mFilter;
    private final WeakReference<RelevoActivity> relevoActivityWeakReference;

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    public void buscar(DateTime desde, DateTime hasta, String texto) {
        Log.i("app","MyLystAdapter5.buscar(): Buscando "+ texto + "desde " + desde + " hasta " + hasta);
        mFilter.buscar(desde, hasta, texto);
        Toast.makeText(relevoActivityWeakReference.get(), listFilter.size()  + " registros encontrados", Toast.LENGTH_SHORT).show();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title,subtitle1, subtitle2,subtitle3;
        CardView cv;
        ArrayList<GenericObject> suministros2;
        RecyclerView listSuministros;
        LinearLayoutManager llm;
        SuministrosAdapter suministrosAdapter;
        ImageButton opcionesBtn,btnExpand;
        ImageView imagen;
        View view;
        int minHeight;
        ContentValues userData;

        private MyViewHolder(final WeakReference<RelevoActivity> activity,View itemView, ContentValues u) {
            super(itemView);
            cv = itemView.findViewById(R.id.cv);
            view = itemView;
            userData = u;
            minHeight = 0;
            title = itemView.findViewById(R.id.title);
            subtitle1 = itemView.findViewById(R.id.subtitle1);
            subtitle2 = itemView.findViewById(R.id.subtitle2);
            subtitle3 = itemView.findViewById(R.id.subtitle3);
            imagen = itemView.findViewById(R.id.imagen);
            opcionesBtn = itemView.findViewById(R.id.opciones);
            btnExpand = itemView.findViewById(R.id.btnExpand);
            listSuministros = (RecyclerView) itemView.findViewById(R.id.listComments);
            listSuministros.setHasFixedSize(true);
            llm = new LinearLayoutManager(activity.get(),LinearLayoutManager.VERTICAL,false);
            listSuministros.setLayoutManager(llm);
            suministros2 = Lists.newArrayList();
            DatabaseAdmin db = new DatabaseAdmin(activity.get());
            ArrayList<GenericObject> todosSuministros = new ArrayList<GenericObject>();//el inventario del puesto
            SharedPreferences preferences = activity.get().getSharedPreferences("pref",MODE_PRIVATE);
            int idPuesto = preferences.getInt("id_puesto",0);
            todosSuministros = db.obtenerSuministrosByPuesto(idPuesto);
            db.close();
            suministrosAdapter = new SuministrosAdapter(activity.get(), todosSuministros, suministros2, CustomHelper.MODO_VER);
            listSuministros.setAdapter(suministrosAdapter);


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

            opcionesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPopupMenu(getAdapterPosition());

                }

                private void showPopupMenu(int pos){
                    PopupMenu menu = new PopupMenu(activity.get(),view);

                    MenuInflater inflater = menu.getMenuInflater();
                    inflater.inflate(R.menu.popup_menu,menu.getMenu());
                    menu.setOnMenuItemClickListener(new MyListAdapter5.MyMenuItemClickListener(activity, pos));
                    menu.show();
                }
            });
        }

        private void toggleCardViewnHeight(int height) {

            if (cv.getHeight() == minHeight) {
                // expand
                expandView(height); //'height' is the height of screen which we have measured already.

            } else {
                //VACIAMOS LA LISTA DE suministros
                suministros2.clear();
                suministrosAdapter.notifyDataSetChanged();
                listSuministros.setVisibility(View.GONE);
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
    }

    public MyListAdapter5(RelevoActivity activity, List<GenericObject> l, ContentValues u) {
        list = l;
        listFilter = new ArrayList<>();
        listFilter.addAll(list);
        mFilter = new CustomFilter(MyListAdapter5.this);
        userData = u;
        setHasStableIds(true); // para evitar el bug de repetir views
        this.relevoActivityWeakReference = new WeakReference<RelevoActivity>(activity);
    }



    @Override
    public MyListAdapter5.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(relevoActivityWeakReference.get()).inflate(R.layout.item_radios, parent, false);

        return new MyViewHolder(relevoActivityWeakReference,v,userData);
    }


    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final GenericObject object = listFilter.get(position);

        Log.i("app","Binding object: "+object.getValues());
        String nombre_entrante = object.getAsString("nombre_usuario_entrante").toUpperCase();
        String nombre_saliente = object.getAsString("nombre_usuario_saliente").toUpperCase();
        holder.title.setText(object.getAsString("nominativo_puesto"));
        holder.subtitle1.setText("Entrante: "+nombre_entrante);
        holder.subtitle2.setText("Saliente: "+nombre_saliente);
        holder.subtitle3.setText(object.getAsString("timestamp"));
        holder.subtitle3.setVisibility(View.VISIBLE);

        if(object.getAsInt("num_suministros") > 0) {
            holder.btnExpand.setVisibility(View.VISIBLE);
        }
            holder.btnExpand.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    //recuperar suministros desde la BD, insertarlos en el CV y luego expandirlo
                    DatabaseAdmin db = new DatabaseAdmin(relevoActivityWeakReference.get());
                    holder.suministros2.clear();
                    holder.suministros2.addAll(db.obtenerInventarioRelevo(object.getAsInt("id")));
                    Log.i("app", "num de suministros: " + holder.suministros2.size());
                    Log.i("app", "suministros: " + holder.suministros2);
                    if (holder.suministros2.size() > 0) {
                        //crear items para el listview

                        holder.suministrosAdapter.notifyDataSetChanged();
                        holder.listSuministros.setVisibility(View.VISIBLE);
                        //establecemos un tamaño maximo del cv expandido, por ejemplo: 400dp
                        holder.toggleCardViewnHeight(holder.cv.getHeight() + (holder.suministros2.size() * 90));//expandir cardview
                    } else {
                        Log.i("app", "no hay suministros");
                    }
                    db.close();

                }
            });

    }

    public static class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

        public final WeakReference<RelevoActivity> activity;
        int position;

        public MyMenuItemClickListener(WeakReference<RelevoActivity> activity, int position) {

            this.activity = activity;
            this.position = position;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            AlertDialog.Builder dialog;

            switch (item.getItemId()) {


                case R.id.opcion1: // ver

                    Intent intent = new Intent(activity.get(), RelevoNuevo.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("id_objeto", listFilter.get(position).getAsInt("id"));
                    intent.putExtra("modo_view", CustomHelper.MODO_VER);
                    activity.get().startActivity(intent);
                    break;
                case R.id.opcion2: // editar

                    //LLamada a editar
                    intent = new Intent(activity.get().getApplicationContext(), RelevoNuevo.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("id_objeto", listFilter.get(position).getAsInt("id"));
                    intent.putExtra("modo_view", CustomHelper.MODO_EDITAR);
                    activity.get().startActivity(intent);
                    break;

                case R.id.opcion3: //Eliminar
                    dialog = new AlertDialog.Builder(activity.get());
                    dialog.setTitle("Eliminar");
                    dialog.setMessage("¿Está seguro de eliminar este item?");
                    dialog.setCancelable(false);

                    dialog.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialoginterface, int i) {
                            // accion al presionar el boton OK haciendo switch a la variable action
                            //Log.i("app","clic! Eliminar item position: "+position);
                            DatabaseAdmin db = new DatabaseAdmin(activity.get());
                            db.eliminar("relevo",listFilter.get(position).getAsInt("id"));
                            db.close();
                            activity.get().removeItem(listFilter.get(position),position);

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

                default:
            }
            return false;
        }

    }


    @Override
    public int getItemCount() {
        return listFilter.size();
    }

    public  void removeItemFromFilter(int i){
        mFilter.removeItem(i);

    }

    public static class CustomFilter extends Filter {
        private MyListAdapter5 listAdapter;

        private CustomFilter(MyListAdapter5 listAdapter) {
            super();
            this.listAdapter = listAdapter;
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
                    if (    b.getAsString("nominativo_puesto").toLowerCase().contains(filterPattern) ||
                            b.getAsString("nombre_usuario_saliente").toLowerCase().contains(filterPattern) ||
                            b.getAsString("timestamp").toLowerCase().contains(filterPattern) ||
                            b.getAsString("nombre_usuario_entrante").toLowerCase().contains(filterPattern)) {

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
            Log.i("app","MyLystAdapter5.CustomFilter.buscar(): Buscando "+ texto + "desde " + fechaDesde + " hasta " + fechaHasta);

            //sino, obtenemos el texto y filtramos
            final String filterPattern = texto.toLowerCase();
            String fecha;

            for (final GenericObject relevo : list) {
                fecha = relevo.getAsString("timestamp");
                //transformamos del formato yyyy-MM-dd HH:mm:ss al formato yyyy-MM-dd
                DateTime date = DateTime.parse(fecha.substring(0,10));


                if (    filterPattern.isEmpty() || (relevo.getAsString("nominativo_puesto").toLowerCase().contains(filterPattern) ||
                        relevo.getAsString("nombre_usuario_saliente").toLowerCase().contains(filterPattern) ||
                        relevo.getAsString("nombre_usuario_entrante").toLowerCase().contains(filterPattern)) &&
                        CustomHelper.isInIntervaloFechas(fechaDesde,fechaHasta,date)){
                    Log.i("app","MyLystAdapter5.buscar():  1 encontrado");
                    listFilter.add(relevo);
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

    @Override
    public long getItemId(int position) {
        return position;
    } //para evitar el bug de repetir views

    @Override
    public int getItemViewType(int position) {
        return position;
    }  //para evitar el bug de repetir views

}