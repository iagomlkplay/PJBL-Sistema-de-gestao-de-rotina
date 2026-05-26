import java.util.Date;

public class Solicitacao {
    private int id;
    private String justificativa;
    private StatusSolicitacao status;
    private Date dataCriacao;
    private UsuarioDev solicitante;
    private Tarefa tarefaRelacionada;  // nova referência

    public Solicitacao(String justificativa, UsuarioDev solicitante, Tarefa tarefaRelacionada) {
        this.justificativa = justificativa;
        this.status = StatusSolicitacao.PENDENTE;
        this.dataCriacao = new Date();
        this.solicitante = solicitante;
        this.tarefaRelacionada = tarefaRelacionada;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getJustificativa() { return justificativa; }

    public StatusSolicitacao getStatus() { return status; }
    public void setStatus(StatusSolicitacao status) { this.status = status; }

    public Date getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(Date dataCriacao) { this.dataCriacao = dataCriacao; }

    public UsuarioDev getSolicitante() { return solicitante; }

    public Tarefa getTarefaRelacionada() { return tarefaRelacionada; }
}