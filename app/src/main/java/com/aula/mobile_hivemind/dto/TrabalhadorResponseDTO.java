package com.aula.mobile_hivemind.dto;

import com.google.gson.annotations.SerializedName;

public class TrabalhadorResponseDTO {
    @SerializedName("id_trabalhador")
    private Long id;

    @SerializedName("desLogin")
    private String login;

    @SerializedName("des_tipo_perfil")
    private String tipoPerfil;

    @SerializedName("des_senha")
    private String senha;

    @SerializedName("des_imagem")
    private String imagem;

    @SerializedName("des_setor")
    private String setor;

    public TrabalhadorResponseDTO() {}

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getTipoPerfil() {
        return tipoPerfil;
    }

    public void setTipoPerfil(String tipoPerfil) {
        this.tipoPerfil = tipoPerfil;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getImagem() {
        return imagem;
    }

    public void setImagem(String imagem) {
        this.imagem = imagem;
    }

    public String getSetor() {
        return setor;
    }

    public void setSetor(String setor) {
        this.setor = setor;
    }
}