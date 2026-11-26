/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package FORMS;

import BO.BOConsultas;
import BO.BOMedicos;
import BO.BOReceitas;
import MODEL.consultas;
import MODEL.funcionarios;
import MODEL.medicos;
import MODEL.receitas;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Lucas
 */
public class Form_medico extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Form_medico.class.getName());

    /**
     * Creates new form Form_medico
     */
    public Form_medico() {
        this(null);
    }

    // Chamado pelo Login com o funcionário que entrou (cargo medico).
    public Form_medico(funcionarios usuario) {
        initComponents();
        configurarConsultas();
        configurarReceitas();
        identificarMedico(usuario);
    }

    // =====================================================================================
    // Comportamento da tela do médico, fora do código gerado pelo NetBeans.
    // Pesquisa de consultas (igual ao Form_admin) + painel de receita da consulta clicada.
    // =====================================================================================
    private final BOConsultas boConsultas = new BOConsultas();
    private final BOReceitas boReceitas = new BOReceitas();
    private final BOMedicos boMedicos = new BOMedicos();
    private medicos medicoLogado;            // médico que entrou no sistema (null = aberto sem login)
    private final ButtonGroup grupoPesquisaCons = new ButtonGroup();
    private List<consultas> consultasEncontradas = new ArrayList<>();
    private consultas consultaSelecionada;   // consulta clicada na tabela
    private receitas receitaAtual;           // receita dessa consulta (null = ainda não tem)
    private boolean ocupado;                 // true enquanto o banco está sendo acessado
    private long versaoSelecao;              // descarta respostas de cliques antigos
    private static final java.time.format.DateTimeFormatter DATA =
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/uuuu");

    // ------------------------------------ Consultas ------------------------------------
    private void configurarConsultas() {
        JRadioButton[] radios = {Rbidons, RbPascienteCons, RbMedicoCons, RbDataCons,
                RbHorarioCons, RbSalaCons, RbStatusCons};
        String[] campos = {"id", "paciente", "medico", "data", "horario", "sala", "status"};
        for (int i = 0; i < radios.length; i++) {
            radios[i].setActionCommand(campos[i]);
            grupoPesquisaCons.add(radios[i]);
        }
        RbPascienteCons.setSelected(true);
        TxtPesquisaCons.setToolTipText("Paciente: nome/CPF; médico: nome/CRM/CPF; data: dd/MM/aaaa; hora: HH:mm. Vazio lista todas.");
        tblConsulta.setModel(new DefaultTableModel(
                new String[]{"ID", "Paciente", "Médico", "Data", "Horário", "Sala", "Status"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        });
        tblConsulta.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblConsulta.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !ocupado) selecionarConsulta();
        });
        btnPesquiCons.addActionListener(e -> pesquisarConsultas());
        TxtPesquisaCons.addActionListener(e -> pesquisarConsultas());
    }

    private void pesquisarConsultas() {
        if (ocupado) return;
        String campo = grupoPesquisaCons.getSelection().getActionCommand();
        String termo = TxtPesquisaCons.getText().trim();
        // Valida antes de limpar a tela ou acessar o banco.
        try {
            if (!termo.isEmpty()) {
                switch (campo) {
                    case "id": BO.BOPacientes.lerId(termo); break;
                    case "data": BOConsultas.lerData(termo); break;
                    case "horario": BOConsultas.lerHora(termo); break;
                    case "status": BOConsultas.lerStatus(termo); break;
                    default: break;
                }
            }
        } catch (IllegalArgumentException e) { erro(e); return; }
        tblConsulta.clearSelection();
        mostrarSemConsulta();
        executar(() -> boConsultas.pesquisar(campo, termo), lista -> {
            consultasEncontradas = lista;
            DefaultTableModel model = (DefaultTableModel) tblConsulta.getModel();
            model.setRowCount(0);
            for (consultas c : lista) {
                model.addRow(new Object[]{c.getId_consulta(), c.getPaciente().getNome(), c.getMedico().getNome(),
                    formatar(c.getData_consulta()), c.getHora_consulta().toString(), c.getSala(), c.getStatus().name()});
            }
            tblConsulta.setToolTipText(lista.size() + " consulta(s). Clique em uma para ver a receita.");
            if (lista.isEmpty()) JOptionPane.showMessageDialog(this, "Nenhuma consulta encontrada.");
        });
    }

    // Clique na consulta: busca a receita dela e preenche o painel.
    private void selecionarConsulta() {
        int linha = tblConsulta.getSelectedRow();
        if (linha < 0) {
            mostrarSemConsulta();
            return;
        }
        consultas c = consultasEncontradas.get(tblConsulta.convertRowIndexToModel(linha));
        carregarReceita(c);
    }

    private void carregarReceita(consultas c) {
        consultaSelecionada = c;
        receitaAtual = null;
        long pedido = ++versaoSelecao;
        lblConsultaRec.setText("Consulta #" + c.getId_consulta() + " - " + c.getPaciente().getNome()
                + " (" + formatar(c.getData_consulta()) + ")");
        limparCamposReceita();
        executar(() -> boReceitas.buscarporconsulta(c.getId_consulta()), r -> {
            if (pedido != versaoSelecao) return;
            receitaAtual = r;
            if (r != null) {
                txtMedicamentoRec.setText(r.getMedicamento());
                txtDosagemRec.setText(r.getDosagem());
                txtValidadeRec.setText(formatar(r.getValidade()));
                txtObservacoesRec.setText(r.getObservacoes() == null ? "" : r.getObservacoes());
                txtObservacoesRec.setCaretPosition(0);
            }
            atualizarBotoesReceita();
        });
    }

    // ------------------------------------- Receitas -------------------------------------
    private void configurarReceitas() {
        txtValidadeRec.setToolTipText("dd/MM/aaaa (opcional)");
        txtMedicamentoRec.setToolTipText("Obrigatório (até 150 caracteres)");
        txtDosagemRec.setToolTipText("Obrigatório (até 100 caracteres)");
        btnGerarRec.setToolTipText("Cria a receita desta consulta (só quando ela ainda não tem receita)");
        btnSalvarRec.setToolTipText("Salva as alterações da receita existente");
        btnExcluirRec.setToolTipText("Exclui a receita desta consulta");
        btnGerarRec.addActionListener(e -> gerarReceita());
        btnSalvarRec.addActionListener(e -> salvarReceita());
        btnExcluirRec.addActionListener(e -> excluirReceita());
        mostrarSemConsulta();
    }

    private void mostrarSemConsulta() {
        versaoSelecao++;
        consultaSelecionada = null;
        receitaAtual = null;
        lblConsultaRec.setText("Selecione uma consulta na tabela");
        limparCamposReceita();
        atualizarBotoesReceita();
    }

    private void limparCamposReceita() {
        txtMedicamentoRec.setText("");
        txtDosagemRec.setText("");
        txtValidadeRec.setText("");
        txtObservacoesRec.setText("");
    }

    // Sem consulta: tudo desabilitado. Consulta sem receita: só "Gerar nova".
    // Consulta com receita: "Salvar" e "Excluir".
    private void atualizarBotoesReceita() {
        boolean temConsulta = consultaSelecionada != null && !ocupado;
        boolean temReceita = receitaAtual != null;
        for (JComponent campo : new JComponent[]{txtMedicamentoRec, txtDosagemRec, txtValidadeRec, txtObservacoesRec})
            campo.setEnabled(temConsulta);
        btnGerarRec.setEnabled(temConsulta && !temReceita);
        btnSalvarRec.setEnabled(temConsulta && temReceita);
        btnExcluirRec.setEnabled(temConsulta && temReceita);
    }

    private receitas lerCamposReceita() {
        receitas r = new receitas();
        r.setConsulta(consultaSelecionada);
        r.setMedicamento(txtMedicamentoRec.getText().trim());
        r.setDosagem(txtDosagemRec.getText().trim());
        String obs = txtObservacoesRec.getText().trim();
        r.setObservacoes(obs.isEmpty() ? null : obs);
        r.setValidade(BOReceitas.lerValidade(txtValidadeRec.getText()));
        if (r.getMedicamento().isEmpty()) throw new IllegalArgumentException("Informe o medicamento.");
        if (r.getDosagem().isEmpty()) throw new IllegalArgumentException("Informe a dosagem.");
        return r;
    }

    private void gerarReceita() {
        if (ocupado || consultaSelecionada == null || receitaAtual != null) return;
        final receitas r;
        try { r = lerCamposReceita(); } catch (IllegalArgumentException e) { erro(e); return; }
        final consultas c = consultaSelecionada;
        final Boolean assumir = confirmarAssumir(c);
        if (assumir == null) return;
        executar(() -> {
            if (assumir) boConsultas.assumirConsulta(c, medicoLogado);
            boReceitas.Salvar(r);
            return true;
        }, ok -> {
            if (assumir) aplicarNovoMedico(c);
            JOptionPane.showMessageDialog(this, "Receita gerada para a consulta #" + c.getId_consulta() + "."
                    + (assumir ? "\nA consulta agora está no seu nome." : ""));
            carregarReceita(c);
        });
    }

    private void salvarReceita() {
        if (ocupado || consultaSelecionada == null || receitaAtual == null) return;
        final receitas r;
        try { r = lerCamposReceita(); } catch (IllegalArgumentException e) { erro(e); return; }
        r.setId_receita(receitaAtual.getId_receita());
        final consultas c = consultaSelecionada;
        final Boolean assumir = confirmarAssumir(c);
        if (assumir == null) return;
        executar(() -> {
            if (assumir) boConsultas.assumirConsulta(c, medicoLogado);
            boReceitas.Atualizar(r);
            return true;
        }, ok -> {
            if (assumir) aplicarNovoMedico(c);
            JOptionPane.showMessageDialog(this, "Receita atualizada."
                    + (assumir ? "\nA consulta agora está no seu nome." : ""));
            carregarReceita(c);
        });
    }

    // ------------------------------- Assumir a consulta -------------------------------
    // Descobre qual médico está logado (pelo funcionário do Login).
    private void identificarMedico(funcionarios usuario) {
        if (usuario == null) {
            setTitle("Médico");
            return; // aberto direto pelo NetBeans, sem login: não troca médico de consulta
        }
        setTitle("Médico - " + usuario.getNome());
        executar(() -> boMedicos.buscarporfuncionario(usuario.getId_funcionario()), m -> {
            medicoLogado = m;
            if (m == null) {
                JOptionPane.showMessageDialog(this, "O usuário " + usuario.getLogin()
                        + " não está vinculado a nenhum cadastro de médico.\n"
                        + "Peça ao administrador para vincular na aba Médicos.", "Médico", JOptionPane.WARNING_MESSAGE);
            } else {
                setTitle("Médico - " + m.getNome() + " (CRM " + m.getCrm() + ")");
            }
        });
    }

    // null = cancelar; false = a consulta já é do médico logado; true = vai assumir a consulta.
    private Boolean confirmarAssumir(consultas c) {
        if (medicoLogado == null || c.getMedico() == null
                || c.getMedico().getId_medico() == medicoLogado.getId_medico()) return false;
        Object[] opcoes = {"Assumir consulta", "Cancelar"};
        int resposta = JOptionPane.showOptionDialog(this,
                "A consulta #" + c.getId_consulta() + " está no nome do(a) médico(a) " + c.getMedico().getNome() + ".\n"
                + "Deseja mesmo assumir essa consulta?\n"
                + "Ela passará para o seu nome (" + medicoLogado.getNome() + ").",
                "Assumir consulta", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, opcoes, opcoes[1]);
        return resposta == 0 ? Boolean.TRUE : null;
    }

    // Atualiza a consulta na memória e a coluna Médico da tabela, sem pesquisar de novo.
    private void aplicarNovoMedico(consultas c) {
        c.setMedico(medicoLogado);
        int indice = consultasEncontradas.indexOf(c);
        if (indice >= 0) tblConsulta.getModel().setValueAt(medicoLogado.getNome(), indice, 2);
    }

    private void excluirReceita() {
        if (ocupado || consultaSelecionada == null || receitaAtual == null) return;
        final int id = receitaAtual.getId_receita();
        final consultas c = consultaSelecionada;
        Object[] opcoes = {"Excluir", "Cancelar"};
        int resposta = JOptionPane.showOptionDialog(this,
                "Deseja realmente excluir a receita da consulta #" + c.getId_consulta() + "?", "Confirmar exclusão",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, opcoes, opcoes[1]);
        if (resposta != 0) return;
        executar(() -> { boReceitas.excluir(id); return true; }, ok -> {
            JOptionPane.showMessageDialog(this, "Receita excluída.");
            carregarReceita(c);
        });
    }

    // --------------------------------------- Apoio ---------------------------------------
    private void ocupar(boolean valor) {
        ocupado = valor;
        java.util.Enumeration<AbstractButton> radios = grupoPesquisaCons.getElements();
        while (radios.hasMoreElements()) radios.nextElement().setEnabled(!valor);
        TxtPesquisaCons.setEnabled(!valor);
        btnPesquiCons.setEnabled(!valor);
        tblConsulta.setEnabled(!valor);
        atualizarBotoesReceita();
    }

    // Acessa o banco fora da thread da tela e volta para ela no fim.
    private <T> void executar(Callable<T> tarefa, Consumer<T> sucesso) {
        ocupar(true);
        new SwingWorker<T, Void>() {
            @Override protected T doInBackground() throws Exception { return tarefa.call(); }
            @Override protected void done() {
                ocupar(false);
                try { sucesso.accept(get()); }
                catch (Exception e) { erro(e); }
            }
        }.execute();
    }

    private void erro(Throwable e) {
        if (e instanceof java.util.concurrent.ExecutionException && e.getCause() != null) e = e.getCause();
        logger.log(java.util.logging.Level.WARNING, "Form médico", e);
        JOptionPane.showMessageDialog(this, e.getMessage() == null ? "Erro na operação." : e.getMessage(),
                "Médico", JOptionPane.ERROR_MESSAGE);
    }

    private static String formatar(java.util.Date data) {
        return data == null ? "" : DATA.format(new java.sql.Date(data.getTime()).toLocalDate());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        TxtPesquisaCons = new javax.swing.JTextField();
        btnPesquiCons = new javax.swing.JButton();
        jScrollPane8 = new javax.swing.JScrollPane();
        tblConsulta = new javax.swing.JTable();
        RbPascienteCons = new javax.swing.JRadioButton();
        RbSalaCons = new javax.swing.JRadioButton();
        RbMedicoCons = new javax.swing.JRadioButton();
        RbDataCons = new javax.swing.JRadioButton();
        RbHorarioCons = new javax.swing.JRadioButton();
        RbStatusCons = new javax.swing.JRadioButton();
        Rbidons = new javax.swing.JRadioButton();
        PnReceitas = new javax.swing.JPanel();
        lblConsultaRec = new javax.swing.JLabel();
        lblMedicamentoRec = new javax.swing.JLabel();
        txtMedicamentoRec = new javax.swing.JTextField();
        lblDosagemRec = new javax.swing.JLabel();
        txtDosagemRec = new javax.swing.JTextField();
        lblValidadeRec = new javax.swing.JLabel();
        txtValidadeRec = new javax.swing.JTextField();
        lblObservacoesRec = new javax.swing.JLabel();
        jScrollPaneObsRec = new javax.swing.JScrollPane();
        txtObservacoesRec = new javax.swing.JTextArea();
        btnGerarRec = new javax.swing.JButton();
        btnSalvarRec = new javax.swing.JButton();
        btnExcluirRec = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        btnPesquiCons.setText("Pesquisar");

        tblConsulta.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Pasciente", "Medico", "Data", "Horario", "Sala", "Status"
            }
        ));
        jScrollPane8.setViewportView(tblConsulta);

        RbPascienteCons.setText("Pasciente");

        RbSalaCons.setText("Sala");

        RbMedicoCons.setText("Medico");

        RbDataCons.setText("Data");

        RbHorarioCons.setText("Horario");

        RbStatusCons.setText("Status");

        Rbidons.setText("id");

        PnReceitas.setBorder(javax.swing.BorderFactory.createTitledBorder("Receita"));

        lblConsultaRec.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblConsultaRec.setText("Selecione uma consulta na tabela");

        lblMedicamentoRec.setText("Medicamento:");

        lblDosagemRec.setText("Dosagem:");

        lblValidadeRec.setText("Validade (dd/MM/aaaa):");

        lblObservacoesRec.setText("Observações:");

        txtObservacoesRec.setColumns(20);
        txtObservacoesRec.setLineWrap(true);
        txtObservacoesRec.setRows(5);
        txtObservacoesRec.setWrapStyleWord(true);
        jScrollPaneObsRec.setViewportView(txtObservacoesRec);

        btnGerarRec.setText("Gerar nova");

        btnSalvarRec.setText("Salvar");

        btnExcluirRec.setText("Excluir");

        javax.swing.GroupLayout PnReceitasLayout = new javax.swing.GroupLayout(PnReceitas);
        PnReceitas.setLayout(PnReceitasLayout);
        PnReceitasLayout.setHorizontalGroup(
            PnReceitasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PnReceitasLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PnReceitasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblConsultaRec, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblMedicamentoRec)
                    .addComponent(txtMedicamentoRec)
                    .addComponent(lblDosagemRec)
                    .addComponent(txtDosagemRec)
                    .addComponent(lblValidadeRec)
                    .addComponent(txtValidadeRec, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblObservacoesRec)
                    .addComponent(jScrollPaneObsRec)
                    .addGroup(PnReceitasLayout.createSequentialGroup()
                        .addComponent(btnGerarRec, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnSalvarRec, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnExcluirRec, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        PnReceitasLayout.setVerticalGroup(
            PnReceitasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PnReceitasLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblConsultaRec)
                .addGap(18, 18, 18)
                .addComponent(lblMedicamentoRec)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtMedicamentoRec, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblDosagemRec)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtDosagemRec, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblValidadeRec)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtValidadeRec, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblObservacoesRec)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPaneObsRec, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(PnReceitasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnGerarRec, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSalvarRec, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnExcluirRec, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGap(83, 83, 83)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane8)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(Rbidons, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(RbPascienteCons)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(RbMedicoCons, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(RbDataCons)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(RbHorarioCons)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(RbSalaCons)
                                .addGap(18, 18, 18)
                                .addComponent(RbStatusCons, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(12, 12, 12))
                            .addComponent(TxtPesquisaCons, javax.swing.GroupLayout.PREFERRED_SIZE, 530, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(btnPesquiCons, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(30, 30, 30)
                .addComponent(PnReceitas, javax.swing.GroupLayout.PREFERRED_SIZE, 520, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(82, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TxtPesquisaCons, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnPesquiCons, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(RbPascienteCons)
                    .addComponent(RbSalaCons)
                    .addComponent(RbMedicoCons)
                    .addComponent(RbDataCons)
                    .addComponent(RbHorarioCons)
                    .addComponent(RbStatusCons)
                    .addComponent(Rbidons))
                .addGap(25, 25, 25)
                .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 399, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(482, Short.MAX_VALUE))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addComponent(PnReceitas, javax.swing.GroupLayout.PREFERRED_SIZE, 503, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1423, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1017, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        jTabbedPane1.addTab("Consultas", jPanel1);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1382, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1017, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("tab2", jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 1382, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 41, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new Form_medico().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel PnReceitas;
    private javax.swing.JRadioButton RbDataCons;
    private javax.swing.JRadioButton RbHorarioCons;
    private javax.swing.JRadioButton RbMedicoCons;
    private javax.swing.JRadioButton RbPascienteCons;
    private javax.swing.JRadioButton RbSalaCons;
    private javax.swing.JRadioButton RbStatusCons;
    private javax.swing.JRadioButton Rbidons;
    private javax.swing.JTextField TxtPesquisaCons;
    private javax.swing.JButton btnExcluirRec;
    private javax.swing.JButton btnGerarRec;
    private javax.swing.JButton btnPesquiCons;
    private javax.swing.JButton btnSalvarRec;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPaneObsRec;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel lblConsultaRec;
    private javax.swing.JLabel lblDosagemRec;
    private javax.swing.JLabel lblMedicamentoRec;
    private javax.swing.JLabel lblObservacoesRec;
    private javax.swing.JLabel lblValidadeRec;
    private javax.swing.JTable tblConsulta;
    private javax.swing.JTextField txtDosagemRec;
    private javax.swing.JTextField txtMedicamentoRec;
    private javax.swing.JTextArea txtObservacoesRec;
    private javax.swing.JTextField txtValidadeRec;
    // End of variables declaration//GEN-END:variables
}
