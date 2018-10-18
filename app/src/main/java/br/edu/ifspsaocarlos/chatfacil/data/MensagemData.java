package br.edu.ifspsaocarlos.chatfacil.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.Html;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import br.edu.ifspsaocarlos.chatfacil.model.ContatoModel;
import br.edu.ifspsaocarlos.chatfacil.model.MensagemModel;
import br.edu.ifspsaocarlos.chatfacil.util.CriptografiaUtil;
import br.edu.ifspsaocarlos.chatfacil.util.MensageiroUtil;

public class MensagemData {

    private SQLiteDatabase database;
    private SQLiteHelperData dbHelper;

    private String[] cols = new String[]{
            SQLiteHelperData.FIELD_MSG_ID,
            SQLiteHelperData.FIELD_MSG_CONTATO_ID_ORIGEM,
            SQLiteHelperData.FIELD_MSG_CONTATO_ID_DESTINO,
            SQLiteHelperData.FIELD_MSG_ASSUNTO,
            SQLiteHelperData.FIELD_MSG_CORPO,
            SQLiteHelperData.FIELD_MSG_LIDA};


    public MensagemData(Context context) {
        dbHelper = new SQLiteHelperData(context);
    }

    public boolean SalvarMensagem(MensagemModel m) {
        try {
            //descriptografa a mensagem
            m.setCorpo(Html.fromHtml(m.getCorpo()).toString());
            m.setCorpo(CriptografiaUtil.decodificar(m.getCorpo(),m.getFator()));

            database = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(SQLiteHelperData.FIELD_MSG_ID, m.getId());
            values.put(SQLiteHelperData.FIELD_MSG_CONTATO_ID_ORIGEM, m.getOrigemId());
            values.put(SQLiteHelperData.FIELD_MSG_CONTATO_ID_DESTINO, m.getDestinoId());
            values.put(SQLiteHelperData.FIELD_MSG_ASSUNTO, m.getAssunto());
            values.put(SQLiteHelperData.FIELD_MSG_CORPO, m.getCorpo());
            values.put(SQLiteHelperData.FIELD_MSG_LIDA, m.isLida());

            long ret = database.insertOrThrow(SQLiteHelperData.TABLE_MSG, null, values);

            return (ret>0);

        } catch (Exception e) {

            return false;
        } finally {
            database.close();
        }
    }


    public boolean setaLida(String origemId,String destinoId){
        try{
            database = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();

            values.put(SQLiteHelperData.FIELD_MSG_LIDA, true);

            String where = SQLiteHelperData.FIELD_MSG_CONTATO_ID_ORIGEM+" = ? and "+SQLiteHelperData.FIELD_MSG_CONTATO_ID_DESTINO+" = ?";
            String[] argWhere = new String[]{origemId, destinoId};

            long ret = database.update(SQLiteHelperData.TABLE_MSG,values,where,argWhere);

            return (ret>0);

        }catch (Exception e) {

            return false;
        } finally {
            database.close();
        }
    }

    /**
     * pega o id da ultima mensagem enviada do destino pro usuario logado
     * @param IdDestino
     * @return id da ultima msg
     */
    public String ultimoIdMsg(String IdOrigem,String IdDestino){
        long id = 0;

        try{

            database = dbHelper.getReadableDatabase();

            String where ="("+SQLiteHelperData.FIELD_MSG_CONTATO_ID_ORIGEM+" = ? and "+SQLiteHelperData.FIELD_MSG_CONTATO_ID_DESTINO+" =?)";
            String argWhere[]=new String[]{IdOrigem,IdDestino};

            Cursor cursor = database.query(SQLiteHelperData.TABLE_MSG, new String[]{SQLiteHelperData.FIELD_MSG_ID}, where, argWhere,null, null, SQLiteHelperData.FIELD_MSG_ID+ " DESC limit 0,1");

            if(cursor.moveToNext()){
                id = cursor.getLong(0);
            }

        }catch (Exception e){
            e.printStackTrace();
        } finally {
            database.close();
        }

        return Long.toString(id+1);
    }



