/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package BO;

import DAO.DAOMedicos;
import MODEL.consultas;
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
        dao.Salvar(m);
    }

    public void Atualizar(medicos m) {
        dao.Atualizar(m);
    }

    public void excluir(int id) {
        dao.excluir(id);
    }

    public medicos buscarporid(int id) {
        return dao.buscarporid(id);
    }
     public List<medicos> mostratodos(){
        return dao.listarlodos();
    }
}
