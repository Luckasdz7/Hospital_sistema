package UTIL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * PADRÃO DE CRIAÇÃO: Singleton (+ Factory Method).
 *
 * Existe uma única instância desta fábrica no sistema inteiro
 * ({@link #getInstance()}). Ela guarda a configuração do banco em um só lugar
 * e fabrica as conexões ({@link #getConnection()}).
 *
 * Importante: o Singleton é a FÁBRICA, não a Connection. Cada operação do DAO
 * pede uma conexão nova e fecha no fim (try-with-resources), porque as telas
 * acessam o banco em paralelo (SwingWorker) e uma Connection compartilhada
 * daria erro.
 *
 * @author Lucas
 */
public final class ConexaoFactory {

    private static volatile ConexaoFactory instancia;

    private final String url;
    private final String usuario;
    private final String senha;

    // Construtor privado: ninguém fora da classe consegue dar "new".
    private ConexaoFactory() {
        this.url = "jdbc:mysql://localhost:3306/hospital";
        this.usuario = "root";
        this.senha = "123456";
    }

    public static ConexaoFactory getInstance() {
        if (instancia == null) {
            synchronized (ConexaoFactory.class) {
                if (instancia == null) instancia = new ConexaoFactory();
            }
        }
        return instancia;
    }

    public Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(url, usuario, senha);
        } catch (SQLException e) {
            throw new SQLException("Não foi possível conectar ao banco. Verifique se o MySQL está ligado.", e);
        }
    }
}
