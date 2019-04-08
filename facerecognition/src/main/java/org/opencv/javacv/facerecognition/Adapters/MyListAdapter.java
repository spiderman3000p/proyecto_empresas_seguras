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
import org.opencv.javacv.facerecognition.Activities.RadioNuevo;
import org.opencv.javacv.facerecognition.Activities.RadiosActivity;
import org.opencv.javacv.facerecognition.Database.DatabaseAdmin;
import org.opencv.javacv.facerecognition.Helpers.CustomHelper;
import org.opencv.javacv.facerecognition.Models.GenericObject;
import org.opencv.javacv.facerecognition.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.MyViewHolder> implements Filterable {

    private static List<GenericObject> list;
    private static ArrayList<GenericObject> listFilter;
    private CustomFilter mFilter;
    private final WeakReference<RadiosActivity> radiosActivityWeakReference;

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    public void buscar(DateTime desde,DateTime hasta, String texto) {
        Log.i("app","MyLystAdapter.buscar(): Buscando "+ texto + "desde " + desde + " hasta " + hasta);
        mFilter.buscar(desde, hasta, texto);
        Toast.makeText(radiosActivityWeakReference.get(), listFilter.size()  + " registros encontrados", Toast.LENGTH_SHORT).show();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title,subtitle1, subtitle2;
        CardView cv;
        ImageButton opcionesBtn;
        ImageView imagen;
        View view;

        private MyViewHolder(View itemView, final WeakReference<RadiosActivity> activity) {
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
                    showPopupMenu(getAdapterPosition());

                }

                private void showPopupMenu(int pos){
                    PopupMenu menu = new PopupMenu(activity.get(),view);

                    MenuInflater inflater = menu.getMenuInflater();
                    inflater.inflate(R.menu.popup_menu,menu.getMenu());
                    menu.setOnMenuItemClickListener(new MyMenuItemClickListener(activity, pos));
                    menu.show();
                }
            });
        }
    }

    public MyListAdapter(RadiosActivity activity, List<GenericObject> l) {

        this.radiosActivityWeakReference = new WeakReference<>(activity);
        list = l;
        listFilter = new ArrayList<GenericObject>();
        listFilter.addAll(list);
        this.mFilter = new CustomFilter(MyListAdapter.this);
        setHasStableIds(true);
    }



    @Override
    public MyListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(radiosActivityWeakReference.get()).inflate(R.layout.item_radios, parent, false);

        return new MyViewHolder(v,radiosActivityWeakReference);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        GenericObject r = listFilter.get(position);
        holder.title.setText(r.getAsString("nombre_puesto"));
        holder.subtitle1.setText(r.getAsString("nombre_nominativo"));
        holder.subtitle2.setText(r.getAsString("timestamp"));

        if(r.getAsInt("responde") == 1) {
            holder.imagen.setBackgroundResource(R.drawable.boton_redondo_azul);
            holder.imagen.setImageResource(android.R.drawable.sym_action_call);
        }else{
            holder.imagen.setBackgroundResource(R.drawable.boton_redondo_rojo);
            holder.imagen.setImageResource(android.R.drawable.sym_call_outgoing);
        }
    }

    @Override
    public int getItemCount() {
        return listFilter.size();
    }

    public static class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

        public final WeakReference<RadiosActivity> activity;
        int position;

        public MyMenuItemClickListener(WeakReference<RadiosActivity> activity, int position) {

            this.activity = activity;
            this.position = position;
        }

        @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    AlertDialog.Builder dialog;

                    switch (menuItem.getItemId()) {


                        case R.id.opcion1: // ver

                            Intent intent = new Intent(activity.get(), RadioNuevo.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("id_objeto", listFilter.get(position).getAsInt("id"));
                            intent.putExtra("modo_view", CustomHelper.MODO_VER);
                            activity.get().startActivity(intent);
                            break;
                        case R.id.opcion2: // editar

                            //LLamada a editar
                            intent = new Intent(activity.get(), RadioNuevo.class);
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
                                    db.eliminar("radio",listFilter.get(position).getAsInt("id"));
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

    public  void removeItemFromFilter(int i){
        mFilter.removeItem(i);
    }

    public static class CustomFilter extends Filter {
        private MyListAdapter listAdapter;
        private FilterResults results;
        private CustomFilter(MyListAdapter listAdapter) {
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
            //limpiamos el filtro
            listFilter.clear();

            results = new FilterResults();

            //para hacer busquedas por rango de fechas y texto se usa el formato: search; [fechaDesde]; [fechaHasta]; [texto]

            //si no hay ningun texto, mostramos toda la lista
            if (constraint.length() == 0) {
                listFilter.addAll(list);
            } else {
                //sino, obtenemos el texto y filtramos
                final String filterPattern = constraint.toString().toLowerCase();
                int respPattern = 2;//indica que tipo de registros ha de mostrarse segun el tipo de resp: 0 es no respondidas y 1 es respondidas

                if(filterPattern.equals("noresp."))
                    respPattern = 0;
                if(filterPattern.equals("resp."))
                    respPattern = 1;

                for (final GenericObject radio : list) {
                    if (radio.getAsString("nombre_puesto").toLowerCase().contains(filterPattern) ||
                            radio.getAsString("nombre_nominativo").toLowerCase().contains(filterPattern) ||
                            radio.getAsString("timestamp").toLowerCase().contains(filterPattern) ||
                            radio.getAsInt("responde") == respPattern) {
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
            Log.i("app","MyLystAdapter.CustomFilter.buscar(): Buscando "+ texto + "desde " + fechaDesde + " hasta " + fechaHasta);

                //sino, obtenemos el texto y filtramos
                final String filterPattern = texto.toLowerCase();
                String fecha;
                int respPattern = 2;//indica que tipo de registros ha de mostrarse segun el tipo de resp: 0 es no respondidas y 1 es respondidas

                if(filterPattern.equals("noresp."))
                    respPattern = 0;
                if(filterPattern.equals("resp."))
                    respPattern = 1;

                for (final GenericObject radio : list) {
                    fecha = radio.getAsString("timestamp");
                    //transformamos del formato yyyy-MM-dd HH:mm:ss al formato yyyy-MM-dd
                    DateTime date = DateTime.parse(fecha.substring(0,10));


                    if (    filterPattern.isEmpty() || (radio.getAsString("nombre_puesto").toLowerCase().contains(filterPattern) ||
                            radio.getAsString("nombre_nominativo").toLowerCase().contains(filterPattern) ||
                            radio.getAsInt("responde") == respPattern) &&
                            CustomHelper.isInIntervaloFechas(fechaDesde,fechaHasta,date)){
                        Log.i("app","MyLystAdapter.buscar():  1 encontrado");
                        listFilter.add(radio);
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
