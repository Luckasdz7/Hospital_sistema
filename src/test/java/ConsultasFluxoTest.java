import BO.BOConsultas;
import DAO.DAOconsultas;
import FORMS.Form_admin;
import MODEL.*;
import java.lang.reflect.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import javax.swing.*;

/** Teste independente de banco: execute o main com target/classes no classpath. */
public class ConsultasFluxoTest {
    static int verificacoes;
    static void check(boolean ok, String message) {
        verificacoes++;
        if (!ok) throw new AssertionError(message);
    }
    static Object campo(Object alvo, String nome) throws Exception {
        Field f = alvo.getClass().getDeclaredField(nome);
        f.setAccessible(true);
        return f.get(alvo);
    }
    static void definir(Object alvo, String nome, Object valor) throws Exception {
        Field f = alvo.getClass().getDeclaredField(nome);
        f.setAccessible(true); f.set(alvo, valor);
    }
    static void chamar(Object alvo, String nome) throws Exception {
        Method m = alvo.getClass().getDeclaredMethod(nome);
        m.setAccessible(true); m.invoke(alvo);
    }
    static consultas exemplo() {
        pacientes p = new pacientes(); p.setId_paciente(7); p.setNome("Ana"); p.setCpf("123.456.789-00");
        medicos m = new medicos(); m.setId_medico(9); m.setNome("Médico"); m.setCrm("CRM123");
        return new consultas(42, p, m, java.sql.Date.valueOf("2026-11-15"), LocalTime.of(9,30), "02", enum_consultas.Agendada);
    }
    static class BancoSimulado extends DAOconsultas {
        String sql;
        Map<Integer,Object> parametros = new HashMap<>();
        int afetados = 1;
        boolean falhar;
        int fechadas;
        boolean retornarLinha;
        @SuppressWarnings("unchecked")
        static <T> T proxy(Class<T> tipo, InvocationHandler h) {
            return (T) Proxy.newProxyInstance(tipo.getClassLoader(), new Class<?>[]{tipo}, h);
        }
        static Object padrao(Class<?> tipo) {
            if (tipo == boolean.class) return false;
            if (tipo == int.class) return 0;
            if (tipo == long.class) return 0L;
            return null;
        }
        @Override protected Connection conectar() {
            return proxy(Connection.class, (obj,m,args) -> {
                if (m.getName().equals("close")) { fechadas++; return null; }
                if (m.getName().equals("prepareStatement")) {
                    sql = (String) args[0]; parametros.clear();
                    return proxy(PreparedStatement.class, (o,method,a) -> {
                        if (method.getName().startsWith("set")) { parametros.put((Integer)a[0], a[1]); return null; }
                        if (method.getName().equals("executeUpdate")) {
                            if (falhar) throw new SQLException("falha simulada");
                            return afetados;
                        }
                        if (method.getName().equals("executeQuery")) {
                            final boolean[] primeira = {retornarLinha};
                            return proxy(ResultSet.class, (r,rm,ra) -> {
                                switch (rm.getName()) {
                                    case "next": boolean v=primeira[0]; primeira[0]=false; return v;
                                    case "getInt": return switch ((String)ra[0]) { case "id_consulta" -> 42; case "id_paciente" -> 7; default -> 9; };
                                    case "getDate": return java.sql.Date.valueOf("2026-11-15");
                                    case "getTime": return Time.valueOf("09:30:00");
                                    case "getString": return switch ((String)ra[0]) {
                                        case "status" -> "Agendada"; case "sala" -> "02";
                                        case "paciente_nome" -> "Ana"; case "medico_nome" -> "Médico";
                                        default -> "123";
                                    };
                                    default: return padrao(rm.getReturnType());
                                }
                            });
                        }
                        return padrao(method.getReturnType());
                    });
                }
                return padrao(m.getReturnType());
            });
        }
    }
    public static void main(String[] args) throws Exception {
        check(BOConsultas.lerData("15/11/2026").equals(LocalDate.of(2026,11,15)), "Data brasileira");
        check(BOConsultas.lerData("2026-11-15").equals(LocalDate.of(2026,11,15)), "Data ISO");
        for (String invalida : new String[]{"31/02/2026", "29/02/2025", "texto"}) {
            try { BOConsultas.lerData(invalida); throw new AssertionError("Data inválida aceita"); }
            catch (IllegalArgumentException esperado) { verificacoes++; }
        }
        check(BOConsultas.lerHora("09:30").equals(LocalTime.of(9,30)), "Horário");
        try { BOConsultas.lerHora("25:00"); throw new AssertionError(); }
        catch (IllegalArgumentException esperado) { verificacoes++; }
        check(BOConsultas.lerStatus(" realizada ")==enum_consultas.Realizada, "Status normalizado");
        BancoSimulado dao = new BancoSimulado();
        String[][] buscas = {{"id","42"}, {"paciente","Ana"}, {"medico","CRM123"},
            {"data","2026-11-15"}, {"horario","09:30"}, {"sala","02"}, {"status","Agendada"}};
        for (String[] busca : buscas) {
            dao.pesquisar(busca[0],busca[1]);
            check(dao.sql.contains("ORDER BY c.data_consulta DESC, c.hora_consulta DESC, c.id_consulta DESC"), "Ordem " + busca[0]);
            check(dao.parametros.size()==(busca[0].equals("paciente")?3:busca[0].equals("medico")?4:1), "Parâmetros " + busca[0]);
        }
        dao.pesquisar("paciente", "123.456.789-00");
        check(dao.parametros.get(3).equals("%12345678900%"), "CPF sem pontuação");
        dao.pesquisar("medico", "123.456.789-00");
        check(dao.sql.contains("m.crm LIKE") && dao.sql.contains("f.cpf LIKE"), "CRM e CPF médico");
        check(dao.parametros.get(4).equals("%12345678900%"), "CPF médico normalizado");
        dao.pesquisar("paciente", "Ana%' OR 1=1 --");
        check(!dao.sql.contains("OR 1=1") && dao.parametros.get(1).toString().contains("!%"), "Pesquisa parametrizada e literal");
        dao.pesquisar("id", "");
        check(!dao.sql.contains(" WHERE ") && dao.parametros.isEmpty(), "Busca vazia lista todas");
        dao.retornarLinha=true;
        consultas c=dao.buscarporid(42);
        check(c.getId_consulta()==42 && c.getPaciente().getId_paciente()==7 && c.getMedico().getId_medico()==9, "Mapeamento preserva IDs");
        dao.Salvar(exemplo());
        check(dao.sql.startsWith("INSERT") && dao.parametros.size()==6, "Cadastro");
        dao.Atualizar(exemplo());
        check(dao.sql.startsWith("UPDATE") && dao.parametros.get(7).equals(42), "Atualização por ID");
        dao.excluir(42);
        check(dao.sql.startsWith("DELETE") && dao.parametros.get(1).equals(42), "Exclusão por ID");
        dao.afetados=0;
        try { dao.Atualizar(exemplo()); throw new AssertionError(); }
        catch (IllegalStateException esperado) { verificacoes++; }
        dao.falhar=true;
        try { dao.Salvar(exemplo()); throw new AssertionError(); }
        catch (IllegalStateException esperado) { verificacoes++; }
        check(dao.fechadas>=17,"Conexões fechadas inclusive em erro");
        SwingUtilities.invokeAndWait(() -> {
            Form_admin form = null;
            try {
                form=new Form_admin();
                form.pack(); // Detecta componentes ausentes de um eixo do GroupLayout.
                ButtonGroup grupo=(ButtonGroup)campo(form,"grupoPesquisaCons");
                check(grupo.getButtonCount()==7,"Sete filtros");
                JRadioButton paciente=(JRadioButton)campo(form,"RbPascienteCons");
                JRadioButton medico=(JRadioButton)campo(form,"RbMedicoCons");
                paciente.setSelected(true); medico.setSelected(true);
                check(!paciente.isSelected() && medico.isSelected(),"Seleção exclusiva");
                JButton excluir=(JButton)campo(form,"btnExcluirCons");
                check(!excluir.isEnabled() && campo(form,"consultaSelecionadaId")==null,"Estado de cadastro");
                JTable tabela=(JTable)campo(form,"tblConsulta");
                check(!tabela.isCellEditable(0,0),"Tabela não editável");
                // Adapter: a tabela recebe a lista de consultas direto.
                ((ADAPTER.ConsultaTableModel)tabela.getModel()).setDados(List.of(exemplo()));
                check(tabela.getValueAt(0,3).equals("15/11/2026") && tabela.getValueAt(0,1).equals("Ana"),"Adapter mostra a consulta");
                tabela.setRowSelectionInterval(0,0);
                check(campo(form,"consultaSelecionadaId").equals(42) && excluir.isEnabled(),"Selecionar para editar");
                check(((JTextField)campo(form,"txtDatacons")).getText().equals("15/11/2026"),"Data preenchida");
                check(((JTextField)campo(form,"txtHoraCons")).getText().equals("09:30"),"Hora preenchida");
                Object sugestoes=campo(form,"sugestoesPacientes");
                check(((pacientes)campo(sugestoes,"selecionado")).getId_paciente()==7,"Paciente preservado");
                ((JTextField)campo(sugestoes,"editor")).setText("Outro nome");
                check(campo(sugestoes,"selecionado")==null,"Editar texto invalida paciente antigo");
                ((JButton)campo(form,"btnNovaCons")).doClick();
                check(campo(form,"consultaSelecionadaId")==null && tabela.getSelectedRow()==-1 && !excluir.isEnabled(),"Nova consulta limpa seleção");
                check(((JTextField)campo(form,"txtSalaCons")).getText().isEmpty(),"Campos limpos");
            } catch(Exception e) { throw new RuntimeException(e); }
            finally { if(form!=null) form.dispose(); }
        });
        System.out.println("OK: " + verificacoes + " verificações (SQL simulado e Swing, sem alterar banco).");
    }
}
