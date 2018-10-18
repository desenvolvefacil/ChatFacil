package br.edu.ifspsaocarlos.chatfacil.util;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;

import br.edu.ifspsaocarlos.chatfacil.model.ContatoModel;

public final class MensageiroUtil {

    /**
     * WS criado para testes, já que tem crianças na sala que ficam apagando e editando os contatos e mensagens
     */
    //public static final String URL_BASE = "http://testefacil.tk/mensageiro/";
    public static final String URL_BASE = "http://www.nobile.pro.br/sdm4/mensageiro/";

    public static final String ID_USUARIO_LOGADO = "br.edu.ifspsaocarlos.chatfacil.ID_USUARIO_LOGADO";
    public static final String ID_USUARIO_DESTINO = "br.edu.ifspsaocarlos.chatfacil.ID_USUARIO_DESTINO";
    public static final long TEMPO_BUSCA_CHAT = 5000;
    public static final long TEMPO_BUSCA_NOVAS_MSG = 15000;

    public static ContatoModel ContatoLogado = null;
    public static ContatoModel ContatoDestino = null;

    public static void logOff(Context context){
        //remove o usuario logado
        MensageiroUtil.ContatoLogado = null;
        MensageiroUtil.ContatoDestino = null;

        //remove o usuario logado do sharedPreferences
        SharedPreferences database =  context.getSharedPreferences(ID_USUARIO_LOGADO, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = database.edit();
        editor.putLong(ID_USUARIO_LOGADO ,0);
        editor.commit();

        //System.exit(0);

        //cancela todas nofificações ativas
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancelAll();

    }

}
