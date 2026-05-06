import java.util.Date;

public class Tarefa {
    private static int ultimoId = 0;
    private int id;
    private String descricao;
    private Date prazo;
    private NivelImportancia nivelImportancia;
    private StatusTarefa status;
    private UsuarioDev devResponsavel;
    private double horasEstimadas;
    private double horasTrabalhadas;
    private Projeto projetoPai;

    // Construtor sem especificar horas (usa 1.0 como padrão)
    public Tarefa(String descricao, Date prazo, NivelImportancia nivelImportancia, UsuarioDev devResponsavel) {
        this(descricao, prazo, nivelImportancia, devResponsavel, 1.0);
    }

    public Tarefa(String descricao, Date prazo, NivelImportancia nivelImportancia, UsuarioDev devResponsavel, double horasEstimadas) {
        this.id = ++ultimoId;
        this.descricao = descricao;
        this.prazo = prazo;
        this.nivelImportancia = nivelImportancia;
        this.status = StatusTarefa.PENDENTE;
        this.devResponsavel = devResponsavel;
        this.horasEstimadas = horasEstimadas;
        this.horasTrabalhadas = 0.0;
    }

    /**
     * Calcula o progresso baseado no tempo:
     * Se a tarefa está PRONTO ou FEITO -> 100%
     * Caso contrário, progresso = min( (horasTrabalhadas / horasEstimadas) * 100 , 100 )
     */
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
        if (horas > 0) {
            this.horasTrabalhadas += horas;
            // Se ultrapassar 100%, ainda fica FEITO/PRONTO apenas quando status mudar
            System.out.println("Tarefa " + id + " agora tem " + horasTrabalhadas + "h trabalhadas de " + horasEstimadas + "h estimadas.");
        }
    }

    public String getInformacoesDetalhadas() {
        return String.format("Tarefa [ID=%d, Descrição=%s, Prazo=%s, Importância=%s, Status=%s, Progresso=%.1f%%, Horas: %.1f/%.1f]",
                id, descricao, prazo, nivelImportancia, status, calcularProgresso(), horasTrabalhadas, horasEstimadas);
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
    public double getHorasEstimadas() { return horasEstimadas; }
    public void setHorasEstimadas(double horasEstimadas) { this.horasEstimadas = horasEstimadas; }
    public double getHorasTrabalhadas() { return horasTrabalhadas; }
    public void setHorasTrabalhadas(double horasTrabalhadas) { this.horasTrabalhadas = horasTrabalhadas; }
    public Projeto getProjetoPai() { return projetoPai; }
    public void setProjetoPai(Projeto projetoPai) { this.projetoPai = projetoPai; }
}