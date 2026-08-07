/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.hospital;

import BO.BOConsultas;
import BO.BOFuncionarios;
import BO.BOMedicos;
import BO.BOPacientes;
import BO.BOReceitas;
import DAO.texte;
import MODEL.funcionarios;
import UTIL.Conexao;
import java.sql.Connection;
import java.util.Scanner;

/**
 *
 * @author Lucas
 */
public class Hospital {

    public static void main(String[] args) {
        BOConsultas boconsultas = new BOConsultas();
        BOPacientes boPacientes = new BOPacientes();
        BOFuncionarios boFuncionarios = new BOFuncionarios();
        BOMedicos boMedicos = new BOMedicos();
        BOReceitas boReceitas = new BOReceitas();
        
        
        
        System.out.println("Funcionarios");
        Scanner sc = new Scanner(System.in);
        System.out.println("nome:");
        //String nome, String cargo, double salario, String login, String senha, String cpf
        String nome = sc.nextLine();
        System.out.println("cargo:");
        String cargo = sc.nextLine();
        System.out.println("salario:");
        Double salario = sc.nextDouble();
        sc.nextLine();
        System.out.println("login:");
        String login = sc.nextLine();
        System.out.println("senha:");
        String senha = sc.nextLine();
        System.out.println("cpf:");
        String cpf = sc.nextLine();
        
        funcionarios f = new funcionarios(nome, cargo,salario,login,senha,cpf );
        boFuncionarios.Salvar(f);
        
        //funcionarios( String nome, String cargo, double salario, String login, String senha, String cpf) {
        
        
        
        
        
        
                
        
      
    }
}
