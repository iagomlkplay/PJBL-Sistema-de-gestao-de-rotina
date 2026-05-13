import java.util.Date;
import java.util.List;

public class Projeto {
    private int id;
    private String nome;
    private Date prazo;
    private NivelImportancia importancia;
    private StatusTarefa status;

    // Construtor para NOVOS projetos (sem id)
    public Projeto(String nome, Date prazo, NivelImportancia importancia) {
        this.nome = nome;
        this.prazo = prazo;
        this.importancia = importancia;
        this.status = StatusTarefa.PENDENTE;
    }

    // Construtor para leitura do banco (com id e status)
    public Projeto(int id, String nome, Date prazo, NivelImportancia importancia, StatusTarefa status) {
        this.id = id;
        this.nome = nome;
        this.prazo = prazo;
        this.importancia = importancia;
        this.status = status;
    }

    // Recebe a lista de tarefas do projeto para calcular o progresso
    public double calcularProgresso(List<Tarefa> tarefasDoProjeto) {
        if (tarefasDoProjeto == null || tarefasDoProjeto.isEmpty()) {
            return (status == StatusTarefa.PRONTO || status == StatusTarefa.FEITO) ? 100.0 : 0.0;
        }
        double soma = 0;
        for (Tarefa t : tarefasDoProjeto) {
            soma += t.calcularProgresso();
        }
        return soma / tarefasDoProjeto.size();
    }

    // Total de horas - também deve ser obtido via consulta externa
    public double getTotalHorasTrabalhadas(List<Tarefa> tarefasDoProjeto) {
        double total = 0;
        for (Tarefa t : tarefasDoProjeto) total += t.getHorasTrabalhadas();
        return total;
    }

    public double getTotalHorasEstimadas(List<Tarefa> tarefasDoProjeto) {
        double total = 0;
        for (Tarefa t : tarefasDoProjeto) total += t.getHorasEstimadas();
        return total;
    }

    // Versão simplificada - não exibe progresso nem horas
    public String getInformacoesDetalhadas() {
        return String.format("Projeto [ID=%d, Nome=%s, Prazo=%s, Importância=%s, Status=%s]",
                id, nome, prazo, importancia, status);
    }

    // Método mais completo, que recebe a lista de tarefas
    public String getInformacoesDetalhadas(List<Tarefa> tarefasDoProjeto) {
        return String.format("Projeto [ID=%d, Nome=%s, Prazo=%s, Importância=%s, Status=%s, Progresso=%.1f%%, Tarefas=%d, Horas: %.1f/%.1f]",
                id, nome, prazo, importancia, status, calcularProgresso(tarefasDoProjeto),
                tarefasDoProjeto.size(), getTotalHorasTrabalhadas(tarefasDoProjeto), getTotalHorasEstimadas(tarefasDoProjeto));
    }

    // Verifica se todas as tarefas estão PRONTO e atualiza o status do projeto para FEITO
    // Requer acesso ao DAO para persistir a mudança
    public void verificarConclusao(List<Tarefa> tarefasDoProjeto, TarefaDAO tarefaDAO, ProjetoDAO projetoDAO) {
        if (this.status == StatusTarefa.PRONTO || this.status == StatusTarefa.FEITO) return;
        boolean todasPronto = true;
        for (Tarefa t : tarefasDoProjeto) {
            if (t.getStatus() != StatusTarefa.PRONTO) {
                todasPronto = false;
                break;
            }
        }
        if (todasPronto && !tarefasDoProjeto.isEmpty()) {
            this.status = StatusTarefa.FEITO;
            try {
                projetoDAO.atualizarStatus(this.id, StatusTarefa.FEITO);
                System.out.println("Projeto " + this.id + " concluído (todas tarefas PRONTO) - status alterado para FEITO.");
            } catch (Exception e) {
                System.err.println("Erro ao atualizar status do projeto: " + e.getMessage());
                this.status = StatusTarefa.PENDENTE; // rollback
            }
        }
    }

    // Getters e Setters (sem lista de tarefas)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public Date getPrazo() { return prazo; }
    public void setPrazo(Date prazo) { this.prazo = prazo; }
    public NivelImportancia getImportancia() { return importancia; }
    public void setImportancia(NivelImportancia importancia) { this.importancia = importancia; }
    public StatusTarefa getStatus() { return status; }
    public void setStatus(StatusTarefa status) { this.status = status; }
}