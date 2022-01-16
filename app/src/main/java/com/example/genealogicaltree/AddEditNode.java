package com.example.genealogicaltree;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AddEditNode extends AppCompatActivity {
    DbHelper db;
    String id, mode;
    EditText etSurname=null, etName=null, etPatronymic=null, etPlaceBirthday=null,
            etPlaceDeath=null,edDateBirthday=null, edDateDeath=null;

    @SuppressLint("Range")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_node);

        db = new DbHelper(this);

        etSurname = (EditText) findViewById(R.id.etSurname);
        etName = (EditText) findViewById(R.id.etName);
        etPatronymic = (EditText) findViewById(R.id.etPatronymic);
        etPlaceBirthday = (EditText) findViewById(R.id.etPlaceBirthday);
        etPlaceDeath = (EditText) findViewById(R.id.etPlaceDeath);
        edDateBirthday = (EditText) findViewById(R.id.edDateBirthday);
        edDateDeath = (EditText) findViewById(R.id.edDateDeath);

        Intent intent = this.getIntent();
        this.mode = intent.getStringExtra("mode");
        this.id = intent.getStringExtra("id");

        if (mode.equals("edit")) {
            this.setTitle("Редактирование");
            Cursor cr = db.query("select *from ПросмотрДерева where _id="+id);
            if (cr != null) { //и есть в нем данные
                if (cr.getCount() > 0) {
                    cr.moveToFirst();
                    etSurname.setText(cr.getString(cr.getColumnIndex("фамилия")));
                    etName.setText(cr.getString(cr.getColumnIndex("имя")));
                    etPatronymic.setText(cr.getString(cr.getColumnIndex("отчество")));
                    etPlaceBirthday.setText(cr.getString(cr.getColumnIndex("местоРождения")));
                    etPlaceDeath.setText(cr.getString(cr.getColumnIndex("местоСмерти")));
                    edDateBirthday.setText(cr.getString(cr.getColumnIndex("датаРождения")));
                    edDateDeath.setText(cr.getString(cr.getColumnIndex("датаСмерти")));
                }
            }
        } else
            this.setTitle("Добавление");
    }

    //обработчик кнопки добавления внеплановых занятий
    public void onClickBtn(View v)
    {
        if (mode.equals("edit")) {
            if (!db.sqlExec("update Человек set фамилия ='" + etSurname.getText() +
                    "', имя ='" + etName.getText() + "', отчество='" + etPatronymic.getText() +
                    "', датаРождения ='" + edDateBirthday.getText() + "', датаСмерти='" + edDateDeath.getText() +
                    "', местоРождения ='" + etPlaceBirthday.getText() + "', местоСмерти='" + etPlaceDeath.getText() +
                    "' where _id=" + id)) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Не возможно обновить данные в БД, повторите попытку!",
                        Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;
            }
        }
        else {
                if (!db.sqlExec("INSERT INTO Человек('фамилия','имя','отчество','датаРождения','датаСмерти'," +
                        "'местоРождения', 'местоСмерти') values('" + etSurname.getText() + "','" +
                        etName.getText() + "','" + etPatronymic.getText() + "','" + edDateBirthday.getText() +
                        "','" + edDateDeath.getText() + "','" + etPlaceBirthday.getText() + "','" + etPlaceDeath.getText() + "');")) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Не возможно добавить человека в БД, повторите попытку!",
                            Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                }

            String idMax=db.getOneData("select max(_id) as _idU from Человек", "_idU", true);

            if (!id.equals("null")) {
                String inputSql;

                //режим добавления - поколение или брак
                if (mode.equals("add"))
                    inputSql=idMax + "," + id + ",null";
                else
                {
                    inputSql=idMax + ",null," + id;
                    if (!db.sqlExec("update Дерево set кодЧеловекаБрак=" +idMax+" where кодЧеловека="+id)) {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Не возможно обновить данные о браке в БД, повторите попытку!",
                                Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        return;
                    }
                }

                if (!db.sqlExec("INSERT INTO Дерево('кодЧеловека','кодЧеловека2','кодЧеловекаБрак')" +
                        " values(" +inputSql+ ");")) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Не возможно добавить данные в БД, повторите попытку!",
                            Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                }
            }
            else //иначе это корень дерева и данных в бд нет
            {
                if (!db.sqlExec("INSERT INTO Дерево('кодЧеловека','кодЧеловека2','кодЧеловекаБрак')" +
                        " values(" + idMax + ",null ,null);")) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Не возможно добавить данные в БД, повторите попытку!",
                            Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                }
            }
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}