/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package BO;

import DAO.DAOconsultas;
import MODEL.consultas;
import java.util.List;

/**
 *
 * @author Lucas
 */
public class BOConsultas {
    private DAOconsultas dao;

    public BOConsultas() {
        this.dao = new DAOconsultas();
    }

    public void Salvar(consultas c) {
        validar(c);
        dao.Salvar(c);
    }

    public void Atualizar(consultas c) {
        validar(c);
        if (c.getId_consulta() <= 0) throw new IllegalArgumentException("Selecione uma consulta.");
        dao.Atualizar(c);
    }

    public void assumirConsulta(consultas c, MODEL.medicos novoMedico) {
        if (c == null || c.getId_consulta() <= 0) throw new IllegalArgumentException("Selecione uma consulta.");
        if (novoMedico == null || novoMedico.getId_medico() <= 0)
            throw new IllegalArgumentException("Médico logado não identificado.");
        dao.trocarMedico(c.getId_consulta(), novoMedico.getId_medico());
    }

    public void excluir(int id) {
        dao.excluir(id);
    }

    public consultas buscarporid(int id) {
        return dao.buscarporid(id);
    }
    public List<consultas> mostratodos(){
        return dao.mostartodas();
    }

    public List<consultas> pesquisar(String campo, String termo) {
        String valor = termo == null ? "" : termo.trim();
        if (!valor.isEmpty()) {
            switch (campo) {
                case "data": valor = lerData(valor).toString(); break;
                case "horario": valor = lerHora(valor).toString(); break;
                case "status": valor = lerStatus(valor).name(); break;
                case "id":
                    try {
                        if (Integer.parseInt(valor) <= 0) throw new NumberFormatException();
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("O ID deve ser um número inteiro positivo.");
                    }
                    break;
                default: break;
            }
        }
        return dao.pesquisar(campo, valor);
    }

    public List<MODEL.pacientes> pesquisarPacientes(String termo) {
        return termo.trim().length() < 2 ? java.util.Collections.emptyList() : dao.pesquisarPacientes(termo.trim());
    }

    public List<MODEL.medicos> pesquisarMedicos(String termo) {
        return termo.trim().length() < 2 ? java.util.Collections.emptyList() : dao.pesquisarMedicos(termo.trim());
    }

    public static java.time.LocalDate lerData(String texto) {
        try {
            String t = texto.trim();
            return java.time.LocalDate.parse(t, t.contains("/")
                    ? java.time.format.DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(java.time.format.ResolverStyle.STRICT)
                    : java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (java.time.DateTimeException e) {
            throw new IllegalArgumentException("Informe uma data válida: dd/MM/aaaa (ex.: 25/11/2026).", e);
        }
    }

    public static java.time.LocalTime lerHora(String texto) {
        try { return java.time.LocalTime.parse(texto.trim()); }
        catch (java.time.DateTimeException e) {
            throw new IllegalArgumentException("Informe um horário válido: HH:mm ou HH:mm:ss.", e);
        }
    }

    public static MODEL.enum_consultas lerStatus(String texto) {
        for (MODEL.enum_consultas status : MODEL.enum_consultas.values()) {
            if (status.name().equalsIgnoreCase(texto.trim())) return status;
        }
        throw new IllegalArgumentException("Status válido: Agendada, Realizada ou Cancelada.");
    }

    private void validar(consultas c) {
        if (c.getPaciente() == null || c.getPaciente().getId_paciente() <= 0)
            throw new IllegalArgumentException("Selecione um paciente nas sugestões.");
        if (c.getMedico() == null || c.getMedico().getId_medico() <= 0)
            throw new IllegalArgumentException("Selecione um médico nas sugestões.");
        if (c.getData_consulta() == null || c.getHora_consulta() == null || c.getStatus() == null)
            throw new IllegalArgumentException("Preencha data, horário e status.");
        if (c.getSala() == null || c.getSala().isBlank())
            throw new IllegalArgumentException("Informe a sala.");
    }
}
