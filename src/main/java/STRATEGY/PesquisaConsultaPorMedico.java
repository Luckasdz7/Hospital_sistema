package STRATEGY;

import java.util.List;

public class PesquisaConsultaPorMedico implements PesquisaConsulta {
    @Override public String getCampo() { return "medico"; }
    @Override public String getDica() { return "Médico: parte do nome, CRM ou CPF. Vazio lista todas."; }
    @Override public String normalizar(String termo) { return termo.trim(); }
    @Override public String filtroSql() {
        return "(f.nome LIKE ? ESCAPE '!' OR m.crm LIKE ? ESCAPE '!' OR f.cpf LIKE ? ESCAPE '!' OR "
                + "REPLACE(REPLACE(REPLACE(f.cpf,'.',''),'-',''),' ','') LIKE ? ESCAPE '!')";
    }
    @Override public List<Object> parametros(String termo) {
        String c = PesquisaConsulta.contem(termo);
        return List.of(c, c, c, PesquisaConsulta.contem(PesquisaConsulta.soNumeros(termo)));
    }
}
