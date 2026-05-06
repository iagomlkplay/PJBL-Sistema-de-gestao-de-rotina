import java.util.Date;
import java.util.ArrayList;
import java.util.List;

public class Projeto {
    private int id;
    private String nome;
    private Date prazo;
    private NivelImportancia importancia;
    private StatusTarefa status;
    private UsuarioDev devResponsavel;
    private List<Tarefa> tarefas;

    public Projeto(int id, String nome, Date prazo, NivelImportancia importancia, StatusTarefa status, UsuarioDev devResponsavel) {
        this.id = id;
        this.nome = nome;
        this.prazo = prazo;
        this.importancia = importancia;
        this.status = status;
        this.devResponsavel = devResponsavel;
        this.tarefas = new ArrayList<>();
    }

    // Getters e Setters
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
    public UsuarioDev getDevResponsavel() { return devResponsavel; }
    public void setDevResponsavel(UsuarioDev devResponsavel) { this.devResponsavel = devResponsavel; }
    public List<Tarefa> getTarefas() { return tarefas; }
    public void setTarefas(List<Tarefa> tarefas) { this.tarefas = tarefas; }
}