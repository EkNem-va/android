package com.example.genealogicaltree;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.Toast;

import java.util.ArrayList;

import android.view.Menu;
import android.view.MenuItem;

import de.blox.treeview.BaseTreeAdapter;
import de.blox.treeview.TreeNode;
import de.blox.treeview.TreeView;

public class MainActivity extends AppCompatActivity {
    DbHelper db;
    Cursor cr; //курсор на дерево

    int currentSelectedNode=0;

    //класс для хранения связки id и node
    public class idVsNode
    {
        public idVsNode(int id, int idMerry, int numInNode,  TreeNode node) {
            this.id = id;

            this.idMerry = idMerry;
            this.node = node;
            this.numInNode=numInNode;
        }
        public int id;
        public int idMerry;
        public int numInNode;
        public TreeNode node;
    }
    //контейнер для хранения
    ArrayList<idVsNode> idVsNodes=new ArrayList<idVsNode>();
    int cntNodes=0;
    BaseTreeAdapter<Viewholder> adapter;
    TreeView treeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db=new DbHelper(this);

        // creating a variable for tree view.
        treeView = findViewById(R.id.treeView);

        // creating adapter class for our treeview using basetree adapter.
        // inside base tree adapter you have to pass viewholder class along
        // with context and your layout file for treeview node.
        adapter = new BaseTreeAdapter<Viewholder>(this, R.layout.tree_view_node) {
            @NonNull
            @Override
            public Viewholder onCreateViewHolder(View view) {
                return new Viewholder(view);
            }

            @Override
            public void onBindViewHolder(Viewholder viewHolder, Object data, int position) {
                // inside our on bind view holder method we
                // are setting data from object to text view.
                viewHolder.textView.setText(data.toString());
            }
        };

