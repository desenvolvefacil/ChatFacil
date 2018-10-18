package br.edu.ifspsaocarlos.chatfacil;


import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import br.edu.ifspsaocarlos.chatfacil.api.ContatoApi;
import br.edu.ifspsaocarlos.chatfacil.data.ContatoData;
import br.edu.ifspsaocarlos.chatfacil.model.ContatoModel;
import br.edu.ifspsaocarlos.chatfacil.util.CriptografiaUtil;
import br.edu.ifspsaocarlos.chatfacil.util.MensageiroUtil;
import br.edu.ifspsaocarlos.chatfacil.view.CadastraContatoActivity;
import br.edu.ifspsaocarlos.chatfacil.view.ListaContatosActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private List<ContatoModel> listaContatos = new ArrayList<>();

    private Spinner usuariosSP;
    private Retrofit retrofit;
    private Gson gson;

    private String codigoCadastrado = "0";

    public static final String  ULTIMO_CODIGO_CADASTRADO = "ULTIMO_CODIGO_CADASTRADO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        usuariosSP = findViewById(R.id.sp_usuarios);


        Snackbar.make(findViewById(android.R.id.content), "Buscando Contatos no servidor web", Snackbar.LENGTH_INDEFINITE)
                .setAction("Atualizando", null).show();

        AtualizarSpinner();


        //Busca os contatos no WebService
        gson = new GsonBuilder().setLenient().create();

        Retrofit.Builder builder = new Retrofit.Builder();
        builder.baseUrl(MensageiroUtil.URL_BASE);
        builder.addConverterFactory(GsonConverterFactory.create(gson));

        retrofit = builder.build();


        ContatoApi mensageiroApi = retrofit.create(ContatoApi.class);

        mensageiroApi.getContatos().enqueue(new Callback<List<ContatoModel>>() {
            @Override
            public void onResponse(Call<List<ContatoModel>> call, Response<List<ContatoModel>> response) {

                boolean novoContato = false;

                for (ContatoModel c : response.body()) {
                    //verifica se o ID é do APP
                    if (c.getApelido().equals(ContatoModel.UUID)) {
                        if (new ContatoData(getApplicationContext()).SalvarContato(c)) {
                            novoContato = true;
                        }
                    }
                }

                //se existirem novos contatos atualiza a lista
                if (novoContato) {
                    AtualizarSpinner();
                }

                Snackbar.make(findViewById(android.R.id.content), "Atualização completa", Snackbar.LENGTH_SHORT)
                        .setAction("Atualizando", null).show();
            }

            @Override
            public void onFailure(Call<List<ContatoModel>> call, Throwable t) {
                Snackbar.make(findViewById(android.R.id.content), "Erro ao atualizar", Snackbar.LENGTH_SHORT)
                        .setAction("Atualizando", null).show();
            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/

                Intent intent = new Intent(getApplicationContext(), CadastraContatoActivity.class);

                startActivityForResult(intent, ContatoModel.CADASTRA_CONTATO);


            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        //verifica se veio algum usuario destino do receiver
        /*String IdUsuarioDestino = getIntent().getStringExtra(MensageiroUtil.ID_USUARIO_DESTINO);
        getIntent().removeExtra(MensageiroUtil.ID_USUARIO_DESTINO);

        if (IdUsuarioDestino != null) {
            MensageiroUtil.ContatoDestino = new ContatoData(getApplicationContext()).BuscarUsuario(IdUsuarioDestino);

            Log.i("Usuario Destino", "Usuario Destino = " + MensageiroUtil.ContatoDestino.toString());
        }*/


        //verificar se tem algum usuario já logado
        MensageiroUtil.ContatoLogado = new ContatoData(getApplicationContext()).BuscarUsuarioLogado();
        //se houver usuario logado, redireciona para tela inicial
        if (MensageiroUtil.ContatoLogado != null) {
            Intent intent = new Intent(getApplicationContext(), ListaContatosActivity.class);
            startActivity(intent);
        } else {

            AtualizarSpinner();
        }


    }

    public void AtualizarSpinner() {
        listaContatos = new ContatoData(getApplicationContext()).listar();

        List<String> lista = new ArrayList<>();
        //adiciona o vazio a lista
        lista.add("Selecione...");


        int position=0;

        for (ContatoModel c : listaContatos) {
            lista.add(c.getNome());
            if(c.getId().equals(codigoCadastrado)){
                //seta a posição pro ultimo contato cadastrado
                position = lista.size()-1;
            }
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, lista);
        usuariosSP.setAdapter(arrayAdapter);

        usuariosSP.setSelection(position);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ContatoModel.CADASTRA_CONTATO && resultCode == RESULT_OK) {
            //se cadastrou novo contato atualiza a lista

            codigoCadastrado = data.getStringExtra(ULTIMO_CODIGO_CADASTRADO);

            AtualizarSpinner();
        }
    }

    public void logar(View v) {
        if (v.getId() == R.id.bt_logar) {
            if (usuariosSP.getSelectedItemPosition() > 0) {
                //tira 1 na posição para ignorar o "Selecione.."
                MensageiroUtil.ContatoLogado = listaContatos.get(usuariosSP.getSelectedItemPosition() - 1);

                //guarda o contato lgoado no shared preferences
                SharedPreferences sharedPreferences = this.getSharedPreferences(MensageiroUtil.ID_USUARIO_LOGADO, MODE_PRIVATE);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong(MensageiroUtil.ID_USUARIO_LOGADO, Long.parseLong(MensageiroUtil.ContatoLogado.getId()));
                editor.commit();




                //abre a janela principal do programa
                Intent intent = new Intent(getApplicationContext(), ListaContatosActivity.class);
                startActivity(intent);
            } else {
                Snackbar.make(v, "Selecion um perfil ou cadastre um novo.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    }



    /*
    @Override
    protected void onDestroy() {


        Log.e("STOP", "fechando a classe");

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
            alarme.setInexactRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), 5000, p);

        } else {
            Log.i("Script", "Alarme já ativo");
        }

        Log.i("Script","Fechando a Classe");

        super.onDestroy();
    }
    */

    /*
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //seta o usuario logado
        MensageiroUtil.ContatoLogado = listaContatos.get(position);

        //abre a janela principal do programa
        Intent intent = new Intent(getApplicationContext(), ListaContatosActivity.class);
        startActivity(intent);

    }
    */
}
