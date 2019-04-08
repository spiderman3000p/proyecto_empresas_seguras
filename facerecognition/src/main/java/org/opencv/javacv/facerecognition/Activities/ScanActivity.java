package org.opencv.javacv.facerecognition.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.javacv.facerecognition.Database.DatabaseAdmin;
import org.opencv.javacv.facerecognition.Helpers.CustomHelper;
import org.opencv.javacv.facerecognition.PersonRecognizer;
import org.opencv.javacv.facerecognition.R;
import org.opencv.javacv.facerecognition.Tutorial3View;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;


public class ScanActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String    TAG  = "app";
    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 255, 255);
    public static final int        JAVA_DETECTOR = 0;
    public static final int SEARCHING= 1;
    public static final int IDLE= 2;
    private static final int frontCam =1;
    private static final int backCam =2;
    private int faceState=SEARCHING;
    private MenuItem nBackCam;
    private MenuItem               mFrontCam;
    private Mat mRgba;
    private Mat                    mGray;
    private File                   mCascadeFile;
    private CascadeClassifier      mJavaDetector;
    private int mDetectorType = JAVA_DETECTOR;
    private float mRelativeFaceSize   = 0.1f;
    private int mAbsoluteFaceSize   = 0;
    private int mLikely=999;
    String mPath="";
    private Tutorial3View mOpenCvCameraView;
    private int mChooseCamera = backCam;
    private TextView textresult;
    private Bitmap mBitmap;
    private Handler mHandler;
    PersonRecognizer fr;
    private ToggleButton buttonSearch;
    private ImageView ivGreen;
    private ImageButton imCamera;
    private String dni;
    Button clave_btn;

    //private static class MyHandler extends Handler {}
    //private final MyHandler mHandler2 = new MyHandler();
  /* inicializacion estatica... no se usa porque en onResume se llama la inicializacion asyncrona
    static {
        OpenCVLoader.initDebug();
        System.loadLibrary("opencv_java");
    }
*/
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    fr = (PersonRecognizer) getApplicationContext();
                    //fr = new PersonRecognizer(mPath);
                    fr.init(mPath);
                    //fr.setFaceMode(MULTIPLE_FACE_MODE);
                    fr.load();

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.lbpcascadefrontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                        //                 mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                    mOpenCvCameraView.enableView();

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;


            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scan);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        dni = getIntent().getExtras().getString("dni");
        //MyRunnable mRunnable = new MyRunnable(this);
        Log.i(TAG, "ScanActivity.dni: " + dni);
        mOpenCvCameraView = findViewById(R.id.camera_view);

        mOpenCvCameraView.setCvCameraViewListener(this);

        //path publico (accesible desde afuera)
        if (CustomHelper.isExternalStorageReadableAndWritable()) {//si la escritura/lectura externa esta permitida...
            mPath = Environment.getExternalStorageDirectory() + "/databases/facerecogOCV/";
        } else {// sino fijar como path ubicacion privada
            mPath = getFilesDir()+ "/facerecogOCV/";
        }
        //creamos un archivo .nomedia para evitar que el media scanner del explorador de android muestre el contenido de la carpeta en la galeria
        if(!mPath.isEmpty()){
            File file = new File(mPath,".nomedia");

            try {
                if(!file.exists())
                    Files.write("",file, Charsets.UTF_8);
            } catch (IOException e) {
                Log.i("app","Error al crear archivo .nomedia");
                e.printStackTrace();
            }
        }
        //labelsFile = new labels(mPath);
        textresult = findViewById(R.id.textoNombre);

        //obtenemos boton para usar clave, en lugar de reconocimiento facial
        clave_btn = (Button) findViewById(R.id.usar_clave_btn);
        clave_btn.setVisibility(View.GONE);
        clave_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //paramos la busqueda
                finishScanner();
                //y mostramos el dialog con un input para la clave
                //primero construimos el dialogo y luego lo mostramos
                LayoutInflater layoutInflater = LayoutInflater.from(getBaseContext());
                View viewClaveDialog = layoutInflater.inflate(R.layout.clave_dialog, null);
                final EditText etClave = (EditText) viewClaveDialog.findViewById(R.id.etClave);

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ScanActivity.this);
                alertDialog.setView(viewClaveDialog);
                alertDialog.setCancelable(false);
                alertDialog.setPositiveButton("Entrar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String clave = etClave.getText().toString();

                        if (consultarClave(clave,dni))//si la clave coincide con el dni
                            login();//iniciamos login
                        else {
                            Toast.makeText(getBaseContext(), "Clave inválida", Toast.LENGTH_SHORT).show();//mostramos un mensaje
                            startScanner();
                        }
                    }
                }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        startScanner();
                    }
                });
                AlertDialog dialogClave = alertDialog.create();
                dialogClave.show();

            }
        });

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.equals("IMG"))
                {
                    Canvas canvas = new Canvas();
                    canvas.setBitmap(mBitmap);
                }
                else// se recibe un resultado del reconocedor
                {
                    final String label = msg.obj.toString();
                   // final String result = label+" ("+(mLikely > 0 ? (100-mLikely): 0)+"%)";
                    final String result = label+" ("+mLikely+")";
                    //for debug pourpose TODO: quitar esto de aqui y ponerlo dentro de if(result== dni)
                    //textresult.setText(result);

                    ivGreen.setVisibility(View.INVISIBLE);

                    if (mLikely < 0);

                       else if(mLikely <= 80){ // si el resultado de parecido obtenido (confianza) es alto, se toma como resultado positivo valido

                        if(!label.equals("Desconocido")) {//Si la etiqueta devuelta sea distinto a desconocido

                            Log.i(TAG,"Label result: "+label+" dni= "+dni);
                            //comprobamos que el resultado label obtenido sea igual al dni recibido del intent (activity padre)
                            if(label.equals(dni)){
                                Log.i("app","llamando login...para "+dni);
                                ivGreen.setVisibility(View.VISIBLE);
                                textresult.setText(result);
                                /*try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }*/
                                login();
                            }
                        }
                    }
                }
            }
        };
        buttonSearch = findViewById(R.id.buscar_btn);
        ivGreen = findViewById(R.id.icono_verde);
        imCamera = findViewById(R.id.flip_camera);

        ivGreen.setVisibility(View.INVISIBLE);


        imCamera.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                if (mChooseCamera==frontCam)
                {
                    mChooseCamera=backCam;
                    mOpenCvCameraView.setCamBack();
                }
                else
                {
                    mChooseCamera=frontCam;
                    mOpenCvCameraView.setCamFront();

                }
            }
        });

        buttonSearch.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (buttonSearch.isChecked())
                {
                    if (!fr.canPredict())
                    {
                        buttonSearch.setChecked(false);
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.SCanntoPredic), Toast.LENGTH_LONG).show();
                        return;
                    }

                    faceState=SEARCHING;
                    textresult.setVisibility(View.VISIBLE);

                }
                else
                {
                    faceState=IDLE;
                    textresult.setVisibility(View.INVISIBLE);

                }
            }
        });


        boolean success=(new File(mPath)).mkdirs();
        if (!success)
        {
            Log.e("Error","Error creating directory");
        }

        //mHandler2.postDelayed(mRunnable,60000);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mostrarBotonClave();
            }
        },180000);

    }

    private void mostrarBotonClave() {
        clave_btn.setVisibility(View.VISIBLE);
    }

    private boolean consultarClave(String clave, String dni) {
        //buscamos en la BD si la clave coincide con la cedula o dni
        DatabaseAdmin db = new DatabaseAdmin(getBaseContext());

        return db.consultarUsuarioPorDniClave(dni,clave);
    }

    private void startScannerAnimation(){

    }

    private void stopScannerAnimation(){

    }

    private void startScanner(){
        buttonSearch.setChecked(true);
        buttonSearch.callOnClick();
    }

    private void finishScanner(){
        if(buttonSearch.isChecked()) {
            buttonSearch.setChecked(false);
            buttonSearch.callOnClick();
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_4, this, mLoaderCallback);
        //mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
    }

    public void onDestroy() {
        super.onDestroy();
        fr = null;
        mHandler = null;
        mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
        startScanner();
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
        //Imgproc.equalizeHist(inputFrame.gray(),mGray);
        //Mat mRGBa2 = new Mat();
        //double alpha = 1.3;
        //double beta = 40;
        //mGray = inputFrame.gray();

        //aplicamos el filtro ecualizador a la imagen que sale en pantalla
        //Imgproc.equalizeHist(inputFrame.gray(),mGray);

        //mRgba.convertTo(mRGBa2,-1,alpha,beta);

        //mRgba = mRGBa2;
        //mRgba = inputFrame.rgba();
        //Imgproc.cvtColor(mRgba,mGray,Imgproc.COLOR_BGR2GRAY);//la imagen en escala de grises se guarda en mGray

        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
            //  mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
        }

        MatOfRect faces = new MatOfRect();

        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector != null)
                //esta funcion solo detecta rostros no los reconoce. el reconocimiento se hace en PersonRecognizer
                mJavaDetector.detectMultiScale(mGray, faces, 1.1, 8, 8, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        }else {
            Log.e(TAG, "Detection method is not selected!");
        }

        Rect[] facesArray = faces.toArray();


        if ((facesArray.length > 0) && (faceState == SEARCHING))//aqui se hace la deteccion de rostro
        {
            //Mat m2 = new Mat();
            Mat m = new Mat();
            m = mGray.submat(facesArray[0]);
            //justo aqui se debe mejorar la imagen (mejorar la iluminacion)
            //m es la imagen en escala de grises TODO: probar a pasarla a RGB y aplicar el equalizador en esta
            //Imgproc.equalizeHist(m2,m);//esta funcion es la que le cambia el brillo
            //m es la imagen ya modificada
            mBitmap = Bitmap.createBitmap(m.width(),m.height(), Bitmap.Config.ARGB_8888);


            Utils.matToBitmap(m, mBitmap);
            Message msg = new Message();
            String textTochange = "IMG";
            msg.obj = textTochange;
            mHandler.sendMessage(msg);

            textTochange=fr.predict(m);//aqui se hace el reconocimiento, predict retorna el label o numero de cedula si coincide sino retorna "Desconocido"
            mLikely=fr.getProb();
            msg = new Message();
            msg.obj = textTochange;
            mHandler.sendMessage(msg);

        }


        for (int i = 0; i < facesArray.length; i++)
            Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);

        return mRgba;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        if (mOpenCvCameraView.numberCameras()>1)
        {
            nBackCam = menu.add(getResources().getString(R.string.SFrontCamera));
            mFrontCam = menu.add(getResources().getString(R.string.SBackCamera));
        }
        else
        {imCamera.setVisibility(View.INVISIBLE);

        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);

        nBackCam.setChecked(false);
        mFrontCam.setChecked(false);
        if (item == nBackCam)
        {
            mOpenCvCameraView.setCamFront();
            mChooseCamera=frontCam;
        }
        else if (item==mFrontCam)
        {
            mChooseCamera=backCam;
            mOpenCvCameraView.setCamBack();

        }

        item.setChecked(true);

        return true;
    }


    private void login(){

        Intent intent = new Intent();
        intent.putExtra("dni",dni);
        //startActivity(intent);
        Log.i("app","entrando login...");
        setResult(RESULT_OK,intent);
       //s setIntent(intent);
        finish();
        //finish();
    }
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
    }

    public static class MyRunnable implements Runnable{
        private final WeakReference<Activity> mActivity;

        public MyRunnable(Activity activity){
            mActivity = new WeakReference<Activity>(activity);
        }

        @Override
        public void run(){
            Activity activity = mActivity.get();
            if(activity != null){
                Button clave_btn = (Button) activity.findViewById(R.id.usar_clave_btn);
                clave_btn.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle state){
        super.onSaveInstanceState(state);

//        state.putParcelable("userData",userData);

    }

}
