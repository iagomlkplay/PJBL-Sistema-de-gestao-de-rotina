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
    private UsuarioDev devResponsavel;
    private List<Tarefa> tarefas;

    public Projeto(String nome, Date prazo, NivelImportancia importancia, UsuarioDev devResponsavel) {
        this.id = ++ultimoId;
        this.nome = nome;
        this.prazo = prazo;
        this.importancia = importancia;
        this.status = StatusTarefa.PENDENTE;
        this.devResponsavel = devResponsavel;
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

    public String getInformacoesDetalhadas() {
        return String.format("Projeto [ID=%d, Nome=%s, Prazo=%s, Importância=%s, Status=%s, Tarefas=%d]",
                id, nome, prazo, importancia, status, tarefas.size());
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
    public UsuarioDev getDevResponsavel() { return devResponsavel; }
    public void setDevResponsavel(UsuarioDev devResponsavel) { this.devResponsavel = devResponsavel; }
    public List<Tarefa> getTarefas() { return tarefas; }
    public void setTarefas(List<Tarefa> tarefas) { this.tarefas = tarefas; }
}