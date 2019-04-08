package org.opencv.javacv.facerecognition.Callbacks;

/**
 * Created by Rafa on 21/08/2018.
 */

public interface VolleyCallback {
    void onSuccess(Integer result);

    void onError(String error);
}
