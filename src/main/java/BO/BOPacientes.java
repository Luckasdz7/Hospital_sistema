/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package BO;

import DAO.DAOpacientes;
import MODEL.Sexo;
import MODEL.pacientes;
import java.util.List;

/**
 *
 * @author Lucas
 */
public class BOPacientes {
    private DAOpacientes dao;
    private static final List<String> TIPOS_SANGUINEOS = List.of("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");

    public BOPacientes() {
        this.dao = new DAOpacientes();
    }

    public void Salvar(pacientes p) {
        validar(p);
        dao.Salvar(p);
    }

    public void Atualizar(pacientes p) {
        validar(p);
        if (p.getId_paciente() <= 0) throw new IllegalArgumentException("Selecione um paciente.");
        dao.Atualizar(p);
    }

    public void excluir(int id) {
        dao.excluir(id);
    }

    public pacientes buscarporid(int id) {
        return dao.buscarporid(id);
    }
    public List<pacientes> mostratodos(){
        return dao.listartodos();
    }

    public List<pacientes> pesquisar(String campo, String termo) {
        String valor = termo == null ? "" : termo.trim();
        if (!valor.isEmpty()) {
            switch (campo) {
                case "id": lerId(valor); break;
                case "nascimento": valor = BOConsultas.lerData(valor).toString(); break;
                case "sexo": valor = lerSexo(valor).name(); break;
                case "sangue": valor = lerTipoSanguineo(valor); break;
                default: break;
            }
        }
        return dao.pesquisar(campo, valor);
    }

    public static int lerId(String texto) {
        try {
            int id = Integer.parseInt(texto.trim());
            if (id <= 0) throw new NumberFormatException();
            return id;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("O ID deve ser um número inteiro positivo.");
        }
    }

    public static Sexo lerSexo(String texto) {
        switch (texto.trim().toUpperCase()) {
            case "M": case "MASCULINO": return Sexo.M;
            case "F": case "FEMININO": return Sexo.F;
            case "OUTRO": return Sexo.OUTRO;
            default: throw new IllegalArgumentException("Sexo válido: M, F ou Outro.");
        }
    }

    public static String lerTipoSanguineo(String texto) {
        String tipo = texto.replaceAll("\\s", "").toUpperCase();
        if (!TIPOS_SANGUINEOS.contains(tipo))
            throw new IllegalArgumentException("Tipo sanguíneo válido: A+, A-, B+, B-, AB+, AB-, O+ ou O-.");
        return tipo;
    }

    // Grava no mesmo formato já usado na tabela: 000.000.000-00
    public static String lerCpf(String texto) {
        String numeros = texto.replaceAll("\\D", "");
        if (numeros.length() != 11) throw new IllegalArgumentException("Informe um CPF com 11 números.");
        return numeros.substring(0, 3) + "." + numeros.substring(3, 6) + "." + numeros.substring(6, 9) + "-" + numeros.substring(9);
    }

    private void validar(pacientes p) {
        if (p.getNome() == null || p.getNome().isBlank())
            throw new IllegalArgumentException("Informe o nome do paciente.");
        if (p.getNome().length() > 100) throw new IllegalArgumentException("O nome pode ter no máximo 100 caracteres.");
        if (p.getCpf() == null || p.getCpf().length() != 14) throw new IllegalArgumentException("Informe um CPF válido.");
        if (p.getData_nascimento() == null) throw new IllegalArgumentException("Informe a data de nascimento.");
        if (p.getData_nascimento().after(new java.util.Date()))
            throw new IllegalArgumentException("A data de nascimento não pode ser no futuro.");
        if (p.getSexo() == null) throw new IllegalArgumentException("Informe o sexo: M, F ou Outro.");
        if (p.getTelefone() != null && p.getTelefone().length() > 20)
            throw new IllegalArgumentException("O telefone pode ter no máximo 20 caracteres.");
        if (p.getEmail() != null && (p.getEmail().length() > 100 || !p.getEmail().matches("[^@\\s]+@[^@\\s]+")))
            throw new IllegalArgumentException("Informe um e-mail válido (ou deixe em branco).");
        if (p.getEndereco() != null && p.getEndereco().length() > 255)
            throw new IllegalArgumentException("O endereço pode ter no máximo 255 caracteres.");
        if (p.getTipo_sanguineo() != null) p.setTipo_sanguineo(lerTipoSanguineo(p.getTipo_sanguineo()));
    }
}
