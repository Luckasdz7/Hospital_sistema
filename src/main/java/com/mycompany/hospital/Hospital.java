/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.hospital;

/**
 * Ponto de entrada do sistema (exec.mainClass do pom.xml): abre a tela de Login.
 * O antigo teste de cadastro de funcionário pelo console foi substituído,
 * já que o cadastro agora é feito pelo Form_admin.
 *
 * @author Lucas
 */
public class Hospital {

    public static void main(String[] args) {
        FORMS.Login.main(args);
    }
}
