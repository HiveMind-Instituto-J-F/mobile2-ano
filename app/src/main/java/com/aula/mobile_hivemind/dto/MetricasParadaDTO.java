// MetricasParadaDTO.java
package com.aula.mobile_hivemind.dto;

import java.util.List;
import java.util.Map;

public class MetricasParadaDTO {
    private int totalParadasMes;
    private String tempoMedioParadaMinutos;
    private String maquinaComMaisParadas;
    private String setorComMaisParadas;
    private double custoEstimadoParadas;
    private Map<String, Integer> paradasPorSetor;
    private Map<String, Integer> distribuicaoCausas;
    private Map<String, Integer> evolucaoMensal;
    private Map<String, Integer> heatmapHorarios;

    // Construtores
    public MetricasParadaDTO() {}

    // Getters e Setters
    public int getTotalParadasMes() { return totalParadasMes; }
    public void setTotalParadasMes(int totalParadasMes) { this.totalParadasMes = totalParadasMes; }

    @Deprecated
    public double getTempoMedioParadaMinutos() {
        try {
            return Double.parseDouble(tempoMedioParadaMinutos.replaceAll("[^0-9.]", ""));
        } catch (Exception e) {
            return 0;
        }
    }
    public void setTempoMedioParadaMinutos(String tempoMedioParadaMinutos) {
        this.tempoMedioParadaMinutos = tempoMedioParadaMinutos;
    }

    public String getMaquinaComMaisParadas() { return maquinaComMaisParadas; }
    public void setMaquinaComMaisParadas(String maquinaComMaisParadas) { this.maquinaComMaisParadas = maquinaComMaisParadas; }

    public String getSetorComMaisParadas() { return setorComMaisParadas; }
    public void setSetorComMaisParadas(String setorComMaisParadas) { this.setorComMaisParadas = setorComMaisParadas; }

    public double getCustoEstimadoParadas() { return custoEstimadoParadas; }
    public void setCustoEstimadoParadas(double custoEstimadoParadas) { this.custoEstimadoParadas = custoEstimadoParadas; }

    public Map<String, Integer> getParadasPorSetor() { return paradasPorSetor; }
    public void setParadasPorSetor(Map<String, Integer> paradasPorSetor) { this.paradasPorSetor = paradasPorSetor; }

    public Map<String, Integer> getDistribuicaoCausas() { return distribuicaoCausas; }
    public void setDistribuicaoCausas(Map<String, Integer> distribuicaoCausas) { this.distribuicaoCausas = distribuicaoCausas; }

    public Map<String, Integer> getEvolucaoMensal() { return evolucaoMensal; }
    public void setEvolucaoMensal(Map<String, Integer> evolucaoMensal) { this.evolucaoMensal = evolucaoMensal; }

    public Map<String, Integer> getHeatmapHorarios() { return heatmapHorarios; }
    public void setHeatmapHorarios(Map<String, Integer> heatmapHorarios) { this.heatmapHorarios = heatmapHorarios; }
}