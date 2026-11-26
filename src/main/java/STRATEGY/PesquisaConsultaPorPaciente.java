package STRATEGY;

import java.util.List;

public class PesquisaConsultaPorPaciente implements PesquisaConsulta {
    @Override public String getCampo() { return "paciente"; }
    @Override public String getDica() { return "Paciente: parte do nome ou CPF (com ou sem pontos). Vazio lista todas."; }
    @Override public String normalizar(String termo) { return termo.trim(); }
    @Override public String filtroSql() {
        return "(p.nome LIKE ? ESCAPE '!' OR p.cpf LIKE ? ESCAPE '!' OR "
                + "REPLACE(REPLACE(REPLACE(p.cpf,'.',''),'-',''),' ','') LIKE ? ESCAPE '!')";
    }
    @Override public List<Object> parametros(String termo) {
        return List.of(PesquisaConsulta.contem(termo), PesquisaConsulta.contem(termo),
                PesquisaConsulta.contem(PesquisaConsulta.soNumeros(termo)));
    }
}
