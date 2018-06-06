package com.carlosalbertoxw.crud_android_sqlite.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.carlosalbertoxw.crud_android_sqlite.R;
import com.carlosalbertoxw.crud_android_sqlite.adapters.NoteListAdapter;
import com.carlosalbertoxw.crud_android_sqlite.database.NoteDTO;
import com.carlosalbertoxw.crud_android_sqlite.models.Note;

import java.util.List;

public class NoteListActivity extends AppCompatActivity {

    private NoteDTO dto;
    private List<Note> list;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);
        dto = new NoteDTO(this);

        listView = (ListView) findViewById(R.id.lstNotes);

        list = dto.getNotes();

        listView.setAdapter(new NoteListAdapter(getApplicationContext(),list));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(NoteListActivity.this,NoteFormActivity.class);
                intent.putExtra("id",list.get(position).getId());
                intent.putExtra("titulo",list.get(position).getTitle());
                intent.putExtra("texto",list.get(position).getText());
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        list = dto.getNotes();
        listView = (ListView) findViewById(R.id.lstNotes);
        listView.setAdapter(new NoteListAdapter(getApplicationContext(),list));
    }
}
