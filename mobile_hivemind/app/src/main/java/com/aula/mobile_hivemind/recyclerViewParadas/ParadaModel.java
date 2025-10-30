package com.aula.mobile_hivemind.recyclerViewParadas;

import android.graphics.Color;

import java.util.Calendar;

public class ParadaModel {
    private final String nome;
    private String setor;
    private String hora;
    private Calendar data;
    private String descricao;

    public ParadaModel(String nome, String setor, String hora, Calendar data, String descricao) {
        this.nome = nome;
        this.setor = setor;
        this.hora = hora;
        this.data = data;
        this.descricao = descricao;
    }

    public ParadaModel(String nome, String setor, String hora) {
        this.nome = nome;
        this.setor = setor;
        this.hora = hora;
    }
    public ParadaModel(String nome, Calendar data) {
        this.nome = nome;
        this.data = data;
    }

    public String getNome() {
        return nome;
    }

    public String getSetor() {
        return setor;
    }

    public String getHora() {
        return hora;
    }

    public Calendar getData() {
        return data;
    }

    public String getDescricao() {
        return descricao;
    }

    // Setters opcionais
    public void setSetor(String setor) {
        this.setor = setor;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public void setData(Calendar data) {
        this.data = data;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
