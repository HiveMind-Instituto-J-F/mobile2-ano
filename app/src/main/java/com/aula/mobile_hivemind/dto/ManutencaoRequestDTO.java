package com.aula.mobile_hivemind.dto;

import com.google.gson.annotations.SerializedName;

import java.sql.Date;
import java.sql.Time;

public class ManutencaoRequestDTO {

    @SerializedName("id_maquina")
    private Integer id_maquina;

    @SerializedName("id_usuario")
    private Integer id_usuario;

    @SerializedName("des_acao_realizada")
    private String descricao;

    @SerializedName("des_setor")
    private String setor;

    @SerializedName("dt_manutencao")
    private Date dt_manutencao;
    @SerializedName("hora_inicio")
    private Time hora_inicio;

    @SerializedName("hora_fim")
    private Time hora_fim;

    public ManutencaoRequestDTO() {}

    public ManutencaoRequestDTO(Integer id_usuario, Integer id_maquina, Date dt_manutencao,
                                String setor, Time hora_inicio, Time hora_fim, String descricao) {
        this.id_usuario = id_usuario;
        this.id_maquina = id_maquina;
        this.dt_manutencao = dt_manutencao;
        this.setor = setor;
        this.hora_inicio = hora_inicio;
        this.hora_fim = hora_fim;
        this.descricao = descricao;
    }

    public void setId_maquina(Integer id_maquina) {
        this.id_maquina = id_maquina;
    }

    public Integer getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(Integer id_usuario) {
        this.id_usuario = id_usuario;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getSetor() {
        return setor;
    }

    public void setSetor(String setor) {
        this.setor = setor;
    }

    // ✅ CORRIGIDO: Retornar Date, não String
    public Date getDt_manutencao() {
        return dt_manutencao;
    }

    public void setDt_manutencao(Date dt_manutencao) {
        this.dt_manutencao = dt_manutencao;
    }

    // ✅ CORRIGIDO: Retornar Time, não String
    public Time getHora_inicio() {
        return hora_inicio;
    }

    public void setHora_inicio(Time hora_inicio) {
        this.hora_inicio = hora_inicio;
    }

    // ✅ CORRIGIDO: Retornar Time, não String
    public Time getHora_fim() {
        return hora_fim;
    }

    public void setHora_fim(Time hora_fim) {
        this.hora_fim = hora_fim;
    }
}