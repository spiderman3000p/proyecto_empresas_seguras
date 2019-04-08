package org.opencv.javacv.facerecognition;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.googlecode.javacv.cpp.opencv_contrib.FaceRecognizer;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_core.MatVector;
import com.googlecode.javacv.cpp.opencv_imgproc;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.javacv.facerecognition.Helpers.labels;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;

public class PersonRecognizer extends Application {
	final String TAG = "app";
    FaceRecognizer faceRecognizer = null;
	String mPath;
	int count=0;
	boolean trained;
    int[] labels;
    File root;
    MatVector images;
	org.opencv.javacv.facerecognition.Helpers.labels labelsFile = null;
	static  final int WIDTH= 128;
	static  final int HEIGHT= 128;
	private int mProb=999;
    public File[] imageFiles;

    public PersonRecognizer(){

    }
    public void init(String path){
        if(faceRecognizer == null)
        faceRecognizer =  com.googlecode.javacv.cpp.opencv_contrib.createLBPHFaceRecognizer(1,8,8,8,100);
        mPath = path;
        if(labelsFile == null)//si el archivo con todos los labels se encuientra vacio se crea
            labelsFile = new labels(mPath);
		count = 0;
    }

	public void add(Mat m, String description) {
		Log.i(TAG,"PersonRecognizer.add(Mat m,String descripcion): descripcion:"+description);
		Bitmap bmp= Bitmap.createBitmap(m.width(), m.height(), Bitmap.Config.ARGB_8888);

		//normalizar valores de la imagen (contraste, brillo)
		//equalizeHist(m, m);//esto da error y brickea la app

		Utils.matToBitmap(m,bmp);
		bmp= Bitmap.createScaledBitmap(bmp, WIDTH, HEIGHT, false);

		FileOutputStream f;
		try {
			f = new FileOutputStream(mPath+description+"-"+count+".jpg",true);
			count++;
			bmp.compress(Bitmap.CompressFormat.JPEG, 100, f);
			f.close();

		} catch (Exception e) {
			Log.e("error",e.getCause()+" "+e.getMessage());
			e.printStackTrace();

		}

		trained = false; // al agregar nuevas imagenes se hace necesario volver a entrenar, por eso marcamos como entrenado = false
	}

