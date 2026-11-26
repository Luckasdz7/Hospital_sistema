package STRATEGY;

import BO.BOConsultas;
import java.util.List;

public class PesquisaConsultaPorStatus implements PesquisaConsulta {
    @Override public String getCampo() { return "status"; }
    @Override public String getDica() { return "Status: Agendada, Realizada ou Cancelada. Vazio lista todas."; }
    @Override public String normalizar(String termo) { return BOConsultas.lerStatus(termo).name(); }
    @Override public String filtroSql() { return "c.status = ?"; }
    @Override public List<Object> parametros(String termo) { return List.of(termo); }
}
