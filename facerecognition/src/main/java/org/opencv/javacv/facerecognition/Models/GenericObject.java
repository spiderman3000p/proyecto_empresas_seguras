package org.opencv.javacv.facerecognition.Models;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by AlexManuel on 01/09/2015.
 */
public class GenericObject implements Parcelable {

    ContentValues values;
    private int row;

    public GenericObject(){
        values = new ContentValues();
    }

    protected GenericObject(Parcel in) {
        values = in.readParcelable(ContentValues.class.getClassLoader());
        row = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(values, flags);
        dest.writeInt(row);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GenericObject> CREATOR = new Creator<GenericObject>() {
        @Override
        public GenericObject createFromParcel(Parcel in) {
            return new GenericObject(in);
        }

        @Override
        public GenericObject[] newArray(int size) {
            return new GenericObject[size];
        }
    };

    public String getAsString(String keyword) {
        return values.getAsString(keyword);
    }
    public int getAsInt(String keyword) {
        return values.getAsInteger(keyword);
    }
    public boolean getAsBoolean(String keyword) {
        return values.getAsBoolean(keyword);
    }
    public void set(String keyword,int valor) {
        values.put(keyword,valor);
    }
    public void set(String keyword,boolean valor) {
        values.put(keyword,valor);
    }
    public void set(String keyword,byte[] valor) {
        values.put(keyword,valor);
    }
    public void set(String keyword,String valor) {
        values.put(keyword,valor);
    }
    public ContentValues getValues(){
        return values;
    }
    public void setValues(ContentValues v){
        this.values = v;
    }
    public String toString(){ return this.values.getAsString("nombre"); }
    public byte[] getAsByte(String keyword) {
        return ((byte[]) values.get(keyword));
    }
    public boolean hasKey(String key){
        return values.containsKey(key);
    }
}
