package com.carlosalbertoxw.crud_android_sqlite.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.carlosalbertoxw.crud_android_sqlite.R;
import com.carlosalbertoxw.crud_android_sqlite.models.Note;

import java.util.List;

public class NoteListAdapter extends ArrayAdapter<Note> {
    private Context context;
    private List<Note> items;

    public NoteListAdapter(Context context, List<Note> items) {
        super(context, R.layout.activity_note_list, items);
        this.context=context;
        this.items=items;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(this.context);
        View view = inflater.inflate(R.layout.adapter_note_list,parent,false);
        Note item = this.items.get(position);
        TextView lblTitle = (TextView) view.findViewById(R.id.lblTitle);
        lblTitle.setText(item.getTitle());
        return view;
    }
}
