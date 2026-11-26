package STRATEGY;

import java.util.List;

public class PesquisaConsultaPorId implements PesquisaConsulta {
    @Override public String getCampo() { return "id"; }
    @Override public String getDica() { return "ID: número da consulta. Vazio lista todas."; }
    @Override public String normalizar(String termo) {
        try {
            int id = Integer.parseInt(termo.trim());
            if (id <= 0) throw new NumberFormatException();
            return String.valueOf(id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("O ID deve ser um número inteiro positivo.");
        }
    }
    @Override public String filtroSql() { return "c.id_consulta = ?"; }
    @Override public List<Object> parametros(String termo) { return List.of(Integer.valueOf(termo)); }
}
