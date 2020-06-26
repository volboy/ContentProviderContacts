package com.example.contentprovidercontacts;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_READ_CONTACTS=1; //запрос на чтение контактов
    private static boolean READ_CONTACTS_GRANTED=false; //статус разрешения на чтения контактов

    ListView contactList;
    ArrayList<String> contacts=new ArrayList<String>();
    Button addBtn;
    EditText contactText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contactList=findViewById(R.id.contactList);
        addBtn=findViewById(R.id.addBtn);
        contactText=findViewById(R.id.newContact);

        //получаем разрешения
        int hasReadContactPermission= ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        //если устройство до API 23, устанавливаем разрешение
        if (hasReadContactPermission== PackageManager.PERMISSION_GRANTED){
            READ_CONTACTS_GRANTED=true;
        }
        else{
            //вызываем диалоговое окно для установки разрешений
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CODE_READ_CONTACTS);
        }

        //если разрешение установлено, загружаем контакты
        if (READ_CONTACTS_GRANTED){
            loadContacts();
        }
    }

    public void onAddContact(View v){
        ContentValues contactValues=new ContentValues(); //словарь для хранения данных контакта
        String newContact=contactText.getText().toString();
        contactText.setText("");
        contactValues.put(ContactsContract.RawContacts.ACCOUNT_NAME, newContact); //название контакта
        contactValues.put(ContactsContract.RawContacts.ACCOUNT_TYPE, newContact); //тип контакта
        Uri newUri=getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, contactValues); //добавляем контакт в таблицу rawContacts
                                                                                                        //метод возвращает ссылку на добавленный контакт
        long rawContactsId= ContentUris.parseId(newUri); //получаем id из только что добавленного контакта
        contactValues.clear(); //очищаем наш словарь

        //заполняем словарь данными
        contactValues.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactsId);
        contactValues.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        contactValues.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, newContact);


        getContentResolver().insert(ContactsContract.Data.CONTENT_URI, contactValues); //заполняем данными другую таблицу - data
        Toast.makeText(getApplicationContext(), newContact + " добавлен в список контактов", Toast.LENGTH_LONG).show();
        loadContacts();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissoins, int[] grantResult){
        switch (requestCode){
            case REQUEST_CODE_READ_CONTACTS:
                if (grantResult.length>0 && grantResult[0]==PackageManager.PERMISSION_GRANTED){
                    READ_CONTACTS_GRANTED=true;
                }
        }
        if (READ_CONTACTS_GRANTED){
            loadContacts();
        }
        else{
            Toast.makeText(this, "Требуется установить разрешения", Toast.LENGTH_LONG).show();
            addBtn.setEnabled(READ_CONTACTS_GRANTED);
        }
    }

    private void loadContacts(){
        contacts.clear();
        ContentResolver contentResolver=getContentResolver();
        Cursor cursor=contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null,null);
        if (cursor!=null){
            while (cursor.moveToNext()){
                //получаем каждый контакт
                String contact=cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));
                //добавляем контакт в список
                contacts.add(contact);

            }
            cursor.close();
        }
        //создаем адаптер
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, contacts);
        //устанавливаем для списка адаптер
        contactList.setAdapter(adapter);
    }
}
