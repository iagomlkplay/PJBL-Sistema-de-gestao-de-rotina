import java.util.Date;

public class Relatorio {
    private static int ultimoId = 0;
    private int id;
    private Date dataEnvio;
    private String conteudo;
    private Tarefa tarefaRelacionada;
    private Projeto projetoRelacionado;

    public Relatorio(String conteudo) {
        this.id = ++ultimoId;
        this.conteudo = conteudo;
        this.dataEnvio = new Date();
    }

    // Getters e Setters
    public int getId() { return id; }
    public Date getDataEnvio() { return dataEnvio; }
    public void setDataEnvio(Date dataEnvio) { this.dataEnvio = dataEnvio; }
    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }
    public Tarefa getTarefaRelacionada() { return tarefaRelacionada; }
    public void setTarefaRelacionada(Tarefa tarefaRelacionada) { this.tarefaRelacionada = tarefaRelacionada; }
    public Projeto getProjetoRelacionado() { return projetoRelacionado; }
    public void setProjetoRelacionado(Projeto projetoRelacionado) { this.projetoRelacionado = projetoRelacionado; }
}