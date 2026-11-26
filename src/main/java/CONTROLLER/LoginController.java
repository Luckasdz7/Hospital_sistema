package CONTROLLER;

import BO.BOFuncionarios;
import MODEL.funcionarios;

/**
 * CONTROLLER (MVC) da tela de Login.
 * A View (FORMS.Login) só lê os campos e chama este controller.
 *
 * @author Lucas
 */
public class LoginController {

    private final BOFuncionarios boFuncionarios = new BOFuncionarios();

    /** Devolve o funcionário logado ou lança IllegalArgumentException com a mensagem para a tela. */
    public funcionarios autenticar(String login, String senha) {
        if (login == null || login.isBlank() || senha == null || senha.isEmpty())
            throw new IllegalArgumentException("Informe o login e a senha.");
        funcionarios f = boFuncionarios.logar(login.trim(), senha);
        if (f == null) throw new IllegalArgumentException("Senha ou Login errados\nTente novamente");
        return f;
    }
}
