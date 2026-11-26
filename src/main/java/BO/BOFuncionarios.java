/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package BO;

import DAO.DAOfuncionarios;
import MODEL.funcionarios;
import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author Lucas
 */
public class BOFuncionarios {
    private DAOfuncionarios dao;

    public BOFuncionarios() {
        this.dao = new DAOfuncionarios();
    }

    // Aba Funcionários (todos os campos, inclusive salário)
    public void Salvar(funcionarios f) {
        validar(f, true);
        dao.Salvar(f);
    }

    public void Atualizar(funcionarios f) {
        validar(f, false);
        dao.Atualizar(f);
    }

    // Aba Usuários (só dados de acesso: nome, CPF, login, senha e perfil)
    public void SalvarUsuario(funcionarios f) {
        validar(f, true);
        dao.SalvarUsuario(f);
    }

    public void AtualizarUsuario(funcionarios f) {
        validar(f, false);
        dao.AtualizarUsuario(f);
    }

    public void excluir(int id) {
        dao.Excluir(id);
    }

    public funcionarios buscarporid(int id) {
        return dao.buscarporid(id);
    }
    public List<funcionarios> Buscartodos() {
        return dao.listartodos();
    }
    public funcionarios logar(String login, String senha){
        try{
           return dao.buscarlogin(login, senha);
        }catch(Exception e){
            return null;
        }
    }

    public List<funcionarios> pesquisar(String campo, String termo) {
        String valor = termo == null ? "" : termo.trim();
        if (!valor.isEmpty()) {
            switch (campo) {
                case "id": BOPacientes.lerId(valor); break;
                case "salario": valor = lerSalario(valor).toPlainString(); break;
                default: break;
            }
        }
        return dao.pesquisar(campo, valor);
    }

    // O Login compara com "admin", "medico" e "secretaria".
    public static String lerCargo(String texto) {
        switch (texto.trim().toLowerCase()) {
            case "admin": case "administrador": return "admin";
            case "medico": case "médico": return "medico";
            case "secretaria": case "secretária": case "recepcionista": return "secretaria";
            default: throw new IllegalArgumentException("Cargo/perfil válido: admin, medico ou secretaria.");
        }
    }

    // A coluna cpf de funcionarios é VARCHAR(11): grava só os números.
    public static String lerCpf(String texto) {
        String numeros = texto.replaceAll("\\D", "");
        if (numeros.length() != 11) throw new IllegalArgumentException("Informe um CPF com 11 números.");
        return numeros;
    }

    // Aceita 1500, 1500.50, 1.500,50 ou R$ 1.500,50
    public static BigDecimal lerSalario(String texto) {
        String t = texto.replace("R$", "").replaceAll("\\s", "");
        if (t.contains(",")) t = t.replace(".", "").replace(",", ".");
        try {
            BigDecimal salario = new BigDecimal(t);
            if (salario.signum() < 0 || salario.compareTo(new BigDecimal("99999999.99")) > 0 || salario.scale() > 2)
                throw new NumberFormatException();
            return salario;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Informe um salário válido (ex.: 3500,00).");
        }
    }

    private void validar(funcionarios f, boolean novo) {
        if (!novo && f.getId_funcionario() <= 0) throw new IllegalArgumentException("Selecione um funcionário.");
        if (f.getNome() == null || f.getNome().isBlank()) throw new IllegalArgumentException("Informe o nome.");
        if (f.getNome().length() > 100) throw new IllegalArgumentException("O nome pode ter no máximo 100 caracteres.");
        if (f.getCpf() == null || !f.getCpf().matches("\\d{11}")) throw new IllegalArgumentException("Informe um CPF com 11 números.");
        f.setCargo(lerCargo(f.getCargo() == null ? "" : f.getCargo()));
        if (f.getLogin() == null || f.getLogin().isBlank()) throw new IllegalArgumentException("Informe o login.");
        if (f.getLogin().length() > 50 || f.getLogin().matches(".*\\s.*"))
            throw new IllegalArgumentException("O login não pode ter espaços e deve ter no máximo 50 caracteres.");
        if (novo && (f.getSenha() == null || f.getSenha().isEmpty()))
            throw new IllegalArgumentException("Informe a senha.");
        if (f.getSenha() != null && f.getSenha().length() > 255)
            throw new IllegalArgumentException("A senha pode ter no máximo 255 caracteres.");
        if (f.getSalario() < 0) throw new IllegalArgumentException("O salário não pode ser negativo.");
    }
}
