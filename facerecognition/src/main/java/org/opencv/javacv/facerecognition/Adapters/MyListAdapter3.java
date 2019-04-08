package org.opencv.javacv.facerecognition.Adapters;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.opencv.javacv.facerecognition.Activities.ControlAzucarActivity;
import org.opencv.javacv.facerecognition.Activities.ControlAzucarNuevo;
import org.opencv.javacv.facerecognition.Database.DatabaseAdmin;
import org.opencv.javacv.facerecognition.Helpers.CustomHelper;
import org.opencv.javacv.facerecognition.Models.GenericObject;
import org.opencv.javacv.facerecognition.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MyListAdapter3 extends RecyclerView.Adapter<MyListAdapter3.MyViewHolder> implements Filterable {

    private static List<GenericObject> list;
    private static ArrayList<GenericObject> listFilter;
    private CustomFilter mFilter;
    private final WeakReference<ControlAzucarActivity> controlAzucarActivityWeakReference;

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    public void buscar(DateTime desde, DateTime hasta, String texto) {
        Log.i("app","MyLystAdapter3.buscar(): Buscando "+ texto + "desde " + desde + " hasta " + hasta);
        mFilter.buscar(desde, hasta, texto);
        Toast.makeText(controlAzucarActivityWeakReference.get(), listFilter.size()  + " registros encontrados", Toast.LENGTH_SHORT).show();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title,subtitle1, subtitle2;
        CardView cv;
        ImageButton opcionesBtn;
        ImageView imagen;
        View view;

        private MyViewHolder(View itemView, final WeakReference<ControlAzucarActivity> activity) {
            super(itemView);

            cv = itemView.findViewById(R.id.cv);

            view = itemView;
            title = itemView.findViewById(R.id.title);
            subtitle1 = itemView.findViewById(R.id.subtitle1);
            subtitle2 = itemView.findViewById(R.id.subtitle2);
            imagen = itemView.findViewById(R.id.imagen);
            opcionesBtn = itemView.findViewById(R.id.opciones);

            opcionesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPopupMenu(view,getAdapterPosition());

                }

                private void showPopupMenu(View v,int pos){
                    PopupMenu menu = new PopupMenu(view.getContext(),view);

                    MenuInflater inflater = menu.getMenuInflater();
                    inflater.inflate(R.menu.popup_menu,menu.getMenu());
                    menu.setOnMenuItemClickListener(new MyMenuItemClickListener(activity,pos));
                    menu.show();
                }
            });
        }
    }

    public MyListAdapter3(ControlAzucarActivity activity, List<GenericObject> l) {

        list = l;
        listFilter = new ArrayList();
        listFilter.addAll(list);
        this.mFilter = new CustomFilter(MyListAdapter3.this);
        this.controlAzucarActivityWeakReference = new WeakReference<ControlAzucarActivity>(activity);
        setHasStableIds(true);
    }



    @Override
    public MyListAdapter3.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(controlAzucarActivityWeakReference.get()).inflate(R.layout.item_radios, parent, false);

        return new MyViewHolder(v,controlAzucarActivityWeakReference);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        GenericObject r = listFilter.get(position);
        Log.i("app",position+".r: "+r.toString());
        holder.title.setText(r.getAsString("orden_trabajo"));
        holder.subtitle1.setText(r.getAsString("comentario"));
        holder.subtitle2.setText(r.getAsString("timestamp"));
        holder.imagen.setBackgroundResource(R.drawable.boton_redondo_azul);

    }

    @Override
    public int getItemCount() {
        return listFilter.size();
    }

    public static class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

        int position;
        WeakReference<ControlAzucarActivity> activity;

        public MyMenuItemClickListener(WeakReference<ControlAzucarActivity> activity,int position) {
            this.position = position;
            this.activity = activity;
        }

        @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    AlertDialog.Builder dialog;

                    switch (menuItem.getItemId()) {


                        case R.id.opcion1: // ver

                            Intent intent = new Intent(activity.get().getApplicationContext(), ControlAzucarNuevo.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("id_objeto", listFilter.get(position).getAsInt("id"));
                            intent.putExtra("modo_view", CustomHelper.MODO_VER);
                            activity.get().startActivity(intent);
                            break;
                        case R.id.opcion2: // editar

                            //LLamada a editar
                            intent = new Intent(activity.get().getApplicationContext(), ControlAzucarNuevo.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            //
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
                                    db.eliminar("controlsalida",listFilter.get(position).getAsInt("id"));
                                    activity.get().removeItem(listFilter.get(position),position);
                                    db.close();
                                    //ControlAzucarActivity.removeItem(listFilter.get(position),position);

                                }
                            });

                            dialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialoginterface, int i) {
                                    // accion al presionar el boton cancelar

                                }
                            });

                            dialog.show();

                            break;

                        default:
                    }
                    return false;
                }

    }

    public  void removeItemFromFilter(int i){
        mFilter.removeItem(i);

    }

    public static class CustomFilter extends Filter {
        private MyListAdapter3 listAdapter;

        private CustomFilter(MyListAdapter3 listAdapter) {
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
                for (final GenericObject radio : list) {
                    if (radio.getAsString("orden_trabajo").toLowerCase().contains(filterPattern) ||
                            radio.getAsString("comentario").toLowerCase().contains(filterPattern) ||
                            radio.getAsString("timestamp").toLowerCase().contains(filterPattern)
                        ) {
                        listFilter.add(radio);
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
            Log.i("app","MyLystAdapter3.CustomFilter.buscar(): Buscando "+ texto + "desde " + fechaDesde + " hasta " + fechaHasta);

            //sino, obtenemos el texto y filtramos
            final String filterPattern = texto.toLowerCase();
            String fecha;

            for (final GenericObject control : list) {
                fecha = control.getAsString("timestamp");
                //transformamos del formato yyyy-MM-dd HH:mm:ss al formato yyyy-MM-dd
                DateTime date = DateTime.parse(fecha.substring(0,10));


                if (    filterPattern.isEmpty() || (control.getAsString("orden_trabajo").toLowerCase().contains(filterPattern) ||
                        control.getAsString("comentario").toLowerCase().contains(filterPattern)) &&
                        CustomHelper.isInIntervaloFechas(fechaDesde,fechaHasta,date)){
                    Log.i("app","MyLystAdapter.buscar():  1 encontrado");
                    listFilter.add(control);
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
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

}
