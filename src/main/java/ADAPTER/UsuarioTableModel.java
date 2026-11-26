package ADAPTER;

import MODEL.funcionarios;

/** Adapter: List<funcionarios> -> JTable da aba Usuários (só dados de acesso). */
public class UsuarioTableModel extends TabelaAdapter<funcionarios> {

    public UsuarioTableModel() {
        super("ID", "Nome", "CPF", "Login", "Perfil");
    }

    @Override
    protected Object valor(funcionarios f, int coluna) {
        switch (coluna) {
            case 0: return f.getId_funcionario();
            case 1: return f.getNome();
            case 2: return f.getCpf();
            case 3: return f.getLogin();
            case 4: return f.getCargo();
            default: return "";
        }
    }
}
