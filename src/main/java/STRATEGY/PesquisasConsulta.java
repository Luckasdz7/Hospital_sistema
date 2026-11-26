package STRATEGY;

import java.util.LinkedHashMap;
import java.util.Map;

/** Registro das estratégias de pesquisa de consultas, por nome do filtro. */
public final class PesquisasConsulta {

    private static final Map<String, PesquisaConsulta> ESTRATEGIAS = new LinkedHashMap<>();
    static {
        for (PesquisaConsulta e : new PesquisaConsulta[]{
                new PesquisaConsultaPorId(), new PesquisaConsultaPorPaciente(), new PesquisaConsultaPorMedico(),
                new PesquisaConsultaPorData(), new PesquisaConsultaPorHorario(), new PesquisaConsultaPorSala(),
                new PesquisaConsultaPorStatus()}) {
            ESTRATEGIAS.put(e.getCampo(), e);
        }
    }

    private PesquisasConsulta() {
    }

    public static PesquisaConsulta porCampo(String campo) {
        PesquisaConsulta e = campo == null ? null : ESTRATEGIAS.get(campo);
        if (e == null) throw new IllegalArgumentException("Selecione um tipo de pesquisa válido.");
        return e;
    }
}
