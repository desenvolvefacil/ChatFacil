package br.edu.ifspsaocarlos.chatfacil.view;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import br.edu.ifspsaocarlos.chatfacil.MainActivity;
import br.edu.ifspsaocarlos.chatfacil.R;
import br.edu.ifspsaocarlos.chatfacil.adapter.ListaMsgAdapter;
import br.edu.ifspsaocarlos.chatfacil.api.MensagemApi;
import br.edu.ifspsaocarlos.chatfacil.data.ContatoData;
import br.edu.ifspsaocarlos.chatfacil.data.MensagemData;
import br.edu.ifspsaocarlos.chatfacil.model.MensagemModel;
import br.edu.ifspsaocarlos.chatfacil.util.CriptografiaUtil;
import br.edu.ifspsaocarlos.chatfacil.util.MensageiroUtil;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatActivity extends AppCompatActivity implements View.OnKeyListener {

    private EditText msgET;
    private RecyclerView recyclerView;
    private ListaMsgAdapter adapter;
    private boolean baixando =false;
    private  boolean telaAberta = true;

    private  boolean salvandoMsg = false;

    private  CarregaMensagensThread carregaMensagensThread;
    //private  Intent chatServiceIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        recyclerView = findViewById(R.id.rv_msgs);

        msgET = findViewById(R.id.et_msg_chat);
        msgET.setOnKeyListener(this);




        //setupRecycler();


        //Busca o id da ultima msg enviada
        /*
        String ultimoIdMsg = new MensagemData(getApplicationContext()).ultimoIdMsg(MensageiroUtil.ContatoDestino.getId(),MensageiroUtil.ContatoLogado.getId());

        MensagemService.ultimoId = ultimoIdMsg;
        MensagemService.recyclerView = recyclerView;
        MensagemService.adapter = adapter;


        //cria thread pra buscar msgs novas no servidor (passando ultimo id, adapter, e recycler view)
        chatServiceIntent = new Intent(getApplicationContext(),MensagemService.class );


        startService(chatServiceIntent);*/




    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        String IdUsuarioDestino = getIntent().getStringExtra(MensageiroUtil.ID_USUARIO_DESTINO);
        //getIntent().removeExtra(MensageiroUtil.ID_USUARIO_DESTINO);

        if (IdUsuarioDestino != null) {
            MensageiroUtil.ContatoDestino = new ContatoData(getApplicationContext()).BuscarUsuario(IdUsuarioDestino);

            Log.i("Usuario Destino", "Usuario Destino = " + MensageiroUtil.ContatoDestino.toString());

            //recupera o usuario logado
            MensageiroUtil.ContatoLogado = new ContatoData(getApplicationContext()).BuscarUsuarioLogado();

        }

        try {
            //cancela a notificação da msg caso tenha
            NotificationManager nm = (NotificationManager) getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
            nm.cancel(MensageiroUtil.ContatoDestino.getId(), Integer.parseInt(MensageiroUtil.ContatoDestino.getId()));

            //nm.cancelAll();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.setTitle(".::"+MensageiroUtil.ContatoDestino.getNome()+"::.");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeButtonEnabled(true);

        setupRecycler();

        telaAberta = true;
        salvandoMsg=false;

        //ativa a thread que fica buscando novas msg
        if(carregaMensagensThread==null) {
            carregaMensagensThread = new CarregaMensagensThread();
        }

        carregaMensagensThread.start();

        //seta todas mensagens recebidas como lida
        new MensagemData(getApplicationContext()).setaLida(MensageiroUtil.ContatoDestino.getId(),MensageiroUtil.ContatoLogado.getId());

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        MensageiroUtil.ContatoDestino = null;

        //abre a tela de lista de contatos
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(intent);


        finish();
    }

    public void enviarMsg(View view) {

        //adapter.AtualizaListaMsg();

        //recyclerView.scrollToPosition(adapter.getItemCount()-1);
        if (!salvandoMsg) {
            salvandoMsg = true;
            if (view.getId() == R.id.ib_enviar_msg || view == msgET) {
                String msg = msgET.getText().toString();

                if (msg != "") {
                    //Esconde o teclado
                    //((InputMethodManager) getSystemService(view.getContext().INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(msgET.getWindowToken(), 0);

                    //mostra o teclado
                    //((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(editText, 0);
                    final MensagemModel mensagemModel = new MensagemModel();
                    mensagemModel.setLida(false);
                    mensagemModel.setAssunto(MensagemModel.TIPO_TEXTO);
                    mensagemModel.setId("0");

                    mensagemModel.setOrigem(MensageiroUtil.ContatoLogado);
                    mensagemModel.setDestino(MensageiroUtil.ContatoDestino);


                    //criptografa a mensagem
                    mensagemModel.setCorpo(CriptografiaUtil.codificar(msg, mensagemModel.getFator()));
                    //codifica para html
                    mensagemModel.setCorpo(TextUtils.htmlEncode(mensagemModel.getCorpo()));

                    Gson gson = new GsonBuilder().setLenient().create();

                    Retrofit.Builder builder = new Retrofit.Builder();
                    builder.baseUrl(MensageiroUtil.URL_BASE);
                    builder.addConverterFactory(GsonConverterFactory.create(gson));

                    Retrofit retrofit = builder.build();

                    MensagemApi mensagemApi = retrofit.create(MensagemApi.class);


                    RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), gson.toJson(mensagemModel));

                    mensagemApi.postMensagem(requestBody).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            //mostrar meg e atualizar recylcer view de mensagens

                            try {
                                Gson gson = new GsonBuilder().setLenient().create();

                                MensagemModel retorno = gson.fromJson(response.body().string(), MensagemModel.class);

                                long id = Integer.parseInt(retorno.getId());

                                if (id > 0) {

                                    mensagemModel.setId(retorno.getId());

                                    //cadastra a msg no banco de dados
                                    if (new MensagemData(getApplicationContext()).SalvarMensagem(mensagemModel)) {
                                        //esconde o teclado
                                        ((InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(msgET.getWindowToken(), 0);


                                        //Atualiza o recycles view e mostra a mesagem

                                        adapter.AtualizaListaMsg();

                                        recyclerView.scrollToPosition(adapter.getItemCount() - 1);

                                        msgET.setText("");

                                        salvandoMsg = false;
                                    }
                                } else {
                                    Snackbar.make(findViewById(android.R.id.content), "Erro ao atualizar Banco", Snackbar.LENGTH_LONG)
                                            .setAction("Erro ao enviar, verifique a conexão com a internet e tente novamente.", null).show();

                                    salvandoMsg = false;
                                }
                            } catch (Exception e) {
                                Snackbar.make(findViewById(android.R.id.content), "Erro ao atualizar Servidor", Snackbar.LENGTH_LONG)
                                        .setAction("Erro ao enviar, verifique a conexão com a internet e tente novamente.", null).show();

                                salvandoMsg = false;
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            //mostrar snack bar com erro
                            salvandoMsg = false;

                            Snackbar.make(findViewById(android.R.id.content), "Erro ao atualizar", Snackbar.LENGTH_LONG)
                                    .setAction("Erro ao enviar, verifique a conexão com a internet e tente novamente.", null).show();
                        }
                    });

                }
            }
        }
    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent event) {

        if(view==msgET){
            if(event.getAction()==KeyEvent.ACTION_UP && keyCode==KeyEvent.KEYCODE_ENTER){
                enviarMsg(view);

                return true;
            }
        }

        return false;
    }

    @Override
    protected void onDestroy() {

        //stopService(chatServiceIntent);
        telaAberta = false;

        try {
            //Log.i("Thread","parando thread que busca novas msg");
            carregaMensagensThread.interrupt();

        }catch (Exception ex){}

        super.onDestroy();
    }

    @Override
    protected void onStop() {

        try {
            //Log.i("Thread","parando thread que busca novas msg");
            carregaMensagensThread.interrupt();

            getIntent().putExtra(MensageiroUtil.ID_USUARIO_DESTINO,MensageiroUtil.ContatoDestino.getId());
            MensageiroUtil.ContatoDestino = null;
        }catch (Exception ex){}

        telaAberta= false;



        super.onStop();
    }

    private void setupRecycler() {
        // Configurando o gerenciador de layout para ser uma lista.
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Adiciona o adapter que irá anexar os objetos à lista.
        // Está sendo criado com lista vazia, pois será preenchida posteriormente.
        adapter = new ListaMsgAdapter(getApplicationContext());
        recyclerView.setAdapter(adapter);

        //posiciona o recycler view para a última msg
        recyclerView.scrollToPosition(adapter.getItemCount()-1);

        // Configurando um dividr entre linhas, para uma melhor visualização.
        //recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }


    private class CarregaMensagensThread extends Thread{
        @Override
        public void run() {
            super.run();

            try {
                while (telaAberta) {

                    Log.e("baixando CHAT: ", (String.valueOf(baixando)));
                    if (!baixando) {
                        if (MensageiroUtil.ContatoLogado != null && MensageiroUtil.ContatoDestino != null) {
                            //busca todas mensagens recebidas do contato
                            baixando = true;

                            //busca o ultimo id enviado
                            String ultimoIdMsg = new MensagemData(getApplicationContext()).ultimoIdMsg(MensageiroUtil.ContatoDestino.getId(), MensageiroUtil.ContatoLogado.getId());

                            Gson gson = new GsonBuilder().setLenient().create();

                            Retrofit.Builder builder = new Retrofit.Builder();
                            builder.baseUrl(MensageiroUtil.URL_BASE);
                            builder.addConverterFactory(GsonConverterFactory.create(gson));

                            Retrofit retrofit = builder.build();

                            MensagemApi mensagemApi = retrofit.create(MensagemApi.class);

                            mensagemApi.getMensagems(ultimoIdMsg, MensageiroUtil.ContatoDestino.getId(), MensageiroUtil.ContatoLogado.getId()).enqueue(new Callback<List<MensagemModel>>() {
                                @Override
                                public void onResponse(Call<List<MensagemModel>> call, Response<List<MensagemModel>> response) {
                                    List<MensagemModel> lista = response.body();

                                    if (lista.size() > 0) {
                                        MensagemData data = new MensagemData(recyclerView.getContext());

                                        for (MensagemModel m : lista) {

                                            m.setLida(true);

                                            if (data.SalvarMensagem(m)) {
                                                //atualiza o adapter e move para a mensagem recebida
                                                adapter.AtualizaListaMsg();
                                                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                                            }
                                        }

                                        try{
                                            Vibrator v = (Vibrator)getSystemService(getApplicationContext().VIBRATOR_SERVICE);
                                            v.vibrate(500);
                                        } catch (Exception e) {
                                        }
                                    }

                                    baixando = false;

                                }

                                @Override
                                public void onFailure(Call<List<MensagemModel>> call, Throwable t) {
                                    baixando = false;
                                }
                            });

                        } else {

                        }
                    }

                    //da uma pausa antes da proxima execução
                    try {
                        sleep(MensageiroUtil.TEMPO_BUSCA_CHAT);
                    }catch (Exception e){baixando = false;}
                }

            } catch (Exception e) {
                //e.printStackTrace();
            }

        }
    }
}
