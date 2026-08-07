/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MODEL;

/**
 *
 * @author Lucas
 */
public class funcionarios {
    private int id_funcionario;
    private String nome;
    private String cargo; 
    private double salario; 
    private String login;
    private String senha;
    private String cpf;
    public funcionarios(){
        
    }
    

    public funcionarios(int id_funcionario, String nome, String cargo, double salario, String login, String senha, String cpf) {
        this.id_funcionario = id_funcionario;
        this.nome = nome;
        this.cargo = cargo;
        this.salario = salario;
        this.login = login;
        this.senha = senha;
        this.cpf = cpf;
    }
        public funcionarios( String nome, String cargo, double salario, String login, String senha, String cpf) {
        this.nome = nome;
        this.cargo = cargo;
        this.salario = salario;
        this.login = login;
        this.senha = senha;
        this.cpf = cpf;
    }
       @Override
    public String toString() {
        return "id" + id_funcionario + ", Nome: " + nome + ", cargo: " + cargo + ", salario" + salario + ", login" + login + ", senha" + senha + ", cpf" + cpf;
    }

    public int getId_funcionario() {
        return id_funcionario;
    }

    public void setId_funcionario(int id_funcionario) {
        this.id_funcionario = id_funcionario;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public double getSalario() {
        return salario;
    }

    public void setSalario(double salario) {
        this.salario = salario;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }
    
    
    
}
