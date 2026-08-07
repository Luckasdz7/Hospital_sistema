/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import MODEL.consultas;
import MODEL.enum_consultas;
import MODEL.medicos;
import MODEL.pacientes;
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
public class DAOconsultas {
    Connection conn;
     public DAOconsultas(){
        conn = new Conexao().conectar();
    }
     public void Salvar(consultas c){
         String sql = "INSERT INTO consultas(id_paciente,id_medico,data_consulta,hora_consulta,sala, status) VALUES (?,?,?,?,?,?)";
         try{
             PreparedStatement stmt = conn.prepareStatement(sql);
             stmt.setInt(1, c.getPaciente().getId_paciente());
             stmt.setInt(2, c.getMedico().getId_medico());
             stmt.setDate(3, new java.sql.Date(c.getData_consulta().getTime()));
             stmt.setTime(4, java.sql.Time.valueOf(c.getHora_consulta()));
             stmt.setString(5, c.getSala());
             stmt.setObject(6, c.getStatus().name());
             stmt.execute();
              System.out.println("SUCESSO AO SALVAR CONSULTA");
         }catch(SQLException e){
             System.out.println("ERRO AO SALVAR CONSULTA");
             System.out.println(e.getErrorCode());
         }
     }
      public void Atualizar(consultas c){
         String sql = "update consultas set id_paciente = ?,id_medico = ? ,data_consulta = ?,hora_consulta = ?,sala = ?, status = ? WHERE id_consulta = ?";
         try{
             PreparedStatement stmt = conn.prepareStatement(sql);
             stmt.setInt(1, c.getPaciente().getId_paciente());
             stmt.setInt(2, c.getMedico().getId_medico());
             stmt.setDate(3, new java.sql.Date(c.getData_consulta().getTime()));
             stmt.setTime(4, java.sql.Time.valueOf(c.getHora_consulta()));
             stmt.setString(5, c.getSala());
             stmt.setObject(6, c.getStatus().name());
             stmt.setInt(7, c.getId_consulta());
             stmt.execute();
             
              System.out.println("SUCESSO AO Atualizar CONSULTA");
         }catch(SQLException e){
             System.out.println("ERRO AO Atualizar CONSULTA");
             System.out.println(e.getErrorCode());
         }
         
         
     }
      public void excluir(int id){
          String sql = "DELETE FROM consultas WHERE id_consulta = ?";
         try{
             PreparedStatement stmt = conn.prepareStatement(sql);
             stmt.setInt(1, id );
             stmt.execute();
              System.out.println("SUCESSO AO excluir CONSULTA");
         }catch(SQLException e){
             System.out.println("ERRO AO excluir CONSULTA");
             System.out.println(e.getErrorCode());
         }
      }
      
      public List<consultas> mostartodas(){
          List<consultas> lista = new ArrayList<>();
          String sql = "SELECT * FROM consultas";
          try{
              PreparedStatement stmt = conn.prepareStatement(sql);
              ResultSet rs = stmt.executeQuery();
              while(rs.next()){
                  lista.add(mapear(rs));
              }
              return lista;
              
          }catch(SQLException e){
             System.out.println("ERRO AO mostar CONSULTAS");
             System.out.println(e.getErrorCode());
             return null;
         }
      }
      
      public consultas buscarporid(int id){
          consultas c = null;
          String sql = "SELECT * FROM consultas WHERE id_consulta = ?";
          try{
              PreparedStatement stmt = conn.prepareStatement(sql);
              stmt.setInt(1, id);
              ResultSet rs = stmt.executeQuery();
              while(rs.next()){
                  c = mapear(rs);
              }
              return c;
              
          }catch(SQLException e){
             System.out.println("ERRO AO mostar CONSULTAS");
             System.out.println(e.getErrorCode());
             return null;
         }
      }
      
     private consultas mapear(ResultSet rs) {
    consultas c = new consultas();
    try {
        
        DAOMedicos daopa = new DAOMedicos(); 
        medicos m = daopa.buscarporid(rs.getInt("id_medico"));
        
        DAOpacientes pasc = new DAOpacientes();
        pacientes p = pasc.buscarporid(rs.getInt("id_paciente"));
        
        c.setId_consulta(rs.getInt("id_consulta"));
        c.setPaciente(p);
        c.setMedico(m);
        c.setData_consulta(rs.getDate("data_consulta"));
        
        
        c.setHora_consulta(rs.getTime("hora_consulta").toLocalTime());
        
        c.setSala(rs.getString("sala"));
        
        
        c.setStatus(enum_consultas.valueOf(rs.getString("status")));
        
        return c;
        
    } catch(SQLException e) {
        System.out.println("ERRO AO MAPEAR CONSULTA");
        System.out.println(e.getErrorCode());
        return null; 
    }
}
          

}
