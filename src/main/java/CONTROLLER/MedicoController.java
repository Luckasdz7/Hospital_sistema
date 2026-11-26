package CONTROLLER;

import BO.BOMedicos;
import BO.BOPacientes;
import MODEL.funcionarios;
import MODEL.medicos;
import java.util.List;

/**
 * CONTROLLER (MVC) da aba Médicos do Form_admin e da identificação do médico logado.
 *
 * @author Lucas
 */
public class MedicoController {

    private final BOMedicos bo = new BOMedicos();

    public void validarTermo(String campo, String termo) {
        if (termo != null && !termo.isBlank() && campo.equals("id")) BOPacientes.lerId(termo);
    }

    public List<medicos> pesquisar(String campo, String termo) {
        return bo.pesquisar(campo, termo);
    }

    public medicos montar(Integer id, funcionarios funcionario, String crm, String telefone,
            String especialidade, String email) {
        if (funcionario == null) throw new IllegalArgumentException("Selecione nas sugestões o funcionário (login) do médico.");
        if (crm == null || crm.isBlank()) throw new IllegalArgumentException("Informe o CRM.");
        medicos m = new medicos();
        m.setFuncionario(funcionario);
        m.setId_funcionario(funcionario.getId_funcionario());
        m.setCrm(crm.trim());
        m.setTelefone(PacienteController.nulo(telefone));
        m.setEspecialidade(PacienteController.nulo(especialidade));
        m.setEmail(PacienteController.nulo(email));
        if (id != null) m.setId_medico(id);
        return m;
    }

    public void salvar(medicos m) {
        if (m.getId_medico() > 0) bo.Atualizar(m); else bo.Salvar(m);
    }

    public void excluir(int id) {
        bo.excluir(id);
    }

    public List<funcionarios> sugerirFuncionarios(String termo, int idMedicoAtual) {
        return bo.pesquisarFuncionarios(termo, idMedicoAtual);
    }

    /** Médico vinculado ao funcionário que fez login (null se não houver). */
    public medicos buscarPorFuncionario(funcionarios usuario) {
        return usuario == null ? null : bo.buscarporfuncionario(usuario.getId_funcionario());
    }
}
