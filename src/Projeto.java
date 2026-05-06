import java.util.Date;
import java.util.ArrayList;
import java.util.List;

public class Projeto {
    private static int ultimoId = 0;
    private int id;
    private String nome;
    private Date prazo;
    private NivelImportancia importancia;
    private StatusTarefa status;
    private List<Tarefa> tarefas;

    public Projeto(String nome, Date prazo, NivelImportancia importancia) {
        this.id = ++ultimoId;
        this.nome = nome;
        this.prazo = prazo;
        this.importancia = importancia;
        this.status = StatusTarefa.PENDENTE;
        this.tarefas = new ArrayList<>();
    }

    public double calcularProgresso() {
        if (tarefas.isEmpty()) {
            return (status == StatusTarefa.PRONTO || status == StatusTarefa.FEITO) ? 100.0 : 0.0;
        }
        double soma = 0;
        for (Tarefa t : tarefas) {
            soma += t.calcularProgresso();
        }
        return soma / tarefas.size();
    }

    public double getTotalHorasTrabalhadas() {
        double total = 0;
        for (Tarefa t : tarefas) {
            total += t.getHorasTrabalhadas();
        }
        return total;
    }

    public double getTotalHorasEstimadas() {
        double total = 0;
        for (Tarefa t : tarefas) {
            total += t.getHorasEstimadas();
        }
        return total;
    }

    public String getInformacoesDetalhadas() {
        return String.format("Projeto [ID=%d, Nome=%s, Prazo=%s, Importância=%s, Status=%s, Progresso=%.1f%%, Tarefas=%d, Horas: %.1f/%.1f]",
                id, nome, prazo, importancia, status, calcularProgresso(), tarefas.size(), getTotalHorasTrabalhadas(), getTotalHorasEstimadas());
    }

    public void verificarConclusao() {
        if (status == StatusTarefa.PRONTO) return;
        boolean todasProntas = true;
        for (Tarefa t : tarefas) {
            if (t.getStatus() != StatusTarefa.PRONTO) {
                todasProntas = false;
                break;
            }
        }
        if (todasProntas && !tarefas.isEmpty()) {
            this.status = StatusTarefa.FEITO;
            System.out.println("Projeto " + id + " (" + nome + ") concluiu todas as tarefas. Status agora: FEITO. Gestor pode validar para PRONTO.");
        }
    }

    // Getters e Setters
    public int getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public Date getPrazo() { return prazo; }
    public void setPrazo(Date prazo) { this.prazo = prazo; }
    public NivelImportancia getImportancia() { return importancia; }
    public void setImportancia(NivelImportancia importancia) { this.importancia = importancia; }
    public StatusTarefa getStatus() { return status; }
    public void setStatus(StatusTarefa status) { this.status = status; }
    public List<Tarefa> getTarefas() { return tarefas; }
    public void setTarefas(List<Tarefa> tarefas) { this.tarefas = tarefas; }
}