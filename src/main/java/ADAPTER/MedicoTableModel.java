package ADAPTER;

import MODEL.medicos;

/** Adapter: List<medicos> -> JTable. */
public class MedicoTableModel extends TabelaAdapter<medicos> {

    public MedicoTableModel() {
        super("ID", "Nome", "CRM", "Especialidade", "Telefone", "E-mail");
    }

    @Override
    protected Object valor(medicos m, int coluna) {
        switch (coluna) {
            case 0: return m.getId_medico();
            case 1: return m.getNome();
            case 2: return m.getCrm();
            case 3: return m.getEspecialidade();
            case 4: return m.getTelefone();
            case 5: return m.getEmail();
            default: return "";
        }
    }
}
