import java.util.Date;

public class Tarefa {
    private static int ultimoId = 0;
    private int id;
    private String descricao;
    private Date prazo;
    private NivelImportancia nivelImportancia;
    private StatusTarefa status;
    private UsuarioDev devResponsavel;

    public Tarefa(String descricao, Date prazo, NivelImportancia nivelImportancia, UsuarioDev devResponsavel) {
        this.id = ++ultimoId;
        this.descricao = descricao;
        this.prazo = prazo;
        this.nivelImportancia = nivelImportancia;
        this.status = StatusTarefa.PENDENTE;
        this.devResponsavel = devResponsavel;
    }

    public double calcularProgresso() {
        if (status == StatusTarefa.PRONTO || status == StatusTarefa.FEITO) return 100.0;
        return 0.0;
    }

    public String getInformacoesDetalhadas() {
        return String.format("Tarefa [ID=%d, Descrição=%s, Prazo=%s, Importância=%s, Status=%s]",
                id, descricao, prazo, nivelImportancia, status);
    }

    // Getters e Setters
    public int getId() { return id; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public Date getPrazo() { return prazo; }
    public void setPrazo(Date prazo) { this.prazo = prazo; }
    public NivelImportancia getNivelImportancia() { return nivelImportancia; }
    public void setNivelImportancia(NivelImportancia nivelImportancia) { this.nivelImportancia = nivelImportancia; }
    public StatusTarefa getStatus() { return status; }
    public void setStatus(StatusTarefa status) { this.status = status; }
    public UsuarioDev getDevResponsavel() { return devResponsavel; }
    public void setDevResponsavel(UsuarioDev devResponsavel) { this.devResponsavel = devResponsavel; }
}