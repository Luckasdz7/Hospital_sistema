package STRATEGY;

import BO.BOConsultas;
import java.util.List;

public class PesquisaConsultaPorHorario implements PesquisaConsulta {
    @Override public String getCampo() { return "horario"; }
    @Override public String getDica() { return "Horário: HH:mm (ex.: 09:30). Vazio lista todas."; }
    @Override public String normalizar(String termo) { return BOConsultas.lerHora(termo).toString(); }
    @Override public String filtroSql() { return "c.hora_consulta = ?"; }
    @Override public List<Object> parametros(String termo) {
        return List.of(java.sql.Time.valueOf(java.time.LocalTime.parse(termo)));
    }
}
