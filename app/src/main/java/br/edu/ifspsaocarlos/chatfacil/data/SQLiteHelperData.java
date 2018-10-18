package br.edu.ifspsaocarlos.chatfacil.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelperData extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "chatfacil.db";
    private static final int DATABASE_VERSION = 1;

    //campos da tabela contatos
    static final String TABLE_CONTATO = "contatos";
    static final String FIELD_CONTATO_ID = "contato_id";
    static final String FIELD_CONTATO_NOME = "contato_nome";

    //campos da tabela de mensagens
    static final String TABLE_MSG = "mensagens";
    static final String FIELD_MSG_ID = "msg_id";
    static final String FIELD_MSG_CONTATO_ID_ORIGEM = "msg_origem_id";
    static final String FIELD_MSG_CONTATO_ID_DESTINO = "msg_destino_id";
    static final String FIELD_MSG_ASSUNTO = "msg_assunto";
    static final String FIELD_MSG_CORPO = "msg_corpo";
    static final String FIELD_MSG_LIDA = "msg_lida";

    //SQL para criação da tabela contatos
    private static final String DATABASE_CREATE_CONTATOS = "CREATE TABLE " + TABLE_CONTATO + " (" +
            FIELD_CONTATO_ID + " BIGINT PRIMARY KEY, " +
            FIELD_CONTATO_NOME + " VARCHAR(100) NOT NULL " +
            ")";

    //SQL para criação da tabela mensagens
    private static final String DATABASE_CREATE_MSGS = "CREATE TABLE " + TABLE_MSG + " (" +
            FIELD_MSG_ID + " BIGINT PRIMARY KEY, " +
            FIELD_MSG_CONTATO_ID_ORIGEM + " BIGINT , " +
            FIELD_MSG_CONTATO_ID_DESTINO + " BIGINT , " +
            FIELD_MSG_ASSUNTO + " VARCHAR(50) NOT NULL, " +
            FIELD_MSG_CORPO + " VARCHAR(150) NOT NULL, " +
            FIELD_MSG_LIDA + " INTEGER NOT NULL DEFAULT 0, " +

            " FOREIGN KEY(" + FIELD_MSG_CONTATO_ID_ORIGEM + ") REFERENCES " + TABLE_CONTATO + "(" + FIELD_CONTATO_ID + "), " +
            " FOREIGN KEY(" + FIELD_MSG_CONTATO_ID_DESTINO + ") REFERENCES " + TABLE_CONTATO + "(" + FIELD_CONTATO_ID + ") " +
            ")";

    public SQLiteHelperData(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_CONTATOS);
        db.execSQL(DATABASE_CREATE_MSGS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
