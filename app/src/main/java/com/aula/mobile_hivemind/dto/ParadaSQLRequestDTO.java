package com.aula.mobile_hivemind.dto;

import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ParadaSQLRequestDTO {
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
    private String hora_Inicio;

    @SerializedName("hora_fim")
    private String hora_Fim;

    public ParadaSQLRequestDTO(RegistroParadaResponseDTO paradaMongo) {
        this.id_maquina = paradaMongo.getId_maquina();
        this.id_usuario = paradaMongo.getId_usuario();
        this.des_parada = paradaMongo.getDes_parada();
        this.des_setor = paradaMongo.getDes_setor();

        this.dt_parada = formatarData(paradaMongo.getDt_parada());
        this.hora_Inicio = formatarHora(paradaMongo.getHora_Inicio());
        this.hora_Fim = formatarHora(paradaMongo.getHora_Fim());
    }

    private String formatarData(Date dateTime) {
        if (dateTime == null) return null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(dateTime);
    }

    private String formatarHora(Date dateTime) {
        if (dateTime == null) return null;
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return timeFormat.format(dateTime);
    }

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

    public String getHora_Inicio() { return hora_Inicio; }
    public void setHora_Inicio(String hora_Inicio) { this.hora_Inicio = hora_Inicio; }

    public String getHora_Fim() { return hora_Fim; }
    public void setHora_Fim(String hora_Fim) { this.hora_Fim = hora_Fim; }
}
