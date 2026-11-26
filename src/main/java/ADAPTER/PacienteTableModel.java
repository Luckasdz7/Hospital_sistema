package ADAPTER;

import MODEL.Sexo;
import MODEL.pacientes;

/** Adapter: List<pacientes> -> JTable. */
public class PacienteTableModel extends TabelaAdapter<pacientes> {

    public PacienteTableModel() {
        super("ID", "Nome", "CPF", "Nascimento", "Telefone", "E-mail", "Sexo", "Tipo sanguíneo");
    }

    @Override
    protected Object valor(pacientes p, int coluna) {
        switch (coluna) {
            case 0: return p.getId_paciente();
            case 1: return p.getNome();
            case 2: return p.getCpf();
            case 3: return data(p.getData_nascimento());
            case 4: return p.getTelefone();
            case 5: return p.getEmail();
            case 6: return p.getSexo() == null ? "" : p.getSexo() == Sexo.OUTRO ? "Outro" : p.getSexo().name();
            case 7: return p.getTipo_sanguineo();
            default: return "";
        }
    }
}
