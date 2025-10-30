package com.aula.mobile_hivemind.dto;

import com.google.gson.annotations.SerializedName;

public class ParadaSQLResponseDTO {
    @SerializedName("id_registro_paradas")
    private Integer id_registro_paradas;

    @SerializedName("id_maquina")
    private Integer id_maquina;

    @SerializedName("id_usuario")
    private Integer id_usuario;

    @SerializedName("des_parada")
    private String des_parada;

    @SerializedName("des_setor")
    private String des_setor;

    @SerializedName("dt_parada")
    private String dt_parada;

    @SerializedName("hora_inicio")
    private String hora_inicio;

    @SerializedName("hora_fim")
    private String hora_fim;

    // Getters e Setters
    public Integer getId_registro_paradas() { return id_registro_paradas; }
    public void setId_registro_paradas(Integer id_registro_paradas) { this.id_registro_paradas = id_registro_paradas; }

    public Integer getId_maquina() { return id_maquina; }
    public void setId_maquina(Integer id_maquina) { this.id_maquina = id_maquina; }

    public Integer getId_usuario() { return id_usuario; }
    public void setId_usuario(Integer id_usuario) { this.id_usuario = id_usuario; }

    public String getDes_parada() { return des_parada; }
    public void setDes_parada(String des_parada) { this.des_parada = des_parada; }

    public String getDes_setor() { return des_setor; }
    public void setDes_setor(String des_setor) { this.des_setor = des_setor; }

    public String getDt_parada() { return dt_parada; }
    public void setDt_parada(String dt_parada) { this.dt_parada = dt_parada; }

    public String getHora_inicio() { return hora_inicio; }
    public void setHora_inicio(String hora_inicio) { this.hora_inicio = hora_inicio; }

    public String getHora_fim() { return hora_fim; }
    public void setHora_fim(String hora_fim) { this.hora_fim = hora_fim; }
}