import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UsuarioDev extends Usuario {
    private List<String> especialidades;
    private List<Tarefa> tarefas;        // tarefas atribuídas (visão local)
    private int gestorId;                // ID do gestor responsável

    // Construtor com ID (banco)
    public UsuarioDev(int id, String nome, String cpf, String email, String senha) {
        super(id, nome, cpf, email, senha);
        this.especialidades = new ArrayList<>();
        this.tarefas = new ArrayList<>();
        this.tipoUsuario = TipoUsuario.DEV;
    }

    // Construtor sem ID (novo cadastro)
    public UsuarioDev(String nome, String cpf, String email, String senha) {
        super(nome, cpf, email, senha);
        this.especialidades = new ArrayList<>();
        this.tarefas = new ArrayList<>();
        this.tipoUsuario = TipoUsuario.DEV;
    }

    // RF04: visualizar seus itens (tarefas e projetos em que participa)
    public void visualizarPropriosProjetosTarefas() {
        // Agrupar tarefas por projeto
        List<Projeto> projetosParticipados = tarefas.stream()
                .map(Tarefa::getProjetoPai)
                .filter(p -> p != null)
                .distinct()
                .collect(Collectors.toList());

        System.out.println("--- Projetos em que participo ---");
        for (Projeto p : projetosParticipados) {
            System.out.println(p.getInformacoesDetalhadas() + " - Progresso: " + p.calcularProgresso() + "%");
        }
        System.out.println("--- Minhas Tarefas ---");
        for (Tarefa t : tarefas) {
            System.out.println(t.getInformacoesDetalhadas() + " - Progresso: " + t.calcularProgresso() + "%");
        }
    }

    // RF04 (parte de colegas) - mostra todos os devs e seu progresso (baseado em tarefas)
    public void visualizarProgressoEquipe() {
        Sistema sistema = Sistema.getInstance();
        System.out.println("--- Progresso de todos os Desenvolvedores ---");
        for (UsuarioDev dev : sistema.getDevs()) {
            double progresso = dev.calcularProgressoTotal();
            System.out.println(dev.getNome() + " (ID " + dev.getId() + ") - Progresso geral: " + progresso + "%");
        }
    }

    // RF04 - visualizar detalhes de um colega específico (suas tarefas e projetos)
    public void visualizarDetalhesColega(UsuarioDev colega) {
        System.out.println("=== Detalhes de " + colega.getNome() + " ===");
        // Projetos do colega (derivados das tarefas)
        List<Projeto> projetosColega = colega.getTarefas().stream()
                .map(Tarefa::getProjetoPai)
                .filter(p -> p != null)
                .distinct()
                .collect(Collectors.toList());
        System.out.println("--- Projetos ---");
        for (Projeto p : projetosColega) {
            System.out.println(p.getInformacoesDetalhadas());
        }
        System.out.println("--- Tarefas ---");
        for (Tarefa t : colega.getTarefas()) {
            System.out.println(t.getInformacoesDetalhadas());
        }
    }

    // RF05: alterar status de tarefa
    public void alterarStatusTarefa(Tarefa tarefa, StatusTarefa novoStatus) {
        if (tarefa == null) {
            System.out.println("Tarefa inválida.");
            return;
        }
        if (!tarefas.contains(tarefa)) {
            System.out.println("Você não pode alterar uma tarefa que não lhe foi atribuída.");
            return;
        }
        if (tarefa.getStatus() == StatusTarefa.PRONTO) {
            System.out.println("Não é possível alterar status de tarefa já validada (PRONTO).");
            return;
        }
        StatusTarefa antigo = tarefa.getStatus();
        tarefa.setStatus(novoStatus);

        // Verificar projeto pai para atualizar conclusão
        Projeto projetoPai = tarefa.getProjetoPai();
        if (projetoPai != null) {
            projetoPai.verificarConclusao();
        }

        System.out.println("Status da tarefa " + tarefa.getId() + " alterado de " + antigo + " para " + novoStatus);
        Sistema.getInstance().notificarGestorMudancaStatus(tarefa, this);
    }

    // RF06: enviar relatório final
    public void enviarRelatorioFinal(Object item, String conteudo) {
        Relatorio relatorio = new Relatorio(conteudo);
        relatorio.setDataEnvio(new java.util.Date());
        if (item instanceof Tarefa) {
            relatorio.setTarefaRelacionada((Tarefa) item);
            System.out.println("Relatório final enviado para a tarefa " + ((Tarefa) item).getId());
        } else if (item instanceof Projeto) {
            relatorio.setProjetoRelacionado((Projeto) item);
            System.out.println("Relatório final enviado para o projeto " + ((Projeto) item).getId());
        } else {
            System.out.println("Item inválido para relatório.");
            return;
        }
        Sistema.getInstance().adicionarRelatorio(relatorio);
        System.out.println("Conteúdo: " + conteudo);
    }

    // RF07: solicitar reorganização
    public void solicitarReorganizacao(String justificativa) {
        SolicitacaoMudanca solicitacao = new SolicitacaoMudanca(justificativa, this);
        Sistema.getInstance().adicionarSolicitacao(solicitacao);
        System.out.println("Solicitação de reorganização enviada. Justificativa: " + justificativa);
    }

    // Progresso total baseado apenas em tarefas
    public double calcularProgressoTotal() {
        if (tarefas.isEmpty()) return 0.0;
        double soma = 0.0;
        for (Tarefa t : tarefas) {
            soma += t.calcularProgresso();
        }
        return soma / tarefas.size();
    }

    // Visualizar tarefas de um projeto específico
    public void visualizarTarefasDoProjeto(Projeto projeto) {
        System.out.println("=== Tarefas do Projeto: " + projeto.getNome() + " ===");
        for (Tarefa t : projeto.getTarefas()) {
            System.out.println(t.getInformacoesDetalhadas());
        }
    }

    // Getters e setters
    public List<String> getEspecialidades() { return especialidades; }
    public void setEspecialidades(List<String> especialidades) { this.especialidades = especialidades; }
    public List<Tarefa> getTarefas() { return tarefas; }
    public void setTarefas(List<Tarefa> tarefas) { this.tarefas = tarefas; }
    public int getGestorId() { return gestorId; }
    public void setGestorId(int gestorId) { this.gestorId = gestorId; }
}