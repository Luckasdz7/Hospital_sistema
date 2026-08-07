/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import MODEL.funcionarios;
import MODEL.medicos;
import UTIL.Conexao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Lucas
 */
public class DAOMedicos {
    Connection conn;
    public DAOMedicos(){
        conn = new Conexao().conectar();
    }
    public void Salvar(medicos m){
        //nao esquecer mudar no final do projeto quando tudo der certo, mude o e.geterrocode pra outro, o usuario nao tem q saber onde aconteceu o erro, o err ofoi dele ou do banco
        
        String sql = "INSERT INTO medicos(nome,crm,especialidade,telefone,email,salario,id_funcionario) VALUES(?,?,?,?,?,?,?)";
        try{
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, m.getNome());
            stmt.setString(2, m.getCrm());
            stmt.setString(3, m.getEspecialidade());
            stmt.setString(4, m.getTelefone());
            stmt.setString(5, m.getEmail());
            stmt.setDouble(6, m.getSalario());
            stmt.setInt(7, m.getFuncionario().getId_funcionario());
            stmt.execute();
            System.out.println("Sucesso ao Salvar");
            
        }catch(SQLException e) {
            System.out.println("Erro ao Salvar");
            System.out.println(e.getErrorCode());
        }
        
    }
    public void Atualizar(medicos m){
        String sql = "UPDATE medicos SET nome = ?,crm = ? ,especialidade = ? ,telefone = ? ,email = ? ,salario = ?,id_funcionario = ?  WHERE id_medico = ?";
        
        try{
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, m.getNome());
            stmt.setString(2, m.getCrm());
            stmt.setString(3, m.getEspecialidade());
            stmt.setString(4, m.getTelefone());
            stmt.setString(5, m.getEmail());
            stmt.setDouble(6, m.getSalario());
            stmt.setInt(7, m.getFuncionario().getId_funcionario());
            stmt.execute();
            System.out.println("Sucesso ao Atualizar");
        }catch(SQLException e) {
            System.out.println("Erro ao Atualizar");
            System.out.println(e.getErrorCode());
        }
    
    }
    public void excluir(int id) {
        String sql ="DELET FROM medicos WHERE id_medico = ?";
        try{
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.execute();
            System.out.println("Sucesso ao Excluir");
        }catch(SQLException e) {
            System.out.println("Erro ao Excluir");
            System.out.println(e.getErrorCode());
        }
    }
    public List<medicos> listarlodos(){
        List<medicos> medicos = new ArrayList<>();
        String sql = "SELECT  medicos.id_medico, funcionarios.nome, medicos.crm, medicos.especialidade, medicos.email.telefone, medicos.email , funcionarios.salario, funcionarios.id_funcionario\n" +
"FROM medicos JOIN funcionarios ON medicos.id_funcionario = funcionarios.id_funcionario";
        try{
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
                medicos.add(mapear(rs));
            }
            return medicos;
        }catch(SQLException e) {
            System.out.println("Erro ao listar medicos");
            System.out.println(e.getErrorCode());
            return null;
        }

    }
    
    public medicos buscarporcmd(String cmd){
        String sql = "SELECT  medicos.id_medico, funcionarios.nome, medicos.crm, medicos.especialidade, medicos.email.telefone, medicos.email , funcionarios.salario, funcionarios.id_funcionario\n" +
"FROM medicos JOIN funcionarios ON medicos.id_funcionario = funcionarios.id_funcionario where medicos.crm = ?";
        try{
            medicos m = new medicos();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, cmd);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
               m = mapear(rs);
            }
            return m;
            
        }catch(SQLException e) {
            System.out.println("Erro ao buscar medico");
            System.out.println(e.getErrorCode());
            return null;
        }
    }
    public medicos buscarporid(int id){
        String sql = "SELECT  medicos.id_medico, funcionarios.nome, medicos.crm, medicos.especialidade, medicos.email.telefone, medicos.email , funcionarios.salario, funcionarios.id_funcionario\n" +
"FROM medicos JOIN funcionarios ON medicos.id_funcionario = funcionarios.id_funcionario where medicos.id_medico = ?";
        try{
            medicos m = new medicos();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
               m = mapear(rs);
            }
            return m;
            
        }catch(SQLException e) {
            System.out.println("Erro ao buscar medico");
            System.out.println(e.getErrorCode());
            return null;
        }
    }
    
    private medicos mapear(ResultSet rs){
       // nome,crm,especialidade,telefone,email,salario,id_funcionario
        medicos m = new medicos();
        try{
            
            m.setId_medico(rs.getInt("id_medico"));
             m.setNome(rs.getString("nome"));
             m.setCrm(rs.getString("crm"));
             m.setEspecialidade(rs.getString("especialidade"));
             m.setTelefone(rs.getString("telefone"));
             m.setEmail(rs.getString("email"));
             m.setSalario(rs.getDouble("salario"));
             m.setId_funcionario(rs.getInt("id_funcionario"));
             
             
             
             return m;
        }catch(SQLException e) {
            System.out.println("Erro ao mapear");
            System.out.println(e.getErrorCode());
            return null;
        }
       
    }
        
}

