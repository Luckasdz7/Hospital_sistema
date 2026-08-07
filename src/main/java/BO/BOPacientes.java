/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package BO;

import DAO.DAOpacientes;
import MODEL.medicos;
import MODEL.pacientes;
import java.util.List;

/**
 *
 * @author Lucas
 */
public class BOPacientes {
    private DAOpacientes dao;

    public BOPacientes() {
        this.dao = new DAOpacientes();
    }

    public void Salvar(pacientes p) {
        dao.Salvar(p);
    }

    public void Atualizar(pacientes p) {
        dao.Atualizar(p);
    }

    public void excluir(int id) {
        dao.excluir(id);
    }

    public pacientes buscarporid(int id) {
        return dao.buscarporid(id);
    }
    public List<pacientes> mostratodos(){
        return dao.listartodos();
    }
}