	public boolean train() {
		Log.i(TAG,"PersonRecognizer.train()");
		root = new File(mPath);

		FilenameFilter pngFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".jpg");

			}
		};

		imageFiles = root.listFiles(pngFilter);

		Log.i(TAG,"mPath:"+mPath+" imageFiles:"+imageFiles.toString());
		images = new MatVector(imageFiles.length);

		labels = new int[imageFiles.length];

		int counter = 0;
		int label;
		int icount = 0;
		IplImage img=null;
		IplImage grayImg;

		int i1=mPath.length();

		for (File image : imageFiles) {
			String p = image.getAbsolutePath();
            //Log.i(TAG,"- image.getAbsolutePath(): "+p);
			img = cvLoadImage(p);

			if (img==null)
				Log.e("Error","Error cVLoadImage");
			//Log.i("image",p);

			int i2=p.lastIndexOf("-");//aqui se busca la ultima ubicacion del caracter -
			int i3=p.lastIndexOf(".");//aqui se busca la ultima ubicacion del caracter .
			String sufijo = p.substring(i2+1,i3);//aqui se busca el numero que esta entre el ultimo - y el ultimo ., por ejemplo:
			sufijo = sufijo.replace("copia","");//eliminamos la palabra copia si existiera en el sufijo
			sufijo = sufijo.replace("Copia","");//eliminamos la palabra Copia si existiera en el sufijo
			sufijo = sufijo.replace("(","");//eliminamos los parentesis si existieran en el sufijo
			sufijo = sufijo.replace(")","");//eliminamos los parentesis si existieran en el sufijo
			sufijo = sufijo.replace("-","");//eliminamos los guiones si existieran en el sufijo
			sufijo = sufijo.replace("_","");//eliminamos los pisos si existieran en el sufijo
			sufijo = sufijo.replace(".","");//eliminamos los parentesis si existieran en el sufijo
			sufijo = sufijo.replace(",","");//eliminamos los parentesis si existieran en el sufijo
			sufijo = sufijo.replace(";","");//eliminamos los parentesis si existieran en el sufijo
			sufijo = sufijo.replace(":","");//eliminamos los parentesis si existieran en el sufijo
			sufijo = sufijo.replace(" ", "");//eliminamos los parentesis si existieran en el sufijo
			try {
				icount = Integer.parseInt(sufijo);
			}catch(Exception e){
				Log.e("app","Excepcion al obtener icount o sufijo en en archivo: " + p);
			}
			// si el nombre del archivo es faceRecogOCV/20362122-3.jpg, icount vale 3. Este es el contador por label, cuando un mismo label se repite.
			if (count < icount)
				count = icount + 1;//si el contador de repeticion actual es menor al nuevo valor de contador encontrado, se incrementa el contador de repeticion actual

			String description=p.substring(i1,i2);//obtengo el texto desde el inicio hasta el ultimo -, por ejemplo si el nombre del archivo
			//es faceRecogOCV/20362122-3.jpg, description vale faceRecogOCV/20362122
			if (labelsFile.get(description) < 0)
				labelsFile.add(description, labelsFile.max()+1);

			label = labelsFile.get(description);

			grayImg = IplImage.create(img != null ? img.width() : 0, img != null ? img.height() : 0, IPL_DEPTH_8U, 1);//creamos la imagen

			cvCvtColor(img, grayImg, CV_BGR2GRAY);//pasamos la imagen en escala de grises

			images.put(counter, grayImg);//metemos la imagen en escala de grises en el vector de imagenes, en el indice counter. counter lleva la cuenta
			//de cada imagen encontrada, no importa si pertenece al mismo rostro o no

			labels[counter] = label;//se asigna el key al label en la posicion del contador general

			counter++;//se incrementa el contador general

		}
		if (counter>0) {//si el contador general es mayor a cero, es decir, si hay al menos una imagen en la carpeta faceRecogOCV
			if (labelsFile.max() > 1 ) {
				faceRecognizer.train(images, labels);
                Toast.makeText(this,"Trained",Toast.LENGTH_SHORT).show();
                trained = true;
			}
			labelsFile.Save();
		}
		return true;
	}

	public boolean canPredict()
	{
		return (labelsFile.max() > 1 && trained);
	}


	public String predict(Mat m) {

		if (!canPredict()) {
            Toast.makeText(this, "No se puede predict", Toast.LENGTH_SHORT).show();
            return "";
        }
		int n[] = new int[1];
		double p[] = new double[1];
		IplImage ipl = MatToIplImage(m);
		String label = "";

		faceRecognizer.predict(ipl, n,p);

		if (n[0]!=-1)
			mProb=(int)p[0];
		else
			mProb=-1;

			if ((n[0] != -1)&&(p[0]<80)) {

					return labelsFile.get(n[0]);
            }

			return "Desconocido";
	}

	IplImage MatToIplImage(Mat m)
	{


		Bitmap bmp=Bitmap.createBitmap(m.width(), m.height(), Bitmap.Config.ARGB_8888);


		Utils.matToBitmap(m, bmp);
		return BitmapToIplImage(bmp, PersonRecognizer.WIDTH, PersonRecognizer.HEIGHT);

	}

	IplImage BitmapToIplImage(Bitmap bmp, int width, int height) {

		if ((width != -1) || (height != -1)) {

			bmp = Bitmap.createScaledBitmap(bmp, width, height, false);
        }

		IplImage image = IplImage.create(bmp.getWidth(), bmp.getHeight(),
				IPL_DEPTH_8U, 4);

		bmp.copyPixelsToBuffer(image.getByteBuffer());

		IplImage grayImg = IplImage.create(image.width(), image.height(),
				IPL_DEPTH_8U, 1);

		cvCvtColor(image, grayImg, opencv_imgproc.CV_BGR2GRAY);

		return grayImg;
	}



	protected void SaveBmp(Bitmap bmp,String path)
	{
		FileOutputStream file;
		try {
			file = new FileOutputStream(path , true);

			bmp.compress(Bitmap.CompressFormat.JPEG,100,file);
			file.close();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e("",e.getMessage()+e.getCause());
			e.printStackTrace();
		}

	}


	public void load() {
		if(!trained)
			train();
	}

	public int getProb() {
		// TODO Auto-generated method stub
		return mProb;
	}

}
