package com.example.servicos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

    public void excluirTodos() {
        getWritableDatabase().delete("servicos", null, null);
    }

    public void excluirPassados() {
        List<Servico> servicos = listar();
        long now = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("pt-BR"));
        sdf.setLenient(false);
        SimpleDateFormat sdfDateOnly = new SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"));
        sdfDateOnly.setLenient(false);

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (Servico s : servicos) {
                long time = parseServicoTime(s, sdf, sdfDateOnly);
                if (time < now) {
                    db.delete("servicos", "id = ?", new String[]{String.valueOf(s.getId())});
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
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
        ordenarServicos(lista);
        return lista;
    }

    private void ordenarServicos(List<Servico> servicos) {
        if (servicos == null || servicos.isEmpty()) return;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("pt-BR"));
        sdf.setLenient(false);
        SimpleDateFormat sdfDateOnly = new SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"));
        sdfDateOnly.setLenient(false);

        long now = System.currentTimeMillis();

        Collections.sort(servicos, (s1, s2) -> {
            long time1 = parseServicoTime(s1, sdf, sdfDateOnly);
            long time2 = parseServicoTime(s2, sdf, sdfDateOnly);

            boolean isPast1 = time1 < now;
            boolean isPast2 = time2 < now;

            if (isPast1 != isPast2) {
                return isPast1 ? 1 : -1;
            }

            return Long.compare(time1, time2);
        });
    }

    private long parseServicoTime(Servico s, SimpleDateFormat sdf, SimpleDateFormat sdfDateOnly) {
        try {
            String dateStr = s.getDate();
            String timeStr = s.getTime();
            if (dateStr == null) dateStr = "";
            if (timeStr == null || timeStr.trim().isEmpty()) {
                timeStr = "00:00";
            }
            String fullStr = dateStr.trim() + " " + timeStr.trim();
            Date d = sdf.parse(fullStr);
            if (d != null) return d.getTime();
        } catch (Exception e) {
            try {
                Date d = sdfDateOnly.parse(s.getDate());
                if (d != null) return d.getTime();
            } catch (Exception ignored) {}
        }
        return Long.MAX_VALUE;
    }
}
