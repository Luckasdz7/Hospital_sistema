/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import MODEL.Sexo;
import MODEL.pacientes;
import UTIL.ConexaoFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Lucas
 */
public class DAOpacientes {
    private static final String SELECT = "SELECT * FROM pacientes ";
    private static final String ORDEM = " ORDER BY nome, id_paciente";

    protected Connection conectar() throws SQLException {
        // Singleton: todas as conexões saem da mesma fábrica.
        return ConexaoFactory.getInstance().getConnection();
    }

    public void Salvar(pacientes p) {
        gravar(p, false);
    }

    public void Atualizar(pacientes p) {
        gravar(p, true);
    }

    private void gravar(pacientes p, boolean atualizar) {
        String sql = atualizar
                ? "UPDATE pacientes SET nome=?, cpf=?, data_nascimento=?, telefone=?, email=?, sexo=?, endereco=?, tipo_sanguineo=? WHERE id_paciente=?"
                : "INSERT INTO pacientes(nome,cpf,data_nascimento,telefone,email,sexo,endereco,tipo_sanguineo) VALUES(?,?,?,?,?,?,?,?)";
        try (Connection conn = conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getNome());
            stmt.setString(2, p.getCpf());
            stmt.setDate(3, new java.sql.Date(p.getData_nascimento().getTime()));
            stmt.setString(4, p.getTelefone());
            stmt.setString(5, p.getEmail());
            stmt.setString(6, sexoBanco(p.getSexo()));
            stmt.setString(7, p.getEndereco());
            stmt.setString(8, p.getTipo_sanguineo());
            if (atualizar) stmt.setInt(9, p.getId_paciente());
            if (stmt.executeUpdate() != 1) throw new SQLException("O paciente não existe mais ou não foi gravado.");
        } catch (SQLException e) {
            throw new IllegalStateException(e.getErrorCode() == 1062
                    ? "Já existe um paciente cadastrado com esse CPF."
                    : "Não foi possível salvar o paciente. Verifique os dados e a conexão.", e);
        }
    }

    public void excluir(int id) {
        try (Connection conn = conectar();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM pacientes WHERE id_paciente=?")) {
            stmt.setInt(1, id);
            if (stmt.executeUpdate() != 1) throw new SQLException("Paciente não encontrado.");
        } catch (SQLException e) {
            throw new IllegalStateException("Não foi possível excluir o paciente. Ele pode ter sido removido ou estar vinculado a outros registros.", e);
        }
    }

    public List<pacientes> listartodos() {
        return pesquisar("nome", "");
    }

    public pacientes buscarporid(int id) {
        List<pacientes> lista = pesquisar("id", String.valueOf(id));
        return lista.isEmpty() ? null : lista.get(0);
    }

    public pacientes buscarporcpf(String cpf) {
        List<pacientes> lista = consultar(" WHERE cpf = ?", List.of(cpf));
        return lista.isEmpty() ? null : lista.get(0);
    }

    // A coluna vem de uma lista fixa; todos os valores são parâmetros SQL.
    public List<pacientes> pesquisar(String campo, String termo) {
        if (termo.isBlank()) {
            if (!List.of("id", "nome", "cpf", "nascimento", "telefone", "email", "sexo", "sangue").contains(campo))
                throw new IllegalArgumentException("Selecione um tipo de pesquisa válido.");
            return consultar("", List.of());
        }
        switch (campo) {
            case "id": return consultar(" WHERE id_paciente = ?", List.of(Integer.valueOf(termo)));
            case "nome": return consultar(" WHERE nome LIKE ? ESCAPE '!'", List.of(contem(termo)));
            case "cpf":
                return consultar(" WHERE cpf LIKE ? ESCAPE '!' OR "
                        + "REPLACE(REPLACE(REPLACE(cpf,'.',''),'-',''),' ','') LIKE ? ESCAPE '!'",
                        List.of(contem(termo), contem(termo.replaceAll("[.\\-\\s]", ""))));
            case "nascimento": return consultar(" WHERE data_nascimento = ?", List.of(Date.valueOf(termo)));
            case "telefone": return consultar(" WHERE telefone LIKE ? ESCAPE '!'", List.of(contem(termo)));
            case "email": return consultar(" WHERE email LIKE ? ESCAPE '!'", List.of(contem(termo)));
            case "sexo": return consultar(" WHERE sexo = ?", List.of(sexoBanco(Sexo.valueOf(termo))));
            case "sangue": return consultar(" WHERE tipo_sanguineo = ?", List.of(termo));
            default: throw new IllegalArgumentException("Selecione um tipo de pesquisa válido.");
        }
    }

    private List<pacientes> consultar(String filtro, List<Object> valores) {
        List<pacientes> lista = new ArrayList<>();
        try (Connection conn = conectar();
             PreparedStatement stmt = conn.prepareStatement(SELECT + filtro + ORDEM)) {
            for (int i = 0; i < valores.size(); i++) stmt.setObject(i + 1, valores.get(i));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new IllegalStateException("Não foi possível pesquisar os pacientes. Verifique a conexão.", e);
        }
    }

    private static String contem(String termo) {
        return "%" + termo.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
    }

    // No banco o ENUM é ('M','F','Outro'); no Java é M, F, OUTRO.
    private static String sexoBanco(Sexo sexo) {
        if (sexo == null) return null;
        return sexo == Sexo.OUTRO ? "Outro" : sexo.name();
    }

    private static Sexo sexoJava(String valor) {
        if (valor == null || valor.isBlank()) return null;
        return Sexo.valueOf(valor.trim().toUpperCase());
    }

    private pacientes mapear(ResultSet rs) throws SQLException {
        pacientes p = new pacientes();
        p.setId_paciente(rs.getInt("id_paciente"));
        p.setNome(rs.getString("nome"));
        p.setCpf(rs.getString("cpf"));
        p.setData_nascimento(rs.getDate("data_nascimento"));
        p.setTelefone(rs.getString("telefone"));
        p.setEmail(rs.getString("email"));
        p.setSexo(sexoJava(rs.getString("sexo")));
        p.setEndereco(rs.getString("endereco"));
        p.setTipo_sanguineo(rs.getString("tipo_sanguineo"));
        return p;
    }
}
