/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package BO;

import DAO.DAOreceitas;
import MODEL.receitas;
import java.util.List;

/**
 *
 * @author Lucas
 */
public class BOReceitas {
    private DAOreceitas dao;

    public BOReceitas() {
        this.dao = new DAOreceitas();
    }

    // "Gerar nova": só grava se a consulta ainda não tiver receita.
    public void Salvar(receitas r) {
        validar(r);
        if (dao.buscarporconsulta(r.getConsulta().getId_consulta()) != null)
            throw new IllegalArgumentException("Essa consulta já possui uma receita. Edite a receita existente.");
        dao.Salvar(r);
    }

    public void Atualizar(receitas r) {
        validar(r);
        if (r.getId_receita() <= 0) throw new IllegalArgumentException("Selecione uma receita.");
        dao.Atualizar(r);
    }

    public void excluir(int id) {
        if (id <= 0) throw new IllegalArgumentException("Selecione uma receita.");
        dao.excluir(id);
    }

    public receitas buscarporid(int id) {
        return dao.buscarporid(id);
    }

    public receitas buscarporconsulta(int idConsulta) {
        return dao.buscarporconsulta(idConsulta);
    }

    public List<receitas> mostratodos(){
        return dao.mostartodas();
    }

    // Validade é opcional: vazio = sem validade; senão dd/MM/aaaa.
    public static java.util.Date lerValidade(String texto) {
        if (texto == null || texto.isBlank()) return null;
        return java.sql.Date.valueOf(BOConsultas.lerData(texto));
    }

    private void validar(receitas r) {
        if (r.getConsulta() == null || r.getConsulta().getId_consulta() <= 0)
            throw new IllegalArgumentException("Selecione uma consulta na tabela.");
        if (r.getMedicamento() == null || r.getMedicamento().isBlank())
            throw new IllegalArgumentException("Informe o medicamento.");
        if (r.getMedicamento().length() > 150)
            throw new IllegalArgumentException("O medicamento pode ter no máximo 150 caracteres.");
        if (r.getDosagem() == null || r.getDosagem().isBlank())
            throw new IllegalArgumentException("Informe a dosagem.");
        if (r.getDosagem().length() > 100)
            throw new IllegalArgumentException("A dosagem pode ter no máximo 100 caracteres.");
    }
}
