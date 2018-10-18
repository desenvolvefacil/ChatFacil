package br.edu.ifspsaocarlos.chatfacil.view;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Calendar;
import java.util.List;

import br.edu.ifspsaocarlos.chatfacil.MainActivity;
import br.edu.ifspsaocarlos.chatfacil.R;
import br.edu.ifspsaocarlos.chatfacil.adapter.ListaContatoAdapter;
import br.edu.ifspsaocarlos.chatfacil.api.MensagemApi;
import br.edu.ifspsaocarlos.chatfacil.data.ContatoData;
import br.edu.ifspsaocarlos.chatfacil.data.MensagemData;
import br.edu.ifspsaocarlos.chatfacil.model.ContatoModel;
import br.edu.ifspsaocarlos.chatfacil.model.MensagemModel;
import br.edu.ifspsaocarlos.chatfacil.util.CriptografiaUtil;
import br.edu.ifspsaocarlos.chatfacil.util.MensageiroUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ListaContatosActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ListView contatosLV;
    private List<ContatoModel> listaContatos;
    private ListaContatoAdapter listaContatoAdapter;
    private CarregaNovasMensagensThread carregaMensagensThread;

    private boolean appAberta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_contatos);

        appAberta = true;

        //verifica se tem contato logado
        if (MensageiroUtil.ContatoLogado == null) {
            finish();
        } else {


            //busca todas mensagens que o contato logado já enviou
            Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    //Log.i("SCRIPT","Buscando MSG enviadas pelo contato: "+MensageiroUtil.ContatoLogado.getNome());

                    List<ContatoModel> lst = new ContatoData(getApplicationContext()).listar();

                    Gson gson = new GsonBuilder().setLenient().create();

                    Retrofit.Builder builder = new Retrofit.Builder();
                    builder.baseUrl(MensageiroUtil.URL_BASE);
                    builder.addConverterFactory(GsonConverterFactory.create(gson));

                    Retrofit retrofit = builder.build();

                    MensagemApi mensagemApi = retrofit.create(MensagemApi.class);

                    for (ContatoModel c : lst) {
                        String ultimoIdEnviado = new MensagemData(getApplicationContext()).ultimoIdMsg(MensageiroUtil.ContatoLogado.getId(), c.getId());

                        //Log.i("Buscando",ultimoIdEnviado+" | "+MensageiroUtil.ContatoLogado.getId()+" | "+c.getId());

                        mensagemApi.getMensagems(ultimoIdEnviado, MensageiroUtil.ContatoLogado.getId(), c.getId()).enqueue(new Callback<List<MensagemModel>>() {
                            @Override
                            public void onResponse(Call<List<MensagemModel>> call, Response<List<MensagemModel>> response) {
                                if (response.body().size() > 0) {

                                    for (MensagemModel m : response.body()) {
                                        new MensagemData(getApplicationContext()).SalvarMensagem(m);

                                        //Log.d("NM", m.getCorpo());
                                    }

                                    AtualizarListView();
                                }
                            }

                            @Override
                            public void onFailure(Call<List<MensagemModel>> call, Throwable t) {

                            }
                        });
                    }

                }
            });


            //veriica se tem usuario destino

            contatosLV = findViewById(R.id.lv_lista_contatos);

               /* listaContatos = new ContatoData(getApplicationContext()).listar();

                listaContatoAdapter = new ListaContatoAdapter(this,listaContatos);*/

            //contatosLV.setDivider(null);
            contatosLV.setDividerHeight(10);

            //contatosLV.setAdapter(listaContatoAdapter);

            contatosLV.setOnItemClickListener(this);

            this.setTitle("Olá " + MensageiroUtil.ContatoLogado.getNome());

        }

    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.novo_contato) {

            Intent intent = new Intent(getApplicationContext(), CadastraContatoActivity.class);

            startActivityForResult(intent, ContatoModel.CADASTRA_CONTATO);

            return true;
        }

        if (id == R.id.sair) {
            MensageiroUtil.logOff(getApplicationContext());
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        appAberta = true;
        AtualizarListView();


        //ativa a thread que fica buscando novas msg
        carregaMensagensThread = new CarregaNovasMensagensThread();
        carregaMensagensThread.start();


        //verifica se tem usuario destino
        if (MensageiroUtil.ContatoDestino != null) {
            //se houver, sigifica que o chegou nova msg no alarm, então abre a tela de msg
            Intent intent = new Intent(this, ChatActivity.class);
            startActivity(intent);
        }

    }

    @Override
    protected void onDestroy() {
        appAberta = false;
        try {
            //Log.i("Thread","parando thread que busca novas msg");
            carregaMensagensThread.interrupt();
        } catch (Exception ex) {
        }

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ContatoModel.CADASTRA_CONTATO && resultCode == RESULT_OK) {
            //se cadastrou novo contato atualiza a lista
            AtualizarListView();
        }
    }

    public void AtualizarListView() {
        //Log.i("AtualizaListaView","AtualizaListaView");

        listaContatos = new ContatoData(getApplicationContext()).listar();

        listaContatoAdapter = new ListaContatoAdapter(this, listaContatos);

        contatosLV.setAdapter(listaContatoAdapter);

    }

    @Override
    public void onBackPressed() {
        //remove o contato logado
        //MensageiroUtil.ContatoLogado=null;
        //super.onBackPressed();

        MensageiroUtil.logOff(getApplicationContext());

        //abre a tela de lista de contatos
        Intent intent = new Intent(getApplicationContext(), ListaContatosActivity.class);
        startActivity(intent);


        finish();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        /*ContatoModel c = new ContatoModel();
        c.setNome("ZZZ");

        MensageiroUtil.listaContatos.add(c);
        MensageiroUtil.listaContatoAdapter.notifyDataSetChanged();*/

        MensageiroUtil.ContatoDestino = listaContatos.get(position);

        Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);

    }


    //classe para buscar as msg novas no servidor
    private class CarregaNovasMensagensThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                while (true) {
                    //Log.e("Novas MGS", "Fazendo download de novas msg");

                    String IdUsuarioLogado = MensageiroUtil.ContatoLogado.getId();

                    for (ContatoModel c : listaContatos) {

                        if (MensageiroUtil.ContatoDestino == null || !c.getId().equals(MensageiroUtil.ContatoDestino.getId())) {

                            //busca o Id da Ultima msg
                            final String IdUltimaMsg = new MensagemData(getApplicationContext()).ultimoIdMsg(c.getId(), String.valueOf(IdUsuarioLogado));

                            Gson gson = new GsonBuilder().setLenient().create();

                            Retrofit.Builder builder = new Retrofit.Builder();
                            builder.baseUrl(MensageiroUtil.URL_BASE);
                            builder.addConverterFactory(GsonConverterFactory.create(gson));

                            Retrofit retrofit = builder.build();


                            MensagemApi mensageiroApi = retrofit.create(MensagemApi.class);

                            //Log.i("BUSCANDO", IdUltimaMsg + " | " + c.getId() + " | " + String.valueOf(IdUsuarioLogado));

                            mensageiroApi.getMensagems(IdUltimaMsg, c.getId(), String.valueOf(IdUsuarioLogado)).enqueue(new Callback<List<MensagemModel>>() {
                                @Override
                                public void onResponse(Call<List<MensagemModel>> call, Response<List<MensagemModel>> response) {

                                    if (response.body().size() > 0) {
                                        ContatoModel dest = null;
                                        String MSG = "";

                                        for (MensagemModel m : response.body()) {
                                            //salva as msg no banco de dados local
                                            new MensagemData(getApplicationContext()).SalvarMensagem(m);

                                            dest = m.getOrigem();
                                            MSG += m.getCorpo() + "\n";
                                        }

                                        //***colocar na intent o id do usuario que envio a msg pra abrir posteriormente
                                        NotificationManager nm = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);


                                        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                                        intent.putExtra(MensageiroUtil.ID_USUARIO_DESTINO, dest.getId());

                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                        PendingIntent p = PendingIntent.getActivity(getApplicationContext(), Integer.parseInt(dest.getId()), intent, 0);
                                        //PendingIntent.FLAG_UPDATE_CURRENT


                                        Notification.Builder builder = new Notification.Builder(getApplicationContext());
                                        builder.setTicker(dest.getId());
                                        builder.setContentTitle("Nova msg de " + CriptografiaUtil.decodificar(dest.getNome(), ContatoModel.FATOR_CRIPTOGRAFIA));
                                        builder.setContentText(MSG);
                                        builder.setSmallIcon(R.drawable.chat_facil_notification);
                                        builder.setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.chat_facil_notification));
                                        builder.setContentIntent(p);

                                        Notification n = builder.build();
                                        n.vibrate = new long[]{150, 300, 150, 600};
                                        n.flags = Notification.FLAG_AUTO_CANCEL;
                                        //nm.notify(R.drawable.chat_facil_icon, n);

                                        nm.notify(dest.getId(), Integer.parseInt(dest.getId()), n);

                                        try {
                                            Uri som = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                            Ringtone toque = RingtoneManager.getRingtone(getApplicationContext(), som);
                                            toque.play();
                                        } catch (Exception e) {
                                        }

                                        try {
                                            Vibrator v = (Vibrator) getSystemService(getApplicationContext().VIBRATOR_SERVICE);
                                            v.vibrate(500);
                                        } catch (Exception e) {
                                        }

                                        AtualizarListView();
                                    }

                                }

                                @Override
                                public void onFailure(Call<List<MensagemModel>> call, Throwable t) {
                                    //Log.e("Erro Receiver", "Erro ao buscar contatos");
                                }
                            });
                        }
                    }

                    try {
                        sleep(MensageiroUtil.TEMPO_BUSCA_NOVAS_MSG);
                    } catch (Exception e) {
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    @Override
    protected void onStop() {

        Log.e("", "STOP");

        //ativa o alarm para continuar buscando as novas msg
        boolean alarmeAtivo = (PendingIntent.getBroadcast(this, 0, new Intent("ALARME_DISPARADO"), PendingIntent.FLAG_NO_CREATE) == null);

        if (alarmeAtivo) {
            Log.i("Script", "Ativando Alarm");

            Intent intent = new Intent("ALARME_DISPARADO");
            PendingIntent p = PendingIntent.getBroadcast(this, 0, intent, 0);

            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(System.currentTimeMillis());
            c.add(Calendar.SECOND, 1);

            AlarmManager alarme = (AlarmManager) getSystemService(ALARM_SERVICE);
            //alarme.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), 50, p);
            alarme.setInexactRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), 60000, p);

        } else {
            Log.i("Script", "Alarme já ativo");
        }

        super.onStop();
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e("", "Restart");



        /*
        boolean alarmeAtivo = (PendingIntent.getBroadcast(this, 0, new Intent("ALARME_DISPARADO"), PendingIntent.FLAG_NO_CREATE) == null);

        //desativa o alarm
        if(!alarmeAtivo){
            Log.i("Script", "Parando o Alarm");

            Intent intent = new Intent("ALARME_DISPARADO");
            PendingIntent p = PendingIntent.getBroadcast(this, 0, intent, 0);

            AlarmManager alarme = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarme.cancel(p);
        }
        else{
            Log.i("Script", "Nenhum alarm ativo");
        }
        */
    }


}
