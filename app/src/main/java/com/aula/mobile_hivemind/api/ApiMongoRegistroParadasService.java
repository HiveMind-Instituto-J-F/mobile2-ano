package com.aula.mobile_hivemind.api;

import com.aula.mobile_hivemind.dto.RegistroParadaRequestDTO;
import com.aula.mobile_hivemind.dto.RegistroParadaResponseDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiMongoRegistroParadasService {
    @GET("api-mongo/selecionar")
    Call<List<RegistroParadaResponseDTO>> getAllRegistros();

    @POST("api-mongo/inserir")
    Call<okhttp3.ResponseBody> criarRegistro(@Body RegistroParadaRequestDTO requestDTO);

    @PUT("api-mongo/atualizar/{id}")
    Call<String> atualizarRegistro(@Path("id") String id, @Body RegistroParadaRequestDTO requestDTO);

    @PATCH("api-mongo/atualizarParcialmente/{id}")
    Call<String> atualizarParcialmente(@Path("id") String id, @Body RegistroParadaRequestDTO requestDTO);

    @DELETE("api-mongo/deletar/{id}")
    Call<okhttp3.ResponseBody> deletarRegistro(@Path("id") String id);
}
