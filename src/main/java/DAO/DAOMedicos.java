/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import MODEL.funcionarios;
import MODEL.medicos;
import UTIL.Conexao;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Lucas
 */
public class DAOMedicos {
    // A tabela medicos não tem nome nem salário: esses dados vêm do funcionário vinculado.
    private static final String SELECT = "SELECT m.id_medico, m.crm, m.especialidade, m.telefone, m.email, m.id_funcionario, "
            + "COALESCE(f.nome, CONCAT('Médico CRM ', m.crm)) AS nome, f.cpf, f.cargo, f.salario, f.login "
            + "FROM medicos m LEFT JOIN funcionarios f ON f.id_funcionario = m.id_funcionario ";
    private static final String ORDEM = " ORDER BY nome, m.id_medico";

    protected Connection conectar() throws SQLException {
        Connection conn = new Conexao().conectar();
        if (conn == null) throw new SQLException("Não foi possível conectar ao banco.");
        return conn;
    }

    public void Salvar(medicos m) {
        gravar(m, false);
    }

    public void Atualizar(medicos m) {
        gravar(m, true);
    }

    private void gravar(medicos m, boolean atualizar) {
        String sql = atualizar
                ? "UPDATE medicos SET crm=?, especialidade=?, telefone=?, email=?, id_funcionario=? WHERE id_medico=?"
                : "INSERT INTO medicos(crm,especialidade,telefone,email,id_funcionario) VALUES(?,?,?,?,?)";
        try (Connection conn = conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, m.getCrm());
            stmt.setString(2, m.getEspecialidade());
            stmt.setString(3, m.getTelefone());
            stmt.setString(4, m.getEmail());
            int idFuncionario = m.getFuncionario() != null ? m.getFuncionario().getId_funcionario() : m.getId_funcionario();
            if (idFuncionario > 0) stmt.setInt(5, idFuncionario); else stmt.setNull(5, Types.INTEGER);
            if (atualizar) stmt.setInt(6, m.getId_medico());
            if (stmt.executeUpdate() != 1) throw new SQLException("O médico não existe mais ou não foi gravado.");
        } catch (SQLException e) {
            throw new IllegalStateException(e.getErrorCode() == 1062
                    ? "Já existe um médico com esse CRM, e-mail ou funcionário vinculado."
                    : "Não foi possível salvar o médico. Verifique os dados e a conexão.", e);
        }
    }

    public void excluir(int id) {
        try (Connection conn = conectar();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM medicos WHERE id_medico=?")) {
            stmt.setInt(1, id);
            if (stmt.executeUpdate() != 1) throw new SQLException("Médico não encontrado.");
        } catch (SQLException e) {
            throw new IllegalStateException(e.getErrorCode() == 1451
                    ? "Não é possível excluir: o médico possui consultas vinculadas."
                    : "Não foi possível excluir o médico. Ele pode ter sido removido.", e);
        }
    }

    public List<medicos> listarlodos() {
        return pesquisar("nome", "");
    }

    public medicos buscarporcmd(String crm) {
        List<medicos> lista = consultar(" WHERE m.crm = ?", List.of(crm));
        return lista.isEmpty() ? null : lista.get(0);
    }

    // Médico vinculado ao login (usado para saber qual médico entrou no sistema).
    public medicos buscarporfuncionario(int idFuncionario) {
        List<medicos> lista = consultar(" WHERE m.id_funcionario = ?", List.of(idFuncionario));
        return lista.isEmpty() ? null : lista.get(0);
    }

    public medicos buscarporid(int id) {
        List<medicos> lista = pesquisar("id", String.valueOf(id));
        return lista.isEmpty() ? null : lista.get(0);
    }

    // A coluna vem de uma lista fixa; todos os valores são parâmetros SQL.
    public List<medicos> pesquisar(String campo, String termo) {
        String filtro;
        switch (campo) {
            case "id": filtro = "m.id_medico = ?"; break;
            case "nome": filtro = "f.nome LIKE ? ESCAPE '!'"; break;
            case "crm": filtro = "m.crm LIKE ? ESCAPE '!'"; break;
            case "especialidade": filtro = "m.especialidade LIKE ? ESCAPE '!'"; break;
            case "telefone": filtro = "m.telefone LIKE ? ESCAPE '!'"; break;
            case "email": filtro = "m.email LIKE ? ESCAPE '!'"; break;
            default: throw new IllegalArgumentException("Selecione um tipo de pesquisa válido.");
        }
        if (termo.isBlank()) return consultar("", List.of());
        Object valor = campo.equals("id") ? Integer.valueOf(termo) : contem(termo);
        return consultar(" WHERE " + filtro, List.of(valor));
    }

    private List<medicos> consultar(String filtro, List<Object> valores) {
        List<medicos> lista = new ArrayList<>();
        try (Connection conn = conectar();
             PreparedStatement stmt = conn.prepareStatement(SELECT + filtro + ORDEM)) {
            for (int i = 0; i < valores.size(); i++) stmt.setObject(i + 1, valores.get(i));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new IllegalStateException("Não foi possível pesquisar os médicos. Verifique a conexão.", e);
        }
    }

    // Sugestões do campo Funcionário: só cargo "medico" e sem outro médico já vinculado.
    public List<funcionarios> pesquisarFuncionarios(String termo, int idMedicoAtual) {
        List<funcionarios> lista = new ArrayList<>();
        String sql = "SELECT f.id_funcionario, f.nome, f.cpf, f.cargo, f.login FROM funcionarios f "
                + "WHERE f.cargo = 'medico' AND (f.nome LIKE ? ESCAPE '!' OR f.cpf LIKE ? ESCAPE '!' OR f.login LIKE ? ESCAPE '!') "
                + "AND NOT EXISTS (SELECT 1 FROM medicos m WHERE m.id_funcionario = f.id_funcionario AND m.id_medico <> ?) "
                + "ORDER BY f.nome, f.id_funcionario LIMIT 20";
        try (Connection conn = conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, contem(termo));
            stmt.setString(2, contem(termo.replaceAll("[.\\-\\s]", "")));
            stmt.setString(3, contem(termo));
            stmt.setInt(4, idMedicoAtual);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    funcionarios f = new funcionarios();
                    f.setId_funcionario(rs.getInt("id_funcionario"));
                    f.setNome(rs.getString("nome"));
                    f.setCpf(rs.getString("cpf"));
                    f.setCargo(rs.getString("cargo"));
                    f.setLogin(rs.getString("login"));
                    lista.add(f);
                }
            }
            return lista;
        } catch (SQLException e) { throw new IllegalStateException("Erro ao pesquisar funcionários.", e); }
    }

    private static String contem(String termo) {
        return "%" + termo.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
    }

    private medicos mapear(ResultSet rs) throws SQLException {
        medicos m = new medicos();
        m.setId_medico(rs.getInt("id_medico"));
        m.setCrm(rs.getString("crm"));
        m.setEspecialidade(rs.getString("especialidade"));
        m.setTelefone(rs.getString("telefone"));
        m.setEmail(rs.getString("email"));
        m.setNome(rs.getString("nome"));
        m.setCpf(rs.getString("cpf"));
        int idFuncionario = rs.getInt("id_funcionario");
        if (!rs.wasNull()) {
            funcionarios f = new funcionarios();
            f.setId_funcionario(idFuncionario);
            f.setNome(rs.getString("nome"));
            f.setCpf(rs.getString("cpf"));
            f.setCargo(rs.getString("cargo"));
            f.setSalario(rs.getDouble("salario"));
            f.setLogin(rs.getString("login"));
            m.setFuncionario(f);
            m.setId_funcionario(idFuncionario);
            m.setCargo(f.getCargo());
            m.setSalario(f.getSalario());
            m.setLogin(f.getLogin());
        }
        return m;
    }
}
