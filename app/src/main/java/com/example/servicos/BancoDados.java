package com.example.servicos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class BancoDados extends SQLiteOpenHelper {

    private static final String NOME_BANCO = "servicos.db";
    private static final int VERSAO = 6;

    public BancoDados(Context context) {
        super(context, NOME_BANCO, null, VERSAO);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE servicos (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, description TEXT, date TEXT, time TEXT, client_name TEXT, address TEXT, price REAL DEFAULT 0)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS servicos");
        db.execSQL("DROP TABLE IF EXISTS tasks");
        onCreate(db);
    }

    public void inserir(Servico s) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("title", s.getTitle());
        v.put("description", s.getDescription());
        v.put("date", s.getDate());
        v.put("time", s.getTime() != null ? s.getTime() : "");
        v.put("client_name", s.getClientName());
        v.put("address", s.getAddress() != null ? s.getAddress() : "");
        v.put("price", s.getPrice());
        db.insert("servicos", null, v);
    }

    public void atualizar(Servico s) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("title", s.getTitle());
        v.put("description", s.getDescription());
        v.put("date", s.getDate());
        v.put("time", s.getTime() != null ? s.getTime() : "");
        v.put("client_name", s.getClientName());
        v.put("address", s.getAddress() != null ? s.getAddress() : "");
        v.put("price", s.getPrice());
        db.update("servicos", v, "id = ?", new String[]{String.valueOf(s.getId())});
    }

    public void excluir(long id) {
        getWritableDatabase().delete("servicos", "id = ?", new String[]{String.valueOf(id)});
    }

    public List<Servico> listar() {
        List<Servico> lista = new ArrayList<>();
        Cursor c = getReadableDatabase().query("servicos", null, null, null, null, null, "id DESC");
        if (c.moveToFirst()) {
            do {
                int timeIndex = c.getColumnIndex("time");
                String time = timeIndex != -1 ? c.getString(timeIndex) : "";

                int addressIndex = c.getColumnIndex("address");
                String address = addressIndex != -1 ? c.getString(addressIndex) : "";

                lista.add(new Servico(
                        c.getLong(c.getColumnIndexOrThrow("id")),
                        c.getString(c.getColumnIndexOrThrow("title")),
                        c.getString(c.getColumnIndexOrThrow("description")),
                        c.getString(c.getColumnIndexOrThrow("date")),
                        time != null ? time : "",
                        c.getString(c.getColumnIndexOrThrow("client_name")),
                        address != null ? address : "",
                        c.getDouble(c.getColumnIndexOrThrow("price"))
                ));
            } while (c.moveToNext());
        }
        c.close();
        return lista;
    }
}
