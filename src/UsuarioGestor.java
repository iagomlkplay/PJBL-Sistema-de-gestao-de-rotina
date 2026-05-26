import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class UsuarioGestor extends Usuario {
    private String departamento;
    private transient UsuarioDAO usuarioDAO;
    private transient ProjetoDAO projetoDAO;
    private transient TarefaDAO tarefaDAO;

    // Construtor com ID
    public UsuarioGestor(int id, String nome, String cpf, String email, String senha, String departamento) {
        super(id, nome, cpf, email, senha);
        this.departamento = departamento;
        this.tipoUsuario = TipoUsuario.GESTOR;
        inicializarDAOs();
    }

    // Construtor sem ID
    public UsuarioGestor(String nome, String cpf, String email, String senha, String departamento) {
        super(nome, cpf, email, senha);
        this.departamento = departamento;
        this.tipoUsuario = TipoUsuario.GESTOR;
        inicializarDAOs();
    }

    private void inicializarDAOs() {
        usuarioDAO = new UsuarioDAO();
        projetoDAO = new ProjetoDAO();
        tarefaDAO = new TarefaDAO();
    }

    // Exibir equipe
    public List<UsuarioDev> getEquipe() {
        try {
            return usuarioDAO.listarDevsPorGestor(this.getId());
        } catch (SQLException e) {
            System.err.println("Erro ao carregar equipe: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // RF08: visualizar todos os projetos da equipe e tarefas
    public String visualizarTodosProjetosTarefas() {
        StringBuilder sb = new StringBuilder();
        List<UsuarioDev> equipe = getEquipe();
        Sistema sistema = Sistema.getInstance();
        sb.append("--- Projetos da equipe ---\n");
        for (Projeto p : sistema.getProjetos()) {
            boolean temTarefaNaEquipe = false;
            try {
                List<Tarefa> tarefasProjeto = tarefaDAO.listarPorProjeto(p.getId(), usuarioDAO, projetoDAO);
                for (Tarefa t : tarefasProjeto) {
                    int devId = t.getDevResponsavel().getId();
                    if (equipe.stream().anyMatch(d -> d.getId() == devId)) {
                        temTarefaNaEquipe = true;
                        break;
                    }
                }
                if (temTarefaNaEquipe) {
                    sb.append(p.getInformacoesDetalhadas(tarefasProjeto)).append("\n");
                }
            } catch (SQLException e) {
                sb.append(p.getInformacoesDetalhadas()).append(" (erro ao carregar tarefas)\n");
            }
        }
        sb.append("--- Tarefas da equipe ---\n");
        for (UsuarioDev dev : equipe) {
            try {
                List<Tarefa> tarefasDev = tarefaDAO.listarPorDev(dev.getId(), usuarioDAO, projetoDAO);
                for (Tarefa t : tarefasDev) {
                    sb.append(t.getInformacoesDetalhadas()).append(" - Responsável: ").append(dev.getNome()).append("\n");
                }
            } catch (SQLException e) {
                sb.append("Erro ao listar tarefas do dev ").append(dev.getNome()).append("\n");
            }
        }
        return sb.toString();
    }

    // RF09: criar projeto
    public void criarProjeto(String nome, Date prazo, NivelImportancia importancia) {
        Sistema sistema = Sistema.getInstance();
        Projeto projeto = new Projeto(nome, prazo, importancia, this.getId()); // passa o ID do gestor
        sistema.adicionarProjeto(projeto);
        System.out.println("Projeto criado: " + nome + " (ID " + projeto.getId() + ")");
    }

    // RF09: criar tarefa avulsa (sem projeto)
    public void criarAtribuirTarefa(String descricao, Date prazo, NivelImportancia importancia, int devId, double horasEstimadas) {
        Sistema sistema = Sistema.getInstance();
        UsuarioDev dev = sistema.buscarDevPorId(devId);
        List<UsuarioDev> equipe = getEquipe();
        if (dev == null || equipe.stream().noneMatch(d -> d.getId() == dev.getId())) {
            System.out.println("Dev não encontrado ou não está na sua equipe.");
            return;
        }
        Tarefa tarefa = new Tarefa(descricao, prazo, importancia, dev, horasEstimadas);
        sistema.adicionarTarefa(tarefa);
        System.out.println("Tarefa criada com " + horasEstimadas + "h estimadas e atribuída ao dev " + dev.getNome());
    }

    // RF09: criar tarefa dentro de um projeto
    public void criarAtribuirTarefaEmProjeto(String descricao, Date prazo, NivelImportancia importancia,
                                             int devId, int projetoId, double horasEstimadas) {
        Sistema sistema = Sistema.getInstance();
        UsuarioDev dev = sistema.buscarDevPorId(devId);
        Projeto projeto = sistema.buscarProjetoPorId(projetoId);
        List<UsuarioDev> equipe = getEquipe();

        if (dev == null || equipe.stream().noneMatch(d -> d.getId() == dev.getId())) {
            System.out.println("Dev não encontrado ou não está na sua equipe.");
            return;
        }
        if (projeto == null) {
            System.out.println("Projeto não encontrado.");
            return;
        }
        // Verifica se o projeto pertence à equipe
        List<Tarefa> tarefasProjeto;
        try {
            tarefasProjeto = tarefaDAO.listarPorProjeto(projetoId, usuarioDAO, projetoDAO);
            boolean projetoPertenceEquipe = tarefasProjeto.stream()
                    .anyMatch(t -> equipe.stream().anyMatch(d -> d.getId() == t.getDevResponsavel().getId()));
            if (!projetoPertenceEquipe && !tarefasProjeto.isEmpty()) {
                System.out.println("Este projeto não pertence à sua equipe.");
                return;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao verificar projeto: " + e.getMessage());
            return;
        }

        Tarefa tarefa = new Tarefa(descricao, prazo, importancia, dev, horasEstimadas);
        tarefa.setProjetoPai(projeto);
        sistema.adicionarTarefa(tarefa);
        System.out.println("Tarefa adicionada ao projeto " + projeto.getNome() + " (ID " + projeto.getId() +
                ") com " + horasEstimadas + "h estimadas, atribuída ao dev " + dev.getNome());
    }

    // RF10: processar solicitação de mudança
    public void processarSolicitacaoMudanca(SolicitacaoMudanca solicitacao, boolean aprovado) {
        if (solicitacao == null || solicitacao.getStatus() != StatusSolicitacao.PENDENTE) {
            System.out.println("Solicitação inválida ou já processada.");
            return;
        }
        // Verifica se o solicitante pertence à equipe
        if (getEquipe().stream().noneMatch(d -> d.getId() == solicitacao.getSolicitante().getId())) {
            System.out.println("Este desenvolvedor não pertence à sua equipe.");
            return;
        }
        solicitacao.setStatus(aprovado ? StatusSolicitacao.APROVADA : StatusSolicitacao.REJEITADA);
        try {
            SolicitacaoDAO dao = new SolicitacaoDAO();
            dao.atualizarStatus(solicitacao.getId(), solicitacao.getStatus());
        } catch (SQLException e) {
            System.err.println("Erro ao salvar status da solicitação: " + e.getMessage());
            solicitacao.setStatus(StatusSolicitacao.PENDENTE);
            return;
        }
        if (aprovado) {
            System.out.println("Solicitação " + solicitacao.getId() + " APROVADA. Reorganização será feita manualmente pelo gestor.");
        } else {
            System.out.println("Solicitação " + solicitacao.getId() + " REJEITADA.");
        }
    }

    // RF11: validar finalização
    public void validarFinalizacao(Object item) {
        if (item instanceof Tarefa) {
            Tarefa tarefa = (Tarefa) item;
            // Verifica se o dev responsável está na equipe
            if (getEquipe().stream().noneMatch(d -> d.getId() == tarefa.getDevResponsavel().getId())) {
                System.out.println("Você não pode validar uma tarefa de um dev fora da sua equipe.");
                return;
            }
            if (tarefa.getStatus() == StatusTarefa.FEITO) {
                tarefa.setStatus(StatusTarefa.PRONTO);
                try {
                    tarefaDAO.atualizarStatus(tarefa.getId(), StatusTarefa.PRONTO);
                    System.out.println("Tarefa " + tarefa.getId() + " validada como PRONTA.");
                    Projeto projetoPai = tarefa.getProjetoPai();
                    if (projetoPai != null) {
                        List<Tarefa> tarefasDoProjeto = tarefaDAO.listarPorProjeto(projetoPai.getId(), usuarioDAO, projetoDAO);
                        projetoPai.verificarConclusao(tarefasDoProjeto, projetoDAO);
                    }
                } catch (SQLException e) {
                    System.err.println("Erro ao salvar validação da tarefa: " + e.getMessage());
                    tarefa.setStatus(StatusTarefa.FEITO);
                }
            } else {
                System.out.println("Tarefa " + tarefa.getId() + " não está com status FEITO (atual: " + tarefa.getStatus() + ")");
            }
        } else if (item instanceof Projeto) {
            Projeto projeto = (Projeto) item;
            // Verifica se o projeto pertence à equipe
            List<Tarefa> tarefasProjeto;
            try {
                tarefasProjeto = tarefaDAO.listarPorProjeto(projeto.getId(), usuarioDAO, projetoDAO);
                boolean pertence = tarefasProjeto.stream()
                        .anyMatch(t -> getEquipe().stream().anyMatch(d -> d.getId() == t.getDevResponsavel().getId()));
                if (!pertence && !tarefasProjeto.isEmpty()) {
                    System.out.println("Este projeto não pertence à sua equipe.");
                    return;
                }
            } catch (SQLException e) {
                System.err.println("Erro ao verificar projeto: " + e.getMessage());
                return;
            }
            if (projeto.getStatus() == StatusTarefa.FEITO) {
                projeto.setStatus(StatusTarefa.PRONTO);
                try {
                    projetoDAO.atualizarStatus(projeto.getId(), StatusTarefa.PRONTO);
                    System.out.println("Projeto " + projeto.getId() + " validado como PRONTO.");
                } catch (SQLException e) {
                    System.err.println("Erro ao salvar validação do projeto: " + e.getMessage());
                    projeto.setStatus(StatusTarefa.FEITO);
                }
            } else {
                System.out.println("Projeto " + projeto.getId() + " não está com status FEITO.");
            }
        }
    }

    // RF12: reatribuir tarefa atrasada
    public void reatribuirTarefaAtrasada(Tarefa tarefa, UsuarioDev novoDev) {
        if (tarefa.getStatus() != StatusTarefa.ATRASADO) {
            System.out.println("Esta tarefa não está atrasada (status: " + tarefa.getStatus() + ")");
            return;
        }
        List<UsuarioDev> equipe = getEquipe();
        if (equipe.stream().noneMatch(d -> d.getId() == tarefa.getDevResponsavel().getId()) ||
                equipe.stream().noneMatch(d -> d.getId() == novoDev.getId())) {
            System.out.println("O dev antigo ou o novo dev não pertencem à sua equipe.");
            return;
        }
        String nomeAntigo = tarefa.getDevResponsavel().getNome();
        try {
            tarefaDAO.reatribuirDev(tarefa.getId(), novoDev.getId());
        } catch (SQLException e) {
            System.err.println("Erro ao reatribuir tarefa: " + e.getMessage());
            return;
        }
        tarefa.setDevResponsavel(novoDev);
        System.out.println("Tarefa atrasada " + tarefa.getId() + " reatribuída de " + nomeAntigo + " para " + novoDev.getNome());
    }

    public List<SolicitacaoMudanca> listarSolicitacoesPendentes() {
        Sistema sistema = Sistema.getInstance();
        List<SolicitacaoMudanca> todas = sistema.getSolicitacoesPorGestor(this.getId());
        return todas.stream()
                .filter(s -> s.getStatus() == StatusSolicitacao.PENDENTE)
                .collect(Collectors.toList());
    }

    // Método auxiliar para exibir no console (opcional, para compatibilidade)
    public void exibirSolicitacoesPendentes() {
        List<SolicitacaoMudanca> pendentes = listarSolicitacoesPendentes();
        if (pendentes.isEmpty()) {
            System.out.println("Não há solicitações pendentes.");
        } else {
            System.out.println("--- Solicitações Pendentes ---");
            for (SolicitacaoMudanca s : pendentes) {
                System.out.println("ID: " + s.getId() + " | De: " + s.getSolicitante().getNome() + " | Justificativa: " + s.getJustificativa());
            }
        }
    }

    @Override
    public String toString() {
        return this.getNome(); // Exibe o nome do gestor no combo de cadastro
    }

    // Getters e setters
    public String getDepartamento() { return departamento; }

    // setDepartamento() não é usado porque o departamento é definido no construtor e nunca alterado. Pode ser removido.
    public void setDepartamento(String departamento) { this.departamento = departamento; }
}