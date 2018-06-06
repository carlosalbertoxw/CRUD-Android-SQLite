package com.carlosalbertoxw.crud_android_sqlite.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.carlosalbertoxw.crud_android_sqlite.R;

public class MainActivity extends AppCompatActivity {

    private Button btnNoteForm;
    private Button btnNoteList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnNoteForm = (Button)findViewById(R.id.btnNoteForm);
        btnNoteList = (Button)findViewById(R.id.btnNoteList);

        btnNoteForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, NoteFormActivity.class);
                startActivity(i);
            }
        });
        btnNoteList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, NoteListActivity.class);
                startActivity(i);
            }
        });
    }
}
