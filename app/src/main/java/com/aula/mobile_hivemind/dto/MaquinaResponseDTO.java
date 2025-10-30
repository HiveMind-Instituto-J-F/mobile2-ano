package com.aula.mobile_hivemind.dto;

import com.google.gson.annotations.SerializedName;

public class MaquinaResponseDTO {
    @SerializedName("id_maquina")
    private Long id;

    @SerializedName("des_nome")
    private String nome;

    @SerializedName("des_tipo")
    private String tipo;

    @SerializedName("des_setor")
    private String setor;

    @SerializedName("des_maquina")
    private String descricao;

    @SerializedName("des_status_operacional")
    private String status_operacional;

    @SerializedName("des_nivel_confianca")
    private Integer nivel_confianca;

    public MaquinaResponseDTO() {}

    public MaquinaResponseDTO(Long id, String nome, String tipo, String setor, String descricao, String status_operacional, Integer nivel_confianca) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.setor = setor;
        this.descricao = descricao;
        this.status_operacional = status_operacional;
        this.nivel_confianca = nivel_confianca;
    }

    // Getters e Setters (mantenha os mesmos)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getSetor() {
        return setor;
    }

    public void setSetor(String setor) {
        this.setor = setor;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getStatus_operacional() {
        return status_operacional;
    }

    public void setStatus_operacional(String status_operacional) {
        this.status_operacional = status_operacional;
    }

    public Integer getNivel_confianca() {
        return nivel_confianca;
    }

    public void setNivel_confianca(Integer nivel_confianca) {
        this.nivel_confianca = nivel_confianca;
    }
}