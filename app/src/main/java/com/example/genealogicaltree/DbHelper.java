package com.example.genealogicaltree;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.view.Gravity;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
public class DbHelper extends SQLiteOpenHelper {
    //имя файла
    private static String DB_NAME = "db.sqlite3";
    //контекст бд
    final Context context;

    public DbHelper(Context context) {
        super(context, DB_NAME, null, 3);
        this.context = context;
    }

    @Override //перегруженный метод когда бд создается
    public void onCreate(SQLiteDatabase db) {
        //создаем таблицы
        db.execSQL("CREATE TABLE 'Человек' (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "'фамилия' text NOT NULL," +
                "'имя' text NOT NULL," +
                "'отчество' text NOT NULL," +
                "'датаРождения' date," +
                "'датаСмерти' date," +
                "'местоРождения' text," +
                "'местоСмерти' text);");

        db.execSQL("CREATE TABLE 'Дерево' (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "'кодЧеловека' integer NOT NULL," +
                "'кодЧеловека2' integer," +
                "'кодЧеловекаБрак' integer," +
                "FOREIGN KEY('кодЧеловека') references 'Человек'(_id) on delete cascade, " +
                "FOREIGN KEY('кодЧеловека2') references 'Человек'(_id) on delete cascade,"+
                "FOREIGN KEY('кодЧеловекаБрак') references 'Человек'(_id) on delete cascade);");

        //создаем вьюв, дерево разложенное по уровням
        db.execSQL("create view ПросмотрДерева as select ch._id, ch.фамилия, ch.имя," +
                "ch.отчество, ch.датаРождения, ch.датаСмерти, ch.местоРождения, ch.местоСмерти," +
                "d.кодЧеловека2, d.кодЧеловекаБрак,"+
                " ch.фамилия ||' '|| ch.имя ||' '|| ch.отчество ||', \r\n '|| ch.датаРождения ||' - '|| ch.датаСмерти"+
                " ||', \r\n '|| ch.местоРождения ||' - '|| ch.местоСмерти as param2 "+
                "from Человек ch join Дерево d on ch._id=d.кодЧеловека order by ch._id, d.кодЧеловекаБрак");

//todo del
        db.execSQL("INSERT INTO Человек('фамилия','имя','отчество','датаРождения','датаСмерти'," +
                "'местоРождения', 'местоСмерти') " +
                "values('Иванов', 'Иван','Иванович','1985-12-20','2020-12-01','Владимир', 'Владимир')," +
                    "('Кубышко', 'Елена','Викторовна','1965-12-20','2019-06-01','Владимир', 'Владимир'),"+
                    "('Марченко', 'Евгений','Алексеевич','1975-12-20','2021-02-05','Владимир', 'Владимир');");

        db.execSQL("INSERT INTO Дерево('кодЧеловека','кодЧеловека2','кодЧеловекаБрак') " +
                "values(1,null,3),(2,null,1),(3,1,null);" );

        ///

    }
    //перегруженный метод при обновлении бд
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //удаляем все таблицы
        db.execSQL("DROP TABLE IF EXISTS 'Дерево'");
        db.execSQL("DROP TABLE IF EXISTS 'Человек'");
        //вызываем создание бд
        this.onCreate(db);
    }

    //метод для открытия курсора по выборке
    public Cursor query(String query)
    {
        Cursor cr=null;
        try //пытаемся открыть курсор
        { cr=this.getWritableDatabase().rawQuery(query, null);}
        catch (Exception e) //ловим исключения
        {   //собщаем пользователю ошибку во всплываем сообщении, если она случилась
            Toast toast = Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG);
            //позиционрование сообщения
            toast.setGravity(Gravity.CENTER, 0, 0);
            //отображаем
            toast.show();
            return null;
        }
        return cr;
    }

    //метод для вызова вставки, обновления данных и прочих команд
    public boolean sqlExec(String query)
    {
        String err;
        try{this.getWritableDatabase().execSQL(query);}
        catch (Exception e)
        {
            Toast toast = Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return false;
        }
        return true;
    }

    //метод получения одних данных из курсора, по заданной выборке и полю
    public String getOneData(String query, String nameColumn, boolean printErr)
    {
        String result=null;
        try {//открываем курсор
            Cursor cr = this.getWritableDatabase().rawQuery(query, null);
            //перемещаем указатель на начало данных
            cr.moveToFirst();
            //если в курсоре пусто то возвратим null
            if (cr.getCount() == 0) return null;
            //получаем данные
            result=cr.getString(cr.getColumnIndex(nameColumn));
        }
        catch (Exception e)
        {
            if (printErr) {
                Toast toast = Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
            return null;
        }
        return result;
    }
}