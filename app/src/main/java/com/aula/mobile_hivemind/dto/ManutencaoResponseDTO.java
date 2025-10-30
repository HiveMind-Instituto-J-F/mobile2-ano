package com.aula.mobile_hivemind.dto;

import com.google.gson.annotations.SerializedName;

public class ManutencaoResponseDTO {

    @SerializedName("id_manutencao")
    private String id;

    @SerializedName("id_maquina")
    private Integer id_maquina;

    @SerializedName("id_usuario")
    private Integer id_usuario;

    @SerializedName("acao_realizada")
    private String descricao;

    @SerializedName("dt_manutencao")
    private String date;

    @SerializedName("hora_inicio")
    private String hora_inicio;

    @SerializedName("hora_fim")
    private String hora_fim;

    public ManutencaoResponseDTO() {}

    public ManutencaoResponseDTO(String id, Integer id_maquina, Integer id_usuario, String descricao, String date, String hora_inicio, String hora_fim) {
        this.id = id;
        this.id_maquina = id_maquina;
        this.id_usuario = id_usuario;
        this.descricao = descricao;
        this.date = date;
        this.hora_inicio = hora_inicio;
        this.hora_fim = hora_fim;
    }

    public String getId() { return id; }
    public Integer getId_maquina() { return id_maquina; }
    public Integer getId_usuario() { return id_usuario; }
    public String getDescricao() { return descricao; }
    public String getDate() { return date; }
    public String getHora_inicio() { return hora_inicio; }
    public String getHora_fim() { return hora_fim; }
}
