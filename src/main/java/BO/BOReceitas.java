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

    public void Salvar(receitas r) {
        dao.Salvar(r);
    }

    public void Atualizar(receitas r) {
        dao.Atualizar(r);
    }

    public void excluir(int id) {
        dao.excluir(id);
    }

    public receitas buscarporid(int id) {
        return dao.buscarporid(id);
    }
    public List<receitas> mostratodos(){
        return dao.mostartodas();
    }
}
