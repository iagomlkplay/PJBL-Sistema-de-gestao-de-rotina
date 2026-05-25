import java.util.Date;
import java.sql.SQLException;

public class Tarefa {
    private int id;
    private String descricao;
    private Date prazo;
    private NivelImportancia nivelImportancia;
    private StatusTarefa status;
    private UsuarioDev devResponsavel;
    private double horasEstimadas;
    private double horasTrabalhadas;
    private Projeto projetoPai;

    // Construtor para NOVAS tarefas (sem id, com horas estimadas)
    public Tarefa(String descricao, Date prazo, NivelImportancia nivelImportancia, UsuarioDev devResponsavel, double horasEstimadas) {
        this.descricao = descricao;
        this.prazo = prazo;
        this.nivelImportancia = nivelImportancia;
        this.status = StatusTarefa.PENDENTE;
        this.devResponsavel = devResponsavel;
        this.horasEstimadas = horasEstimadas;
        this.horasTrabalhadas = 0.0;
    }

    // Construtor para leitura do banco (com id)
    public Tarefa(int id, String descricao, Date prazo, NivelImportancia nivelImportancia, StatusTarefa status,
                  UsuarioDev devResponsavel, double horasEstimadas, double horasTrabalhadas, Projeto projetoPai) {
        this.id = id;
        this.descricao = descricao;
        this.prazo = prazo;
        this.nivelImportancia = nivelImportancia;
        this.status = status;
        this.devResponsavel = devResponsavel;
        this.horasEstimadas = horasEstimadas;
        this.horasTrabalhadas = horasTrabalhadas;
        this.projetoPai = projetoPai;
    }

    // Construtor sem horas especificadas (mantém compatibilidade)
    public Tarefa(String descricao, Date prazo, NivelImportancia nivelImportancia, UsuarioDev devResponsavel) {
        this(descricao, prazo, nivelImportancia, devResponsavel, 1.0);
    }

    // Método para calcular progresso
    public double calcularProgresso() {
        if (status == StatusTarefa.PRONTO || status == StatusTarefa.FEITO) {
            return 100.0;
        }
        if (horasEstimadas <= 0) return 0.0;
        double progresso = (horasTrabalhadas / horasEstimadas) * 100;
        return Math.min(progresso, 100.0);
    }

    // Método para adicionar horas trabalhadas
    public void adicionarHorasTrabalhadas(double horas) {
        if (horas <= 0) return;
        if (status == StatusTarefa.FEITO || status == StatusTarefa.PRONTO) {
            System.out.println("Tarefa " + id + " já está concluída. Não é possível adicionar mais horas.");
            return;
        }
        this.horasTrabalhadas += horas;
        System.out.println("Tarefa " + id + " agora tem " + horasTrabalhadas + "h trabalhadas de " + horasEstimadas + "h estimadas.");
        // Persistir a alteração no banco
        try {
            TarefaDAO dao = new TarefaDAO();
            dao.adicionarHorasTrabalhadas(this.id, horas);
        } catch (SQLException e) {
            System.err.println("Erro ao salvar horas trabalhadas: " + e.getMessage());
            this.horasTrabalhadas -= horas; // reverte a alteração em memória
        }
    }

    public String getInformacoesDetalhadas() {
        return String.format("Tarefa [ID=%d, Descrição=%s, Prazo=%s, Importância=%s, Status=%s, Progresso=%.1f%%, Horas: %.1f/%.1f]",
                id, descricao, prazo, nivelImportancia, status, calcularProgresso(), horasTrabalhadas, horasEstimadas);
    }

    // Getters e Setters
    /* Os métodos setPrazo(), setNivelImportancia(), setHorasEstimadas() e setHorasTrabalhadas() não são realmente necessários.
       A lógica de negócio não altera esses campos após a criação da tarefa (exceto horasTrabalhadas, que é incrementada pelo método adicionarHorasTrabalhadas, não pelo setter).
    */
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
    public double getHorasEstimadas() { return horasEstimadas; }
    public void setHorasEstimadas(double horasEstimadas) { this.horasEstimadas = horasEstimadas; }
    public double getHorasTrabalhadas() { return horasTrabalhadas; }
    public void setHorasTrabalhadas(double horasTrabalhadas) { this.horasTrabalhadas = horasTrabalhadas; }
    public Projeto getProjetoPai() { return projetoPai; }
    public void setProjetoPai(Projeto projetoPai) { this.projetoPai = projetoPai; }
}