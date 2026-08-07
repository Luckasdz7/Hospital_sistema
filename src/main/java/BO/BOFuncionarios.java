/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package BO;

import DAO.DAOfuncionarios;
import MODEL.funcionarios;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author Lucas
 */
public class BOFuncionarios {
    private DAOfuncionarios dao;

    public BOFuncionarios() {
        this.dao = new DAOfuncionarios();
    }

    public void Salvar(funcionarios f) {
        dao.Salvar(f);
    }

    public void Atualizar(funcionarios f) {
        dao.Atualizar(f);
    }

    public void excluir(int id) {
        dao.Excluir(id);
    }

    public funcionarios buscarporid(int id) {
        return dao.buscarporid(id);
    }
    public List<funcionarios> Buscartodos() {
        return dao.listartodos();
    }
    public funcionarios logar(String login, String senha){
        try{
           return dao.buscarlogin(login, senha);
        }catch(Exception e){
            return null;
        }
    }
}
