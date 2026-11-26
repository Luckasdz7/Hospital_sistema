package CONTROLLER;

import BO.BOConsultas;
import BO.BOPacientes;
import MODEL.pacientes;
import java.util.List;

/**
 * CONTROLLER (MVC) da aba Pacientes do Form_admin.
 *
 * @author Lucas
 */
public class PacienteController {

    private final BOPacientes bo = new BOPacientes();

    /** Valida o termo antes de ir ao banco (mensagem imediata na tela). */
    public void validarTermo(String campo, String termo) {
        if (termo == null || termo.isBlank()) return;
        switch (campo) {
            case "id": BOPacientes.lerId(termo); break;
            case "nascimento": BOConsultas.lerData(termo); break;
            case "sexo": BOPacientes.lerSexo(termo); break;
            case "sangue": BOPacientes.lerTipoSanguineo(termo); break;
            default: break;
        }
    }

    public List<pacientes> pesquisar(String campo, String termo) {
        return bo.pesquisar(campo, termo);
    }

    public pacientes montar(Integer id, String nome, String cpf, String nascimento, String sexo,
            String telefone, String email, String endereco, String sangue) {
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("Informe o nome do paciente.");
        pacientes p = new pacientes();
        p.setNome(nome.trim());
        p.setCpf(BOPacientes.lerCpf(cpf));
        p.setData_nascimento(java.sql.Date.valueOf(BOConsultas.lerData(nascimento)));
        p.setSexo(BOPacientes.lerSexo(sexo));
        p.setTelefone(nulo(telefone));
        p.setEmail(nulo(email));
        p.setEndereco(nulo(endereco));
        String tipo = nulo(sangue);
        p.setTipo_sanguineo(tipo == null ? null : BOPacientes.lerTipoSanguineo(tipo));
        if (id != null) p.setId_paciente(id);
        return p;
    }

    public void salvar(pacientes p) {
        if (p.getId_paciente() > 0) bo.Atualizar(p); else bo.Salvar(p);
    }

    public void excluir(int id) {
        bo.excluir(id);
    }

    static String nulo(String texto) {
        if (texto == null) return null;
        String t = texto.trim();
        return t.isEmpty() ? null : t;
    }
}
