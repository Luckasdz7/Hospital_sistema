/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package BO;

import DAO.DAOMedicos;
import MODEL.funcionarios;
import MODEL.medicos;
import java.util.List;

/**
 *
 * @author Lucas
 */
public class BOMedicos {
    private DAOMedicos dao;

    public BOMedicos() {
        this.dao = new DAOMedicos();
    }

    public void Salvar(medicos m) {
        validar(m);
        dao.Salvar(m);
    }

    public void Atualizar(medicos m) {
        validar(m);
        if (m.getId_medico() <= 0) throw new IllegalArgumentException("Selecione um médico.");
        dao.Atualizar(m);
    }

    public void excluir(int id) {
        dao.excluir(id);
    }

    public medicos buscarporfuncionario(int idFuncionario) {
        return dao.buscarporfuncionario(idFuncionario);
    }

    public medicos buscarporid(int id) {
        return dao.buscarporid(id);
    }
     public List<medicos> mostratodos(){
        return dao.listarlodos();
    }

    public List<medicos> pesquisar(String campo, String termo) {
        String valor = termo == null ? "" : termo.trim();
        if (!valor.isEmpty() && campo.equals("id")) BOPacientes.lerId(valor);
        return dao.pesquisar(campo, valor);
    }

    // Sugestões para o campo Funcionário (login do médico no sistema).
    public List<funcionarios> pesquisarFuncionarios(String termo, int idMedicoAtual) {
        return termo.trim().length() < 2 ? java.util.Collections.emptyList()
                : dao.pesquisarFuncionarios(termo.trim(), idMedicoAtual);
    }

    private void validar(medicos m) {
        if (m.getFuncionario() == null || m.getFuncionario().getId_funcionario() <= 0)
            throw new IllegalArgumentException("Selecione nas sugestões o funcionário (login) do médico.");
        if (m.getCrm() == null || m.getCrm().isBlank()) throw new IllegalArgumentException("Informe o CRM.");
        if (m.getCrm().length() > 20) throw new IllegalArgumentException("O CRM pode ter no máximo 20 caracteres.");
        if (m.getEspecialidade() != null && m.getEspecialidade().length() > 70)
            throw new IllegalArgumentException("A especialidade pode ter no máximo 70 caracteres.");
        if (m.getTelefone() != null && m.getTelefone().length() > 20)
            throw new IllegalArgumentException("O telefone pode ter no máximo 20 caracteres.");
        if (m.getEmail() != null && (m.getEmail().length() > 100 || !m.getEmail().matches("[^@\\s]+@[^@\\s]+")))
            throw new IllegalArgumentException("Informe um e-mail válido (ou deixe em branco).");
    }
}
