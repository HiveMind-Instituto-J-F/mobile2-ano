package com.aula.mobile_hivemind.dto;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class RegistroParadaRequestDTO {

    @SerializedName("id_manutencao")
    private Integer id_manutencao;

    @SerializedName("id_maquina")
    private Integer id_maquina;

    @SerializedName("id_usuario")
    private Integer id_usuario;

    @SerializedName("des_parada")
    private String des_parada;

    @SerializedName("des_setor")
    private String des_setor;

    @SerializedName("dt_parada")
    private Date dt_parada;

    @SerializedName("hora_inicio")
    private Date hora_Inicio;

    @SerializedName("hora_fim")
    private Date hora_Fim;

    public RegistroParadaRequestDTO() {}

    public Integer getId_manutencao() {
        return id_manutencao;
    }

    public void setId_manutencao(Integer id_manutencao) {
        this.id_manutencao = id_manutencao;
    }

    public RegistroParadaRequestDTO(Integer id_manutencao, Integer id_maquina, Integer id_usuario,
                                    String des_parada, String des_setor, Date dt_parada,
                                    Date hora_Inicio, Date hora_Fim) {
        this.id_manutencao = id_manutencao;
        this.id_maquina = id_maquina;
        this.id_usuario = id_usuario;
        this.des_parada = des_parada;
        this.des_setor = des_setor;
        this.dt_parada = dt_parada;
        this.hora_Inicio = hora_Inicio;
        this.hora_Fim = hora_Fim;
    }

    public RegistroParadaRequestDTO(Integer id_maquina, Integer id_usuario, String des_parada, String des_setor, Date dt_parada, Date hora_Inicio, Date hora_Fim) {
        this.id_maquina = id_maquina;
        this.id_usuario = id_usuario;
        this.des_parada = des_parada;
        this.des_setor = des_setor;
        this.dt_parada = dt_parada;
        this.hora_Inicio = hora_Inicio;
        this.hora_Fim = hora_Fim;
    }

    // Resto dos getters e setters...
    public Integer getId_maquina() {
        return id_maquina;
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

    public String getDes_parada() {
        return des_parada;
    }

    public void setDes_parada(String des_parada) {
        this.des_parada = des_parada;
    }

    public String getDes_setor() {
        return des_setor;
    }

    public void setDes_setor(String des_setor) {
        this.des_setor = des_setor;
    }

    public Date getDt_parada() {
        return dt_parada;
    }

    public void setDt_parada(Date dt_parada) {
        this.dt_parada = dt_parada;
    }

    public Date getHora_Inicio() {
        return hora_Inicio;
    }

    public void setHora_Inicio(Date hora_Inicio) {
        this.hora_Inicio = hora_Inicio;
    }

    public Date getHora_Fim() {
        return hora_Fim;
    }

    public void setHora_Fim(Date hora_Fim) {
        this.hora_Fim = hora_Fim;
    }
}