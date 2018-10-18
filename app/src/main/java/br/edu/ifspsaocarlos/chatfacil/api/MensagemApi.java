package br.edu.ifspsaocarlos.chatfacil.api;

import br.edu.ifspsaocarlos.chatfacil.model.MensagemModel;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

import java.util.List;

public interface MensagemApi {
    @POST("mensagem")
    Call<ResponseBody> postMensagem(@Body RequestBody novaMsg);

    @GET("rawmensagens/{ultimaMensagemId}/{origemId}/{destinoId}")
    Call<List<MensagemModel>> getMensagems(@Path("ultimaMensagemId") String ultimaMensagemId,
                                           @Path("origemId") String origemId, @Path("destinoId")String destinoId);
}
