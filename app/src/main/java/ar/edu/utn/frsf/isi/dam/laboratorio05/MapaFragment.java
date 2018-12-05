package ar.edu.utn.frsf.isi.dam.laboratorio05;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.MyDatabase;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.Reclamo;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.ReclamoDao;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapaFragment extends SupportMapFragment implements
        OnMapReadyCallback {

    private static final int EVENTO_LISTA_RECLAMOS=1;
    private static final int EVENTO_BUSCAR_RECLAMOS=2;

    private GoogleMap miMapa;
    private int tipoMapa;
    private ReclamoDao reclamoDao;
    private ArrayList<Reclamo> listaReclamos= new ArrayList<Reclamo>();
    private Reclamo reclamo;


    private onMapaListener listener;

    //creo una interfaz para que la actividad pueda pasar paramentros al fragmento
    public interface onMapaListener{

        void coordenadasSeleccionadas(LatLng c);

    }

    public void setListener(onMapaListener listener) {
        this.listener = listener;
    }


    public MapaFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        tipoMapa = 0;
        Bundle argumentos = getArguments();

        if (argumentos != null) {
            tipoMapa = argumentos.getInt("tipo_mapa", 0);
        }

        getMapAsync(this);

        reclamoDao=MyDatabase.getInstance(this.getActivity()).getReclamoDao();
        return rootView;


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        //inicializo el mapa con una instacia de googleMap
        miMapa = googleMap;
        actualizarMapa();
        Bundle arg=getArguments();

        if(arg!=null) {


            switch (arg.getInt("tipo_mapa", 0)) {
                case 1:
                    googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                        @Override
                        public void onMapLongClick(LatLng latLng) {
                            listener.coordenadasSeleccionadas(latLng);
                        }
                    });
                    break;

                case 2:
                    obtenerListaDeReclamos();

            }

        }



    }


    public void actualizarMapa() {

        final String[] permiso = {Manifest.permission.ACCESS_FINE_LOCATION};

        //Pido los permisos necesarios para acceder a la localizacion, sino me los da le aviso que no puede usar la funcionalidad
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                (new AlertDialog.Builder(getContext())).setTitle(getString(R.string.solicPermisoUbic)).
                        setMessage(getString(R.string.textoSolicPermUbic)).
                        setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                ActivityCompat.requestPermissions(getActivity(), permiso, 5);

                            }
                        }).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getContext(), getString(R.string.errorUbicacion), Toast.LENGTH_LONG).show(); }
                }).create().show();
            }
            else {

                ActivityCompat.requestPermissions(getActivity(),permiso,5);
                return;
            }

        }
        // una vez que me da los permisos recien ahi le permito usar la funcion de localizacion
        miMapa.setMyLocationEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        switch(requestCode) {
            case 5:
                if(grantResults.length>0&& grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    actualizarMapa();
                else
                    Toast.makeText(getActivity(), getString(R.string.errorUbicacion), Toast.LENGTH_LONG).show();
        }
    }

    public void obtenerListaDeReclamos(){
        listaReclamos.clear();
        Runnable cargarReclamosConMarca = new Runnable() {
            @Override
            public void run() {
                listaReclamos.addAll(reclamoDao.getAll());
                Message mensaje = gestorDeEventos.obtainMessage(EVENTO_LISTA_RECLAMOS);
                mensaje.sendToTarget();
            }
        };
        Thread thread= new Thread(cargarReclamosConMarca);
        thread.start();


    }

    public void obtenerUnRelcamo(){

        Runnable cargarReclamo = new Runnable() {
            @Override
            public void run() {

                reclamo=reclamoDao.getById(getArguments().getInt("idReclamos"));
                Message mensaje = gestorDeEventos.obtainMessage(EVENTO_BUSCAR_RECLAMOS);
                mensaje.sendToTarget();

            }
        };
        Thread thread;
        thread = new Thread(cargarReclamo);
        thread.start();

    }

    Handler gestorDeEventos= new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LatLng latLng;
            CameraUpdate cu;
            switch (msg.what){
                case EVENTO_LISTA_RECLAMOS:

                ArrayList<MarkerOptions> marcadores=new ArrayList<>();

                for (Reclamo r: listaReclamos){

                    latLng= new LatLng(r.getLatitud(),r.getLongitud());

                    miMapa.addMarker(new MarkerOptions().position(latLng));
                    marcadores.add(new MarkerOptions().position(latLng));

                }
                LatLngBounds.Builder builder= new LatLngBounds.Builder();
                for(MarkerOptions markerOptions: marcadores)
                    builder.include(markerOptions.getPosition());
                LatLngBounds bounds= builder.build();
                cu= CameraUpdateFactory.newLatLngBounds(bounds, 0);
                    miMapa.moveCamera(cu);

                 break;

                case EVENTO_BUSCAR_RECLAMOS:

                    latLng=new LatLng(reclamo.getLatitud(),reclamo.getLongitud());
                    miMapa.addMarker(new MarkerOptions().position(latLng));
                    Circle radio= miMapa.addCircle(new CircleOptions().center(latLng).radius(500));
                    radio.setFillColor(Color.TRANSPARENT);
                    radio.setStrokeColor(Color.RED);
                    cu= CameraUpdateFactory.newLatLngZoom(latLng, 15.0f);
                    miMapa.moveCamera(cu);
                    break;




            }

        }
    };

}

