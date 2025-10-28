package com.aula.mobile_hivemind.dto.trabalhador;

public class TrabalhadorResponseDTO {
    private Long id;
    private Long id_planta;
    private String tipo_perfil;
    private String login;
    private String senha;
    private String setor;
    private String imagem;

    public TrabalhadorResponseDTO() {}

    public Long getId() {
        return id;
    }

    public Long getId_planta() {
        return id_planta;
    }

    public String getTipo_perfil() {
        return tipo_perfil;
    }

    public void setTipo_perfil(String tipo_perfil) {
        this.tipo_perfil = tipo_perfil;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getSetor() {
        return setor;
    }

    public void setSetor(String setor) {
        this.setor = setor;
    }

    public String getImagem() {return imagem;}

    public void setImagem(String imagem) {this.imagem = imagem;}
}
