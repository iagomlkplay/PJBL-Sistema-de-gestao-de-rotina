import java.util.Date;

public class SolicitacaoMudanca {
    private static int ultimoId = 0;
    private int id;
    private String justificativa;
    private StatusSolicitacao status;
    private Date dataCriacao;
    private UsuarioDev solicitante;

    public SolicitacaoMudanca(String justificativa, UsuarioDev solicitante) {
        this.id = ++ultimoId;
        this.justificativa = justificativa;
        this.status = StatusSolicitacao.PENDENTE;
        this.dataCriacao = new Date();
        this.solicitante = solicitante;
    }

    // Getters e Setters
    public int getId() { return id; }
    public String getJustificativa() { return justificativa; }
    public void setJustificativa(String justificativa) { this.justificativa = justificativa; }
    public StatusSolicitacao getStatus() { return status; }
    public void setStatus(StatusSolicitacao status) { this.status = status; }
    public Date getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(Date dataCriacao) { this.dataCriacao = dataCriacao; }
    public UsuarioDev getSolicitante() { return solicitante; }
    public void setSolicitante(UsuarioDev solicitante) { this.solicitante = solicitante; }
}