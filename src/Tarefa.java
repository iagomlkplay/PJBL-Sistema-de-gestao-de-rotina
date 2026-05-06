import java.util.Date;

public class Tarefa {
    private int id;
    private String descricao;
    private Date prazo;
    private NivelImportancia nivelImportancia;
    private StatusTarefa status;
    private UsuarioDev devResponsavel; // referência ao dev responsável

    public Tarefa(int id, String descricao, Date prazo, NivelImportancia nivelImportancia, StatusTarefa status, UsuarioDev devResponsavel) {
        this.id = id;
        this.descricao = descricao;
        this.prazo = prazo;
        this.nivelImportancia = nivelImportancia;
        this.status = status;
        this.devResponsavel = devResponsavel;
    }

    public String getInformacoesDetalhadas() {
        return String.format("Tarefa [ID=%d, Descrição=%s, Prazo=%s, Importância=%s, Status=%s, Responsável=%s]",
                id, descricao, prazo, nivelImportancia, status, (devResponsavel != null ? devResponsavel.getNome() : "Nenhum"));
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
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