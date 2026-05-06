import java.util.Date;

public class Relatorio {
    private int id;
    private Date dataEnvio;
    private String conteudo;

    public Relatorio(int id, Date dataEnvio, String conteudo) {
        this.id = id;
        this.dataEnvio = dataEnvio;
        this.conteudo = conteudo;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Date getDataEnvio() { return dataEnvio; }
    public void setDataEnvio(Date dataEnvio) { this.dataEnvio = dataEnvio; }
    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }
}