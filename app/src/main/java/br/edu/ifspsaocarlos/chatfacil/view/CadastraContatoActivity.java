package br.edu.ifspsaocarlos.chatfacil.view;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.Charset;

import br.edu.ifspsaocarlos.chatfacil.MainActivity;
import br.edu.ifspsaocarlos.chatfacil.api.ContatoApi;
import br.edu.ifspsaocarlos.chatfacil.data.ContatoData;
import br.edu.ifspsaocarlos.chatfacil.util.CriptografiaUtil;
import br.edu.ifspsaocarlos.chatfacil.util.MensageiroUtil;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import com.google.gson.Gson;

import android.support.design.widget.Snackbar;

import br.edu.ifspsaocarlos.chatfacil.R;
import br.edu.ifspsaocarlos.chatfacil.model.ContatoModel;

public class CadastraContatoActivity extends AppCompatActivity {

    private EditText nomeET;

    private Retrofit retrofit;
    private Gson gson;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastra_contato);

        nomeET = findViewById(R.id.et_contato_nome);

        gson = new Gson();

        retrofit = new Retrofit.Builder().baseUrl(MensageiroUtil.URL_BASE).build();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        finish();
    }

    public void SalvarContato(View view){

       if(view.getId()==R.id.bt_contato_salvar) {
           //pega os campos da tela
           String nome = nomeET.getText().toString();

           if (nome.length() > 3) {
               nomeET.setBackgroundColor(Color.WHITE);

               final ContatoModel contato = new ContatoModel();

               //criptografa o nome
               nome = CriptografiaUtil.codificar(nome, ContatoModel.FATOR_CRIPTOGRAFIA);

               nome = TextUtils.htmlEncode(nome);


               contato.setNome(nome);
               contato.setApelido(ContatoModel.UUID);

               RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), gson.toJson(contato));

               ContatoApi mensageiroApi = retrofit.create(ContatoApi.class);

                //cadastra no webservice
               mensageiroApi.postContato(requestBody).enqueue(new Callback<ResponseBody>() {

                   @Override
                   public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                       try {
                           ContatoModel contatoCadastrado = gson.fromJson(response.body().string(), ContatoModel.class);

                           Toast.makeText(getApplicationContext(), "ID: " + contato.getId(), Toast.LENGTH_LONG);

                           //envia o contato cadastrado no banco de dados
                           new ContatoData(getApplicationContext()).SalvarContato(contatoCadastrado);


                           Intent resultIntent = new Intent();
                           resultIntent.putExtra(MainActivity.ULTIMO_CODIGO_CADASTRADO, contatoCadastrado.getId());
                           //avisa que foi cadastrado um novo contato
                           setResult(RESULT_OK,resultIntent);

                           finish();

                       } catch (IOException e) {
                           e.printStackTrace();
                       }
                   }

                   @Override
                   public void onFailure(Call<ResponseBody> call, Throwable t) {
                       Toast.makeText(getApplicationContext(), "Erro ao cadastrar Contato", Toast.LENGTH_LONG).show();
                   }

               });


           } else {
               nomeET.setBackgroundColor(Color.RED);
               Snackbar.make(view, "Nome deve conter 3 caracteres", Snackbar.LENGTH_LONG)
                       .setAction("Action", null).show();
           }
       }
    }
}
