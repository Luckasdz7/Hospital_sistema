package STRATEGY;

import java.util.List;

public class PesquisaConsultaPorSala implements PesquisaConsulta {
    @Override public String getCampo() { return "sala"; }
    @Override public String getDica() { return "Sala: parte do nome da sala. Vazio lista todas."; }
    @Override public String normalizar(String termo) { return termo.trim(); }
    @Override public String filtroSql() { return "c.sala LIKE ? ESCAPE '!'"; }
    @Override public List<Object> parametros(String termo) { return List.of(PesquisaConsulta.contem(termo)); }
}
