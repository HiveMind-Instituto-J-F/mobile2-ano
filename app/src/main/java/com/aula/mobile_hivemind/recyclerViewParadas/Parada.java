package com.aula.mobile_hivemind.recyclerViewParadas;

public class Parada {
    private String id;
    private Integer idMaquina;
    private String nomeMaquina;
    private Integer codigoColaborador;
    private String setor;
    private String descricaoParada;
    private String dataParada;


    public Parada() {}

    public Parada(Integer idMaquina, String nomeMaquina, Integer codigoColaborador, String setor, String descricaoParada, String dataParada) {
        this.idMaquina = idMaquina;
        this.codigoColaborador = codigoColaborador;
        this.nomeMaquina = nomeMaquina;
        this.setor = setor;
        this.descricaoParada = descricaoParada;
        this.dataParada = dataParada;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getIdMaquina() {
        return idMaquina;
    }

    public void setIdMaquina(Integer idMaquina) {
        this.idMaquina = idMaquina;
    }

    public String getNomeMaquina() {
        return nomeMaquina;
    }

    public void setNomeMaquina(String nomeMaquina) {
        this.nomeMaquina = nomeMaquina;
    }

    public Integer getCodigoColaborador() {
        return codigoColaborador;
    }

    public void setCodigoColaborador(Integer codigoColaborador) {
        this.codigoColaborador = codigoColaborador;
    }

    public String getSetor() {
        return setor;
    }

    public void setSetor(String setor) {
        this.setor = setor;
    }

    public String getDescricaoParada() {
        return descricaoParada;
    }

    public void setDescricaoParada(String descricaoParada) {
        this.descricaoParada = descricaoParada;
    }

    public String getDataParada() {
        return dataParada;
    }

    public void setDataParada(String dataParada) {
        this.dataParada = dataParada;
    }
}
