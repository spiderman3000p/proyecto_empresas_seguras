package org.opencv.javacv.facerecognition.Activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.common.base.Strings;
import com.googlecode.javacv.cpp.opencv_core;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.javacv.facerecognition.Helpers.CustomHelper;
import org.opencv.javacv.facerecognition.PersonRecognizer;
import org.opencv.javacv.facerecognition.R;
import org.opencv.javacv.facerecognition.Tutorial3View;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FdActivity extends Activity implements CvCameraViewListener2 {

    private static final String TAG = "app";
    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);

    public static final int TRAINING = 0;
    public static final int SEARCHING = 1;
    public static final int IDLE = 2;
    private static final int frontCam = 1;
    private static final int backCam = 2;
    private int faceState = IDLE;
    private MenuItem nBackCam;
    private MenuItem mFrontCam;
    private Mat mRgba;
    private Mat mGray;
    private CascadeClassifier mJavaDetector;
    private int mAbsoluteFaceSize = 0;
    private int mLikely = 999;

    String mPath = "";

    private Tutorial3View mOpenCvCameraView;
    private int mChooseCamera = backCam;

    EditText text;
    TextView textresult;
    private ImageView Iv;
    Bitmap mBitmap;
    Handler mHandler;

    PersonRecognizer fr;
    ToggleButton toggleButtonGrabar, toggleButtonTrain, buttonSearch;
    ImageView ivGreen, ivYellow, ivRed;
    ImageButton imCamera;
    String defaultLabel;
    TextView textState;
    //TODO: utilizar alguna funcion para ajustar automaticamente la calidad de la imagen si es muy oscura

    static final long MAXIMG = 1;

    int countImages = 0;

