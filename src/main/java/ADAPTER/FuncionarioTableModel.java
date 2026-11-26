package ADAPTER;

import MODEL.funcionarios;

/** Adapter: List<funcionarios> -> JTable da aba Funcionários (com salário). */
public class FuncionarioTableModel extends TabelaAdapter<funcionarios> {

    private static final java.text.NumberFormat MOEDA =
            java.text.NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("pt-BR"));
    static { MOEDA.setMinimumFractionDigits(2); MOEDA.setMaximumFractionDigits(2); }

    public FuncionarioTableModel() {
        super("ID", "Nome", "CPF", "Cargo", "Salário", "Login");
    }

    @Override
    protected Object valor(funcionarios f, int coluna) {
        switch (coluna) {
            case 0: return f.getId_funcionario();
            case 1: return f.getNome();
            case 2: return f.getCpf();
            case 3: return f.getCargo();
            case 4: return MOEDA.format(f.getSalario());
            case 5: return f.getLogin();
            default: return "";
        }
    }
}
