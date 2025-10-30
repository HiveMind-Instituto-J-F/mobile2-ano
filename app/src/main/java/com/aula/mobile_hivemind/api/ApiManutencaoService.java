package com.aula.mobile_hivemind.api;

import com.aula.mobile_hivemind.dto.ManutencaoRequestDTO;
import com.aula.mobile_hivemind.dto.ManutencaoResponseDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiManutencaoService {

    @GET("api/manutencao/listar")
    Call<List<ManutencaoResponseDTO>> listarManutencoes();

    @POST("api/manutencao/inserir")
    Call<String> inserirManutencao(@Body ManutencaoRequestDTO manutencao);
    @PUT("api/manutencao/atualizar/{id}")
    Call<String> atualizarManutencao(@Path("id") String id, ManutencaoRequestDTO manutencao);

    @PATCH("api/manutencao/atualizarParcial/{id}")
    Call<String> atualizarManutencaoParcial(@Path("id") String id, ManutencaoRequestDTO manutencao);

    @DELETE("api/manutencao/excluir/{id}")
    Call<String> excluirManutencao(@Path("id") String id);

}
