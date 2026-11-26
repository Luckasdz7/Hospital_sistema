/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package FORMS;

import BO.BOFuncionarios;
import BO.BOConsultas;
import BO.BOMedicos;
import BO.BOPacientes;
import MODEL.consultas;
import MODEL.pacientes;
import MODEL.medicos;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import MODEL.funcionarios;
import java.util.List;

/**
 *
 * @author Lucas
 */
public class Form_admin extends javax.swing.JFrame {
    
    // BOFuncionarios boFuncionarios = new BOFuncionarios();
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Form_admin.class.getName());

    /**
     * Creates new form Hospital_form
     */
    public Form_admin() {
        initComponents();
        configurarConsultas();
        configurarPacientes();
        configurarMedicos();
        configurarUsuarios();
        configurarFuncionarios();
       
    }
    


    // Comportamento exclusivo da aba Consultas, fora do código gerado pelo NetBeans.
    private final BOConsultas boConsultas = new BOConsultas();
    private final ButtonGroup grupoPesquisaCons = new ButtonGroup();
    private List<consultas> consultasEncontradas = new ArrayList<>();
    private Integer consultaSelecionadaId;
    private boolean consultasOcupadas;
    private SugestoesConsulta<pacientes> sugestoesPacientes;
    private SugestoesConsulta<medicos> sugestoesMedicos;
    private static final java.time.format.DateTimeFormatter DATA_CONS =
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/uuuu");

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
        txtDatacons.setToolTipText("dd/MM/aaaa");
        txtHoraCons.setToolTipText("HH:mm ou HH:mm:ss");
        txtStatusCons.setToolTipText("Agendada, Realizada ou Cancelada");
        tblConsulta.setModel(new DefaultTableModel(
                new String[]{"ID", "Paciente", "Médico", "Data", "Horário", "Sala", "Status"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        });
        tblConsulta.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblConsulta.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !consultasOcupadas) selecionarConsulta();
        });
        sugestoesPacientes = new SugestoesConsulta<>(CbPascienteCOns,
                boConsultas::pesquisarPacientes,
                p -> p.getNome() + " | CPF: " + p.getCpf() + " | #" + p.getId_paciente());
        sugestoesMedicos = new SugestoesConsulta<>(CbMedicoCons,
                boConsultas::pesquisarMedicos,
                m -> m.getNome() + " | CRM: " + m.getCrm() + " | #" + m.getId_medico());
        btnPesquiCons.addActionListener(e -> pesquisarConsultas());
        TxtPesquisaCons.addActionListener(e -> pesquisarConsultas());
        btnSalvarCons.addActionListener(e -> salvarConsulta());
        btnExcluirCons.addActionListener(e -> excluirConsulta());
        btnNovaCons.addActionListener(e -> limparConsulta());
        limparConsulta();
    }

    private void limparConsulta() {
        consultaSelecionadaId = null;
        tblConsulta.clearSelection();
        sugestoesPacientes.selecionar(null);
        sugestoesMedicos.selecionar(null);
        txtDatacons.setText("");
        txtHoraCons.setText("");
        txtSalaCons.setText("");
        txtStatusCons.setText("Agendada");
        btnExcluirCons.setEnabled(false);
        btnSalvarCons.setToolTipText("Cadastrar uma nova consulta");
    }

    private void selecionarConsulta() {
        int linha = tblConsulta.getSelectedRow();
        if (linha < 0) {
            if (consultaSelecionadaId != null) limparConsulta();
            return;
        }
        consultas c = consultasEncontradas.get(tblConsulta.convertRowIndexToModel(linha));
        consultaSelecionadaId = c.getId_consulta();
        sugestoesPacientes.selecionar(c.getPaciente());
        sugestoesMedicos.selecionar(c.getMedico());
        txtDatacons.setText(DATA_CONS.format(new java.sql.Date(c.getData_consulta().getTime()).toLocalDate()));
        txtHoraCons.setText(c.getHora_consulta().toString());
        txtSalaCons.setText(c.getSala());
        txtStatusCons.setText(c.getStatus().name());
        btnExcluirCons.setEnabled(true);
        btnSalvarCons.setToolTipText("Atualizar a consulta #" + consultaSelecionadaId);
    }

    private void pesquisarConsultas() {
        if (consultasOcupadas) return;
        String campo = grupoPesquisaCons.getSelection().getActionCommand();
        String termo = TxtPesquisaCons.getText().trim();
        // Valida antes de apagar a seleção ou consultar o banco.
        try {
            if (!termo.isEmpty()) {
                switch (campo) {
                    case "id":
                        if (Integer.parseInt(termo) <= 0) throw new IllegalArgumentException("Informe um ID positivo.");
                        break;
                    case "data": BOConsultas.lerData(termo); break;
                    case "horario": BOConsultas.lerHora(termo); break;
                    case "status": BOConsultas.lerStatus(termo); break;
                    default: break;
                }
            }
        } catch (NumberFormatException e) {
            erroConsulta(new IllegalArgumentException("O ID deve ser um número inteiro positivo."));
            return;
        } catch (IllegalArgumentException e) { erroConsulta(e); return; }
        limparConsulta();
        executarConsulta(() -> boConsultas.pesquisar(campo, termo), lista -> {
            consultasEncontradas = lista;
            DefaultTableModel model = (DefaultTableModel) tblConsulta.getModel();
            model.setRowCount(0);
            for (consultas c : lista) {
                model.addRow(new Object[]{c.getId_consulta(), c.getPaciente().getNome(),
                    c.getMedico().getNome(), DATA_CONS.format(new java.sql.Date(c.getData_consulta().getTime()).toLocalDate()),
                    c.getHora_consulta().toString(), c.getSala(), c.getStatus().name()});
            }
            tblConsulta.setToolTipText(lista.size() + " consulta(s). Ordem: data e horário mais recentes primeiro.");
            if (lista.isEmpty()) JOptionPane.showMessageDialog(this, "Nenhuma consulta encontrada.");
        });
    }

    private void salvarConsulta() {
        if (consultasOcupadas) return;
        final consultas c = new consultas();
        final boolean editar = consultaSelecionadaId != null;
        try {
            pacientes paciente = sugestoesPacientes.selecionado;
            medicos medico = sugestoesMedicos.selecionado;
            if (paciente == null || medico == null)
                throw new IllegalArgumentException("Selecione o paciente e o médico nas sugestões.");
            if (txtSalaCons.getText().isBlank()) throw new IllegalArgumentException("Informe a sala.");
            c.setPaciente(paciente);
            c.setMedico(medico);
            c.setData_consulta(java.sql.Date.valueOf(BOConsultas.lerData(txtDatacons.getText())));
            c.setHora_consulta(BOConsultas.lerHora(txtHoraCons.getText()));
            c.setStatus(BOConsultas.lerStatus(txtStatusCons.getText()));
            c.setSala(txtSalaCons.getText().trim());
            if (editar) c.setId_consulta(consultaSelecionadaId);
        } catch (IllegalArgumentException e) { erroConsulta(e); return; }
        executarConsulta(() -> {
            if (editar) boConsultas.Atualizar(c); else boConsultas.Salvar(c);
            return true;
        }, sucesso -> {
            limparConsulta();
            JOptionPane.showMessageDialog(this, editar ? "Consulta atualizada." : "Consulta cadastrada.");
            pesquisarConsultas();
        });
    }

    private void excluirConsulta() {
        if (consultasOcupadas || consultaSelecionadaId == null) return;
        int id = consultaSelecionadaId;
        Object[] opcoes = {"Excluir", "Cancelar"};
        int resposta = JOptionPane.showOptionDialog(this,
                "Deseja realmente excluir a consulta #" + id + "?", "Confirmar exclusão",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, opcoes, opcoes[1]);
        if (resposta != 0) return;
        executarConsulta(() -> { boConsultas.excluir(id); return true; }, sucesso -> {
            limparConsulta();
            JOptionPane.showMessageDialog(this, "Consulta excluída.");
            pesquisarConsultas();
        });
    }

    private void ocuparConsultas(boolean ocupado) {
        consultasOcupadas = ocupado;
        habilitarComponentes(jPanel7, !ocupado);
        btnNovaCons.setEnabled(!ocupado);
        java.util.Enumeration<AbstractButton> radios = grupoPesquisaCons.getElements();
        while (radios.hasMoreElements()) radios.nextElement().setEnabled(!ocupado);
        btnPesquiCons.setEnabled(!ocupado);
        TxtPesquisaCons.setEnabled(!ocupado);
        tblConsulta.setEnabled(!ocupado);
        btnExcluirCons.setEnabled(!ocupado && consultaSelecionadaId != null);
    }

    private void habilitarComponentes(java.awt.Container painel, boolean habilitar) {
        for (java.awt.Component componente : painel.getComponents()) {
            componente.setEnabled(habilitar);
            if (componente instanceof java.awt.Container)
                habilitarComponentes((java.awt.Container) componente, habilitar);
        }
    }

    private <T> void executarConsulta(Callable<T> tarefa, Consumer<T> sucesso) {
        ocuparConsultas(true);
        new SwingWorker<T, Void>() {
            @Override protected T doInBackground() throws Exception { return tarefa.call(); }
            @Override protected void done() {
                ocuparConsultas(false);
                try { sucesso.accept(get()); }
                catch (Exception e) { erroConsulta(e); }
            }
        }.execute();
    }

    private void erroConsulta(Throwable erro) {
        if (erro instanceof java.util.concurrent.ExecutionException && erro.getCause() != null)
            erro = erro.getCause();
        logger.log(java.util.logging.Level.WARNING, "Operação de consultas", erro);
        JOptionPane.showMessageDialog(this, erro.getMessage() == null ? "Erro na operação de consultas." : erro.getMessage(),
                "Consultas", JOptionPane.ERROR_MESSAGE);
    }

    /** Autocomplete com debounce e descarte de respostas antigas; os IDs ficam nos objetos. */
    private class SugestoesConsulta<T> {
        private final JComboBox<String> combo;
        private final JTextField editor;
        private final java.util.function.Function<String, List<T>> pesquisar;
        private final java.util.function.Function<T, String> rotulo;
        private final Map<String, T> resultados = new LinkedHashMap<>();
        private final Timer espera;
        private boolean preenchendo;
        private long versao;
        private T selecionado;

        SugestoesConsulta(JComboBox<String> combo,
                java.util.function.Function<String, List<T>> pesquisar,
                java.util.function.Function<T, String> rotulo) {
            this.combo = combo;
            this.pesquisar = pesquisar;
            this.rotulo = rotulo;
            combo.removeAllItems();
            combo.setEditable(true);
            combo.setMaximumRowCount(8);
            combo.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
                public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                    if (combo.getItemCount() == 0) {
                        SwingUtilities.invokeLater(() -> {
                            if (combo.getItemCount() == 0) combo.hidePopup();
                        });
                    }
                }
                public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) { }
                public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) { }
            });
            editor = (JTextField) combo.getEditor().getEditorComponent();
            editor.setToolTipText("Digite pelo menos 2 caracteres e selecione uma sugestão (até 20 resultados).");
            espera = new Timer(300, e -> buscar());
            espera.setRepeats(false);
            editor.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { alterado(); }
                public void removeUpdate(DocumentEvent e) { alterado(); }
                public void changedUpdate(DocumentEvent e) { alterado(); }
            });
            combo.addActionListener(e -> {
                if (preenchendo) return;
                Object item = combo.getSelectedItem();
                T registro = resultados.get(item);
                if (registro != null && String.valueOf(item).equals(editor.getText())) {
                    selecionado = registro;
                    versao++;
                    espera.stop();
                }
            });
        }

        private void alterado() {
            if (preenchendo) return;
            selecionado = null;
            versao++;
            espera.restart();
        }

        void selecionar(T registro) {
            versao++;
            espera.stop();
            preenchendo = true;
            try {
                resultados.clear();
                combo.removeAllItems();
                selecionado = registro;
                String texto = registro == null ? "" : rotulo.apply(registro);
                if (registro != null) {
                    resultados.put(texto, registro);
                    combo.addItem(texto);
                    combo.setSelectedItem(texto);
                }
                editor.setText(texto);
                combo.hidePopup();
            } finally { preenchendo = false; }
        }

        private void buscar() {
            if (selecionado != null || consultasOcupadas) return;
            String texto = editor.getText();
            long pedido = versao;
            if (texto.trim().length() < 2) {
                aplicar(java.util.Collections.emptyList(), texto);
                return;
            }
            new SwingWorker<List<T>, Void>() {
                protected List<T> doInBackground() { return pesquisar.apply(texto.trim()); }
                protected void done() {
                    if (pedido != versao || consultasOcupadas || !isDisplayable()) return;
                    try { aplicar(get(), texto); }
                    catch (Exception e) { erroConsulta(e); }
                }
            }.execute();
        }

        private void aplicar(List<T> lista, String texto) {
            preenchendo = true;
            int cursor = editor.getCaretPosition();
            try {
                // Reabrir após a troca do modelo força o popup a recalcular seu tamanho.
                combo.hidePopup();
                resultados.clear();
                DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
                for (T registro : lista) {
                    String nome = rotulo.apply(registro);
                    resultados.put(nome, registro);
                    model.addElement(nome);
                }
                model.setSelectedItem(null);
                combo.setModel(model);
                editor.setText(texto);
                editor.setCaretPosition(Math.min(cursor, texto.length()));
                editor.setToolTipText(texto.trim().length() < 2
                        ? "Digite pelo menos 2 caracteres para pesquisar."
                        : lista.isEmpty() ? "Nenhum registro encontrado. Confira os cadastros no banco."
                        : "Selecione uma das sugestões abaixo.");
                if (!lista.isEmpty() && editor.isFocusOwner() && combo.isShowing()) combo.showPopup();
                else combo.hidePopup();
            } finally { preenchendo = false; }
        }
    }


    // =====================================================================================
    // Pacientes, Médicos, Usuários e Funcionários: mesmo fluxo da aba Consultas
    // (pesquisa por tipo, tabela, Novo / Salvar / Excluir, operações fora da thread da tela).
    // =====================================================================================
    private static final java.text.NumberFormat MOEDA = java.text.NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("pt-BR"));
    static { MOEDA.setMinimumFractionDigits(2); MOEDA.setMaximumFractionDigits(2); }

    private void agruparRadios(ButtonGroup grupo, JRadioButton[] radios, String[] campos, JRadioButton padrao) {
        for (int i = 0; i < radios.length; i++) {
            radios[i].setActionCommand(campos[i]);
            grupo.add(radios[i]);
        }
        padrao.setSelected(true);
    }

    private void prepararTabela(JTable tabela, java.util.function.BooleanSupplier ocupada, Runnable selecionar, String... colunas) {
        tabela.setModel(new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        });
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !ocupada.getAsBoolean()) selecionar.run();
        });
    }

    private void preencherTabela(JTable tabela, List<Object[]> linhas, String descricao) {
        DefaultTableModel model = (DefaultTableModel) tabela.getModel();
        model.setRowCount(0);
        for (Object[] linha : linhas) model.addRow(linha);
        tabela.setToolTipText(linhas.size() + " " + descricao + "(s) encontrado(s).");
        if (linhas.isEmpty()) JOptionPane.showMessageDialog(this, "Nenhum " + descricao + " encontrado.");
    }

    private <T> void executarAba(java.util.function.Consumer<Boolean> ocupar, String modulo,
            Callable<T> tarefa, Consumer<T> sucesso) {
        ocupar.accept(true);
        new SwingWorker<T, Void>() {
            @Override protected T doInBackground() throws Exception { return tarefa.call(); }
            @Override protected void done() {
                ocupar.accept(false);
                try { sucesso.accept(get()); }
                catch (Exception e) { erroAba(modulo, e); }
            }
        }.execute();
    }

    private void erroAba(String modulo, Throwable erro) {
        if (erro instanceof java.util.concurrent.ExecutionException && erro.getCause() != null)
            erro = erro.getCause();
        logger.log(java.util.logging.Level.WARNING, "Operação de " + modulo, erro);
        JOptionPane.showMessageDialog(this, erro.getMessage() == null ? "Erro na operação de " + modulo + "." : erro.getMessage(),
                modulo, JOptionPane.ERROR_MESSAGE);
    }

    private boolean confirmarExclusao(String mensagem) {
        Object[] opcoes = {"Excluir", "Cancelar"};
        return JOptionPane.showOptionDialog(this, mensagem, "Confirmar exclusão", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE, null, opcoes, opcoes[1]) == 0;
    }

    private static String texto(JTextField campo) {
        return campo.getText().trim();
    }

    private static String textoOuNulo(JTextField campo) {
        String t = campo.getText().trim();
        return t.isEmpty() ? null : t;
    }

    private static String vazioSeNulo(Object valor) {
        return valor == null ? "" : valor.toString();
    }

    // ----------------------------------- Pacientes -----------------------------------
    private final BOPacientes boPacientes = new BOPacientes();
    private final ButtonGroup grupoPesquisaPac = new ButtonGroup();
    private List<pacientes> pacientesEncontrados = new ArrayList<>();
    private Integer pacienteSelecionadoId;
    private boolean pacientesOcupados;

    private void configurarPacientes() {
        agruparRadios(grupoPesquisaPac,
                new JRadioButton[]{RbIdPac, RbNomePac, RbCpfPac, RbNascimentoPac, RbTelefonePac, RbEmailPac, RbSexoPac, RbSanguePac},
                new String[]{"id", "nome", "cpf", "nascimento", "telefone", "email", "sexo", "sangue"}, RbNomePac);
        txtPesquisaPac.setToolTipText("Nome, CPF, telefone e e-mail: parte do texto; nascimento: dd/MM/aaaa; sexo: M, F ou Outro. Vazio lista todos.");
        txtCpfPac.setToolTipText("000.000.000-00 ou só os 11 números");
        txtNascimentoPac.setToolTipText("dd/MM/aaaa");
        txtSexoPac.setToolTipText("M, F ou Outro");
        txtSanguePac.setToolTipText("A+, A-, B+, B-, AB+, AB-, O+ ou O- (opcional)");
        prepararTabela(tblPaciente, () -> pacientesOcupados, this::selecionarPaciente,
                "ID", "Nome", "CPF", "Nascimento", "Telefone", "E-mail", "Sexo", "Tipo sanguíneo");
        btnPesquisarPac.addActionListener(e -> pesquisarPacientes());
        txtPesquisaPac.addActionListener(e -> pesquisarPacientes());
        btnSalvarPac.addActionListener(e -> salvarPaciente());
        btnExcluirPac.addActionListener(e -> excluirPaciente());
        btnNovoPac.addActionListener(e -> limparPaciente());
        limparPaciente();
    }

    private void limparPaciente() {
        pacienteSelecionadoId = null;
        tblPaciente.clearSelection();
        for (JTextField campo : new JTextField[]{txtNomePac, txtCpfPac, txtNascimentoPac, txtTelefonePac,
                txtSexoPac, txtEmailPac, txtEnderecoPac, txtSanguePac}) campo.setText("");
        btnExcluirPac.setEnabled(false);
        btnSalvarPac.setToolTipText("Cadastrar um novo paciente");
    }

    private void selecionarPaciente() {
        int linha = tblPaciente.getSelectedRow();
        if (linha < 0) {
            if (pacienteSelecionadoId != null) limparPaciente();
            return;
        }
        pacientes p = pacientesEncontrados.get(tblPaciente.convertRowIndexToModel(linha));
        pacienteSelecionadoId = p.getId_paciente();
        txtNomePac.setText(p.getNome());
        txtCpfPac.setText(p.getCpf());
        txtNascimentoPac.setText(p.getData_nascimento() == null ? ""
                : DATA_CONS.format(new java.sql.Date(p.getData_nascimento().getTime()).toLocalDate()));
        txtTelefonePac.setText(vazioSeNulo(p.getTelefone()));
        txtSexoPac.setText(p.getSexo() == null ? "" : p.getSexo() == MODEL.Sexo.OUTRO ? "Outro" : p.getSexo().name());
        txtEmailPac.setText(vazioSeNulo(p.getEmail()));
        txtEnderecoPac.setText(vazioSeNulo(p.getEndereco()));
        txtSanguePac.setText(vazioSeNulo(p.getTipo_sanguineo()));
        btnExcluirPac.setEnabled(true);
        btnSalvarPac.setToolTipText("Atualizar o paciente #" + pacienteSelecionadoId);
    }

    private void pesquisarPacientes() {
        if (pacientesOcupados) return;
        String campo = grupoPesquisaPac.getSelection().getActionCommand();
        String termo = texto(txtPesquisaPac);
        // Valida antes de apagar a seleção ou consultar o banco.
        try {
            if (!termo.isEmpty()) {
                switch (campo) {
                    case "id": BOPacientes.lerId(termo); break;
                    case "nascimento": BOConsultas.lerData(termo); break;
                    case "sexo": BOPacientes.lerSexo(termo); break;
                    case "sangue": BOPacientes.lerTipoSanguineo(termo); break;
                    default: break;
                }
            }
        } catch (IllegalArgumentException e) { erroAba("Pacientes", e); return; }
        limparPaciente();
        executarAba(this::ocuparPacientes, "Pacientes", () -> boPacientes.pesquisar(campo, termo), lista -> {
            pacientesEncontrados = lista;
            List<Object[]> linhas = new ArrayList<>();
            for (pacientes p : lista) {
                linhas.add(new Object[]{p.getId_paciente(), p.getNome(), p.getCpf(),
                    p.getData_nascimento() == null ? "" : DATA_CONS.format(new java.sql.Date(p.getData_nascimento().getTime()).toLocalDate()),
                    vazioSeNulo(p.getTelefone()), vazioSeNulo(p.getEmail()),
                    p.getSexo() == null ? "" : p.getSexo() == MODEL.Sexo.OUTRO ? "Outro" : p.getSexo().name(),
                    vazioSeNulo(p.getTipo_sanguineo())});
            }
            preencherTabela(tblPaciente, linhas, "paciente");
        });
    }

    private void salvarPaciente() {
        if (pacientesOcupados) return;
        final pacientes p = new pacientes();
        final boolean editar = pacienteSelecionadoId != null;
        try {
            if (texto(txtNomePac).isEmpty()) throw new IllegalArgumentException("Informe o nome do paciente.");
            p.setNome(texto(txtNomePac));
            p.setCpf(BOPacientes.lerCpf(texto(txtCpfPac)));
            p.setData_nascimento(java.sql.Date.valueOf(BOConsultas.lerData(texto(txtNascimentoPac))));
            p.setSexo(BOPacientes.lerSexo(texto(txtSexoPac)));
            p.setTelefone(textoOuNulo(txtTelefonePac));
            p.setEmail(textoOuNulo(txtEmailPac));
            p.setEndereco(textoOuNulo(txtEnderecoPac));
            String sangue = textoOuNulo(txtSanguePac);
            p.setTipo_sanguineo(sangue == null ? null : BOPacientes.lerTipoSanguineo(sangue));
            if (editar) p.setId_paciente(pacienteSelecionadoId);
        } catch (IllegalArgumentException e) { erroAba("Pacientes", e); return; }
        executarAba(this::ocuparPacientes, "Pacientes", () -> {
            if (editar) boPacientes.Atualizar(p); else boPacientes.Salvar(p);
            return true;
        }, sucesso -> {
            limparPaciente();
            JOptionPane.showMessageDialog(this, editar ? "Paciente atualizado." : "Paciente cadastrado.");
            pesquisarPacientes();
        });
    }

    private void excluirPaciente() {
        if (pacientesOcupados || pacienteSelecionadoId == null) return;
        int id = pacienteSelecionadoId;
        if (!confirmarExclusao("Deseja realmente excluir o paciente #" + id + "?\n"
                + "As consultas e receitas desse paciente também serão excluídas.")) return;
        executarAba(this::ocuparPacientes, "Pacientes", () -> { boPacientes.excluir(id); return true; }, sucesso -> {
            limparPaciente();
            JOptionPane.showMessageDialog(this, "Paciente excluído.");
            pesquisarPacientes();
        });
    }

    private void ocuparPacientes(boolean ocupado) {
        pacientesOcupados = ocupado;
        habilitarComponentes(jPanel1, !ocupado);
        btnExcluirPac.setEnabled(!ocupado && pacienteSelecionadoId != null);
    }

    // ------------------------------------ Médicos ------------------------------------
    private final BOMedicos boMedicos = new BOMedicos();
    private final ButtonGroup grupoPesquisaMed = new ButtonGroup();
    private List<medicos> medicosEncontrados = new ArrayList<>();
    private Integer medicoSelecionadoId;
    private boolean medicosOcupados;
    private SugestoesConsulta<funcionarios> sugestoesFuncionarioMed;

    private void configurarMedicos() {
        agruparRadios(grupoPesquisaMed,
                new JRadioButton[]{RbIdMed, RbNomeMed, RbCrmMed, RbEspecialidadeMed, RbTelefoneMed, RbEmailMed},
                new String[]{"id", "nome", "crm", "especialidade", "telefone", "email"}, RbNomeMed);
        txtPesquisaMed.setToolTipText("Pesquisa por parte do texto (ID: número exato). Vazio lista todos.");
        CbFuncionarioMed.setToolTipText("Funcionário com cargo medico que será o login deste médico");
        prepararTabela(tblMedico, () -> medicosOcupados, this::selecionarMedico,
                "ID", "Nome", "CRM", "Especialidade", "Telefone", "E-mail");
        sugestoesFuncionarioMed = new SugestoesConsulta<>(CbFuncionarioMed,
                termo -> boMedicos.pesquisarFuncionarios(termo, medicoSelecionadoId == null ? 0 : medicoSelecionadoId),
                f -> f.getNome() + " | CPF: " + f.getCpf() + " | #" + f.getId_funcionario());
        btnPesquisarMed.addActionListener(e -> pesquisarMedicos());
        txtPesquisaMed.addActionListener(e -> pesquisarMedicos());
        btnSalvarMed.addActionListener(e -> salvarMedico());
        btnExcluirMed.addActionListener(e -> excluirMedico());
        btnNovoMed.addActionListener(e -> limparMedico());
        limparMedico();
    }

    private void limparMedico() {
        medicoSelecionadoId = null;
        tblMedico.clearSelection();
        sugestoesFuncionarioMed.selecionar(null);
        for (JTextField campo : new JTextField[]{txtCrmMed, txtTelefoneMed, txtEspecialidadeMed, txtEmailMed}) campo.setText("");
        btnExcluirMed.setEnabled(false);
        btnSalvarMed.setToolTipText("Cadastrar um novo médico");
    }

    private void selecionarMedico() {
        int linha = tblMedico.getSelectedRow();
        if (linha < 0) {
            if (medicoSelecionadoId != null) limparMedico();
            return;
        }
        medicos m = medicosEncontrados.get(tblMedico.convertRowIndexToModel(linha));
        medicoSelecionadoId = m.getId_medico();
        sugestoesFuncionarioMed.selecionar(m.getFuncionario());
        txtCrmMed.setText(m.getCrm());
        txtTelefoneMed.setText(vazioSeNulo(m.getTelefone()));
        txtEspecialidadeMed.setText(vazioSeNulo(m.getEspecialidade()));
        txtEmailMed.setText(vazioSeNulo(m.getEmail()));
        btnExcluirMed.setEnabled(true);
        btnSalvarMed.setToolTipText("Atualizar o médico #" + medicoSelecionadoId);
    }

    private void pesquisarMedicos() {
        if (medicosOcupados) return;
        String campo = grupoPesquisaMed.getSelection().getActionCommand();
        String termo = texto(txtPesquisaMed);
        try {
            if (!termo.isEmpty() && campo.equals("id")) BOPacientes.lerId(termo);
        } catch (IllegalArgumentException e) { erroAba("Médicos", e); return; }
        limparMedico();
        executarAba(this::ocuparMedicos, "Médicos", () -> boMedicos.pesquisar(campo, termo), lista -> {
            medicosEncontrados = lista;
            List<Object[]> linhas = new ArrayList<>();
            for (medicos m : lista) {
                linhas.add(new Object[]{m.getId_medico(), m.getNome(), m.getCrm(), vazioSeNulo(m.getEspecialidade()),
                    vazioSeNulo(m.getTelefone()), vazioSeNulo(m.getEmail())});
            }
            preencherTabela(tblMedico, linhas, "médico");
        });
    }

    private void salvarMedico() {
        if (medicosOcupados) return;
        final medicos m = new medicos();
        final boolean editar = medicoSelecionadoId != null;
        try {
            funcionarios funcionario = sugestoesFuncionarioMed.selecionado;
            if (funcionario == null) throw new IllegalArgumentException("Selecione nas sugestões o funcionário (login) do médico.");
            if (texto(txtCrmMed).isEmpty()) throw new IllegalArgumentException("Informe o CRM.");
            m.setFuncionario(funcionario);
            m.setId_funcionario(funcionario.getId_funcionario());
            m.setCrm(texto(txtCrmMed));
            m.setTelefone(textoOuNulo(txtTelefoneMed));
            m.setEspecialidade(textoOuNulo(txtEspecialidadeMed));
            m.setEmail(textoOuNulo(txtEmailMed));
            if (editar) m.setId_medico(medicoSelecionadoId);
        } catch (IllegalArgumentException e) { erroAba("Médicos", e); return; }
        executarAba(this::ocuparMedicos, "Médicos", () -> {
            if (editar) boMedicos.Atualizar(m); else boMedicos.Salvar(m);
            return true;
        }, sucesso -> {
            limparMedico();
            JOptionPane.showMessageDialog(this, editar ? "Médico atualizado." : "Médico cadastrado.");
            pesquisarMedicos();
        });
    }

    private void excluirMedico() {
        if (medicosOcupados || medicoSelecionadoId == null) return;
        int id = medicoSelecionadoId;
        if (!confirmarExclusao("Deseja realmente excluir o médico #" + id + "?\n"
                + "O funcionário (login) vinculado não será apagado.")) return;
        executarAba(this::ocuparMedicos, "Médicos", () -> { boMedicos.excluir(id); return true; }, sucesso -> {
            limparMedico();
            JOptionPane.showMessageDialog(this, "Médico excluído.");
            pesquisarMedicos();
        });
    }

    private void ocuparMedicos(boolean ocupado) {
        medicosOcupados = ocupado;
        habilitarComponentes(jPanel2, !ocupado);
        btnExcluirMed.setEnabled(!ocupado && medicoSelecionadoId != null);
    }

    // ------------------------------------ Usuários ------------------------------------
    // Usuários = dados de acesso da tabela funcionarios (login, senha e perfil/cargo).
    private final BOFuncionarios boFuncionarios = new BOFuncionarios();
    private final ButtonGroup grupoPesquisaUsu = new ButtonGroup();
    private List<funcionarios> usuariosEncontrados = new ArrayList<>();
    private Integer usuarioSelecionadoId;
    private boolean usuariosOcupados;

    private void configurarUsuarios() {
        agruparRadios(grupoPesquisaUsu,
                new JRadioButton[]{RbIdUsu, RbNomeUsu, RbCpfUsu, RbLoginUsu, RbPerfilUsu},
                new String[]{"id", "nome", "cpf", "login", "cargo"}, RbLoginUsu);
        txtPesquisaUsu.setToolTipText("Pesquisa por parte do texto (ID: número exato). Vazio lista todos.");
        txtCpfUsu.setToolTipText("11 números");
        txtPerfilUsu.setToolTipText("admin, medico ou secretaria");
        prepararTabela(tblUsuario, () -> usuariosOcupados, this::selecionarUsuario, "ID", "Nome", "CPF", "Login", "Perfil");
        btnPesquisarUsu.addActionListener(e -> pesquisarUsuarios());
        txtPesquisaUsu.addActionListener(e -> pesquisarUsuarios());
        btnSalvarUsu.addActionListener(e -> salvarUsuario());
        btnExcluirUsu.addActionListener(e -> excluirUsuario());
        btnNovoUsu.addActionListener(e -> limparUsuario());
        limparUsuario();
    }

    private void limparUsuario() {
        usuarioSelecionadoId = null;
        tblUsuario.clearSelection();
        for (JTextField campo : new JTextField[]{txtNomeUsu, txtCpfUsu, txtPerfilUsu, txtLoginUsu, txtSenhaUsu}) campo.setText("");
        txtSenhaUsu.setToolTipText("Senha do novo usuário");
        btnExcluirUsu.setEnabled(false);
        btnSalvarUsu.setToolTipText("Cadastrar um novo usuário");
    }

    private void selecionarUsuario() {
        int linha = tblUsuario.getSelectedRow();
        if (linha < 0) {
            if (usuarioSelecionadoId != null) limparUsuario();
            return;
        }
        funcionarios f = usuariosEncontrados.get(tblUsuario.convertRowIndexToModel(linha));
        usuarioSelecionadoId = f.getId_funcionario();
        txtNomeUsu.setText(f.getNome());
        txtCpfUsu.setText(vazioSeNulo(f.getCpf()));
        txtPerfilUsu.setText(vazioSeNulo(f.getCargo()));
        txtLoginUsu.setText(f.getLogin());
        txtSenhaUsu.setText("");
        txtSenhaUsu.setToolTipText("Deixe em branco para manter a senha atual");
        btnExcluirUsu.setEnabled(true);
        btnSalvarUsu.setToolTipText("Atualizar o usuário #" + usuarioSelecionadoId);
    }

    private void pesquisarUsuarios() {
        if (usuariosOcupados) return;
        String campo = grupoPesquisaUsu.getSelection().getActionCommand();
        String termo = texto(txtPesquisaUsu);
        try {
            if (!termo.isEmpty() && campo.equals("id")) BOPacientes.lerId(termo);
        } catch (IllegalArgumentException e) { erroAba("Usuários", e); return; }
        limparUsuario();
        executarAba(this::ocuparUsuarios, "Usuários", () -> boFuncionarios.pesquisar(campo, termo), lista -> {
            usuariosEncontrados = lista;
            List<Object[]> linhas = new ArrayList<>();
            for (funcionarios f : lista) {
                linhas.add(new Object[]{f.getId_funcionario(), f.getNome(), vazioSeNulo(f.getCpf()), f.getLogin(), vazioSeNulo(f.getCargo())});
            }
            preencherTabela(tblUsuario, linhas, "usuário");
        });
    }

    private void salvarUsuario() {
        if (usuariosOcupados) return;
        final funcionarios f = new funcionarios();
        final boolean editar = usuarioSelecionadoId != null;
        try {
            if (texto(txtNomeUsu).isEmpty()) throw new IllegalArgumentException("Informe o nome.");
            f.setNome(texto(txtNomeUsu));
            f.setCpf(BOFuncionarios.lerCpf(texto(txtCpfUsu)));
            f.setCargo(BOFuncionarios.lerCargo(texto(txtPerfilUsu)));
            f.setLogin(texto(txtLoginUsu));
            f.setSenha(new String(txtSenhaUsu.getPassword()));
            if (!editar && f.getSenha().isEmpty()) throw new IllegalArgumentException("Informe a senha.");
            if (editar) f.setId_funcionario(usuarioSelecionadoId);
        } catch (IllegalArgumentException e) { erroAba("Usuários", e); return; }
        executarAba(this::ocuparUsuarios, "Usuários", () -> {
            if (editar) boFuncionarios.AtualizarUsuario(f); else boFuncionarios.SalvarUsuario(f);
            return true;
        }, sucesso -> {
            limparUsuario();
            JOptionPane.showMessageDialog(this, editar ? "Usuário atualizado." : "Usuário cadastrado.");
            pesquisarUsuarios();
        });
    }

    private void excluirUsuario() {
        if (usuariosOcupados || usuarioSelecionadoId == null) return;
        int id = usuarioSelecionadoId;
        if (!confirmarExclusao("Deseja realmente excluir o usuário #" + id + "?\n"
                + "O cadastro de funcionário também será apagado (é a mesma tabela).")) return;
        executarAba(this::ocuparUsuarios, "Usuários", () -> { boFuncionarios.excluir(id); return true; }, sucesso -> {
            limparUsuario();
            JOptionPane.showMessageDialog(this, "Usuário excluído.");
            pesquisarUsuarios();
        });
    }

    private void ocuparUsuarios(boolean ocupado) {
        usuariosOcupados = ocupado;
        habilitarComponentes(jPanel5, !ocupado);
        btnExcluirUsu.setEnabled(!ocupado && usuarioSelecionadoId != null);
    }

    // ---------------------------------- Funcionários ----------------------------------
    private final ButtonGroup grupoPesquisaFunc = new ButtonGroup();
    private List<funcionarios> funcionariosEncontrados = new ArrayList<>();
    private Integer funcionarioSelecionadoId;
    private boolean funcionariosOcupados;

    private void configurarFuncionarios() {
        agruparRadios(grupoPesquisaFunc,
                new JRadioButton[]{RbIdFunc, RbNomeFunc, RbCpfFunc, RbCargoFunc, RbSalarioFunc, RbLoginFunc},
                new String[]{"id", "nome", "cpf", "cargo", "salario", "login"}, RbNomeFunc);
        txtPesquisaFunc.setToolTipText("Nome, CPF, cargo e login: parte do texto; salário: valor exato (ex.: 3500,00). Vazio lista todos.");
        txtCpfFunc.setToolTipText("11 números");
        txtCargoFunc.setToolTipText("admin, medico ou secretaria");
        txtSalarioFunc.setToolTipText("Ex.: 3500,00");
        prepararTabela(tblFuncionario, () -> funcionariosOcupados, this::selecionarFuncionario,
                "ID", "Nome", "CPF", "Cargo", "Salário", "Login");
        btnPesquisarFunc.addActionListener(e -> pesquisarFuncionarios());
        txtPesquisaFunc.addActionListener(e -> pesquisarFuncionarios());
        btnSalvarFunc.addActionListener(e -> salvarFuncionario());
        btnExcluirFunc.addActionListener(e -> excluirFuncionario());
        btnNovoFunc.addActionListener(e -> limparFuncionario());
        limparFuncionario();
    }

    private void limparFuncionario() {
        funcionarioSelecionadoId = null;
        tblFuncionario.clearSelection();
        for (JTextField campo : new JTextField[]{txtNomeFunc, txtCpfFunc, txtCargoFunc, txtSalarioFunc, txtLoginFunc, txtSenhaFunc})
            campo.setText("");
        txtSenhaFunc.setToolTipText("Senha do novo funcionário");
        btnExcluirFunc.setEnabled(false);
        btnSalvarFunc.setToolTipText("Cadastrar um novo funcionário");
    }

    private void selecionarFuncionario() {
        int linha = tblFuncionario.getSelectedRow();
        if (linha < 0) {
            if (funcionarioSelecionadoId != null) limparFuncionario();
            return;
        }
        funcionarios f = funcionariosEncontrados.get(tblFuncionario.convertRowIndexToModel(linha));
        funcionarioSelecionadoId = f.getId_funcionario();
        txtNomeFunc.setText(f.getNome());
        txtCpfFunc.setText(vazioSeNulo(f.getCpf()));
        txtCargoFunc.setText(vazioSeNulo(f.getCargo()));
        txtSalarioFunc.setText(MOEDA.format(f.getSalario()));
        txtLoginFunc.setText(f.getLogin());
        txtSenhaFunc.setText("");
        txtSenhaFunc.setToolTipText("Deixe em branco para manter a senha atual");
        btnExcluirFunc.setEnabled(true);
        btnSalvarFunc.setToolTipText("Atualizar o funcionário #" + funcionarioSelecionadoId);
    }

    private void pesquisarFuncionarios() {
        if (funcionariosOcupados) return;
        String campo = grupoPesquisaFunc.getSelection().getActionCommand();
        String termo = texto(txtPesquisaFunc);
        try {
            if (!termo.isEmpty()) {
                switch (campo) {
                    case "id": BOPacientes.lerId(termo); break;
                    case "salario": BOFuncionarios.lerSalario(termo); break;
                    default: break;
                }
            }
        } catch (IllegalArgumentException e) { erroAba("Funcionários", e); return; }
        limparFuncionario();
        executarAba(this::ocuparFuncionarios, "Funcionários", () -> boFuncionarios.pesquisar(campo, termo), lista -> {
            funcionariosEncontrados = lista;
            List<Object[]> linhas = new ArrayList<>();
            for (funcionarios f : lista) {
                linhas.add(new Object[]{f.getId_funcionario(), f.getNome(), vazioSeNulo(f.getCpf()), vazioSeNulo(f.getCargo()),
                    MOEDA.format(f.getSalario()), f.getLogin()});
            }
            preencherTabela(tblFuncionario, linhas, "funcionário");
        });
    }

    private void salvarFuncionario() {
        if (funcionariosOcupados) return;
        final funcionarios f = new funcionarios();
        final boolean editar = funcionarioSelecionadoId != null;
        try {
            if (texto(txtNomeFunc).isEmpty()) throw new IllegalArgumentException("Informe o nome.");
            f.setNome(texto(txtNomeFunc));
            f.setCpf(BOFuncionarios.lerCpf(texto(txtCpfFunc)));
            f.setCargo(BOFuncionarios.lerCargo(texto(txtCargoFunc)));
            f.setSalario(BOFuncionarios.lerSalario(texto(txtSalarioFunc)).doubleValue());
            f.setLogin(texto(txtLoginFunc));
            f.setSenha(new String(txtSenhaFunc.getPassword()));
            if (!editar && f.getSenha().isEmpty()) throw new IllegalArgumentException("Informe a senha.");
            if (editar) f.setId_funcionario(funcionarioSelecionadoId);
        } catch (IllegalArgumentException e) { erroAba("Funcionários", e); return; }
        executarAba(this::ocuparFuncionarios, "Funcionários", () -> {
            if (editar) boFuncionarios.Atualizar(f); else boFuncionarios.Salvar(f);
            return true;
        }, sucesso -> {
            limparFuncionario();
            JOptionPane.showMessageDialog(this, editar ? "Funcionário atualizado." : "Funcionário cadastrado.");
            pesquisarFuncionarios();
        });
    }

    private void excluirFuncionario() {
        if (funcionariosOcupados || funcionarioSelecionadoId == null) return;
        int id = funcionarioSelecionadoId;
        if (!confirmarExclusao("Deseja realmente excluir o funcionário #" + id + "?\n"
                + "Se ele for médico, o cadastro de médico fica sem login vinculado.")) return;
        executarAba(this::ocuparFuncionarios, "Funcionários", () -> { boFuncionarios.excluir(id); return true; }, sucesso -> {
            limparFuncionario();
            JOptionPane.showMessageDialog(this, "Funcionário excluído.");
            pesquisarFuncionarios();
        });
    }

    private void ocuparFuncionarios(boolean ocupado) {
        funcionariosOcupados = ocupado;
        habilitarComponentes(jPanel3, !ocupado);
        btnExcluirFunc.setEnabled(!ocupado && funcionarioSelecionadoId != null);
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
        txtPesquisaPac = new javax.swing.JTextField();
        btnPesquisarPac = new javax.swing.JButton();
        btnNovoPac = new javax.swing.JButton();
        jScrollPane11 = new javax.swing.JScrollPane();
        tblPaciente = new javax.swing.JTable();
        jPanel13 = new javax.swing.JPanel();
        lblNomePac = new javax.swing.JLabel();
        txtNomePac = new javax.swing.JTextField();
        lblCpfPac = new javax.swing.JLabel();
        txtCpfPac = new javax.swing.JTextField();
        lblNascimentoPac = new javax.swing.JLabel();
        txtNascimentoPac = new javax.swing.JTextField();
        lblTelefonePac = new javax.swing.JLabel();
        txtTelefonePac = new javax.swing.JTextField();
        lblSexoPac = new javax.swing.JLabel();
        txtSexoPac = new javax.swing.JTextField();
        lblEmailPac = new javax.swing.JLabel();
        txtEmailPac = new javax.swing.JTextField();
        lblEnderecoPac = new javax.swing.JLabel();
        txtEnderecoPac = new javax.swing.JTextField();
        lblSanguePac = new javax.swing.JLabel();
        txtSanguePac = new javax.swing.JTextField();
        btnSalvarPac = new javax.swing.JButton();
        btnExcluirPac = new javax.swing.JButton();
        RbIdPac = new javax.swing.JRadioButton();
        RbNomePac = new javax.swing.JRadioButton();
        RbCpfPac = new javax.swing.JRadioButton();
        RbNascimentoPac = new javax.swing.JRadioButton();
        RbTelefonePac = new javax.swing.JRadioButton();
        RbEmailPac = new javax.swing.JRadioButton();
        RbSexoPac = new javax.swing.JRadioButton();
        RbSanguePac = new javax.swing.JRadioButton();
        jPanel2 = new javax.swing.JPanel();
        txtPesquisaMed = new javax.swing.JTextField();
        btnPesquisarMed = new javax.swing.JButton();
        btnNovoMed = new javax.swing.JButton();
        jScrollPane10 = new javax.swing.JScrollPane();
        tblMedico = new javax.swing.JTable();
        jPanel9 = new javax.swing.JPanel();
        lblFuncionarioMed = new javax.swing.JLabel();
        CbFuncionarioMed = new javax.swing.JComboBox<>();
        lblCrmMed = new javax.swing.JLabel();
        txtCrmMed = new javax.swing.JTextField();
        lblTelefoneMed = new javax.swing.JLabel();
        txtTelefoneMed = new javax.swing.JTextField();
        lblEspecialidadeMed = new javax.swing.JLabel();
        txtEspecialidadeMed = new javax.swing.JTextField();
        lblEmailMed = new javax.swing.JLabel();
        txtEmailMed = new javax.swing.JTextField();
        btnSalvarMed = new javax.swing.JButton();
        btnExcluirMed = new javax.swing.JButton();
        RbIdMed = new javax.swing.JRadioButton();
        RbNomeMed = new javax.swing.JRadioButton();
        RbCrmMed = new javax.swing.JRadioButton();
        RbEspecialidadeMed = new javax.swing.JRadioButton();
        RbTelefoneMed = new javax.swing.JRadioButton();
        RbEmailMed = new javax.swing.JRadioButton();
        jPanel5 = new javax.swing.JPanel();
        txtPesquisaUsu = new javax.swing.JTextField();
        btnPesquisarUsu = new javax.swing.JButton();
        btnNovoUsu = new javax.swing.JButton();
        jScrollPane7 = new javax.swing.JScrollPane();
        tblUsuario = new javax.swing.JTable();
        jPanel6 = new javax.swing.JPanel();
        lblNomeUsu = new javax.swing.JLabel();
        txtNomeUsu = new javax.swing.JTextField();
        lblCpfUsu = new javax.swing.JLabel();
        txtCpfUsu = new javax.swing.JTextField();
        lblPerfilUsu = new javax.swing.JLabel();
        txtPerfilUsu = new javax.swing.JTextField();
        lblLoginUsu = new javax.swing.JLabel();
        txtLoginUsu = new javax.swing.JTextField();
        lblSenhaUsu = new javax.swing.JLabel();
        txtSenhaUsu = new javax.swing.JPasswordField();
        btnSalvarUsu = new javax.swing.JButton();
        btnExcluirUsu = new javax.swing.JButton();
        RbIdUsu = new javax.swing.JRadioButton();
        RbNomeUsu = new javax.swing.JRadioButton();
        RbCpfUsu = new javax.swing.JRadioButton();
        RbLoginUsu = new javax.swing.JRadioButton();
        RbPerfilUsu = new javax.swing.JRadioButton();
        jPanel4 = new javax.swing.JPanel();
        TxtPesquisaCons = new javax.swing.JTextField();
        btnPesquiCons = new javax.swing.JButton();
        jScrollPane8 = new javax.swing.JScrollPane();
        tblConsulta = new javax.swing.JTable();
        jPanel7 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        txtDatacons = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        txtHoraCons = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        txtSalaCons = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        txtStatusCons = new javax.swing.JTextField();
        btnSalvarCons = new javax.swing.JButton();
        btnExcluirCons = new javax.swing.JButton();
        CbPascienteCOns = new javax.swing.JComboBox<>();
        CbMedicoCons = new javax.swing.JComboBox<>();
        RbPascienteCons = new javax.swing.JRadioButton();
        RbSalaCons = new javax.swing.JRadioButton();
        RbMedicoCons = new javax.swing.JRadioButton();
        RbDataCons = new javax.swing.JRadioButton();
        RbHorarioCons = new javax.swing.JRadioButton();
        RbStatusCons = new javax.swing.JRadioButton();
        Rbidons = new javax.swing.JRadioButton();
        jPanel3 = new javax.swing.JPanel();
        txtPesquisaFunc = new javax.swing.JTextField();
        btnPesquisarFunc = new javax.swing.JButton();
        btnNovoFunc = new javax.swing.JButton();
        jScrollPane9 = new javax.swing.JScrollPane();
        tblFuncionario = new javax.swing.JTable();
        jPanel8 = new javax.swing.JPanel();
        lblNomeFunc = new javax.swing.JLabel();
        txtNomeFunc = new javax.swing.JTextField();
        lblCpfFunc = new javax.swing.JLabel();
        txtCpfFunc = new javax.swing.JTextField();
        lblCargoFunc = new javax.swing.JLabel();
        txtCargoFunc = new javax.swing.JTextField();
        lblSalarioFunc = new javax.swing.JLabel();
        txtSalarioFunc = new javax.swing.JTextField();
        lblLoginFunc = new javax.swing.JLabel();
        txtLoginFunc = new javax.swing.JTextField();
        lblSenhaFunc = new javax.swing.JLabel();
        txtSenhaFunc = new javax.swing.JPasswordField();
        btnSalvarFunc = new javax.swing.JButton();
        btnExcluirFunc = new javax.swing.JButton();
        RbIdFunc = new javax.swing.JRadioButton();
        RbNomeFunc = new javax.swing.JRadioButton();
        RbCpfFunc = new javax.swing.JRadioButton();
        RbCargoFunc = new javax.swing.JRadioButton();
        RbSalarioFunc = new javax.swing.JRadioButton();
        RbLoginFunc = new javax.swing.JRadioButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        btnPesquisarPac.setText("Pesquisar");

        btnNovoPac.setText("Novo paciente");

        tblPaciente.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Nome", "CPF", "Nascimento", "Telefone", "E-mail", "Sexo", "Tipo sanguíneo"
            }
        ));
        jScrollPane11.setViewportView(tblPaciente);

        lblNomePac.setText("Nome");

        lblCpfPac.setText("CPF");

        lblNascimentoPac.setText("Nascimento");

        lblTelefonePac.setText("Telefone");

        lblSexoPac.setText("Sexo");

        lblEmailPac.setText("E-mail");

        lblEnderecoPac.setText("Endereço");

        lblSanguePac.setText("Tipo sang.");

        btnSalvarPac.setText("Salvar");

        btnExcluirPac.setText("Excluir");

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel13Layout.createSequentialGroup()
                    .addGap(18, 18, 18)
                    .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel13Layout.createSequentialGroup()
                            .addComponent(lblNomePac, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(txtNomePac, javax.swing.GroupLayout.PREFERRED_SIZE, 454, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel13Layout.createSequentialGroup()
                            .addComponent(lblCpfPac, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(txtCpfPac, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(10, 10, 10)
                            .addComponent(lblNascimentoPac, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(txtNascimentoPac, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel13Layout.createSequentialGroup()
                            .addComponent(lblTelefonePac, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(txtTelefonePac, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(10, 10, 10)
                            .addComponent(lblSexoPac, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(txtSexoPac, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel13Layout.createSequentialGroup()
                            .addComponent(lblEmailPac, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(txtEmailPac, javax.swing.GroupLayout.PREFERRED_SIZE, 454, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel13Layout.createSequentialGroup()
                            .addComponent(lblEnderecoPac, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(txtEnderecoPac, javax.swing.GroupLayout.PREFERRED_SIZE, 454, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel13Layout.createSequentialGroup()
                            .addComponent(lblSanguePac, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(txtSanguePac, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel13Layout.createSequentialGroup()
                            .addComponent(btnExcluirPac, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(btnSalvarPac, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGap(30, 30, Short.MAX_VALUE))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel13Layout.createSequentialGroup()
                    .addGap(40, 40, 40)
                    .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblNomePac, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtNomePac, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblCpfPac, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtCpfPac, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblNascimentoPac, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtNascimentoPac, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblTelefonePac, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtTelefonePac, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblSexoPac, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtSexoPac, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblEmailPac, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtEmailPac, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblEnderecoPac, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtEnderecoPac, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblSanguePac, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtSanguePac, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(33, 33, 33)
                    .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnSalvarPac, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnExcluirPac, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(40, 40, Short.MAX_VALUE))
        );

        RbIdPac.setText("ID");

        RbNomePac.setText("Nome");

        RbCpfPac.setText("CPF");

        RbNascimentoPac.setText("Nascimento");

        RbTelefonePac.setText("Telefone");

        RbEmailPac.setText("E-mail");

        RbSexoPac.setText("Sexo");

        RbSanguePac.setText("Tipo sang.");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGap(40, 40, 40)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jScrollPane11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtPesquisaPac, javax.swing.GroupLayout.PREFERRED_SIZE, 530, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(RbIdPac, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(10, 10, 10)
                                    .addComponent(RbNomePac, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(10, 10, 10)
                                    .addComponent(RbCpfPac, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(10, 10, 10)
                                    .addComponent(RbNascimentoPac, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(10, 10, 10)
                                    .addComponent(RbTelefonePac, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(10, 10, 10)
                                    .addComponent(RbEmailPac, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(10, 10, 10)
                                    .addComponent(RbSexoPac, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(10, 10, 10)
                                    .addComponent(RbSanguePac, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGap(18, 18, 18)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(btnPesquisarPac, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnNovoPac, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGap(40, 40, Short.MAX_VALUE)
                    .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(30, 30, 30))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGap(27, 27, 27)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(txtPesquisaPac, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnPesquisarPac, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(18, 18, 18)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(RbIdPac, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(RbNomePac, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(RbCpfPac, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(RbNascimentoPac, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(RbTelefonePac, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(RbEmailPac, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(RbSexoPac, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(RbSanguePac, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnNovoPac, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(24, 24, 24)
                            .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 399, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(40, 40, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Pacientes", jPanel1);

        btnPesquisarMed.setText("Pesquisar");

        btnNovoMed.setText("Novo médico");

        tblMedico.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Nome", "CRM", "Especialidade", "Telefone", "E-mail"
            }
        ));
        jScrollPane10.setViewportView(tblMedico);

        lblFuncionarioMed.setText("Funcionário");

        CbFuncionarioMed.setEditable(true);

        lblCrmMed.setText("CRM");

        lblTelefoneMed.setText("Telefone");

        lblEspecialidadeMed.setText("Especialidade");

        lblEmailMed.setText("E-mail");

        btnSalvarMed.setText("Salvar");

        btnExcluirMed.setText("Excluir");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel9Layout.createSequentialGroup()
                    .addGap(18, 18, 18)
                    .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel9Layout.createSequentialGroup()
                            .addComponent(lblFuncionarioMed, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(CbFuncionarioMed, javax.swing.GroupLayout.PREFERRED_SIZE, 454, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel9Layout.createSequentialGroup()
                            .addComponent(lblCrmMed, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(txtCrmMed, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(10, 10, 10)
                            .addComponent(lblTelefoneMed, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(txtTelefoneMed, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel9Layout.createSequentialGroup()
                            .addComponent(lblEspecialidadeMed, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(txtEspecialidadeMed, javax.swing.GroupLayout.PREFERRED_SIZE, 454, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel9Layout.createSequentialGroup()
                            .addComponent(lblEmailMed, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(txtEmailMed, javax.swing.GroupLayout.PREFERRED_SIZE, 454, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel9Layout.createSequentialGroup()
                            .addComponent(btnExcluirMed, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(btnSalvarMed, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGap(30, 30, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel9Layout.createSequentialGroup()
                    .addGap(40, 40, 40)
                    .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblFuncionarioMed, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(CbFuncionarioMed, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblCrmMed, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtCrmMed, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblTelefoneMed, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtTelefoneMed, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblEspecialidadeMed, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtEspecialidadeMed, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblEmailMed, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtEmailMed, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(33, 33, 33)
                    .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnSalvarMed, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnExcluirMed, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(40, 40, Short.MAX_VALUE))
        );

        RbIdMed.setText("ID");

        RbNomeMed.setText("Nome");

        RbCrmMed.setText("CRM");

        RbEspecialidadeMed.setText("Especialidade");

        RbTelefoneMed.setText("Telefone");

        RbEmailMed.setText("E-mail");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addGap(40, 40, 40)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jScrollPane10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtPesquisaMed, javax.swing.GroupLayout.PREFERRED_SIZE, 530, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addComponent(RbIdMed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(10, 10, 10)
                                    .addComponent(RbNomeMed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(10, 10, 10)
                                    .addComponent(RbCrmMed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(10, 10, 10)
                                    .addComponent(RbEspecialidadeMed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(10, 10, 10)
                                    .addComponent(RbTelefoneMed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(10, 10, 10)
                                    .addComponent(RbEmailMed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGap(18, 18, 18)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(btnPesquisarMed, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnNovoMed, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGap(40, 40, Short.MAX_VALUE)
                    .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(30, 30, 30))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addGap(27, 27, 27)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(txtPesquisaMed, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnPesquisarMed, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(18, 18, 18)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(RbIdMed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(RbNomeMed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(RbCrmMed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(RbEspecialidadeMed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(RbTelefoneMed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(RbEmailMed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnNovoMed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(24, 24, 24)
                            .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 399, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(40, 40, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Medicos", jPanel2);

        btnPesquisarUsu.setText("Pesquisar");

        btnNovoUsu.setText("Novo usuário");

        tblUsuario.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Nome", "CPF", "Login", "Perfil"
            }
        ));
        jScrollPane7.setViewportView(tblUsuario);

        lblNomeUsu.setText("Nome");

        lblCpfUsu.setText("CPF");

        lblPerfilUsu.setText("Perfil");

        lblLoginUsu.setText("Login");

        lblSenhaUsu.setText("Senha");

        btnSalvarUsu.setText("Salvar");

        btnExcluirUsu.setText("Excluir");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel6Layout.createSequentialGroup()
                    .addGap(18, 18, 18)
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel6Layout.createSequentialGroup()
                            .addComponent(lblNomeUsu, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(txtNomeUsu, javax.swing.GroupLayout.PREFERRED_SIZE, 454, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel6Layout.createSequentialGroup()
                            .addComponent(lblCpfUsu, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(txtCpfUsu, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(10, 10, 10)
                            .addComponent(lblPerfilUsu, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(txtPerfilUsu, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel6Layout.createSequentialGroup()
                            .addComponent(lblLoginUsu, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(txtLoginUsu, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(10, 10, 10)
                            .addComponent(lblSenhaUsu, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(txtSenhaUsu, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel6Layout.createSequentialGroup()
                            .addComponent(btnExcluirUsu, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(btnSalvarUsu, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGap(30, 30, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel6Layout.createSequentialGroup()
                    .addGap(40, 40, 40)
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblNomeUsu, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtNomeUsu, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblCpfUsu, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtCpfUsu, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblPerfilUsu, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtPerfilUsu, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblLoginUsu, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtLoginUsu, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblSenhaUsu, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtSenhaUsu, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(33, 33, 33)
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnSalvarUsu, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnExcluirUsu, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(40, 40, Short.MAX_VALUE))
        );

        RbIdUsu.setText("ID");

        RbNomeUsu.setText("Nome");

        RbCpfUsu.setText("CPF");

        RbLoginUsu.setText("Login");

        RbPerfilUsu.setText("Perfil");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel5Layout.createSequentialGroup()
                    .addGap(40, 40, 40)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel5Layout.createSequentialGroup()
                            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtPesquisaUsu, javax.swing.GroupLayout.PREFERRED_SIZE, 530, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(jPanel5Layout.createSequentialGroup()
                                    .addComponent(RbIdUsu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(10, 10, 10)
                                    .addComponent(RbNomeUsu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(10, 10, 10)
                                    .addComponent(RbCpfUsu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(10, 10, 10)
                                    .addComponent(RbLoginUsu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(10, 10, 10)
                                    .addComponent(RbPerfilUsu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGap(18, 18, 18)
                            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(btnPesquisarUsu, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnNovoUsu, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGap(40, 40, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(30, 30, 30))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel5Layout.createSequentialGroup()
                    .addGap(27, 27, 27)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel5Layout.createSequentialGroup()
                            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(txtPesquisaUsu, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnPesquisarUsu, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(18, 18, 18)
                            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(RbIdUsu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(RbNomeUsu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(RbCpfUsu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(RbLoginUsu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(RbPerfilUsu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnNovoUsu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(24, 24, 24)
                            .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 399, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(40, 40, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Usuarios", jPanel5);

        btnNovaCons = new javax.swing.JButton("Nova consulta");

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

        jLabel14.setText("Pasciente");

        jLabel15.setText("Medico");

        jLabel16.setText("Data");

        jLabel17.setText("Hora");

        txtHoraCons.addActionListener(this::txtHoraConsActionPerformed);

        jLabel18.setText("Sala");

        jLabel19.setText("Status");

        btnSalvarCons.setText("Salvar");

        btnExcluirCons.setText("Excluir");

        CbPascienteCOns.setEditable(true);
        CbPascienteCOns.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        CbMedicoCons.setEditable(true);
        CbMedicoCons.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(jPanel7Layout.createSequentialGroup()
                            .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(txtDatacons, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(txtHoraCons, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel7Layout.createSequentialGroup()
                            .addGap(6, 6, 6)
                            .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel7Layout.createSequentialGroup()
                                    .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(CbMedicoCons, javax.swing.GroupLayout.PREFERRED_SIZE, 428, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel7Layout.createSequentialGroup()
                                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(CbPascienteCOns, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(txtSalaCons, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(txtStatusCons, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                        .addComponent(btnExcluirCons, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnSalvarCons, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(253, 253, 253)))
                .addContainerGap(27, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(71, 71, 71)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(CbPascienteCOns, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(CbMedicoCons, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDatacons, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtHoraCons, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSalaCons, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtStatusCons, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(33, 33, 33)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSalvarCons, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnExcluirCons, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(125, Short.MAX_VALUE))
        );

        RbPascienteCons.setText("Pasciente");

        RbSalaCons.setText("Sala");

        RbMedicoCons.setText("Medico");

        RbDataCons.setText("Data");

        RbHorarioCons.setText("Horario");

        RbStatusCons.setText("Status");

        Rbidons.setText("id");

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
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnPesquiCons, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnNovaCons, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 43, Short.MAX_VALUE)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(48, 48, 48))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
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
                            .addComponent(Rbidons)
                            .addComponent(btnNovaCons))
                        .addGap(24, 24, 24)
                        .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 399, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(482, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Consultas", jPanel4);

        btnPesquisarFunc.setText("Pesquisar");

        btnNovoFunc.setText("Novo funcionário");

        tblFuncionario.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Nome", "CPF", "Cargo", "Salário", "Login"
            }
        ));
        jScrollPane9.setViewportView(tblFuncionario);

        lblNomeFunc.setText("Nome");

        lblCpfFunc.setText("CPF");

        lblCargoFunc.setText("Cargo");

        lblSalarioFunc.setText("Salário");

        lblLoginFunc.setText("Login");

        lblSenhaFunc.setText("Senha");

        btnSalvarFunc.setText("Salvar");

        btnExcluirFunc.setText("Excluir");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel8Layout.createSequentialGroup()
                    .addGap(18, 18, 18)
                    .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel8Layout.createSequentialGroup()
                            .addComponent(lblNomeFunc, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(txtNomeFunc, javax.swing.GroupLayout.PREFERRED_SIZE, 454, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel8Layout.createSequentialGroup()
                            .addComponent(lblCpfFunc, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(txtCpfFunc, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(10, 10, 10)
                            .addComponent(lblCargoFunc, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(txtCargoFunc, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel8Layout.createSequentialGroup()
                            .addComponent(lblSalarioFunc, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(txtSalarioFunc, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(10, 10, 10)
                            .addComponent(lblLoginFunc, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(txtLoginFunc, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel8Layout.createSequentialGroup()
                            .addComponent(lblSenhaFunc, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(txtSenhaFunc, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel8Layout.createSequentialGroup()
                            .addComponent(btnExcluirFunc, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(btnSalvarFunc, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGap(30, 30, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel8Layout.createSequentialGroup()
                    .addGap(40, 40, 40)
                    .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblNomeFunc, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtNomeFunc, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblCpfFunc, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtCpfFunc, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblCargoFunc, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtCargoFunc, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblSalarioFunc, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtSalarioFunc, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblLoginFunc, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtLoginFunc, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblSenhaFunc, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtSenhaFunc, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(33, 33, 33)
                    .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnSalvarFunc, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnExcluirFunc, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(40, 40, Short.MAX_VALUE))
        );

        RbIdFunc.setText("ID");

        RbNomeFunc.setText("Nome");

        RbCpfFunc.setText("CPF");

        RbCargoFunc.setText("Cargo");

        RbSalarioFunc.setText("Salário");

        RbLoginFunc.setText("Login");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup()
                    .addGap(40, 40, 40)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtPesquisaFunc, javax.swing.GroupLayout.PREFERRED_SIZE, 530, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(jPanel3Layout.createSequentialGroup()
                                    .addComponent(RbIdFunc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(10, 10, 10)
                                    .addComponent(RbNomeFunc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(10, 10, 10)
                                    .addComponent(RbCpfFunc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(10, 10, 10)
                                    .addComponent(RbCargoFunc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(10, 10, 10)
                                    .addComponent(RbSalarioFunc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(10, 10, 10)
                                    .addComponent(RbLoginFunc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGap(18, 18, 18)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(btnPesquisarFunc, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnNovoFunc, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGap(40, 40, Short.MAX_VALUE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(30, 30, 30))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup()
                    .addGap(27, 27, 27)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(txtPesquisaFunc, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnPesquisarFunc, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(18, 18, 18)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(RbIdFunc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(RbNomeFunc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(RbCpfFunc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(RbCargoFunc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(RbSalarioFunc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(RbLoginFunc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnNovoFunc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(24, 24, 24)
                            .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 399, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(40, 40, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Funcionarios", jPanel3);

        getContentPane().add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtHoraConsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtHoraConsActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtHoraConsActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new Form_admin().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> CbFuncionarioMed;
    private javax.swing.JComboBox<String> CbMedicoCons;
    private javax.swing.JComboBox<String> CbPascienteCOns;
    private javax.swing.JRadioButton RbCargoFunc;
    private javax.swing.JRadioButton RbCpfFunc;
    private javax.swing.JRadioButton RbCpfPac;
    private javax.swing.JRadioButton RbCpfUsu;
    private javax.swing.JRadioButton RbCrmMed;
    private javax.swing.JRadioButton RbDataCons;
    private javax.swing.JRadioButton RbEmailMed;
    private javax.swing.JRadioButton RbEmailPac;
    private javax.swing.JRadioButton RbEspecialidadeMed;
    private javax.swing.JRadioButton RbHorarioCons;
    private javax.swing.JRadioButton RbIdFunc;
    private javax.swing.JRadioButton RbIdMed;
    private javax.swing.JRadioButton RbIdPac;
    private javax.swing.JRadioButton RbIdUsu;
    private javax.swing.JRadioButton RbLoginFunc;
    private javax.swing.JRadioButton RbLoginUsu;
    private javax.swing.JRadioButton RbMedicoCons;
    private javax.swing.JRadioButton RbNascimentoPac;
    private javax.swing.JRadioButton RbNomeFunc;
    private javax.swing.JRadioButton RbNomeMed;
    private javax.swing.JRadioButton RbNomePac;
    private javax.swing.JRadioButton RbNomeUsu;
    private javax.swing.JRadioButton RbPascienteCons;
    private javax.swing.JRadioButton RbPerfilUsu;
    private javax.swing.JRadioButton RbSalaCons;
    private javax.swing.JRadioButton RbSalarioFunc;
    private javax.swing.JRadioButton RbSanguePac;
    private javax.swing.JRadioButton RbSexoPac;
    private javax.swing.JRadioButton RbStatusCons;
    private javax.swing.JRadioButton RbTelefoneMed;
    private javax.swing.JRadioButton RbTelefonePac;
    private javax.swing.JRadioButton Rbidons;
    private javax.swing.JTextField TxtPesquisaCons;
    private javax.swing.JButton btnExcluirCons;
    private javax.swing.JButton btnExcluirFunc;
    private javax.swing.JButton btnExcluirMed;
    private javax.swing.JButton btnExcluirPac;
    private javax.swing.JButton btnExcluirUsu;
    private javax.swing.JButton btnNovaCons;
    private javax.swing.JButton btnNovoFunc;
    private javax.swing.JButton btnNovoMed;
    private javax.swing.JButton btnNovoPac;
    private javax.swing.JButton btnNovoUsu;
    private javax.swing.JButton btnPesquiCons;
    private javax.swing.JButton btnPesquisarFunc;
    private javax.swing.JButton btnPesquisarMed;
    private javax.swing.JButton btnPesquisarPac;
    private javax.swing.JButton btnPesquisarUsu;
    private javax.swing.JButton btnSalvarCons;
    private javax.swing.JButton btnSalvarFunc;
    private javax.swing.JButton btnSalvarMed;
    private javax.swing.JButton btnSalvarPac;
    private javax.swing.JButton btnSalvarUsu;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel lblCargoFunc;
    private javax.swing.JLabel lblCpfFunc;
    private javax.swing.JLabel lblCpfPac;
    private javax.swing.JLabel lblCpfUsu;
    private javax.swing.JLabel lblCrmMed;
    private javax.swing.JLabel lblEmailMed;
    private javax.swing.JLabel lblEmailPac;
    private javax.swing.JLabel lblEnderecoPac;
    private javax.swing.JLabel lblEspecialidadeMed;
    private javax.swing.JLabel lblFuncionarioMed;
    private javax.swing.JLabel lblLoginFunc;
    private javax.swing.JLabel lblLoginUsu;
    private javax.swing.JLabel lblNascimentoPac;
    private javax.swing.JLabel lblNomeFunc;
    private javax.swing.JLabel lblNomePac;
    private javax.swing.JLabel lblNomeUsu;
    private javax.swing.JLabel lblPerfilUsu;
    private javax.swing.JLabel lblSalarioFunc;
    private javax.swing.JLabel lblSanguePac;
    private javax.swing.JLabel lblSenhaFunc;
    private javax.swing.JLabel lblSenhaUsu;
    private javax.swing.JLabel lblSexoPac;
    private javax.swing.JLabel lblTelefoneMed;
    private javax.swing.JLabel lblTelefonePac;
    private javax.swing.JTable tblConsulta;
    private javax.swing.JTable tblFuncionario;
    private javax.swing.JTable tblMedico;
    private javax.swing.JTable tblPaciente;
    private javax.swing.JTable tblUsuario;
    private javax.swing.JTextField txtCargoFunc;
    private javax.swing.JTextField txtCpfFunc;
    private javax.swing.JTextField txtCpfPac;
    private javax.swing.JTextField txtCpfUsu;
    private javax.swing.JTextField txtCrmMed;
    private javax.swing.JTextField txtDatacons;
    private javax.swing.JTextField txtEmailMed;
    private javax.swing.JTextField txtEmailPac;
    private javax.swing.JTextField txtEnderecoPac;
    private javax.swing.JTextField txtEspecialidadeMed;
    private javax.swing.JTextField txtHoraCons;
    private javax.swing.JTextField txtLoginFunc;
    private javax.swing.JTextField txtLoginUsu;
    private javax.swing.JTextField txtNascimentoPac;
    private javax.swing.JTextField txtNomeFunc;
    private javax.swing.JTextField txtNomePac;
    private javax.swing.JTextField txtNomeUsu;
    private javax.swing.JTextField txtPerfilUsu;
    private javax.swing.JTextField txtPesquisaFunc;
    private javax.swing.JTextField txtPesquisaMed;
    private javax.swing.JTextField txtPesquisaPac;
    private javax.swing.JTextField txtPesquisaUsu;
    private javax.swing.JTextField txtSalaCons;
    private javax.swing.JTextField txtSalarioFunc;
    private javax.swing.JTextField txtSanguePac;
    private javax.swing.JPasswordField txtSenhaFunc;
    private javax.swing.JPasswordField txtSenhaUsu;
    private javax.swing.JTextField txtSexoPac;
    private javax.swing.JTextField txtStatusCons;
    private javax.swing.JTextField txtTelefoneMed;
    private javax.swing.JTextField txtTelefonePac;
    // End of variables declaration//GEN-END:variables
}