        treeView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter_view, View v, int i, long l) {
                //Log.d("TAG","pos"+i);
                currentSelectedNode=i;
                treeView.showContextMenu();
            }
        });

        // below line is setting adapter for our tree.
        treeView.setAdapter(adapter);

        //treeView.setUseMaxSize(true);
        readNode();

        // добавляем контекстное меню
        registerForContextMenu(treeView);
    }

    //перегруженный метод активности - когда активность возобновляет работу
    @Override
    public void onResume(){
        super.onResume();
        readNode();
    }

    //draw Tree from DB
    @SuppressLint("Range")
    private void readNode()
    {
        try {
            idVsNodes.clear();
            //получим курсор на выборку
            cr = db.query("select * from ПросмотрДерева");
            boolean setRoot=false;
            cntNodes=0;

            //если курсор открыт
            if (cr != null) { //и есть в нем данные
                if (cr.getCount() > 0) {
                    cr.moveToFirst(); //передвинем указатель в начало, так как он в конце
                    do {
                        int id = cr.getInt(cr.getColumnIndex("_id"));
                        String data=cr.getString(cr.getColumnIndex("param2"));
                        TreeNode node = new TreeNode(data);

                        int idPeople=0;
                        try { idPeople = cr.getInt(cr.getColumnIndex("кодЧеловека2")); }
                        catch (Exception ignored) { continue; }

                        int idPeopleMerry=0;
                        try { idPeopleMerry = cr.getInt(cr.getColumnIndex("кодЧеловекаБрак")); }
                        catch (Exception ignored) { continue; }

                        int numNode=-1;

                        //начальный корень
                        if (!setRoot)
                        {
                            adapter.setRootNode(node);
                            setRoot=true;
                            numNode=cntNodes++;
                        }

                        if (idPeople != 0) //добавим новый нод к существующему
                        {
                            for (idVsNode nodeList : idVsNodes) {
                                if (nodeList.id == idPeople) {
                                    nodeList.node.addChild(node);
                                    numNode=cntNodes++;
                                }
                            }
                        }

                        //если есть отец или мать, добавим к существующему ноду, так как много корней не может быть
                        if (idPeopleMerry!=0)
                            for (idVsNode nodeList : idVsNodes) {
                                if (nodeList.id == idPeopleMerry) {
                                    nodeList.node.setData(nodeList.node.getData() + "\r\n" + data);
                                    numNode=nodeList.numInNode;
                                }
                            }

                        idVsNode ins = new idVsNode(id, idPeopleMerry,numNode, node);
                        idVsNodes.add(ins);
                    } while (cr.moveToNext()); //пока фетчится продолжаем
                }
            }

        }
        catch(Exception e)
        {
            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.BOTTOM, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setText(e.getMessage());
            toast.show();

        }
    }

    //метод проверяет наличие второго человека в листе
    private boolean hasSecondPeople()
    {
        for (idVsNode nodeList : idVsNodes)
            if(nodeList.numInNode==currentSelectedNode && nodeList.idMerry == 0)
                    return false;
        return true;
    }

    private String getIdPeople(boolean second)
    {
        for (idVsNode nodeList : idVsNodes)
            if(nodeList.numInNode==currentSelectedNode) {
                if (second) return String.valueOf((nodeList.idMerry));
                return String.valueOf((nodeList.id));
            }
        return null;
    }
    //перегруженный метод для создания выпадающего меню
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.add(Menu.NONE, 8, Menu.NONE, "Добавить");

        if (hasSecondPeople()) {
            menu.add(Menu.NONE, 1, Menu.NONE, "Удалить первого человека");
            menu.add(Menu.NONE, 2, Menu.NONE, "Удалить второго человека");
            menu.add(Menu.NONE, 3, Menu.NONE, "Редактировать первого человека");
            menu.add(Menu.NONE, 4, Menu.NONE, "Редактировать второго человека");
        }
        else
        {
            menu.add(Menu.NONE, 5, Menu.NONE, "Добавить второго человека");
            menu.add(Menu.NONE, 6, Menu.NONE, "Редактировать");
        }
        menu.add(Menu.NONE, 7, Menu.NONE, "Удалить лист");
    }

    //обработчик событий выбора пунктов меню
    public boolean onContextItemSelected(MenuItem item) {
        // получаем из пункта контекстного меню данные по пункту списка
        //получим id первого и второго человека
        String idFirst=getIdPeople(false);
        String idSecond=getIdPeople(true);

        switch (item.getItemId())
        {
            case 1: //Удалить первого человека
                // извлекаем id записи и удаляем соответствующую запись в БД
                if (!db.sqlExec("delete from Человек where _id="+idFirst)) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Не возможно удалить данные в БД, повторите попытку!",
                            Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return false;
                }
                //перезагрузим дерево
                readNode();
                return true;

            case 2: //Удалить второго человека
                if (!db.sqlExec("update Дерево set кодЧеловекаБрак=null where кодЧеловека="+idFirst)) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Не возможно обновить данные о браке в БД, повторите попытку!",
                            Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return false;
                }

                // извлекаем id записи и удаляем соответствующую запись в БД
                if (!db.sqlExec("delete from Человек where _id="+idSecond)) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Не возможно удалить данные в БД, повторите попытку!",
                            Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return false;
                }
                //перезагрузим дерево
                readNode();
                return true;

            case 3: //Редактировать первого человека
            case 6: //"Редактировать"
                Intent intentAddEdit = new Intent(MainActivity.this, AddEditNode.class);
                intentAddEdit.putExtra("mode","edit");
                intentAddEdit.putExtra("id", idFirst);
                startActivity(intentAddEdit);
                break;

            case 4: //Редактировать второго человека
                Intent intentAddEditP = new Intent(MainActivity.this, AddEditNode.class);
                intentAddEditP.putExtra("mode","edit");
                intentAddEditP.putExtra("id", idSecond);
                startActivity(intentAddEditP);
                break;

            case 5: //Добавить второго человека
                Intent intentAdd = new Intent(MainActivity.this, AddEditNode.class);
                intentAdd.putExtra("mode","add2");
                intentAdd.putExtra("id", idFirst);
                startActivity(intentAdd);
                break;

                case 7: //удалить лист

                    if (hasSecondPeople())
                    {// извлекаем id записи и удаляем соответствующую запись в БД
                        if (!db.sqlExec("delete from Человек where _id="+idSecond)) {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    "Не возможно удалить данные в БД, повторите попытку!",
                                    Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return false;
                        }
                    }

                    // извлекаем id записи и удаляем соответствующую запись в БД
                    if (!db.sqlExec("delete from Человек where _id="+idFirst)) {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Не возможно удалить данные в БД, повторите попытку!",
                                Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        return false;
                    }
                    readNode();
                break;

                case 8: //Добавить
                    Intent intentAddNew = new Intent(MainActivity.this, AddEditNode.class);
                    intentAddNew.putExtra("mode","add");
                    if (cr == null) //и нет данных
                    {
                        //if (cr.getCount() > 0)
                            intentAddNew.putExtra("id", "null");
                    }
                    else
                        intentAddNew.putExtra("id", idFirst);

                    startActivity(intentAddNew);
                    break;

            default:break;
        }

        return super.onContextItemSelected(item);
    }
    
}