/*
    static {
        OpenCVLoader.initDebug();
        System.loadLibrary("opencv_java");
    }
*/
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    fr = (PersonRecognizer) getApplicationContext();

                    fr.init(mPath);//inicializamos en modo MULTIPLE_FACE

                    fr.load();

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.lbpcascadefrontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        File mCascadeFile = new File(cascadeDir, "lbpcascade.xml");
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

                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;


            }
        }
    };

    public FdActivity() {

        Log.i(TAG, "Instantiated new " + this.getClass());

    }


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);

        cargarParametros();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.face_detect);

        mOpenCvCameraView = findViewById(R.id.camera_view);

        mOpenCvCameraView.setCvCameraViewListener(this);

        //path publico (accesible desde afuera)
        if (CustomHelper.isExternalStorageReadableAndWritable()) {//si la escritura/lectura externa esta permitida...
            mPath = Environment.getExternalStorageDirectory() + "/databases/facerecogOCV/";
        } else {// sino fijar como path ubicacion privada
            mPath = getFilesDir() + "/facerecogOCV/";
        }
        Log.i(TAG, "Path created: " + mPath);

        Iv = findViewById(R.id.imagen_captura);
        textresult = findViewById(R.id.textoNombre);
        text = findViewById(R.id.nombre_label_input);
        toggleButtonGrabar = findViewById(R.id.grabar);
        buttonSearch = findViewById(R.id.buscar_btn);
        toggleButtonTrain = findViewById(R.id.entrenar_btn);
        textState = findViewById(R.id.estado);
        ivGreen = findViewById(R.id.icono_verde);
        ivYellow = findViewById(R.id.icono_amarillo);
        ivRed = findViewById(R.id.icono_rojo);
        imCamera = findViewById(R.id.flip_camera);

        ivGreen.setVisibility(View.INVISIBLE);
        ivYellow.setVisibility(View.INVISIBLE);
        ivRed.setVisibility(View.INVISIBLE);
        text.setVisibility(View.INVISIBLE);
        textresult.setVisibility(View.INVISIBLE);
        toggleButtonGrabar.setVisibility(View.INVISIBLE);


        if (Strings.isNullOrEmpty(defaultLabel)) {//si no se recibe ningun dni desde la actividad padre
            text.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if ((text.getText().toString().length() > 0) && (toggleButtonTrain.isChecked()))
                        toggleButtonGrabar.setVisibility(View.VISIBLE);
                    else
                        toggleButtonGrabar.setVisibility(View.INVISIBLE);

                    return false;
                }
            });
        } else {
            text.setText(defaultLabel);
            text.setFocusable(false);
            text.setTextSize(24);
        }


        toggleButtonTrain.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (toggleButtonTrain.isChecked()) {
                    textState.setText(getResources().getString(R.string.SEnter));
                    buttonSearch.setVisibility(View.INVISIBLE);
                    textresult.setVisibility(View.VISIBLE);
                    text.setVisibility(View.VISIBLE);
                    textresult.setText(getResources().getString(R.string.SFaceName));
                    if (text.getText().toString().length() > 0)
                        toggleButtonGrabar.setVisibility(View.VISIBLE);


                    ivGreen.setVisibility(View.INVISIBLE);
                    ivYellow.setVisibility(View.INVISIBLE);
                    ivRed.setVisibility(View.INVISIBLE);


                } else {
                    textState.setText(R.string.Straininig);
                    textresult.setText("");
                    text.setVisibility(View.INVISIBLE);

                    buttonSearch.setVisibility(View.VISIBLE);
                    textresult.setText("");
                    {
                        toggleButtonGrabar.setVisibility(View.INVISIBLE);
                        text.setVisibility(View.INVISIBLE);
                    }
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.Straininig), Toast.LENGTH_LONG).show();
                    fr.train();
                    textState.setText(getResources().getString(R.string.SIdle));

                }
            }

        });


        toggleButtonGrabar.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                grabarOnclick();
            }
        });

        imCamera.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                if (mChooseCamera == frontCam) {
                    mChooseCamera = backCam;
                    mOpenCvCameraView.setCamBack();
                } else {
                    mChooseCamera = frontCam;
                    mOpenCvCameraView.setCamFront();

                }
            }
        });

        buttonSearch.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (buttonSearch.isChecked()) {
                    if (!fr.canPredict()/* && !fr.trained()*/) {
                        buttonSearch.setChecked(false);
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.SCanntoPredic), Toast.LENGTH_LONG).show();
                        return;
                    }
                    textState.setText(getResources().getString(R.string.SSearching));
                    toggleButtonGrabar.setVisibility(View.INVISIBLE);
                    toggleButtonTrain.setVisibility(View.INVISIBLE);
                    text.setVisibility(View.INVISIBLE);
                    faceState = SEARCHING;
                    textresult.setVisibility(View.VISIBLE);
                } else {
                    faceState = IDLE;
                    textState.setText(getResources().getString(R.string.SIdle));
                    toggleButtonGrabar.setVisibility(View.INVISIBLE);
                    toggleButtonTrain.setVisibility(View.VISIBLE);
                    text.setVisibility(View.INVISIBLE);
                    textresult.setVisibility(View.INVISIBLE);

                }
            }
        });

        boolean success = (new File(mPath)).mkdirs();
        if (!success) {
            Log.e("Error", "Error creating directory");
        }

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj == "IMG") {
                    Canvas canvas = new Canvas();
                    canvas.setBitmap(mBitmap);
                    Iv.setImageBitmap(mBitmap);
                    if (countImages >= MAXIMG) {
                        toggleButtonGrabar.setChecked(false);
                        grabarOnclick();
                    }
                } else {
                    double porcentaje = mLikely > 0 ? (100 - mLikely) : 0;
                    if (mLikely < 100) {
                        textresult.setText(msg.obj.toString() + " (" + mLikely + ")");
                    }
                    ivGreen.setVisibility(View.INVISIBLE);
                    ivYellow.setVisibility(View.INVISIBLE);
                    ivRed.setVisibility(View.INVISIBLE);

                    if (mLikely < 0)
                        ivRed.setVisibility(View.VISIBLE);
                    else if (mLikely <= 80)
                        ivGreen.setVisibility(View.VISIBLE);
                    else if (mLikely < 100)
                        ivYellow.setVisibility(View.VISIBLE);


                }
            }
        };


    }

    void grabarOnclick() {
        if (toggleButtonGrabar.isChecked())
            faceState = TRAINING;
        else {
            countImages = 0;
            faceState = IDLE;
        }


    }

    private void cargarParametros() {
        Bundle extras = new Bundle();

        extras = getIntent().getExtras();

        if (extras != null) {
            if (extras.get("cedula") != null)
                defaultLabel = extras.getString("cedula");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        //mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_4, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        mHandler = null;
        fr = null;
        mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        String textTochange;

        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            float mRelativeFaceSize = 0.1f;
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
            //  mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
        }

        MatOfRect faces = new MatOfRect();

        if (mJavaDetector != null)
            mJavaDetector.detectMultiScale(mGray, faces, 1.1, 8, 8, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());

        Rect[] facesArray = faces.toArray();//es el array de rostros encontrados en el frame

        if ((facesArray.length == 1) && (faceState == TRAINING) && (countImages < MAXIMG) && (!text.getText().toString().isEmpty())) {
        //cuando solo se encuentre un rostro, y se este en entrenamiento y las imagenes guardadas sean menor a MAXIMG y el campo dni no este vacio
            Mat m = new Mat();
            Rect r = facesArray[0];

            m = mRgba.submat(r);
            mBitmap = Bitmap.createBitmap(m.width(), m.height(), Bitmap.Config.ARGB_8888);

            Utils.matToBitmap(m, mBitmap);
            // SaveBmp(mBitmap,"/sdcard/db/I("+countTrain+")"+countImages+".jpg");

            Message msg = new Message();
            textTochange = "IMG";

            if (countImages < MAXIMG) {
                fr.add(m, text.getText().toString());//add() guarda la imagen en el dispositivo
                countImages++;
            }

            msg.obj = textTochange;
            mHandler.sendMessage(msg);

        } else if ((facesArray.length > 0) && (faceState == SEARCHING)) {
            Mat m = new Mat();
            m = mGray.submat(facesArray[0]);
            mBitmap = Bitmap.createBitmap(m.width(), m.height(), Bitmap.Config.ARGB_8888);

            Utils.matToBitmap(m, mBitmap);
            Message msg = new Message();
            textTochange = "IMG";
            msg.obj = textTochange;
            mHandler.sendMessage(msg);

            textTochange = fr.predict(m);
            mLikely = fr.getProb();
            msg = new Message();
            msg.obj = textTochange;
            mHandler.sendMessage(msg);

        }

        for (int i = 0; i < facesArray.length; i++)
            Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);

        return mRgba;
    }

    Mat IplImageToMat(opencv_core.IplImage iplImage) {
        Mat matImage = new Mat();

        Bitmap bitmap = Bitmap.createBitmap(iplImage.width(), iplImage.height(), Bitmap.Config.ARGB_8888);

        bitmap.copyPixelsFromBuffer(iplImage.getByteBuffer());
        Utils.bitmapToMat(bitmap, matImage);

        return matImage;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        if (mOpenCvCameraView.numberCameras() > 1) {
            nBackCam = menu.add(getResources().getString(R.string.SFrontCamera));
            mFrontCam = menu.add(getResources().getString(R.string.SBackCamera));

        } else {
            imCamera.setVisibility(View.INVISIBLE);

        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);

        nBackCam.setChecked(false);
        mFrontCam.setChecked(false);

        if (item == nBackCam) {
            mOpenCvCameraView.setCamFront();
            mChooseCamera = frontCam;
        }
        //fr.changeRecognizer(0);
        else if (item == mFrontCam) {
            mChooseCamera = backCam;
            mOpenCvCameraView.setCamBack();

        }

        item.setChecked(true);

        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle state){
        super.onSaveInstanceState(state);

     //   state.putParcelable("userData",userData);

    }


}
