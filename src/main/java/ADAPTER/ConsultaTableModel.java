package ADAPTER;

import MODEL.consultas;

/** Adapter: List<consultas> -> JTable (usado no Form_admin e no Form_medico). */
public class ConsultaTableModel extends TabelaAdapter<consultas> {

    public ConsultaTableModel() {
        super("ID", "Paciente", "Médico", "Data", "Horário", "Sala", "Status");
    }

    @Override
    protected Object valor(consultas c, int coluna) {
        switch (coluna) {
            case 0: return c.getId_consulta();
            case 1: return c.getPaciente() == null ? "" : c.getPaciente().getNome();
            case 2: return c.getMedico() == null ? "" : c.getMedico().getNome();
            case 3: return data(c.getData_consulta());
            case 4: return c.getHora_consulta() == null ? "" : c.getHora_consulta().toString();
            case 5: return c.getSala();
            case 6: return c.getStatus() == null ? "" : c.getStatus().name();
            default: return "";
        }
    }
}
