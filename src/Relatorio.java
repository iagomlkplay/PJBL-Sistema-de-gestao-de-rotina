import java.util.Date;

public class Relatorio {
    private int id;
    private Date dataEnvio;
    private String conteudo;
    private Tarefa tarefaRelacionada;
    private Projeto projetoRelacionado;
    private UsuarioDev devRemetente;

    public Relatorio(String conteudo, UsuarioDev devRemetente) {
        this.conteudo = conteudo;
        this.dataEnvio = new Date();
        this.devRemetente = devRemetente;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Date getDataEnvio() { return dataEnvio; }
    public void setDataEnvio(Date dataEnvio) { this.dataEnvio = dataEnvio; }
    public String getConteudo() { return conteudo; }

    // O conteúdo é definido no construtor (new Relatorio(conteudo)) e nunca reatribuído. Então não tem um motivo para o setConteudo() realmente existir.
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }

    public Tarefa getTarefaRelacionada() { return tarefaRelacionada; }
    public void setTarefaRelacionada(Tarefa tarefaRelacionada) { this.tarefaRelacionada = tarefaRelacionada; }
    public Projeto getProjetoRelacionado() { return projetoRelacionado; }
    public void setProjetoRelacionado(Projeto projetoRelacionado) { this.projetoRelacionado = projetoRelacionado; }
    public UsuarioDev getDevRemetente() { return devRemetente; }
    public void setDevRemetente(UsuarioDev devRemetente) { this.devRemetente = devRemetente; }
}