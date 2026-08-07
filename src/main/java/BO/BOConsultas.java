/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package BO;

import DAO.DAOconsultas;
import MODEL.consultas;
import java.util.List;

/**
 *
 * @author Lucas
 */
public class BOConsultas {
    private DAOconsultas dao;

    public BOConsultas() {
        this.dao = new DAOconsultas();
    }

    public void Salvar(consultas c) {
        dao.Salvar(c);
    }

    public void Atualizar(consultas c) {
        dao.Atualizar(c);
    }

    public void excluir(int id) {
        dao.excluir(id);
    }

    public consultas buscarporid(int id) {
        return dao.buscarporid(id);
    }
    public List<consultas> mostratodos(){
        return dao.mostartodas();
    }
}
