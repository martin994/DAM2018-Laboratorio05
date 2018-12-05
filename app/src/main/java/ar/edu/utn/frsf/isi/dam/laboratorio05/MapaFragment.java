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
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.util.ArrayList;
import java.util.Objects;

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
    private static final int EVENTO_HEAT_MAP=3;


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

        reclamoDao= MyDatabase.getInstance(this.getActivity()).getReclamoDao();
        return rootView;


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        //inicializo el mapa con una instacia de googleMap
        miMapa = googleMap;
        actualizarMapa();
            switch (tipoMapa) {
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
                    break;
                case 3:
                    obtenerUnRelcamo();
                    break;
                case 4:
                    obtenerHetaMap();

                    break;

            }




    }


    private void actualizarMapa() {

        if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    9999);
        }
        miMapa.setMyLocationEnabled(true);
        // una vez que me da los permisos recien ahi le permito usar la funcion de localizacion

    }



    private void obtenerListaDeReclamos(){
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

    private void obtenerUnRelcamo(){

        Runnable cargarReclamo = new Runnable() {
            @Override
            public void run() {

                reclamo=reclamoDao.getById(getArguments().getInt("idReclamo"));
                Message mensaje = gestorDeEventos.obtainMessage(EVENTO_BUSCAR_RECLAMOS);
                mensaje.sendToTarget();

            }
        };
        Thread thread;
        thread = new Thread(cargarReclamo);
        thread.start();

    }

    private void obtenerHetaMap(){
        listaReclamos.clear();

        Runnable cargarHeatMap= new Runnable() {
            @Override
            public void run() {
                listaReclamos.addAll(reclamoDao.getAll());
                Message completeMessage;
                completeMessage= gestorDeEventos.obtainMessage(EVENTO_HEAT_MAP);
                completeMessage.sendToTarget();

            }
        };
        Thread thread= new Thread(cargarHeatMap);
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


                case EVENTO_HEAT_MAP:
                    ArrayList<LatLng>coordenadas = new ArrayList<>();
                    LatLngBounds.Builder builder1= new LatLngBounds.Builder();
                    for(int i=0; listaReclamos.size()>i; i++){
                        latLng= new LatLng(listaReclamos.get(i).getLatitud(),
                                listaReclamos.get(i).getLongitud());
                        coordenadas.add(latLng);
                        builder1.include(latLng);
                    }
                    TileProvider heatMapTileProvider = new HeatmapTileProvider.Builder().data(coordenadas)
                            .build();
                    miMapa.addTileOverlay(new TileOverlayOptions().tileProvider(heatMapTileProvider));
                    LatLngBounds latLngBounds= builder1.build();
                    cu= CameraUpdateFactory.newLatLngBounds(latLngBounds, 0);
                    miMapa.moveCamera(cu);
                    break;




            }

        }
    };

}

