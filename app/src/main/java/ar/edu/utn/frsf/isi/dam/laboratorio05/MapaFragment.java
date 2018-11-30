package ar.edu.utn.frsf.isi.dam.laboratorio05;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapaFragment extends SupportMapFragment implements
        OnMapReadyCallback {

    private GoogleMap miMapa;
    private int tipoMapa;

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


        return rootView;


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        miMapa = googleMap;
        actualizarMapa();

    }


    public void actualizarMapa() {

        final String[] permiso = {Manifest.permission.ACCESS_FINE_LOCATION};


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

}

