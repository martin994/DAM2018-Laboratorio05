package ar.edu.utn.frsf.isi.dam.laboratorio05;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.Reclamo;


/**
 * A simple {@link Fragment} subclass.
 */
public class BuscarReclamosFragment extends Fragment {


    private Spinner spnlistaDeReclamos;
    private Button btnBuscarRelcamos;
    private ArrayAdapter adapterRelcamos;
    private OnBuscarListener listener;

    public BuscarReclamosFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_buscar_reclamos, container, false);

        spnlistaDeReclamos=(Spinner) v.findViewById(R.id.spinnerDeReclamos);
        btnBuscarRelcamos=(Button)v.findViewById(R.id.btnBuscarReclamos);
        //aca seteo el nombre del tipo de los reclamos
        List<String> nombreReclamos= getNombreReclamos();

        adapterRelcamos= new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1,nombreReclamos);

        spnlistaDeReclamos.setAdapter(adapterRelcamos);

        btnBuscarRelcamos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.buscarReclamosTipo(spnlistaDeReclamos.getSelectedItem().toString());
            }
        });

        return v;
    }

    private List<String> getNombreReclamos(){
        //armo una lista de string para retornar
        List<String> listaTipo= new ArrayList<>();
        //tomo una lista de reclamos y me quedo solo con el tipo de reclamo
        List<Reclamo.TipoReclamo> list= Arrays.asList(Reclamo.TipoReclamo.values());
        for (int i=0; list.size()>i; i++){
            listaTipo.add(list.get(i).toString());
        }


        return  listaTipo;
    }


    public interface OnBuscarListener{
        public void buscarReclamosTipo(String tipo);
    }

    public void setListener (OnBuscarListener listener){ this.listener=listener;}

}
