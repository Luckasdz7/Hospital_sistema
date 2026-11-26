/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import MODEL.funcionarios;
import UTIL.ConexaoFactory;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Lucas
 */
public class DAOfuncionarios {
    private static final String SELECT = "SELECT * FROM funcionarios ";
    private static final String ORDEM = " ORDER BY nome, id_funcionario";

    protected Connection conectar() throws SQLException {
        // Singleton: todas as conexões saem da mesma fábrica.
        return ConexaoFactory.getInstance().getConnection();
    }

    // Aba Funcionários: grava todos os campos, inclusive salário.
    public void Salvar(funcionarios f) {
        gravar(f, false, true);
    }

    public void Atualizar(funcionarios f) {
        gravar(f, true, true);
    }

    // Aba Usuários: grava só os dados de acesso; o salário fica como está (NULL no cadastro).
    public void SalvarUsuario(funcionarios f) {
        gravar(f, false, false);
    }

    public void AtualizarUsuario(funcionarios f) {
        gravar(f, true, false);
    }

    // Na edição, senha em branco = manter a senha atual.
    private void gravar(funcionarios f, boolean atualizar, boolean comSalario) {
        List<String> colunas = new ArrayList<>(List.of("nome", "cargo", "login", "cpf"));
        List<Object> valores = new ArrayList<>(List.of(f.getNome(), f.getCargo(), f.getLogin(), f.getCpf()));
        if (comSalario) {
            colunas.add("salario");
            valores.add(BigDecimal.valueOf(f.getSalario()).setScale(2, java.math.RoundingMode.HALF_UP));
        }
        if (!atualizar || (f.getSenha() != null && !f.getSenha().isEmpty())) {
            colunas.add("senha");
            valores.add(f.getSenha());
        }
        String sql = atualizar
                ? "UPDATE funcionarios SET " + String.join("=?, ", colunas) + "=? WHERE id_funcionario=?"
                : "INSERT INTO funcionarios(" + String.join(",", colunas) + ") VALUES("
                        + "?,".repeat(colunas.size() - 1) + "?)";
        try (Connection conn = conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < valores.size(); i++) stmt.setObject(i + 1, valores.get(i));
            if (atualizar) stmt.setInt(valores.size() + 1, f.getId_funcionario());
            if (stmt.executeUpdate() != 1) throw new SQLException("O funcionário não existe mais ou não foi gravado.");
        } catch (SQLException e) {
            throw new IllegalStateException(e.getErrorCode() == 1062
                    ? "Esse login já está em uso por outro funcionário."
                    : "Não foi possível salvar o funcionário. Verifique os dados e a conexão.", e);
        }
    }

    public void Excluir(int id) {
        try (Connection conn = conectar();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM funcionarios WHERE id_funcionario=?")) {
            stmt.setInt(1, id);
            if (stmt.executeUpdate() != 1) throw new SQLException("Funcionário não encontrado.");
        } catch (SQLException e) {
            throw new IllegalStateException("Não foi possível excluir o funcionário. Ele pode ter sido removido ou estar vinculado a outros registros.", e);
        }
    }

    public List<funcionarios> listartodos() {
        return pesquisar("nome", "");
    }

    public funcionarios buscarporid(int id) {
        List<funcionarios> lista = pesquisar("id", String.valueOf(id));
        return lista.isEmpty() ? null : lista.get(0);
    }

    public funcionarios buscarlogin(String login, String senha) {
        List<funcionarios> lista = consultar(" WHERE login = ? AND senha = ?", List.of(login, senha));
        return lista.isEmpty() ? null : lista.get(0);
    }

    // A coluna vem de uma lista fixa; todos os valores são parâmetros SQL.
    public List<funcionarios> pesquisar(String campo, String termo) {
        if (!List.of("id", "nome", "cpf", "cargo", "salario", "login").contains(campo))
            throw new IllegalArgumentException("Selecione um tipo de pesquisa válido.");
        if (termo.isBlank()) return consultar("", List.of());
        switch (campo) {
            case "id": return consultar(" WHERE id_funcionario = ?", List.of(Integer.valueOf(termo)));
            case "cpf":
                return consultar(" WHERE REPLACE(REPLACE(REPLACE(cpf,'.',''),'-',''),' ','') LIKE ? ESCAPE '!'",
                        List.of(contem(termo.replaceAll("[.\\-\\s]", ""))));
            case "salario": return consultar(" WHERE salario = ?", List.of(new BigDecimal(termo)));
            default: return consultar(" WHERE " + campo + " LIKE ? ESCAPE '!'", List.of(contem(termo)));
        }
    }

    private List<funcionarios> consultar(String filtro, List<Object> valores) {
        List<funcionarios> lista = new ArrayList<>();
        try (Connection conn = conectar();
             PreparedStatement stmt = conn.prepareStatement(SELECT + filtro + ORDEM)) {
            for (int i = 0; i < valores.size(); i++) stmt.setObject(i + 1, valores.get(i));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new IllegalStateException("Não foi possível pesquisar os funcionários. Verifique a conexão.", e);
        }
    }

    private static String contem(String termo) {
        return "%" + termo.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
    }

    private funcionarios mapear(ResultSet rs) throws SQLException {
        funcionarios f = new funcionarios();
        f.setId_funcionario(rs.getInt("id_funcionario"));
        f.setNome(rs.getString("nome"));
        f.setCargo(rs.getString("cargo"));
        f.setSalario(rs.getDouble("salario"));
        f.setLogin(rs.getString("login"));
        f.setSenha(rs.getString("senha"));
        f.setCpf(rs.getString("cpf"));
        return f;
    }
}
