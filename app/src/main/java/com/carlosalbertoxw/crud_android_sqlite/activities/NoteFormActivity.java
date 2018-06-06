package com.carlosalbertoxw.crud_android_sqlite.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.carlosalbertoxw.crud_android_sqlite.R;
import com.carlosalbertoxw.crud_android_sqlite.database.NoteDTO;
import com.carlosalbertoxw.crud_android_sqlite.models.Note;

public class NoteFormActivity extends AppCompatActivity {

    private EditText txtTitle, txtText;
    private Button btnSave, btnDelete;
    private Intent intent;
    private NoteDTO dto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_form);
        intent = getIntent();
        dto = new NoteDTO(getBaseContext());

        txtTitle = (EditText) findViewById(R.id.txtTitle);
        txtText = (EditText) findViewById(R.id.txtText);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnDelete = (Button) findViewById(R.id.btnDelete);

        if(intent.getLongExtra("id",0)!=0){
            txtTitle.setText(intent.getStringExtra("titulo"));
            txtText.setText(intent.getStringExtra("texto"));
            btnSave.setVisibility(View.VISIBLE);
        }else{
            btnDelete.setVisibility(View.GONE);
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtTitle.getText().toString().length()>0 && txtText.getText().toString().length()>0){
                    Note item = new Note();
                    item.setTitle(txtTitle.getText().toString());
                    item.setText(txtText.getText().toString());

                    if(intent.getLongExtra("id",0)!=0){
                        item.setId(intent.getLongExtra("id",0));
                        long numRows = dto.updateNote(item);
                        if(numRows>0){
                            Toast.makeText(NoteFormActivity.this, "Nota guardada exitosamente!!", Toast.LENGTH_LONG).show();
                            limpiarCampos();
                            Intent i = new Intent(NoteFormActivity.this, NoteListActivity.class);
                            startActivity(i);
                            finish();
                        }else{
                            Toast.makeText(NoteFormActivity.this, "Error al guardar la nota!!", Toast.LENGTH_LONG).show();
                        }
                    }else{
                        long id = dto.saveNote(item);
                        if(id!=-1){
                            Toast.makeText(NoteFormActivity.this, "Nota guardada exitosamente!!", Toast.LENGTH_LONG).show();
                            limpiarCampos();
                            Intent i = new Intent(NoteFormActivity.this, NoteListActivity.class);
                            startActivity(i);
                            finish();
                        }else{
                            Toast.makeText(NoteFormActivity.this, "Error al guardar la nota!!", Toast.LENGTH_LONG).show();
                        }
                    }
                }else{
                    Toast.makeText(NoteFormActivity.this, "Llena todos los campos para poder continuar!!", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Note item = new Note();
                item.setId(intent.getLongExtra("id",0));
                long numRows = dto.deleteNote(item);
                if(numRows>0){
                    Toast.makeText(NoteFormActivity.this, "Nota borrada exitosamente!!", Toast.LENGTH_LONG).show();
                    limpiarCampos();
                    Intent i = new Intent(NoteFormActivity.this, NoteListActivity.class);
                    startActivity(i);
                    finish();
                }else{
                    Toast.makeText(NoteFormActivity.this, "Error al borrar la nota!!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void limpiarCampos(){
        txtTitle.setText("");
        txtText.setText("");
    }
}
