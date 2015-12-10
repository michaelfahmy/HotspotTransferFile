package com.lms.appenza.hotspotfiletransfer;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.TextView;

import java.util.List;
import java.util.zip.Inflater;

/**
 * Created by michael on 08/12/15.
 */
public class StudentAdapter extends ArrayAdapter<StudentItem> {

    Context context;

    public StudentAdapter(Context context, int resource, int textViewResourceId, List<StudentItem> objects) {
        super(context, resource, textViewResourceId, objects);
        this.context = context;
    }

    class ViewHolder {
        CheckedTextView checkedTextView;
        public CheckedTextView getCheckedTextView() {
            return checkedTextView;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewHolder holder = null;

        if ( convertView == null ) {
            convertView = inflater.inflate(R.layout.list_item, parent, false);

            holder = new ViewHolder();
            holder.checkedTextView = (CheckedTextView) convertView.findViewById(R.id.checkedText);
            convertView.setTag(holder);

            holder.checkedTextView.setOnClickListener( new View.OnClickListener() {
                public void onClick(View v) {
                    CheckedTextView cb = (CheckedTextView) v;
                    StudentItem student = (StudentItem) cb.getTag();
                    cb.setChecked(!cb.isChecked());
                    student.setChecked(cb.isChecked());
                }
            });
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        StudentItem student = getItem(position);

//        holder.checkBox.setTag( student );
//        holder.checkBox.setChecked( student.isChecked() );
//        holder.textView.setText( student.getName() );
        holder.checkedTextView.setTag(student);
        holder.checkedTextView.setChecked(student.isChecked());
        holder.checkedTextView.setText(student.getName());

        return convertView;
    }

}
