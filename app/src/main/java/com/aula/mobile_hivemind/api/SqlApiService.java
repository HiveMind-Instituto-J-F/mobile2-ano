
package com.aula.mobile_hivemind.api;

import com.aula.mobile_hivemind.dto.ManutencaoRequestDTO;
import com.aula.mobile_hivemind.dto.ManutencaoResponseDTO;
import com.aula.mobile_hivemind.dto.MaquinaResponseDTO;
import com.aula.mobile_hivemind.dto.ParadaSQLRequestDTO;
import com.aula.mobile_hivemind.dto.ParadaSQLResponseDTO;
import com.aula.mobile_hivemind.dto.TrabalhadorResponseDTO;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface SqlApiService {
    @GET("api/maquina/listar")
    Call<List<MaquinaResponseDTO>> listarMaquinas();

    @GET("api/maquina/{id}")
    Call<MaquinaResponseDTO> getMaquinaPorId(@Path("id") Long id);

    @GET("api/trabalhador/listar")
    Call<List<TrabalhadorResponseDTO>> listarTrabalhadores();

    @POST("api/registro/inserirProcedure")
    Call<ResponseBody> inserirParada(@Body ParadaSQLRequestDTO paradaSQL);

    @GET("api/registro/listar")
    Call<List<ParadaSQLResponseDTO>> listarTodasParadas();

    @GET("api/manutencao/listar")
    Call<List<ManutencaoResponseDTO>> listarManutencoes();

    @POST("api/manutencao/inserir")
    Call<String> inserirManutencao(@Body ManutencaoRequestDTO manutencao);
}
