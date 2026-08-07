/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import UTIL.Conexao;
import java.sql.Connection;

/**
 *
 * @author Lucas
 */
public class texte {
    Connection conn;
    public texte(){
        conn = new Conexao().conectar();
    }
    
    
}
