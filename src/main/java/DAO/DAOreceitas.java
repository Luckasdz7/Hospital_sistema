/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import MODEL.consultas;
import MODEL.enum_consultas;
import MODEL.medicos;
import MODEL.pacientes;
import MODEL.receitas;
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
public class DAOreceitas {
    Connection conn;
     public DAOreceitas(){
        conn = new Conexao().conectar();
    }
     public void Salvar(receitas r){
         String sql = "INSERT INTO receitas(id_consulta,medicamento,dosagem,observacoes,validade) VALUES (?,?,?,?,?)";
         try{
             PreparedStatement stmt = conn.prepareStatement(sql);
             stmt.setInt(1, r.getConsulta().getId_consulta());
             stmt.setString(2, r.getMedicamento());
             stmt.setString(3, r.getDosagem());
             stmt.setString(4, r.getObservacoes());
             stmt.setDate(5, new java.sql.Date(r.getValidade().getTime()));
            
             stmt.execute();
              System.out.println("SUCESSO AO SALVAR receitas");
         }catch(SQLException e){
             System.out.println("ERRO AO SALVAR receitas");
             System.out.println(e.getErrorCode());
         }
     }
      public void Atualizar(receitas r){
          String sql = "INSERT INTO receitas id_consulta = ? ,medicamento = ? ,dosagem= ?,observacoes =? ,validade = ? WHERE id_receita = ?";
         try{
             PreparedStatement stmt = conn.prepareStatement(sql);
             stmt.setInt(1, r.getConsulta().getId_consulta());
             stmt.setString(2, r.getMedicamento());
             stmt.setString(3, r.getDosagem());
             stmt.setString(4, r.getObservacoes());
             stmt.setDate(5, new java.sql.Date(r.getValidade().getTime()));
             stmt.setInt(6, r.getId_receita());
            
             stmt.execute();
              System.out.println("SUCESSO AO Atualizar receitas");
         }catch(SQLException e){
             System.out.println("ERRO AO Atualizar receitas");
             System.out.println(e.getErrorCode());
         }
         
     }
      public void excluir(int id){
          String sql = "DELETE FROM receitas WHERE id_receita = ?";
         try{
             PreparedStatement stmt = conn.prepareStatement(sql);
             stmt.setInt(1, id );
             stmt.execute();
              System.out.println("SUCESSO AO excluir id_receita");
         }catch(SQLException e){
             System.out.println("ERRO AO excluir id_receita");
             System.out.println(e.getErrorCode());
         }
      }
      //terminar ainda
      public List<receitas> mostartodas(){
          List<receitas> lista = new ArrayList<>();
          String sql = "SELECT * FROM receitas";
          try{
              PreparedStatement stmt = conn.prepareStatement(sql);
              ResultSet rs = stmt.executeQuery();
              while(rs.next()){
                  lista.add(mapear(rs));
              }
              return lista;
              
          }catch(SQLException e){
             System.out.println("ERRO AO mostar receitas");
             System.out.println(e.getErrorCode());
             return null;
         }
      }
      
      public receitas buscarporid(int id){
          receitas r = null;
          String sql = "SELECT * FROM receitas WHERE id_receita = ?";
          try{
              PreparedStatement stmt = conn.prepareStatement(sql);
              stmt.setInt(1, id);
              ResultSet rs = stmt.executeQuery();
              while(rs.next()){
                  r = mapear(rs);
              }
              return r;
              
          }catch(SQLException e){
             System.out.println("ERRO AO mostar id_receita");
             System.out.println(e.getErrorCode());
             return null;
         }
      }
    
     private receitas mapear(ResultSet rs) {
    receitas r = new receitas();
    try {
        
        DAOconsultas dao = new DAOconsultas();
        consultas c = dao.buscarporid(rs.getInt("id_consulta"));
        
        r.setId_receita(rs.getInt("Id_receita"));
        r.setConsulta(c);
        r.setMedicamento(rs.getString("medicamento"));
        r.setDosagem(rs.getString("dosagem"));
        r.setObservacoes(rs.getString("observacoes"));
        r.setValidade(rs.getDate("validade"));
   
        return r;
        
    } catch(SQLException e) {
        System.out.println("ERRO AO MAPEAR CONSULTA");
        System.out.println(e.getErrorCode());
        return null; 
    }
}
}
