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
import com.google.android.gms.maps.model.PolylineOptions;
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
    //Evento que me van a decir que tipo de mapa tengo que costruir
    private static final int EVENTO_LISTA_RECLAMOS=1;
    private static final int EVENTO_BUSCAR_RECLAMOS=2;
    private static final int EVENTO_HEAT_MAP=3;
    private static final int EVENTO_BUSCAR_RECLAMOS_TIPO=4;


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

    // seteo el listener
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
        //Verifico el tipo de mapa que se crea con el argumento que me pasa la actividad que lo invoca
        if (argumentos != null) {
            //aca es donde se me pasa el tipo de mapa de que debo crear
            tipoMapa = argumentos.getInt("tipo_mapa", 0);
        }

        getMapAsync(this);
        //creo una instancia del DAO para poder traer los reclamos
        reclamoDao= MyDatabase.getInstance(this.getActivity()).getReclamoDao();
        return rootView;


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        //inicializo el mapa con una instacia de googleMap
        miMapa = googleMap;
        actualizarMapa();
        //De acuerdo al tipo de mapa se llama al metodo correspondiente para armar el maapa
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

                case 5:
                    obtenerTipoReclamo();
                    break;

            }




    }


    private void actualizarMapa() {
        //pido permisos para que el usuario pueda usar la aplicacion y que solamente pueda acceder si otorga los permisos
        if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    9999);
        }
        miMapa.setMyLocationEnabled(true);


    }


//como tengo que acceder a la base de datos todos estos metodos se van a hacer en hilos secundarios gestionados por el handler gestor de eventos
    private void obtenerListaDeReclamos(){
        listaReclamos.clear();
        Runnable cargarReclamosConMarca = new Runnable() {
            @Override
            public void run() {
                //traigo todos los reclamos
                listaReclamos.addAll(reclamoDao.getAll());
                //paso un mensaje al gesto de eventos con el evento correspondiente para saber que mapa tiene que armar
                Message mensaje = gestorDeEventos.obtainMessage(EVENTO_LISTA_RECLAMOS);
                mensaje.sendToTarget();
            }
        };
        Thread t1= new Thread(cargarReclamosConMarca);
        t1.start();


    }

    private void obtenerUnRelcamo(){

        Runnable cargarReclamo = new Runnable() {
            @Override
            public void run() {
                //traigo un reclamo en especifico por su id

                reclamo=reclamoDao.getById(getArguments().getInt("idReclamo"));
                Message mensaje = gestorDeEventos.obtainMessage(EVENTO_BUSCAR_RECLAMOS);
                mensaje.sendToTarget();

            }
        };
        Thread t2 = new Thread(cargarReclamo);
        t2.start();

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
        Thread t3= new Thread(cargarHeatMap);
        t3.start();

    }

    private void obtenerTipoReclamo(){
        listaReclamos.clear();

        Runnable cargarReclamosTipo = new Runnable() {
            @Override
            public void run() {
                //traigo los reclamos por el tipo
                listaReclamos.addAll(reclamoDao.getByTipo(getArguments().getString("tipo_reclamo")));
                Message completeMesssage;
                completeMesssage=gestorDeEventos.obtainMessage(EVENTO_BUSCAR_RECLAMOS_TIPO);
                completeMesssage.sendToTarget();
            }
        };
        Thread t4= new Thread(cargarReclamosTipo);
        t4.start();

    }
    //creo un Handler que va servir para gestionar el tipo de mapa que tengo creaer de acuerdo al evento que se le pasa
    Handler gestorDeEventos= new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //objeto de tipo LatLng para poder ubicar las marcas
            LatLng latLng;
            //objeto para manipular la posicion de la camara
            CameraUpdate camara;
            switch (msg.what){
                case EVENTO_LISTA_RECLAMOS:
                    //Array list para añadir marcadores al mapa
                ArrayList<MarkerOptions> marcadores=new ArrayList<>();
                //recorrer la lista de reclamos
                for (Reclamo r: listaReclamos){
                    //obtener la latitud y longitud de cada reclamos para setearla al objeto
                    latLng= new LatLng(r.getLatitud(),r.getLongitud());
                    //agrego la marca al mapa y al array de marcas
                    miMapa.addMarker(new MarkerOptions().position(latLng));
                    marcadores.add(new MarkerOptions().position(latLng));

                }
                //LatLngBounds me permite definir los limites de la pantalla(marco) para que el nivel de zoom sea correcto y se puedan vilualizar todas las marcas
                LatLngBounds.Builder marco= new LatLngBounds.Builder();
                for(MarkerOptions markerOptions: marcadores)
                    marco.include(markerOptions.getPosition());

                //Seteo a la camara el marco con sus limites correspondientes con el marco
                camara= CameraUpdateFactory.newLatLngBounds(marco.build(), 50);//nivel de limites para que se vena toas las marcas
                    miMapa.moveCamera(camara);

                 break;

                case EVENTO_BUSCAR_RECLAMOS:

                    latLng=new LatLng(reclamo.getLatitud(),reclamo.getLongitud());
                    miMapa.addMarker(new MarkerOptions().position(latLng));
                    //Aca creo la circunferencia al rededor de la marca seleccionada y seteo valores de radio correspondites y colores
                    Circle radio= miMapa.addCircle(new CircleOptions().center(latLng).radius(500));
                    radio.setFillColor(Color.TRANSPARENT);
                    radio.setStrokeColor(Color.RED);
                    //Elijo el nivel de zoom adecuado para que pueda visualizar
                    camara= CameraUpdateFactory.newLatLngZoom(latLng, 15.0f);
                    miMapa.moveCamera(camara);
                    break;


                case EVENTO_HEAT_MAP:
                    ArrayList<LatLng>coordenadas = new ArrayList<>();
                    LatLngBounds.Builder marco2= new LatLngBounds.Builder();
                    for(Reclamo r: listaReclamos){
                        latLng= new LatLng(r.getLatitud(), r.getLongitud());
                        coordenadas.add(latLng);
                        marco2.include(latLng);
                    }
                    //Creo un el mapa de calor y le seteo las coordenadas
                    TileProvider mapaCalor = new HeatmapTileProvider.Builder().data(coordenadas)
                            .build();
                    miMapa.addTileOverlay(new TileOverlayOptions().tileProvider(mapaCalor));
                    camara= CameraUpdateFactory.newLatLngBounds(marco2.build(), 50);
                    miMapa.moveCamera(camara);
                    break;

                case EVENTO_BUSCAR_RECLAMOS_TIPO:
                    ArrayList<LatLng> coordenadasReclamo= new ArrayList<>();
                    LatLngBounds.Builder marco3= new LatLngBounds.Builder();
                    PolylineOptions polylineOptions= new PolylineOptions();
                    for(Reclamo r: listaReclamos){
                        latLng= new LatLng(r.getLatitud(), r.getLongitud());
                        miMapa.addMarker(new MarkerOptions().position(latLng));
                        coordenadasReclamo.add(latLng);
                        marco3.include(latLng);
                        polylineOptions.add(latLng);
                    }
                    miMapa.addPolyline(polylineOptions.width(5).color(Color.RED));
                    camara= CameraUpdateFactory.newLatLngBounds(marco3.build(), 50);
                    miMapa.moveCamera(camara);
                    break;








            }

        }
    };

}

