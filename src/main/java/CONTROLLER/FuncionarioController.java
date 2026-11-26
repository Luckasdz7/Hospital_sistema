package CONTROLLER;

import BO.BOFuncionarios;
import BO.BOPacientes;
import MODEL.funcionarios;
import java.util.List;

/**
 * CONTROLLER (MVC) das abas Funcionários e Usuários do Form_admin
 * (as duas usam a tabela funcionarios).
 *
 * @author Lucas
 */
public class FuncionarioController {

    private final BOFuncionarios bo = new BOFuncionarios();

    public void validarTermo(String campo, String termo) {
        if (termo == null || termo.isBlank()) return;
        switch (campo) {
            case "id": BOPacientes.lerId(termo); break;
            case "salario": BOFuncionarios.lerSalario(termo); break;
            default: break;
        }
    }

    public List<funcionarios> pesquisar(String campo, String termo) {
        return bo.pesquisar(campo, termo);
    }

    /** Aba Funcionários: todos os campos, inclusive salário. */
    public funcionarios montarFuncionario(Integer id, String nome, String cpf, String cargo,
            String salario, String login, String senha) {
        funcionarios f = montarBase(id, nome, cpf, cargo, login, senha);
        f.setSalario(BOFuncionarios.lerSalario(salario == null ? "" : salario.trim()).doubleValue());
        return f;
    }

    /** Aba Usuários: só dados de acesso. */
    public funcionarios montarUsuario(Integer id, String nome, String cpf, String perfil,
            String login, String senha) {
        return montarBase(id, nome, cpf, perfil, login, senha);
    }

    private funcionarios montarBase(Integer id, String nome, String cpf, String cargo, String login, String senha) {
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("Informe o nome.");
        funcionarios f = new funcionarios();
        f.setNome(nome.trim());
        f.setCpf(BOFuncionarios.lerCpf(cpf == null ? "" : cpf));
        f.setCargo(BOFuncionarios.lerCargo(cargo == null ? "" : cargo));
        f.setLogin(login == null ? "" : login.trim());
        f.setSenha(senha == null ? "" : senha);
        if (id == null && f.getSenha().isEmpty()) throw new IllegalArgumentException("Informe a senha.");
        if (id != null) f.setId_funcionario(id);
        return f;
    }

    public void salvarFuncionario(funcionarios f) {
        if (f.getId_funcionario() > 0) bo.Atualizar(f); else bo.Salvar(f);
    }

    public void salvarUsuario(funcionarios f) {
        if (f.getId_funcionario() > 0) bo.AtualizarUsuario(f); else bo.SalvarUsuario(f);
    }

    public void excluir(int id) {
        bo.excluir(id);
    }
}
