package CONTROLLER;

import BO.BOConsultas;
import MODEL.consultas;
import MODEL.medicos;
import MODEL.pacientes;
import STRATEGY.PesquisaConsulta;
import java.util.List;

/**
 * CONTROLLER (MVC) das consultas: usado pelo Form_admin e pelo Form_medico.
 * Recebe o que a View digitou (texto), converte para o MODEL e chama o BO.
 *
 * Os métodos "validar..." e "montar..." rodam na thread da tela (são rápidos e
 * devolvem a mensagem de erro na hora). Os que acessam o banco a View chama
 * dentro do SwingWorker.
 *
 * @author Lucas
 */
public class ConsultaController {

    private final BOConsultas bo = new BOConsultas();

    /** Strategy: valida o termo com a estratégia do radio escolhido. Vazio = todas. */
    public String validarTermo(PesquisaConsulta estrategia, String termo) {
        String t = termo == null ? "" : termo.trim();
        return t.isEmpty() ? "" : estrategia.normalizar(t);
    }

    public List<consultas> pesquisar(PesquisaConsulta estrategia, String termo) {
        return bo.pesquisar(estrategia, termo);
    }

    public consultas montar(Integer id, pacientes paciente, medicos medico,
            String data, String hora, String sala, String status) {
        if (paciente == null || medico == null)
            throw new IllegalArgumentException("Selecione o paciente e o médico nas sugestões.");
        if (sala == null || sala.isBlank()) throw new IllegalArgumentException("Informe a sala.");
        consultas c = new consultas();
        c.setPaciente(paciente);
        c.setMedico(medico);
        c.setData_consulta(java.sql.Date.valueOf(BOConsultas.lerData(data)));
        c.setHora_consulta(BOConsultas.lerHora(hora));
        c.setStatus(BOConsultas.lerStatus(status));
        c.setSala(sala.trim());
        if (id != null) c.setId_consulta(id);
        return c;
    }

    /** Cadastra (id = 0) ou atualiza (id > 0). */
    public void salvar(consultas c) {
        if (c.getId_consulta() > 0) bo.Atualizar(c); else bo.Salvar(c);
    }

    public void excluir(int id) {
        bo.excluir(id);
    }

    /** O médico logado passa a ser o médico da consulta. */
    public void assumir(consultas c, medicos novoMedico) {
        bo.assumirConsulta(c, novoMedico);
    }

    public List<pacientes> sugerirPacientes(String termo) {
        return bo.pesquisarPacientes(termo);
    }

    public List<medicos> sugerirMedicos(String termo) {
        return bo.pesquisarMedicos(termo);
    }
}
