import java.util.Date;
import java.util.ArrayList;
import java.util.List;

public class Projeto {
    private int id;
    private String nome;
    private Date prazo;
    private NivelImportancia importancia;
    private StatusTarefa status;

    // Construtor para NOVOS projetos (sem id)
    public Projeto(String nome, Date prazo, NivelImportancia importancia) {
        this.nome = nome;
        this.prazo = prazo;
        this.importancia = importancia;
        this.status = StatusTarefa.PENDENTE;
    }

    // Construtor para leitura do banco (com id e status)
    public Projeto(int id, String nome, Date prazo, NivelImportancia importancia, StatusTarefa status) {
        this.id = id;
        this.nome = nome;
        this.prazo = prazo;
        this.importancia = importancia;
        this.status = status;
    }

    // O progresso depende de uma consulta ao banco
    public double calcularProgresso() {
        // Aqui precisaríamos consultar o banco para saber as tarefas do projeto.
        // Como não temos acesso ao DAO nesta classe, o melhor é que este método seja usado apenas em contextos onde as tarefas já estão carregadas ou que a consulta seja feita externamente.
        // Por simplicidade, manteremos a lógica que depende da lista, mas agora a lista deve ser passada por parâmetro ou este método deve ser substituído.
        // Vamos alterar a abordagem: o progresso não será calculado diretamente pelo Projeto, mas por um serviço externo.
        // Para compatibilidade, se a lista de tarefas não for fornecida, retornamos 0 (mas isso é frágil).
        // O ideal é remover este método e criar uma classe utilitária.
        System.out.println("Aviso: calcularProgresso() sem lista de tarefas retornará 0%. Use TarefaDAO para obter o progresso real.");
        return 0.0;
    }

    // Alternativa: receber a lista de tarefas como parâmetro
    public double calcularProgresso(List<Tarefa> tarefasDoProjeto) {
        if (tarefasDoProjeto == null || tarefasDoProjeto.isEmpty()) {
            return (status == StatusTarefa.PRONTO || status == StatusTarefa.FEITO) ? 100.0 : 0.0;
        }
        double soma = 0;
        for (Tarefa t : tarefasDoProjeto) {
            soma += t.calcularProgresso();
        }
        return soma / tarefasDoProjeto.size();
    }

    // Total de horas - também deve ser obtido via consulta externa
    public double getTotalHorasTrabalhadas(List<Tarefa> tarefasDoProjeto) {
        double total = 0;
        for (Tarefa t : tarefasDoProjeto) total += t.getHorasTrabalhadas();
        return total;
    }

    public double getTotalHorasEstimadas(List<Tarefa> tarefasDoProjeto) {
        double total = 0;
        for (Tarefa t : tarefasDoProjeto) total += t.getHorasEstimadas();
        return total;
    }

    public String getInformacoesDetalhadas() {
        // Para não quebrar a exibição, retornamos sem as horas (pois não temos a lista)
        // Em uso real, quem chamar este método deve também buscar as tarefas e calcular as horas.
        return String.format("Projeto [ID=%d, Nome=%s, Prazo=%s, Importância=%s, Status=%s]",
                id, nome, prazo, importancia, status);
    }

    // Método mais completo, que recebe a lista de tarefas
    public String getInformacoesDetalhadas(List<Tarefa> tarefasDoProjeto) {
        return String.format("Projeto [ID=%d, Nome=%s, Prazo=%s, Importância=%s, Status=%s, Progresso=%.1f%%, Tarefas=%d, Horas: %.1f/%.1f]",
                id, nome, prazo, importancia, status, calcularProgresso(tarefasDoProjeto),
                tarefasDoProjeto.size(), getTotalHorasTrabalhadas(tarefasDoProjeto), getTotalHorasEstimadas(tarefasDoProjeto));
    }

    public void verificarConclusao() {
        // Este método também depende de ter a lista de tarefas. Para evitar complexidade, mova a lógica para um serviço.
        System.out.println("Aviso: verificarConclusao() não deve ser usado diretamente. Use um serviço que consulte as tarefas do projeto no banco.");
    }

    // Getters e Setters (sem lista de tarefas)
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
}