/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package UTIL;

import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 * Mantida por compatibilidade com código antigo (ex.: DAO.texte).
 * Agora só repassa para o Singleton {@link ConexaoFactory}; os DAOs usam a fábrica direto.
 *
 * @author Lucas
 */
  public class Conexao {

    public Connection conectar() {
        try {
            return ConexaoFactory.getInstance().getConnection();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao conectar tente novamente");
            JOptionPane.showMessageDialog(null, e.getLocalizedMessage());
            return null;
        }
    }
  }
