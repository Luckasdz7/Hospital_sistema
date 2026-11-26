package STRATEGY;

import BO.BOConsultas;
import java.util.List;

public class PesquisaConsultaPorData implements PesquisaConsulta {
    @Override public String getCampo() { return "data"; }
    @Override public String getDica() { return "Data: dd/MM/aaaa (ex.: 25/11/2026). Vazio lista todas."; }
    @Override public String normalizar(String termo) { return BOConsultas.lerData(termo).toString(); }
    @Override public String filtroSql() { return "c.data_consulta = ?"; }
    @Override public List<Object> parametros(String termo) { return List.of(java.sql.Date.valueOf(termo)); }
}
