package ADAPTER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * PADRÃO ESTRUTURAL: Adapter.
 *
 * A JTable só sabe trabalhar com a interface TableModel (linhas e colunas de
 * Object). As nossas telas trabalham com List de objetos do MODEL
 * (consultas, pacientes, medicos...). Esta classe ADAPTA uma List<T> para a
 * interface TableModel: a tela faz só {@code modelo.setDados(lista)} e pega o
 * objeto da linha clicada com {@code modelo.getItem(linha)}, sem montar
 * Object[] na mão nem manter uma lista paralela.
 *
 * Cada subclasse diz só quais são as colunas e o valor de cada coluna.
 *
 * @author Lucas
 */
public abstract class TabelaAdapter<T> extends AbstractTableModel {

    private final String[] colunas;
    private List<T> dados = new ArrayList<>();

    protected TabelaAdapter(String... colunas) {
        this.colunas = colunas;
    }

    /** Valor da coluna {@code coluna} para o objeto {@code item}. */
    protected abstract Object valor(T item, int coluna);

    public void setDados(List<T> novos) {
        dados = novos == null ? new ArrayList<>() : new ArrayList<>(novos);
        fireTableDataChanged();
    }

    public List<T> getDados() {
        return Collections.unmodifiableList(dados);
    }

    /** Objeto da linha (índice do modelo; na tela use convertRowIndexToModel). */
    public T getItem(int linha) {
        return dados.get(linha);
    }

    /** Avisa a tabela que o objeto mudou (ex.: médico da consulta trocado). */
    public void atualizarItem(T item) {
        int linha = dados.indexOf(item);
        if (linha >= 0) fireTableRowsUpdated(linha, linha);
    }

    @Override public int getRowCount() { return dados.size(); }
    @Override public int getColumnCount() { return colunas.length; }
    @Override public String getColumnName(int coluna) { return colunas[coluna]; }
    @Override public boolean isCellEditable(int linha, int coluna) { return false; }

    @Override
    public Object getValueAt(int linha, int coluna) {
        Object v = valor(dados.get(linha), coluna);
        return v == null ? "" : v;
    }

    // Formatação comum das telas.
    protected static final java.time.format.DateTimeFormatter DATA =
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/uuuu");

    protected static String data(java.util.Date d) {
        return d == null ? "" : DATA.format(new java.sql.Date(d.getTime()).toLocalDate());
    }
}