    public MensagemModel pegarUltimaMsg(ContatoModel contatoDestino) {
        MensagemModel m = new MensagemModel();

        m.setLida(true);

        Cursor cursor = null;

        try {

            database = dbHelper.getReadableDatabase();

            String where;
            String argWhere[];

            //remover o IdLogado
            if (MensageiroUtil.ContatoLogado != null) {

                where = new String("("+SQLiteHelperData.FIELD_MSG_CONTATO_ID_ORIGEM+" = ? and "+SQLiteHelperData.FIELD_MSG_CONTATO_ID_DESTINO+" =?) or ("+SQLiteHelperData.FIELD_MSG_CONTATO_ID_ORIGEM+" = ? and "+SQLiteHelperData.FIELD_MSG_CONTATO_ID_DESTINO+" = ?)");
                argWhere = new String[]{MensageiroUtil.ContatoLogado.getId(), contatoDestino.getId(), contatoDestino.getId(), MensageiroUtil.ContatoLogado.getId()};

            } else {
                where = null;
                argWhere = null;
            }

            /*cursor = database.query(SQLiteHelperData.TABLE_CONTATO, new String[]{SQLiteHelperData.FIELD_MSG_CORPO}, where, argWhere,
                    null, null, SQLiteHelperData.FIELD_CONTATO_NOME + " desc limit 0,1");*/

            cursor = database.query(SQLiteHelperData.TABLE_MSG, new String[]{SQLiteHelperData.FIELD_MSG_CONTATO_ID_ORIGEM,SQLiteHelperData.FIELD_MSG_CORPO,SQLiteHelperData.FIELD_MSG_LIDA}, where, argWhere,null, null, SQLiteHelperData.FIELD_MSG_ID+ " DESC limit 0,1");


            if (cursor.moveToNext()) {
                m.setOrigemId(cursor.getString(0));

                m.setCorpo(cursor.getString(1));

                //String ret = cursor.getString(1);
                m.setLida(1==cursor.getInt(2));
            }
        } catch (Exception e) {
            Log.d("ERRO",e.getMessage());
        } finally {
            if(cursor!=null) {
                cursor.close();
            }
            database.close();
        }


        return m;
    }

    public List<MensagemModel> ListarMensagens() {
        List<MensagemModel> lista = new ArrayList<>();

        if(MensageiroUtil.ContatoDestino!=null && MensageiroUtil.ContatoLogado!=null) {
            String idOri = MensageiroUtil.ContatoLogado.getId();
            String idDes = MensageiroUtil.ContatoDestino.getId();

            try {

                database=dbHelper.getReadableDatabase();

                String where = "( "+SQLiteHelperData.FIELD_MSG_CONTATO_ID_ORIGEM+" = ? and "+SQLiteHelperData.FIELD_MSG_CONTATO_ID_DESTINO +" = ? ) or ( "+SQLiteHelperData.FIELD_MSG_CONTATO_ID_ORIGEM+" = ? and "+SQLiteHelperData.FIELD_MSG_CONTATO_ID_DESTINO+" = ? )";
                String argWhere[] = new String[]{idOri,idDes,idDes,idOri};

                Cursor cursor = database.query(SQLiteHelperData.TABLE_MSG, cols, where , argWhere,
                        null, null, SQLiteHelperData.FIELD_MSG_ID);


                while (cursor.moveToNext()) {
                    MensagemModel m = new MensagemModel();

                    m.setId(cursor.getString(0));
                    m.setOrigemId(cursor.getString(1));
                    m.setDestinoId(cursor.getString(2));
                    m.setAssunto(cursor.getString(3));
                    m.setCorpo(cursor.getString(4));
                    m.setLida("1".equals(cursor.getString(4)));


                    lista.add(m);

                    /*SQLiteHelperData.FIELD_MSG_ID,
                            SQLiteHelperData.FIELD_MSG_CONTATO_ID_ORIGEM,
                            SQLiteHelperData.FIELD_MSG_CONTATO_ID_DESTINO,
                            SQLiteHelperData.FIELD_MSG_ASSUNTO,
                            SQLiteHelperData.FIELD_MSG_CORPO,
                            SQLiteHelperData.FIELD_MSG_LIDA*/
                }

            } catch (Exception e) {
                e.printStackTrace();

            } finally {
                database.close();
            }
        }
        return lista;
    }




    /*select msg_id, msg_corpo from mensagens
    where (msg_origem_id = 1 and msg_destino_id =2) or (msg_origem_id = 2 and msg_destino_id =1)

    order by msg_id  desc limit 0,1*/
}
