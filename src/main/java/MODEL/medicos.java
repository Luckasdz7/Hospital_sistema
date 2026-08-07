/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MODEL;

/**
 *
 * @author Lucas
 */
public class medicos extends funcionarios{
     private int id_medico;
   
    private String crm;
    private String especialidade;
    private String telefone;
    private String email;
   
    private funcionarios funcionario;
    
    public medicos(){
        
    }

    public medicos(int id_medico, String crm, String especialidade, String telefone, String email, funcionarios funcionario, int id_funcionario, String nome, String cargo, double salario, String login, String senha, String cpf) {
        super(id_funcionario, nome, cargo, salario, login, senha, cpf);
        this.id_medico = id_medico;
        this.crm = crm;
        this.especialidade = especialidade;
        this.telefone = telefone;
        this.email = email;
        this.funcionario = funcionario;
    }
    

    

    public funcionarios getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(funcionarios funcionario) {
        this.funcionario = funcionario;
    }
    

    public int getId_medico() {
        return id_medico;
    }

    public void setId_medico(int id_medico) {
        this.id_medico = id_medico;
    }

    

    public String getCrm() {
        return crm;
    }

    public void setCrm(String crm) {
        this.crm = crm;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

   
    
    
    
}
