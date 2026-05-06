import java.util.Date;

public class SolicitacaoMudanca {
    private int id;
    private String justificativa;
    private StatusSolicitacao status;
    private Date dataCriacao;

    public SolicitacaoMudanca(int id, String justificativa, StatusSolicitacao status, Date dataCriacao) {
        this.id = id;
        this.justificativa = justificativa;
        this.status = status;
        this.dataCriacao = dataCriacao;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getJustificativa() { return justificativa; }
    public void setJustificativa(String justificativa) { this.justificativa = justificativa; }
    public StatusSolicitacao getStatus() { return status; }
    public void setStatus(StatusSolicitacao status) { this.status = status; }
    public Date getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(Date dataCriacao) { this.dataCriacao = dataCriacao; }
}