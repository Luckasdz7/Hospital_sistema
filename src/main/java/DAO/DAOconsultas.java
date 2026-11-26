package DAO;

import MODEL.consultas;
import MODEL.enum_consultas;
import MODEL.medicos;
import MODEL.pacientes;
import UTIL.Conexao;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DAOconsultas {
    private static final String SELECT = "SELECT c.*, p.nome AS paciente_nome, p.cpf AS paciente_cpf, "
            + "COALESCE(f.nome, CONCAT('Médico CRM ', m.crm)) AS medico_nome, f.cpf AS medico_cpf, m.crm "
            + "FROM consultas c JOIN pacientes p ON p.id_paciente = c.id_paciente "
            + "JOIN medicos m ON m.id_medico = c.id_medico "
            + "LEFT JOIN funcionarios f ON f.id_funcionario = m.id_funcionario ";
    private static final String ORDEM = " ORDER BY c.data_consulta DESC, c.hora_consulta DESC, c.id_consulta DESC";

    protected Connection conectar() throws SQLException {
        Connection conn = new Conexao().conectar();
        if (conn == null) throw new SQLException("Não foi possível conectar ao banco.");
        return conn;
    }

    public void Salvar(consultas c) {
        gravar(c, false);
    }

    public void Atualizar(consultas c) {
        gravar(c, true);
    }

    private void gravar(consultas c, boolean atualizar) {
        String sql = atualizar
                ? "UPDATE consultas SET id_paciente=?, id_medico=?, data_consulta=?, hora_consulta=?, sala=?, status=? WHERE id_consulta=?"
                : "INSERT INTO consultas(id_paciente,id_medico,data_consulta,hora_consulta,sala,status) VALUES(?,?,?,?,?,?)";
        try (Connection conn = conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, c.getPaciente().getId_paciente());
            stmt.setInt(2, c.getMedico().getId_medico());
            stmt.setDate(3, new java.sql.Date(c.getData_consulta().getTime()));
            stmt.setTime(4, Time.valueOf(c.getHora_consulta()));
            stmt.setString(5, c.getSala());
            stmt.setString(6, c.getStatus().name());
            if (atualizar) stmt.setInt(7, c.getId_consulta());
            if (stmt.executeUpdate() != 1) throw new SQLException("A consulta não existe mais ou não foi gravada.");
        } catch (SQLException e) {
            throw new IllegalStateException("Não foi possível salvar a consulta. Verifique os dados e a conexão.", e);
        }
    }

    // Passa a consulta para outro médico (o médico logado que assumiu a consulta).
    public void trocarMedico(int idConsulta, int idMedico) {
        try (Connection conn = conectar();
             PreparedStatement stmt = conn.prepareStatement("UPDATE consultas SET id_medico=? WHERE id_consulta=?")) {
            stmt.setInt(1, idMedico);
            stmt.setInt(2, idConsulta);
            if (stmt.executeUpdate() != 1) throw new SQLException("Consulta não encontrada.");
        } catch (SQLException e) {
            throw new IllegalStateException("Não foi possível assumir a consulta. Ela pode ter sido removida.", e);
        }
    }

    public void excluir(int id) {
        try (Connection conn = conectar();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM consultas WHERE id_consulta=?")) {
            stmt.setInt(1, id);
            if (stmt.executeUpdate() != 1) throw new SQLException("Consulta não encontrada.");
        } catch (SQLException e) {
            throw new IllegalStateException("Não foi possível excluir a consulta. Ela pode possuir receitas vinculadas ou ter sido removida.", e);
        }
    }

    public List<consultas> mostartodas() {
        return pesquisar("paciente", "");
    }

    public consultas buscarporid(int id) {
        List<consultas> lista = pesquisar("id", String.valueOf(id));
        return lista.isEmpty() ? null : lista.get(0);
    }

    // A coluna vem de uma lista fixa; todos os valores são parâmetros SQL.
    public List<consultas> pesquisar(String campo, String termo) {
        String filtro;
        Object valor = contem(termo);
        int parametros = 1;
        switch (campo) {
            case "paciente":
                filtro = "(p.nome LIKE ? ESCAPE '!' OR p.cpf LIKE ? ESCAPE '!' OR "
                        + "REPLACE(REPLACE(REPLACE(p.cpf,'.',''),'-',''),' ','') LIKE ? ESCAPE '!')";
                parametros = 3;
                break;
            case "medico":
                filtro = "(f.nome LIKE ? ESCAPE '!' OR m.crm LIKE ? ESCAPE '!' OR f.cpf LIKE ? ESCAPE '!' OR "
                        + "REPLACE(REPLACE(REPLACE(f.cpf,'.',''),'-',''),' ','') LIKE ? ESCAPE '!')";
                parametros = 4;
                break;
            case "id": filtro = "c.id_consulta = ?"; if (!termo.isBlank()) valor = Integer.valueOf(termo); break;
            case "data": filtro = "c.data_consulta = ?"; if (!termo.isBlank()) valor = Date.valueOf(termo); break;
            case "horario": filtro = "c.hora_consulta = ?"; if (!termo.isBlank()) valor = Time.valueOf(java.time.LocalTime.parse(termo)); break;
            case "sala": filtro = "c.sala LIKE ? ESCAPE '!'"; break;
            case "status": filtro = "c.status = ?"; if (!termo.isBlank()) valor = enum_consultas.valueOf(termo).name(); break;
            default: throw new IllegalArgumentException("Selecione um tipo de pesquisa válido.");
        }
        boolean todos = termo.isBlank();
        List<consultas> lista = new ArrayList<>();
        try (Connection conn = conectar();
             PreparedStatement stmt = conn.prepareStatement(SELECT + (todos ? "" : " WHERE " + filtro) + ORDEM)) {
            if (!todos) {
                for (int i = 1; i <= parametros; i++) {
                    stmt.setObject(i, i == parametros && parametros > 1
                            ? contem(termo.replaceAll("[.\\-\\s]", "")) : valor);
                }
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new IllegalStateException("Não foi possível pesquisar as consultas. Verifique a conexão.", e);
        }
    }

    private static String contem(String termo) {
        return "%" + termo.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
    }

    private consultas mapear(ResultSet rs) throws SQLException {
        pacientes p = new pacientes();
        p.setId_paciente(rs.getInt("id_paciente"));
        p.setNome(rs.getString("paciente_nome"));
        p.setCpf(rs.getString("paciente_cpf"));
        medicos m = new medicos();
        m.setId_medico(rs.getInt("id_medico"));
        m.setNome(rs.getString("medico_nome"));
        m.setCpf(rs.getString("medico_cpf"));
        m.setCrm(rs.getString("crm"));
        return new consultas(rs.getInt("id_consulta"), p, m, rs.getDate("data_consulta"),
                rs.getTime("hora_consulta").toLocalTime(), rs.getString("sala"),
                enum_consultas.valueOf(rs.getString("status")));
    }

    // Buscas dos seletores do agendamento, sem alterar os DAOs dos outros módulos.
    public List<pacientes> pesquisarPacientes(String termo) {
        List<pacientes> lista = new ArrayList<>();
        String sql = "SELECT id_paciente, nome, cpf FROM pacientes WHERE nome LIKE ? ESCAPE '!' "
                + "OR cpf LIKE ? ESCAPE '!' OR REPLACE(REPLACE(REPLACE(cpf,'.',''),'-',''),' ','') LIKE ? ESCAPE '!' ORDER BY nome, id_paciente LIMIT 20";
        try (Connection conn = conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, contem(termo));
            stmt.setString(2, contem(termo));
            stmt.setString(3, contem(termo.replaceAll("[.\\-\\s]", "")));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pacientes p = new pacientes();
                    p.setId_paciente(rs.getInt("id_paciente"));
                    p.setNome(rs.getString("nome"));
                    p.setCpf(rs.getString("cpf"));
                    lista.add(p);
                }
            }
            return lista;
        } catch (SQLException e) { throw new IllegalStateException("Erro ao pesquisar pacientes.", e); }
    }

    public List<medicos> pesquisarMedicos(String termo) {
        List<medicos> lista = new ArrayList<>();
        String sql = "SELECT m.id_medico, COALESCE(f.nome, CONCAT('Médico CRM ', m.crm)) AS nome, f.cpf, m.crm FROM medicos m "
                + "LEFT JOIN funcionarios f ON f.id_funcionario=m.id_funcionario "
                + "WHERE f.nome LIKE ? ESCAPE '!' OR m.crm LIKE ? ESCAPE '!' OR f.cpf LIKE ? ESCAPE '!' "
                + "OR REPLACE(REPLACE(REPLACE(f.cpf,'.',''),'-',''),' ','') LIKE ? ESCAPE '!' ORDER BY f.nome, m.id_medico LIMIT 20";
        try (Connection conn = conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i=1; i<=3; i++) stmt.setString(i, contem(termo));
            stmt.setString(4, contem(termo.replaceAll("[.\\-\\s]", "")));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    medicos m = new medicos();
                    m.setId_medico(rs.getInt("id_medico"));
                    m.setNome(rs.getString("nome"));
                    m.setCpf(rs.getString("cpf"));
                    m.setCrm(rs.getString("crm"));
                    lista.add(m);
                }
            }
            return lista;
        } catch (SQLException e) { throw new IllegalStateException("Erro ao pesquisar médicos.", e); }
    }
}
