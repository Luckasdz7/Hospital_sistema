package STRATEGY;

import java.util.List;

/**
 * PADRÃO COMPORTAMENTAL: Strategy.
 *
 * Cada tipo de pesquisa de consultas (por ID, paciente, médico, data, horário,
 * sala ou status) é uma estratégia diferente com a MESMA interface. Cada uma
 * sabe validar o texto digitado, qual filtro SQL usar e quais parâmetros passar.
 *
 * Quem pesquisa (tela, controller, BO e DAO) não precisa mais de switch: recebe
 * a estratégia escolhida no radio e só chama os métodos dela. Para criar um
 * novo tipo de pesquisa basta criar uma classe nova e registrar em
 * {@link PesquisasConsulta}.
 *
 * @author Lucas
 */
public interface PesquisaConsulta {

    /** Nome do filtro ("id", "paciente", "medico", "data", "horario", "sala", "status"). */
    String getCampo();

    /** Dica mostrada no campo de pesquisa. */
    String getDica();

    /** Valida e padroniza o texto digitado. Lança IllegalArgumentException se for inválido. */
    String normalizar(String termo);

    /** Trecho do WHERE (sempre com "?", nunca com o texto do usuário). */
    String filtroSql();

    /** Valores dos "?" do filtro, na ordem, a partir do termo já normalizado. */
    List<Object> parametros(String termoNormalizado);

    /** "texto" -> "%texto%" escapando % _ ! (usado com ESCAPE '!'). */
    static String contem(String termo) {
        return "%" + termo.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
    }

    static String soNumeros(String termo) {
        return termo.replaceAll("[.\\-\\s]", "");
    }
}
