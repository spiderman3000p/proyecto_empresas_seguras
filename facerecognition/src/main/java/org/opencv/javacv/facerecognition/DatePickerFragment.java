package org.opencv.javacv.facerecognition;

import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.opencv.javacv.facerecognition.Helpers.CustomHelper;

import java.util.Calendar;

/**
 * Created by Rafa on 21/07/2018.
 */

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    DateTime date = null;
    EditText txtDate = null;


    public DatePickerFragment(DateTime date, EditText txtDate) {

        this.date = date;
        this.txtDate = txtDate;
    }

    public DatePickerFragment() {
        this.date = null;
        this.date = null;
    }

    @Override
    public android.app.Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //final DateTime dt = (this.fieldUsed.equalsIgnoreCase("DESDE") ? dtDesde : dtHasta);
        setRetainInstance(true);

        Calendar c = Calendar.getInstance();
        c.setTime(date.toDate());

        DatePickerDialog tpd = new DatePickerDialog(getActivity(), this,
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );

        TextView tvTitle = new TextView(getActivity());
        tvTitle.setText("Elija una fecha");
        tvTitle.setTextColor(Color.parseColor("#FFFFFF"));
        tvTitle.setBackgroundColor(Color.parseColor("#FF0099CC"));
        tvTitle.setPadding(5, 3, 5, 3);
        tvTitle.setGravity(Gravity.CENTER_HORIZONTAL);
        tpd.setCustomTitle(tvTitle);

        return tpd;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);

        DateTime dt = new DateTime(c.getTime());

        txtDate.setText(CustomHelper.parseToString(dt, "yyyy-MM-dd"));
    }

    @Override
    public void onDestroy() {
        dismiss();
        super.onDestroy();
    }
}
