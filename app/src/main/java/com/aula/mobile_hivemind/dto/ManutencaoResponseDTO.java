package com.aula.mobile_hivemind.dto;

import com.google.gson.annotations.SerializedName;

import java.sql.Time;
import java.util.Date;

public class ManutencaoResponseDTO {

    @SerializedName("id_manutencao")
    private Integer id;

    @SerializedName("id_maquina")
    private Integer id_maquina;

    @SerializedName("id_usuario")
    private Integer id_usuario;

    @SerializedName("des_setor")
    private String setor;
    @SerializedName("des_acao_realizada")
    private String descricao;

    @SerializedName("dt_manutencao")
    private Date date;

    @SerializedName("hora_inicio")
    private Time hora_inicio;

    @SerializedName("hora_fim")
    private Time hora_fim;

    public ManutencaoResponseDTO() {}

    public ManutencaoResponseDTO(Integer id, Integer id_maquina, Integer id_usuario, String setor, String descricao, Date date, Time hora_inicio, Time hora_fim) {
        this.id = id;
        this.id_maquina = id_maquina;
        this.id_usuario = id_usuario;
        this.setor = setor;
        this.descricao = descricao;
        this.date = date;
        this.hora_inicio = hora_inicio;
        this.hora_fim = hora_fim;
    }

    public Integer getId() { return id; }
    public Integer getId_maquina() { return id_maquina; }
    public Integer getId_usuario() { return id_usuario; }

    public String getSetor() {
        return setor;
    }

    public void setSetor(String setor) {
        this.setor = setor;
    }

    public String getDescricao() { return descricao; }
    public Date getDate() { return date; }
    public Time getHora_inicio() { return hora_inicio; }
    public Time getHora_fim() { return hora_fim; }
}