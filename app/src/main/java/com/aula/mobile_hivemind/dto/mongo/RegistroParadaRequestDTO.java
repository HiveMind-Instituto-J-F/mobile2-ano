package com.aula.mobile_hivemind.dto.mongo;

import com.google.gson.annotations.SerializedName;
public class RegistroParadaRequestDTO {
    @SerializedName("id_maquina")
    private Integer id_maquina;

    @SerializedName("nomeMaquina")
    private String nomeMaquina;

    @SerializedName("id_usuario")
    private Integer id_usuario;

    @SerializedName("setor")
    private String setor;

    @SerializedName("descricao")
    private String descricao;

    @SerializedName("date")
    private String date;



    public RegistroParadaRequestDTO() {}

    public RegistroParadaRequestDTO(Integer id_maquina, String nomeMaquina, Integer id_usuario, String setor, String descricao, String date) {
        this.id_maquina = id_maquina;
        this.id_usuario = id_usuario;
        this.nomeMaquina = nomeMaquina;
        this.setor = setor;
        this.date = date;
        this.descricao = descricao;
    }

    // Getters e Setters
    public Integer getId_maquina() { return id_maquina; }
    public void setId_maquina(Integer id_maquina) { this.id_maquina = id_maquina; }

    public String getNomeMaquina() { return nomeMaquina; }
    public void setNomeMaquina(String nomeMaquina) { this.nomeMaquina = nomeMaquina; }

    public Integer getId_usuario() { return id_usuario; }
    public void setId_usuario(Integer id_usuario) { this.id_usuario = id_usuario; }

    public String getSetor() { return setor; }
    public void setSetor(String setor) { this.setor = setor; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}
