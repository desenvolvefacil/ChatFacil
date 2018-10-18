package br.edu.ifspsaocarlos.chatfacil.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import br.edu.ifspsaocarlos.chatfacil.MainActivity;
import br.edu.ifspsaocarlos.chatfacil.R;
import br.edu.ifspsaocarlos.chatfacil.api.MensagemApi;
import br.edu.ifspsaocarlos.chatfacil.data.ContatoData;
import br.edu.ifspsaocarlos.chatfacil.data.MensagemData;
import br.edu.ifspsaocarlos.chatfacil.model.ContatoModel;
import br.edu.ifspsaocarlos.chatfacil.model.MensagemModel;
import br.edu.ifspsaocarlos.chatfacil.util.CriptografiaUtil;
import br.edu.ifspsaocarlos.chatfacil.util.MensageiroUtil;
import br.edu.ifspsaocarlos.chatfacil.view.ChatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MensagemReceiver extends BroadcastReceiver {

    private Retrofit retrofit;
    private Gson gson;

    @Override
    public void onReceive(final Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //throw new UnsupportedOperationException("Not yet implemented");

        /*Date d = new Date();
        Log.i("Script", "Executou as: "+d.toString());
        Log.e("erro",d.toString());*/

        Log.i("BroadcastReceiver","Código Executado");

        //se o usuario é nulo, então o software não esta rodando
        if (MensageiroUtil.ContatoLogado == null) {
            //tenta buscar os dados do shared preferences
            long IdUsuarioLogado = context.getSharedPreferences(MensageiroUtil.ID_USUARIO_LOGADO, Context.MODE_PRIVATE).getLong(MensageiroUtil.ID_USUARIO_LOGADO, 0);
            Log.i("ID_LOGADO", String.valueOf(IdUsuarioLogado));


            //verifica se tem usuario logado
            if (IdUsuarioLogado > 0) {

                //busca todos usuarios do banco para verificar
                List<ContatoModel> lista = new ContatoData(context).listar();

                //for para verificar se tem novas mensagens
                for (ContatoModel c : lista) {
                    //usuario não envia msg pra ele mesmo
                    if (!c.getId().equals(String.valueOf(IdUsuarioLogado))) {

                        //busca o Id da Ultima msg
                        final String IdUltimaMsg = new MensagemData(context).ultimoIdMsg(c.getId(), String.valueOf(IdUsuarioLogado));

                        gson = new GsonBuilder().setLenient().create();

                        Retrofit.Builder builder = new Retrofit.Builder();
                        builder.baseUrl(MensageiroUtil.URL_BASE);
                        builder.addConverterFactory(GsonConverterFactory.create(gson));

                        retrofit = builder.build();


                        MensagemApi mensageiroApi = retrofit.create(MensagemApi.class);

                        Log.i("BUSCANDO", IdUltimaMsg + " | " + c.getId() + " | " + String.valueOf(IdUsuarioLogado));

                        mensageiroApi.getMensagems(IdUltimaMsg, c.getId(), String.valueOf(IdUsuarioLogado)).enqueue(new Callback<List<MensagemModel>>() {
                            @Override
                            public void onResponse(Call<List<MensagemModel>> call, Response<List<MensagemModel>> response) {

                                if (response.body().size() > 0) {
                                    ContatoModel dest = null;
                                    String MSG = "";

                                    for (MensagemModel m : response.body()) {
                                        //salva as msg no banco de dados local
                                        new MensagemData(context).SalvarMensagem(m);

                                        dest = m.getOrigem();
                                        MSG += m.getCorpo() + "\n";
                                    }

                                    //***colocar na intent o id do usuario que envio a msg pra abrir posteriormente
                                    NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                                    Intent intent = new Intent(context, ChatActivity.class);
                                    intent.putExtra(MensageiroUtil.ID_USUARIO_DESTINO, dest.getId());
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                    PendingIntent p = PendingIntent.getActivity(context, Integer.parseInt(dest.getId()), intent, 0);
                                    //PendingIntent.FLAG_UPDATE_CURRENT


                                    Notification.Builder builder = new Notification.Builder(context);
                                    builder.setTicker(dest.getId());
                                    builder.setContentTitle("Nova msg de " + CriptografiaUtil.decodificar(dest.getNome(), ContatoModel.FATOR_CRIPTOGRAFIA));
                                    builder.setContentText(MSG);
                                    builder.setSmallIcon(R.drawable.chat_facil_notification);
                                    builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.chat_facil_notification));
                                    builder.setContentIntent(p);

                                    Notification n = builder.build();
                                    n.vibrate = new long[]{150, 300, 150, 600};
                                    n.flags = Notification.FLAG_AUTO_CANCEL;
                                    //nm.notify(R.drawable.chat_facil_icon, n);

                                    nm.notify(dest.getId(), Integer.parseInt(dest.getId()), n);

                                    try {
                                        Uri som = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                        Ringtone toque = RingtoneManager.getRingtone(context, som);
                                        toque.play();
                                    } catch (Exception e) {
                                    }

                                    try{
                                        Vibrator v = (Vibrator)context.getSystemService(context.VIBRATOR_SERVICE);
                                        v.vibrate(500);
                                    } catch (Exception e) {
                                    }

                                }

                            }

                            @Override
                            public void onFailure(Call<List<MensagemModel>> call, Throwable t) {
                                Log.e("Erro Receiver", "Erro ao buscar contatos");
                            }
                        });


                    }
                }

            }

        }
    }


}
