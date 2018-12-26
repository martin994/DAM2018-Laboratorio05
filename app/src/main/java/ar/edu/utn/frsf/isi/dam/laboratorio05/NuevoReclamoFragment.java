package ar.edu.utn.frsf.isi.dam.laboratorio05;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.MyDatabase;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.Reclamo;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.ReclamoDao;

import static android.app.Activity.RESULT_OK;

public class NuevoReclamoFragment extends Fragment {

    public interface OnNuevoLugarListener {
         void obtenerCoordenadas();
    }

    public void setListener(OnNuevoLugarListener listener) {
        this.listener = listener;
    }

    private Reclamo reclamoActual;
    private ReclamoDao reclamoDao;

    private EditText reclamoDesc;
    private EditText mail;
    private Spinner tipoReclamo;
    private TextView tvCoord;
    private Button buscarCoord;
    private Button btnGuardar;
    private OnNuevoLugarListener listener;
    //Variables necesarias para sacar fotos
    private Button btnSacarFoto;
    private ImageView ivFoto;
    private final int REQUEST_IMAGE_SAVE=2;
     private String pathFoto;
    //Variables necesarios para la manipulacion del audio

    private Button btnGrabarAudio;
    private Button btnReproducirAudio;
    private static final String LOG_TAG = "AudioRecordTest";
    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;

    private Boolean grabando = false;
    private Boolean reproduciendo = false;

    private String audioPath;



    private ArrayAdapter<Reclamo.TipoReclamo> tipoReclamoAdapter;
    public NuevoReclamoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        reclamoDao = MyDatabase.getInstance(this.getActivity()).getReclamoDao();

        View v = inflater.inflate(R.layout.fragment_nuevo_reclamo, container, false);

        reclamoDesc = (EditText) v.findViewById(R.id.reclamo_desc);
        mail= (EditText) v.findViewById(R.id.reclamo_mail);
        tipoReclamo= (Spinner) v.findViewById(R.id.reclamo_tipo);
        tvCoord= (TextView) v.findViewById(R.id.reclamo_coord);
        buscarCoord= (Button) v.findViewById(R.id.btnBuscarCoordenadas);
        btnGuardar= (Button) v.findViewById(R.id.btnGuardar);
        //Inicializacion de las variables de los componentes de la foto
        btnSacarFoto=(Button)v.findViewById(R.id.buttonSacarFoto);
        ivFoto=(ImageView)v.findViewById(R.id.imageViewFoto);
        //Inicializacion de las variables de los componentes de los audios
        btnGrabarAudio =(Button)v.findViewById( R.id.buttonGrabar );
        btnReproducirAudio=(Button)v.findViewById( R.id.buttonReproducir);
        btnReproducirAudio.setOnClickListener( listenerPlayer );
        btnGrabarAudio.setOnClickListener( listenerPlayer );


        tipoReclamoAdapter = new ArrayAdapter<Reclamo.TipoReclamo>(getActivity(),android.R.layout.simple_spinner_item,Reclamo.TipoReclamo.values());
        tipoReclamoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tipoReclamo.setAdapter(tipoReclamoAdapter);

        int idReclamo =0;
        if(getArguments()!=null)  {
            idReclamo = getArguments().getInt("idReclamo",0);
        }

        cargarReclamo(idReclamo);


        boolean edicionActivada = !tvCoord.getText().toString().equals("0;0");
        reclamoDesc.setEnabled(edicionActivada );
        mail.setEnabled(edicionActivada );
        tipoReclamo.setEnabled(edicionActivada);
        btnGuardar.setEnabled(edicionActivada);

