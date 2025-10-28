package com.aula.mobile_hivemind.api.sql;

import com.aula.mobile_hivemind.dto.sql.RegistroParadaRequestDTO;
import com.aula.mobile_hivemind.dto.sql.RegistroParadaResponseDTO;
import com.aula.mobile_hivemind.dto.trabalhador.TrabalhadorResponseDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiServiceSQL {
    @GET("/api/registro/listar")
    Call<List<RegistroParadaResponseDTO>> getAllRegistros();

    @POST("/api/registro/inserir")
    Call<okhttp3.ResponseBody> criarRegistro(@Body RegistroParadaRequestDTO requestDTO);

    @GET("/api/trabalhador/buscarPorId/{id}")
    TrabalhadorResponseDTO getTrabalhadorById(@Path("id") String id);



}
