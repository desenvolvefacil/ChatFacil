package br.edu.ifspsaocarlos.chatfacil.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.Html;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import br.edu.ifspsaocarlos.chatfacil.model.ContatoModel;
import br.edu.ifspsaocarlos.chatfacil.util.CriptografiaUtil;
import br.edu.ifspsaocarlos.chatfacil.util.MensageiroUtil;


public class ContatoData {
    private SQLiteDatabase database;
    private SQLiteHelperData dbHelper;
    private SharedPreferences sharedPreferences;

    private String[] cols=new String[] {SQLiteHelperData.FIELD_CONTATO_ID, SQLiteHelperData.FIELD_CONTATO_NOME};

    public ContatoData(Context context) {
        dbHelper = new SQLiteHelperData(context);
        sharedPreferences = context.getSharedPreferences(MensageiroUtil.ID_USUARIO_LOGADO, Context.MODE_PRIVATE);
    }

    public boolean SalvarContato(ContatoModel c){
        try{

            //montar o texto a partir do HTML vindo do site
            String nome = Html.fromHtml(c.getNome()).toString();

            //descriptografa o nome
            nome = CriptografiaUtil.decodificar(nome, ContatoModel.FATOR_CRIPTOGRAFIA);

            c.setNome(nome);

            database=dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(SQLiteHelperData.FIELD_CONTATO_ID, c.getId());
            values.put(SQLiteHelperData.FIELD_CONTATO_NOME, c.getNome());

            long ret = database.insertOrThrow(SQLiteHelperData.TABLE_CONTATO, null, values);

            return (ret>0);
        }catch (Exception e){

            return  false;
        }finally {
            database.close();
        }
    }

    public ContatoModel BuscarUsuario(String id){

        Cursor cursor = null;

        try{
            database=dbHelper.getReadableDatabase();

            String where = new String(SQLiteHelperData.FIELD_CONTATO_ID + " = ? " );
            String argWhere[] = new String[]{id};

            cursor = database.query(SQLiteHelperData.TABLE_CONTATO, cols, where , argWhere,
                    null, null, null);

            if (cursor.moveToNext()) {
                ContatoModel contato = new ContatoModel();

                contato.setId(cursor.getString(0));
                contato.setNome(cursor.getString(1));
                contato.setApelido(ContatoModel.UUID);

                return  contato;
            }
        }catch (Exception e){

        }finally {
            cursor.close();
            database.close();
        }


        return  null;
    }

    public ContatoModel BuscarUsuarioLogado(){
        long idLogado = sharedPreferences.getLong(MensageiroUtil.ID_USUARIO_LOGADO,0);

        if(idLogado>0){
            Cursor cursor = null;

            try {

                database=dbHelper.getReadableDatabase();

                String where = new String(SQLiteHelperData.FIELD_CONTATO_ID + " = ? " );
                String argWhere[] = new String[]{String.valueOf(idLogado)};

                cursor = database.query(SQLiteHelperData.TABLE_CONTATO, cols, where , argWhere,
                        null, null, null);

                if (cursor.moveToNext()) {
                    ContatoModel contato = new ContatoModel();

                    contato.setId(cursor.getString(0));
                    contato.setNome(cursor.getString(1));
                    contato.setApelido(ContatoModel.UUID);

                    return  contato;
                }

            }catch (Exception e){

            }finally {
                cursor.close();
                database.close();
            }
        }

        return  null;
    }

    public  List<ContatoModel> listar(){
        List<ContatoModel> lista = new ArrayList<>();
        Cursor cursor = null;

        try {

            database=dbHelper.getReadableDatabase();

            String where;
            String argWhere[];

            //remover o IdLogado
            if(MensageiroUtil.ContatoLogado != null) {

                where = new String(SQLiteHelperData.FIELD_CONTATO_ID + " <> ? " );
                argWhere = new String[]{MensageiroUtil.ContatoLogado.getId()};

            }else{
                where=null;
                argWhere=null;
            }

            cursor = database.query(SQLiteHelperData.TABLE_CONTATO, cols, where , argWhere,
                    null, null, "LOWER("+ SQLiteHelperData.FIELD_CONTATO_NOME+")");

            while (cursor.moveToNext()) {
                ContatoModel contato = new ContatoModel();

                contato.setId(cursor.getString(0));
                contato.setNome(cursor.getString(1));
                contato.setApelido(ContatoModel.UUID);

                lista.add(contato);
            }
        }catch (Exception e){

        }finally {
            //cursor.close();
            database.close();
        }

        return  lista;

    }
}
