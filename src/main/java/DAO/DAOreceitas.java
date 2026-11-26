/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import MODEL.consultas;
import MODEL.receitas;
import UTIL.ConexaoFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Lucas
 */
public class DAOreceitas {

    private static final String SELECT = "SELECT * FROM receitas ";

    // Mesmo padrão dos outros DAOs: uma conexão por operação, fechada no fim.
    protected Connection conectar() throws SQLException {
        // Singleton: todas as conexões saem da mesma fábrica.
        return ConexaoFactory.getInstance().getConnection();
    }

    public void Salvar(receitas r) {
        gravar(r, false);
    }

    public void Atualizar(receitas r) {
        gravar(r, true);
    }

    private void gravar(receitas r, boolean atualizar) {
        String sql = atualizar
                ? "UPDATE receitas SET id_consulta=?, medicamento=?, dosagem=?, observacoes=?, validade=? WHERE id_receita=?"
                : "INSERT INTO receitas(id_consulta,medicamento,dosagem,observacoes,validade) VALUES(?,?,?,?,?)";
        try (Connection conn = conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, r.getConsulta().getId_consulta());
            stmt.setString(2, r.getMedicamento());
            stmt.setString(3, r.getDosagem());
            stmt.setString(4, r.getObservacoes());
            if (r.getValidade() != null) stmt.setDate(5, new java.sql.Date(r.getValidade().getTime()));
            else stmt.setNull(5, Types.DATE);
            if (atualizar) stmt.setInt(6, r.getId_receita());
            if (stmt.executeUpdate() != 1) throw new SQLException("A receita não existe mais ou não foi gravada.");
        } catch (SQLException e) {
            throw new IllegalStateException(e.getErrorCode() == 1452
                    ? "A consulta dessa receita não existe mais."
                    : "Não foi possível salvar a receita. Verifique os dados e a conexão.", e);
        }
    }

    public void excluir(int id) {
        try (Connection conn = conectar();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM receitas WHERE id_receita=?")) {
            stmt.setInt(1, id);
            if (stmt.executeUpdate() != 1) throw new SQLException("Receita não encontrada.");
        } catch (SQLException e) {
            throw new IllegalStateException("Não foi possível excluir a receita. Ela pode já ter sido removida.", e);
        }
    }

    public List<receitas> mostartodas() {
        return consultar(" ORDER BY id_receita", null);
    }

    public receitas buscarporid(int id) {
        List<receitas> lista = consultar(" WHERE id_receita = ?", id);
        return lista.isEmpty() ? null : lista.get(0);
    }

    // Receita atribuída a uma consulta (a mais recente, caso o banco tenha mais de uma).
    public receitas buscarporconsulta(int idConsulta) {
        List<receitas> lista = consultar(" WHERE id_consulta = ? ORDER BY id_receita DESC LIMIT 1", idConsulta);
        return lista.isEmpty() ? null : lista.get(0);
    }

    private List<receitas> consultar(String filtro, Integer valor) {
        List<receitas> lista = new ArrayList<>();
        try (Connection conn = conectar(); PreparedStatement stmt = conn.prepareStatement(SELECT + filtro)) {
            if (valor != null) stmt.setInt(1, valor);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new IllegalStateException("Não foi possível pesquisar as receitas. Verifique a conexão.", e);
        }
    }

    // Só o ID da consulta é carregado: quem chama já tem a consulta completa na tela.
    private receitas mapear(ResultSet rs) throws SQLException {
        consultas c = new consultas();
        c.setId_consulta(rs.getInt("id_consulta"));
        receitas r = new receitas();
        r.setId_receita(rs.getInt("id_receita"));
        r.setConsulta(c);
        r.setMedicamento(rs.getString("medicamento"));
        r.setDosagem(rs.getString("dosagem"));
        r.setObservacoes(rs.getString("observacoes"));
        r.setValidade(rs.getDate("validade"));
        return r;
    }
}
