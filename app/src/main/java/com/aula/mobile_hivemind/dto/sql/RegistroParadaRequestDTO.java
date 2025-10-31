package com.aula.mobile_hivemind.dto.sql;

import com.google.firebase.database.annotations.NotNull;
import com.google.gson.annotations.SerializedName;

import java.sql.Date;
import java.sql.Time;

public class RegistroParadaRequestDTO {
    @SerializedName("tipo_parada")
    @NotNull
    private String tipo_parada;

    @SerializedName("hora_inicio")
    @NotNull
    private Time hora_inicio;

    @SerializedName("hora_fim")
    @NotNull
    private Time hora_fim;

    @SerializedName("id_maquina")
    @NotNull
    private Integer id_maquina;

    @SerializedName("id_manutencao")
    @NotNull
    private Integer id_manutencao;

    @SerializedName("id_usuario")
    @NotNull
    private Integer id_usuario;

    @SerializedName("date")
    @NotNull
    private Date date;

    @SerializedName("descricao")
    @NotNull
    private String descricao;

    public RegistroParadaRequestDTO() {}

    public RegistroParadaRequestDTO(
            String tipo_parada,
            Time hora_inicio,
            Time hora_fim,
            Integer id_maquina,
            Integer id_manutencao,
            Integer id_usuario,
            Date date,
            String descricao)
    {
        this.tipo_parada = tipo_parada;
        this.hora_inicio = hora_inicio;
        this.hora_fim = hora_fim;
        this.id_maquina = id_maquina;
        this.id_manutencao = id_manutencao;
        this.id_usuario = id_usuario;
        this.date = date;
        this.descricao = descricao;
    }

    public String getTipo_parada() {
        return tipo_parada;
    }

    public void setTipo_parada(String tipo_parada) {
        this.tipo_parada = tipo_parada;
    }

    public Time getHora_inicio() {
        return hora_inicio;
    }

    public void setHora_inicio(Time hora_inicio) {
        this.hora_inicio = hora_inicio;
    }

    public Time getHora_fim() {
        return hora_fim;
    }

    public void setHora_fim(Time hora_fim) {
        this.hora_fim = hora_fim;
    }

    public Integer getId_maquina() {
        return id_maquina;
    }

    public void setId_maquina(Integer id_maquina) {
        this.id_maquina = id_maquina;
    }

    public Integer getId_manutencao() {
        return id_manutencao;
    }

    public void setId_manutencao(Integer id_manutencao) {
        this.id_manutencao = id_manutencao;
    }

    public Integer getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(Integer id_usuario) {
        this.id_usuario = id_usuario;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}