package com.aula.mobile_hivemind.ui.home;

import com.aula.mobile_hivemind.api.mongo.ApiServiceMongo;
import com.aula.mobile_hivemind.api.sql.ApiServiceSQL;
import com.aula.mobile_hivemind.dto.sql.RegistroParadaRequestDTO;
import com.aula.mobile_hivemind.dto.mongo.RegistroParadaResponseDTO;
import com.aula.mobile_hivemind.dto.trabalhador.TrabalhadorResponseDTO;
import com.google.type.DateTime;

import java.sql.Date;

public class RegistroMapper {
    private static DateTime hora;
    private ApiServiceSQL sql;
    private ApiServiceMongo mongo;

    public RegistroMapper(ApiServiceSQL sql) {
        this.sql = sql;
    }

    public RegistroParadaRequestDTO toRegistroParadaRequestDTO(RegistroParadaResponseDTO registroParada) {
        if (registroParada == null) {
            return null;
        }

        TrabalhadorResponseDTO trabalhador = sql.getTrabalhadorById(registroParada.getId());

        RegistroParadaRequestDTO dto = new RegistroParadaRequestDTO();

        if (trabalhador != null){
            dto.setId_usuario(registroParada.getId_usuario());
        }

        dto.setId_maquina(registroParada.getId_maquina());
        dto.setId_usuario(registroParada.getId_usuario());
        dto.setDate(Date.valueOf(registroParada.getDate()));
        dto.setDescricao(registroParada.getDescricao());

        return dto;
    }
}
