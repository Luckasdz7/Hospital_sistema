package FORMS;

import MODEL.funcionarios;
import javax.swing.JFrame;

/**
 * PADRÃO DE CRIAÇÃO: Factory (Simple Factory).
 *
 * Decide qual tela abrir a partir do cargo de quem fez login. O Login não
 * precisa conhecer cada tela: só pede "a tela deste funcionário" para a fábrica.
 * Para criar um perfil novo, basta acrescentar um case aqui.
 *
 * @author Lucas
 */
public final class TelaFactory {

    private TelaFactory() {
    }

    public static JFrame criarTelaInicial(funcionarios usuario) {
        String cargo = usuario.getCargo() == null ? "" : usuario.getCargo().trim().toLowerCase();
        switch (cargo) {
            case "admin": case "administrador":
                return new Form_admin();
            case "medico": case "médico":
                return new Form_medico(usuario);
            case "secretaria": case "secretária": case "recepcionista":
                return new Form_Secretaria();
            default:
                throw new IllegalArgumentException("O cargo \"" + usuario.getCargo()
                        + "\" não tem tela no sistema. Use admin, medico ou secretaria.");
        }
    }
}
