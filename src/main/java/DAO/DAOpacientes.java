/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import MODEL.Sexo;
import MODEL.pacientes;
import UTIL.Conexao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Lucas
 */
public class DAOpacientes {
    Connection conn;
    public DAOpacientes(){
        conn = new Conexao().conectar();
    }
    
    public void Salvar(pacientes p){
        String sql = "INSERT INTO pacientes(nome,cpf,data_nascimento,telefone,email,sexo,endereco,tipo_sanguineo) VALUES (?,?,?,?,?,?,?,?)";
        try{
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, p.getNome());
            stmt.setString(2, p.getCpf());
            stmt.setDate(3, new java.sql.Date(p.getData_nascimento().getTime()));
            stmt.setString(4, p.getTelefone());
            stmt.setString(5, p.getEmail());
            stmt.setString(6, p.getSexo().name());
            stmt.setString(7, p.getEndereco());
            stmt.setString(8, p.getTipo_sanguineo());
            stmt.execute();
             System.out.println("Sucesso ao Excluir paciente");
            
        }catch(SQLException e){
            System.out.println("Erro ao Salvar paciente");
            System.out.println(e.getErrorCode());
            
        }
    }
    public void Atualizar(pacientes p){
        String sql = "UPDATE pacientes SET nome = ?,cpf = ?,data_nascimento = ?,telefone = ?,email = ?,sexo = ?,endereco = ?,tipo_sanguineo = ? where id_paciente = ?";
        try{
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, p.getNome());
            stmt.setString(2, p.getCpf());
            stmt.setDate(3, new java.sql.Date(p.getData_nascimento().getTime()));
            stmt.setString(4, p.getTelefone());
            stmt.setString(5, p.getEmail());
            stmt.setString(6, p.getSexo().name());
            stmt.setString(7, p.getEndereco());
            stmt.setString(8, p.getTipo_sanguineo());
            stmt.setInt(9, p.getId_paciente());
            stmt.execute();
             System.out.println("Sucesso ao Excluir paciente");
            
        }catch(SQLException e){
            System.out.println("Erro ao Atualizar paciente");
            System.out.println(e.getErrorCode());
        }
    }
    
    public void excluir(int id){
        String sql = "delete FROM pacientes WHERE id_paciente = ?";
        try{
             PreparedStatement stmt = conn.prepareStatement(sql);
             stmt.setInt(1, id);
             stmt.execute();
              System.out.println("Sucesso ao Excluir paciente");
        }catch(SQLException e){
            System.out.println("Erro ao Excluir paciente");
            System.out.println(e.getErrorCode());
        }
    }
    public List<pacientes> listartodos(){
        String sql = "SELECT * FROM pacientes";
        List<pacientes> lista = new ArrayList<>();
        try{
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
                lista.add(mapear(rs));
            }
             System.out.println("Sucesso ao listar pacientes");
            return lista;
            
            
        }catch(SQLException e){
            System.out.println("Erro ao listar pacientes");
            System.out.println(e.getErrorCode());
            return null;
        }
    }
    public pacientes buscarporcpf(String cpf){
        String sql = "SELECT * FROM pacientes WHERE cpf = ?";
        pacientes p = new pacientes();
        try{
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, cpf);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
               p = mapear(rs);
            }
             System.out.println("Sucesso buscar paciente por cpf");
            return p;
            
        }catch(SQLException e){
            System.out.println("Erro ao buscar paciente por cpf");
            System.out.println(e.getErrorCode());
            return null;
        }
    }
    
    public pacientes buscarporid(int id){
        String sql = "SELECT * FROM pacientes WHERE id_paciente = ?";
        pacientes p = new pacientes();
        try{
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
               p = mapear(rs);
            }
            
            return p;
            
        }catch(SQLException e){
            System.out.println("Erro ao buscar paciente por id");
            System.out.println(e.getErrorCode());
            return null;
        }
    }
    
    private pacientes mapear(ResultSet rs){
        
        try{
        pacientes p = new pacientes();
        p.setId_paciente(rs.getInt("id_paciente"));
        p.setNome(rs.getString("nome"));
        p.setCpf(rs.getString("cpf"));
        p.setData_nascimento(rs.getDate("data_nascimento"));
        p.setTelefone(rs.getString("telefone"));
        p.setEmail(rs.getString("email"));
        p.setSexo(Sexo.valueOf(rs.getString("sexo")));
        p.setEndereco(rs.getString("endereco"));
        p.setTipo_sanguineo(rs.getString("tipo_sanguineo"));
        
        return p;
        
        }catch(SQLException e){
            System.out.println("Erro ao mapear paciente");
            System.out.println(e.getErrorCode());
            return null;
        }
        
    }
}
