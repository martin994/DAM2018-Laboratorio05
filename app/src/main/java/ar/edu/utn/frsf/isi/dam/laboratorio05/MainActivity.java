package ar.edu.utn.frsf.isi.dam.laboratorio05;

import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.maps.model.LatLng;


// AGREGAR en MapaFragment una interface MapaFragment.OnMapaListener con el método coordenadasSeleccionadas 
// IMPLEMENTAR dicho método en esta actividad.

public class MainActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener,
        NuevoReclamoFragment.OnNuevoLugarListener,MapaFragment.onMapaListener,BuscarReclamosFragment.OnBuscarListener {
        private DrawerLayout drawerLayout;
        private NavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        //Handle when activity is recreated like on orientation Change
        shouldDisplayHomeUp();
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        navView = (NavigationView)findViewById(R.id.navview);
        BienvenidoFragment fragmentInicio = new BienvenidoFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contenido, fragmentInicio)
                .commit();

        navView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {

                        boolean fragmentTransaction = false;
                        Fragment fragment = null;
                        String tag = "";
                        switch (menuItem.getItemId()) {
                            case R.id.optNuevoReclamo:
                                tag = "nuevoReclamoFragment";
                                fragment =  getSupportFragmentManager().findFragmentByTag(tag);
                                if(fragment==null) {
                                    fragment = new NuevoReclamoFragment();
                                    ((NuevoReclamoFragment) fragment).setListener(MainActivity.this);
                                }

                                fragmentTransaction = true;
                                break;
                            case R.id.optListaReclamo:
                                tag="listaReclamos";
                                fragment =  getSupportFragmentManager().findFragmentByTag(tag);
                                if(fragment==null) fragment = new ListaReclamosFragment();
                                fragmentTransaction = true;
                                break;
                            case R.id.optVerMapa:
                                //TODO HABILITAR
                                tag="mapaReclamos";

                                fragment =  getSupportFragmentManager().findFragmentByTag(tag);
                                //TODO si "fragment" es null entonces crear el fragmento mapa, agregar un bundel con el parametro tipo_mapa
                                if(fragment==null){
                                    fragment = new MapaFragment();
                                    Bundle bundle = new Bundle();
                                    bundle.putInt("tipo_mapa", 2);
                                    fragment.setArguments(bundle);
                                    ((MapaFragment) fragment).setListener(MainActivity.this);

                                }


                                // configurar a la actividad como listener de los eventos del mapa ((MapaFragment) fragment).setListener(this);
                               fragmentTransaction = true;
                                break;
                            case R.id.optHeatMap:
                                //TODO HABILITAR

                                tag="mapaReclamos";
                                fragment =  getSupportFragmentManager().findFragmentByTag(tag);

                                //TODO si "fragment" es null entonces crear el fragmento mapa, agregar un bundel con el parametro tipo_mapa
                                // configurar a la actividad como listener de los eventos del mapa ((MapaFragment) fragment).setListener(this);
                                if(fragment==null){
                                    fragment= new MapaFragment();
                                    Bundle bundle= new Bundle();
                                    bundle.putInt("tipo_mapa", 4);
                                    fragment.setArguments(bundle);
                                    ((MapaFragment) fragment).setListener(MainActivity.this);
                                }
                                fragmentTransaction = true;
                                break;

                            case R.id.optBuscar:
                                tag="buscarFragment";
                                fragment= getSupportFragmentManager().findFragmentByTag(tag);
                                if(fragment==null){
                                    fragment= new BuscarReclamosFragment();
                                    ((BuscarReclamosFragment) fragment).setListener(MainActivity.this);
                                }
                                fragmentTransaction=true;

                                break;

                        }

                        if(fragmentTransaction) {
                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.contenido, fragment,tag)
                                    .addToBackStack(null)
                                    .commit();

                            menuItem.setChecked(true);

                            getSupportActionBar().setTitle(menuItem.getTitle());
                        }

                        drawerLayout.closeDrawers();

                        return true;
                    }
                });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackStackChanged() {
        shouldDisplayHomeUp();
    }

    public void shouldDisplayHomeUp(){
        //Enable Up button only  if there are entries in the back stack
        boolean canback = getSupportFragmentManager().getBackStackEntryCount()>0;
        getSupportActionBar().setDisplayHomeAsUpEnabled(canback);
    }

    // AGREGAR en MapaFragment una interface OnMapaListener con el método coordenadasSeleccionadas
    // IMPLEMENTAR dicho método en esta actividad.
    // el objetivo de este método, es simplmente invocar al fragmento "nuevoReclamoFragment"
    // pasando como argumento el objeto "LatLng" elegido por el usuario en el click largo
    // como ubicación del reclamo

        @Override
        public void coordenadasSeleccionadas(LatLng c) {
            String tag = "nuevoReclamoFragment";
            Fragment fragment =  getSupportFragmentManager().findFragmentByTag(tag);
            if(fragment==null) {
                fragment = new NuevoReclamoFragment();
                ((NuevoReclamoFragment) fragment).setListener(MainActivity.this);
            }
            Bundle bundle = new Bundle();
            bundle.putString("latLng",c.latitude+";"+c.longitude);
            fragment.setArguments(bundle);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenido, fragment,tag).
                    addToBackStack(null)
                    .commit();

        }

        //Implementacion de las interfaces para que puedan comunicarce con las actividades, dicen que tipo de fragmento van y el tipo de mapa
    @Override
    public void obtenerCoordenadas() {

        Fragment nuevoFragmento;
        Bundle arg= new Bundle();

        arg.putInt("tipo_mapa",1);

        nuevoFragmento=new MapaFragment();
        nuevoFragmento.setArguments(arg);

        ((MapaFragment)nuevoFragmento).setListener(MainActivity.this);

        getSupportFragmentManager().
                beginTransaction().
                replace(R.id.contenido,nuevoFragmento,"Seleccione las coordenadas").
                addToBackStack(null).
                commit();

        getSupportActionBar().setTitle("Seleccione coordenadas:");
    }


    @Override
    public void buscarReclamosTipo(String tipo) {
        String tag= "mapaReclamos";
        Fragment fragment= getSupportFragmentManager().findFragmentByTag(tag);
        if(fragment==null){
            fragment= new MapaFragment();
            ((MapaFragment) fragment).setListener(this);
        }
        Bundle bundle= new Bundle();
        bundle.putInt("tipo_mapa", 5);
        bundle.putString("tipo_reclamo", tipo);
        fragment.setArguments(bundle);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contenido, fragment)
                .addToBackStack(null)
                .commit();

    }
};





