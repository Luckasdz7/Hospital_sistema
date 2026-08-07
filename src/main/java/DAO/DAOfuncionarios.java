/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import MODEL.funcionarios;
import UTIL.Conexao;
import com.mysql.cj.protocol.Resultset;
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
public class DAOfuncionarios {
    Connection conn;
    public DAOfuncionarios(){
        conn = new Conexao().conectar();
    }
    public void Salvar(funcionarios f){
        String sql = "INSERT INTO funcionarios(nome,cargo,salario,login,senha,cpf) VALUES (?,?,?,?,?,?)";
        try{
            PreparedStatement stmt =  conn.prepareStatement(sql);
            stmt.setString(1, f.getNome());
            stmt.setString(2, f.getCargo());
            stmt.setDouble(3, f.getSalario());
            stmt.setString(4, f.getLogin());
            stmt.setString(5, f.getSenha());
            stmt.setString(6, f.getCpf());
            stmt.execute();
            System.out.println("Sucesso ao Salvar ");
        }catch(SQLException e){
            System.out.println("Erro ao salvar funcionario");
            System.out.println(e.getErrorCode());
        }
    }
    
    public void Atualizar(funcionarios f){
        String sql = "update funcionarios SET nome = ?,cargo = ?,salario = ? ,login = ? ,senha = ? ,cpf = ? where id_funcionario = ?";
        
        try{
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, f.getNome());
            stmt.setString(2, f.getCargo());
            stmt.setDouble(3, f.getSalario());
            stmt.setString(4, f.getLogin());
            stmt.setString(5, f.getSenha());
            stmt.setString(6, f.getCpf());
            stmt.setInt(7, f.getId_funcionario());
            stmt.execute();
            System.out.println("Sucesso ao atualizar funcionario");
        }catch(SQLException e){
            System.out.println("Erro ao Atualizar funcionario");
            System.out.println(e.getErrorCode());
        }
    }
    
    public void Excluir(int id){
        String sql = "DELETE from funcionarios where id_funcionario = ?";
        try{
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.execute();
            System.out.println("Sucesso ao Excluir funcionario");
        }catch(SQLException e){
            System.out.println("Erro ao Excluir funcionario");
            System.out.println(e.getErrorCode());
        }
    }
    public List<funcionarios> listartodos(){
        List<funcionarios> lista = new ArrayList<>();
        
        String sql = "SELECT * FROM funcionarios";
        try{
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        while(rs.next()){
            lista.add(mapear(rs));
        }
        return lista;
        }catch(SQLException e){
            System.out.println("Erro ao listar funcionarios");
            System.out.println(e.getErrorCode());
            return null;
        }
    }
    public funcionarios buscarporid(int id){
        funcionarios f = null;
        String sql = "SELECT * FROM funcionarios where id_funcionario = ?";
        try{
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
                f = mapear(rs);
            }
            return f;
            
        }catch(SQLException e){
            
            System.out.println("Erro ao pesquisar funcionario");
            System.out.println(e.getErrorCode());
            return null;
        }
    }
    public funcionarios buscarlogin(String login, String senha){
        funcionarios f = null;
        String sql = "SELECT * FROM funcionarios where login = ? AND senha = ?";
        try{
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, login);
            stmt.setString(2, senha);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
                f = mapear(rs);
            }
            return f;
            
        }catch(SQLException e){
            
            System.out.println("Erro ao pesquisar funcionario");
            System.out.println(e.getErrorCode());
            return null;
        }
    }
    
    private funcionarios mapear(ResultSet rs){
    
        try{
        funcionarios f = new funcionarios();
        f.setId_funcionario(rs.getInt("id_funcionario"));
        f.setNome(rs.getString("nome"));
        f.setCargo(rs.getString("cargo"));
        f.setSalario(rs.getDouble("salario"));
        f.setLogin(rs.getString("login"));
        f.setSenha(rs.getString("senha"));
        f.setCpf(rs.getString("cpf"));
        return f;
        }catch(SQLException e){
            System.out.println("Erro ao mapear funcionario");
            System.out.println(e.getErrorCode());
            return null;
        }
    
    }

        
    
}
