/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MODEL;
import java.util.Date;
import java.time.LocalTime;


/**
 *
 * @author Lucas
 */
public class consultas {
    private int id_consulta;
    private pacientes paciente;
    private medicos medico;
    private Date data_consulta;
    LocalTime hora_consulta;
    private String sala;
    private enum_consultas status;
    
    public consultas(){
        
    }

    public consultas(int id_consulta, pacientes id_paciente, medicos id_medico, Date data_consulta, LocalTime hora_consulta, String sala, enum_consultas status) {
        this.id_consulta = id_consulta;
        this.paciente = id_paciente;
        this.medico = id_medico;
        this.data_consulta = data_consulta;
        this.hora_consulta = hora_consulta;
        this.sala = sala;
        this.status = status;
    }

    public int getId_consulta() {
        return id_consulta;
    }

    public void setId_consulta(int id_consulta) {
        this.id_consulta = id_consulta;
    }

    public pacientes getPaciente() {
        return paciente;
    }

    public void setPaciente(pacientes paciente) {
        this.paciente = paciente;
    }

    public medicos getMedico() {
        return medico;
    }

    public void setMedico(medicos medico) {
        this.medico = medico;
    }

   

    public Date getData_consulta() {
        return data_consulta;
    }

    public void setData_consulta(Date data_consulta) {
        this.data_consulta = data_consulta;
    }

    public LocalTime getHora_consulta() {
        return hora_consulta;
    }

    public void setHora_consulta(LocalTime hora_consulta) {
        this.hora_consulta = hora_consulta;
    }

    public String getSala() {
        return sala;
    }

    public void setSala(String sala) {
        this.sala = sala;
    }

    public enum_consultas getStatus() {
        return status;
    }

    public void setStatus(enum_consultas status) {
        this.status = status;
    }
    
    
    
}