        buscarCoord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.obtenerCoordenadas();

            }
        });

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveOrUpdateReclamo();
            }
        });


        btnSacarFoto.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //me fijo si tengo los permisos para usar la camara
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, 1);

                    } else {
                        sacarGuardarFoto();
                    }
                }



            }
        } );
        return v;
    }


    private void cargarReclamo(final int id){
        if( id >0){
            Runnable hiloCargaDatos = new Runnable() {
                @Override
                public void run() {
                    reclamoActual = reclamoDao.getById(id);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if(reclamoActual.getPathFoto()!=null)
                                onActivityResult( REQUEST_IMAGE_SAVE,Activity.RESULT_OK,null );
                            mail.setText(reclamoActual.getEmail());
                            tvCoord.setText(reclamoActual.getLatitud()+";"+reclamoActual.getLongitud());
                            reclamoDesc.setText(reclamoActual.getReclamo());
                            reclamoActual.getPathFoto();
                            Reclamo.TipoReclamo[] tipos= Reclamo.TipoReclamo.values();
                            for(int i=0;i<tipos.length;i++) {
                                if(tipos[i].equals(reclamoActual.getTipo())) {
                                    tipoReclamo.setSelection(i);
                                    break;
                                }
                            }
                        }
                    });
                }
            };
            Thread t1 = new Thread(hiloCargaDatos);
            t1.start();
        }else{
            String coordenadas = "0;0";
            if(getArguments()!=null) coordenadas = getArguments().getString("latLng","0;0");
            tvCoord.setText(coordenadas);
            reclamoActual = new Reclamo();
        }

    }

    private void saveOrUpdateReclamo(){


        reclamoActual.setEmail(mail.getText().toString());
        reclamoActual.setReclamo(reclamoDesc.getText().toString());
        reclamoActual.setTipo(tipoReclamoAdapter.getItem(tipoReclamo.getSelectedItemPosition()));
        reclamoActual.setPathFoto( pathFoto );
        reclamoActual.setPathAudio( audioPath );
        if(tvCoord.getText().toString().length()>0 && tvCoord.getText().toString().contains(";")) {
            String[] coordenadas = tvCoord.getText().toString().split(";");
            reclamoActual.setLatitud(Double.valueOf(coordenadas[0]));
            reclamoActual.setLongitud(Double.valueOf(coordenadas[1]));
        }
        Runnable hiloActualizacion = new Runnable() {
            @Override
            public void run() {


                if(reclamoActual.getId()>0) reclamoDao.update(reclamoActual);
                else reclamoDao.insert(reclamoActual);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        // limpiar vista
                        mail.setText(R.string.texto_vacio);
                        tvCoord.setText(R.string.texto_vacio);
                        reclamoDesc.setText(R.string.texto_vacio);
                        getActivity().getFragmentManager().popBackStack();
                    }
                });
            }
        };
        Thread t1 = new Thread(hiloActualizacion);
        t1.start();
    }




    //A partir de aca manejo la parte de sacar fotos


    //Funcion para armar el archivo file

    private File createImageFile()throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir =getActivity().
                getExternalFilesDir( Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        pathFoto = image.getAbsolutePath();


        return image;
    }




    //metodo para sacar la foto
    private void sacarGuardarFoto(){
        Intent sacarFoto= new Intent( MediaStore.ACTION_IMAGE_CAPTURE);

        if(sacarFoto.resolveActivity( getActivity().getPackageManager() )!=null){
            File archivoFoto=null;
            try {
                archivoFoto=createImageFile();

            }catch(IOException e){e.printStackTrace();}

            if ( archivoFoto!= null) {
                Uri photoURI = FileProvider.getUriForFile(getActivity().getApplicationContext(),
                        "ar.edu.utn.frsf.isi.dam.laboratorio05.fileprovider",
                        archivoFoto);


                sacarFoto.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(sacarFoto, REQUEST_IMAGE_SAVE);
            }


        }

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){



        if (requestCode == REQUEST_IMAGE_SAVE && resultCode == RESULT_OK) {
            File file = new File(pathFoto);
            Bitmap imageBitmap = null;
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), Uri.fromFile(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (imageBitmap != null) {
                ivFoto.setImageBitmap(imageBitmap);
            }

        }


    }

    //metodos para grabar y reproducir audio



    private void grabar() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(audioPath);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }


    }
    private void terminarGrabar() {

        try {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;

        } catch (RuntimeException e){
            e.printStackTrace();
        }


    }

    private void reproducir() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(audioPath);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void terminarReproducir() {
        mPlayer.release();
        mPlayer = null;
    }


    View.OnClickListener listenerPlayer = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.buttonReproducir:

                    if(reproduciendo){
                        ((Button) view).setText("Reproducir");
                        reproduciendo=false;
                        terminarReproducir();
                        btnGrabarAudio.setEnabled( true );


                    }else{
                        ((Button) view).setText("pausar.....");
                        reproduciendo=true;
                        reproducir();
                        btnGrabarAudio.setEnabled( false );


                    }
                    break;
                case R.id.buttonGrabar:

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission( getActivity(),
                                Manifest.permission.RECORD_AUDIO )
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions( getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, 2 );

                        } else {
                            if (grabando) {
                                ((Button) view).setText( "Grabar" );
                                grabando = false;
                                terminarGrabar();
                                btnReproducirAudio.setEnabled( true );
                            } else {
                                String timeStamp = new SimpleDateFormat( "yyyyMMdd_HHmmss" ).format( new Date() );
                                String audioFileName = "/MP3_" + timeStamp + ".3gp";
                                audioPath = Environment.getExternalStorageDirectory().getAbsolutePath() + audioFileName;
                                ((Button) view).setText( "grabando....." );
                                grabando = true;
                                grabar();
                                btnReproducirAudio.setEnabled( false );

                            }
                        }
                    }


                    break;
            }
        }
    };


}
