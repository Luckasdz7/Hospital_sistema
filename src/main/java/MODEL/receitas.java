/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MODEL;

import java.util.Date;

/**
 *
 * @author Lucas
 */
public class receitas {
    private int id_receita;
    private consultas consulta;
    private String medicamento;
    private String dosagem;
    private String observacoes;
    private Date validade;
    

    public  receitas(){
        
    }
    public receitas(int id_receita, consultas consulta, String medicamento, String dosagem, String observacoes, Date validade) {
        this.id_receita = id_receita;
        this.consulta = consulta;
        this.medicamento = medicamento;
        this.dosagem = dosagem;
        this.observacoes = observacoes;
        this.validade = validade;
    }

    public int getId_receita() {
        return id_receita;
    }

    public void setId_receita(int id_receita) {
        this.id_receita = id_receita;
    }

    public consultas getConsulta() {
        return consulta;
    }

    public void setConsulta(consultas consulta) {
        this.consulta = consulta;
    }

    public String getMedicamento() {
        return medicamento;
    }

    public void setMedicamento(String medicamento) {
        this.medicamento = medicamento;
    }

    public String getDosagem() {
        return dosagem;
    }

    public void setDosagem(String dosagem) {
        this.dosagem = dosagem;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public Date getValidade() {
        return validade;
    }

    public void setValidade(Date validade) {
        this.validade = validade;
    }
    
    
    
}
