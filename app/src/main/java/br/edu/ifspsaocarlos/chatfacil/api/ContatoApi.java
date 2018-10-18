package br.edu.ifspsaocarlos.chatfacil.api;

import java.util.List;

import br.edu.ifspsaocarlos.chatfacil.model.ContatoModel;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ContatoApi {
    @GET("rawcontatos")
    Call<List<ContatoModel>> getContatos();

    @POST("contato")
    Call<ResponseBody> postContato(@Body RequestBody novoContato);

}
