package CONTROLLER;

import BO.BOReceitas;
import MODEL.consultas;
import MODEL.receitas;

/**
 * CONTROLLER (MVC) das receitas (painel PnReceitas do Form_medico).
 *
 * @author Lucas
 */
public class ReceitaController {

    private final BOReceitas bo = new BOReceitas();

    public receitas buscarPorConsulta(consultas c) {
        return bo.buscarporconsulta(c.getId_consulta());
    }

    /** Converte os campos da tela em uma receita (idReceita = 0 para nova). */
    public receitas montar(int idReceita, consultas consulta, String medicamento, String dosagem,
            String validade, String observacoes) {
        if (consulta == null) throw new IllegalArgumentException("Selecione uma consulta na tabela.");
        receitas r = new receitas();
        r.setId_receita(idReceita);
        r.setConsulta(consulta);
        r.setMedicamento(medicamento == null ? "" : medicamento.trim());
        r.setDosagem(dosagem == null ? "" : dosagem.trim());
        String obs = observacoes == null ? "" : observacoes.trim();
        r.setObservacoes(obs.isEmpty() ? null : obs);
        r.setValidade(BOReceitas.lerValidade(validade));
        if (r.getMedicamento().isEmpty()) throw new IllegalArgumentException("Informe o medicamento.");
        if (r.getDosagem().isEmpty()) throw new IllegalArgumentException("Informe a dosagem.");
        return r;
    }

    /** "Gerar nova" (id = 0) ou "Salvar" (id > 0). */
    public void salvar(receitas r) {
        if (r.getId_receita() > 0) bo.Atualizar(r); else bo.Salvar(r);
    }

    public void excluir(int idReceita) {
        bo.excluir(idReceita);
    }
}
