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

    // Construtor com ID (banco)
    public UsuarioGestor(int id, String nome, String cpf, String email, String senha, String departamento) {
        super(id, nome, cpf, email, senha);
        this.departamento = departamento;
        this.tipoUsuario = TipoUsuario.GESTOR;
        inicializarDAOs();
    }

    // Construtor sem ID (novo cadastro)
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

    // Carrega a equipe do banco sob demanda
    private List<UsuarioDev> carregarEquipe() {
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
        List<UsuarioDev> equipe = carregarEquipe();
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
        Projeto projeto = new Projeto(nome, prazo, importancia);
        sistema.adicionarProjeto(projeto);
        System.out.println("Projeto criado: " + nome + " (ID " + projeto.getId() + ")");
    }

    // RF09: criar tarefa avulsa (sem projeto)
    public void criarAtribuirTarefa(String descricao, Date prazo, NivelImportancia importancia, int devId) {
        criarAtribuirTarefa(descricao, prazo, importancia, devId, 1.0);
    }

    public void criarAtribuirTarefa(String descricao, Date prazo, NivelImportancia importancia, int devId, double horasEstimadas) {
        Sistema sistema = Sistema.getInstance();
        UsuarioDev dev = sistema.buscarDevPorId(devId);
        List<UsuarioDev> equipe = carregarEquipe();
        if (dev == null || equipe.stream().noneMatch(d -> d.getId() == dev.getId())) {
            System.out.println("Dev não encontrado ou não está na sua equipe.");
            return;
        }
        Tarefa tarefa = new Tarefa(descricao, prazo, importancia, dev, horasEstimadas);
        sistema.adicionarTarefa(tarefa);
        System.out.println("Tarefa criada com " + horasEstimadas + "h estimadas e atribuída ao dev " + dev.getNome());
    }

    // RF09: criar tarefa dentro de um projeto
    public void criarAtribuirTarefaEmProjeto(String descricao, Date prazo, NivelImportancia importancia, int devId, int projetoId) {
        criarAtribuirTarefaEmProjeto(descricao, prazo, importancia, devId, projetoId, 1.0);
    }

    public void criarAtribuirTarefaEmProjeto(String descricao, Date prazo, NivelImportancia importancia,
                                             int devId, int projetoId, double horasEstimadas) {
        Sistema sistema = Sistema.getInstance();
        UsuarioDev dev = sistema.buscarDevPorId(devId);
        Projeto projeto = sistema.buscarProjetoPorId(projetoId);
        List<UsuarioDev> equipe = carregarEquipe();
        if (dev == null || equipe.stream().noneMatch(d -> d.getId() == dev.getId())) {
            System.out.println("Dev não encontrado ou não está na sua equipe.");
            return;
        }
        if (projeto == null) {
            System.out.println("Projeto não encontrado.");
            return;
        }
        // Gestor pode adicionar tarefa a qualquer projeto (não verifica dono do projeto)
        Tarefa tarefa = new Tarefa(descricao, prazo, importancia, dev, horasEstimadas);
        tarefa.setProjetoPai(projeto);
        sistema.adicionarTarefa(tarefa);
        // Atualiza também a lista de tarefas do projeto (no banco, já foi via inserção)
        System.out.println("Tarefa adicionada ao projeto " + projeto.getNome() + " (ID " + projeto.getId() +
                ") com " + horasEstimadas + "h estimadas, atribuída ao dev " + dev.getNome());
    }

    // RF10: processar solicitação de mudança
    public void processarSolicitacaoMudanca(SolicitacaoMudanca solicitacao, boolean aprovado) {
        if (solicitacao == null || solicitacao.getStatus() != StatusSolicitacao.PENDENTE) {
            System.out.println("Solicitação inválida ou já processada.");
            return;
        }
        solicitacao.setStatus(aprovado ? StatusSolicitacao.APROVADA : StatusSolicitacao.REJEITADA);
        // Persistir a alteração no banco
        try {
            SolicitacaoDAO dao = new SolicitacaoDAO();
            dao.atualizarStatus(solicitacao.getId(), solicitacao.getStatus());
        } catch (SQLException e) {
            System.err.println("Erro ao salvar status da solicitação: " + e.getMessage());
            // Reverte o estado em memória
            solicitacao.setStatus(StatusSolicitacao.PENDENTE);
            return;
        }
        if (aprovado) {
            System.out.println("Solicitação " + solicitacao.getId() + " APROVADA. Reorganização será feita manualmente pelo gestor.");
        } else {
            System.out.println("Solicitação " + solicitacao.getId() + " REJEITADA.");
        }
    }

    // RF11: validar finalização (sem dependência de listas em memória)
    public void validarFinalizacao(Object item) {
        if (item instanceof Tarefa) {
            Tarefa tarefa = (Tarefa) item;
            if (tarefa.getStatus() == StatusTarefa.FEITO) {
                tarefa.setStatus(StatusTarefa.PRONTO);
                try {
                    tarefaDAO.atualizarStatus(tarefa.getId(), StatusTarefa.PRONTO);
                    System.out.println("Tarefa " + tarefa.getId() + " validada como PRONTA.");

                    // Verificar se o projeto pai deve ser concluído
                    Projeto projetoPai = tarefa.getProjetoPai();
                    if (projetoPai != null) {
                        // Recarregar todas as tarefas do projeto para verificar conclusão
                        List<Tarefa> tarefasDoProjeto = tarefaDAO.listarPorProjeto(projetoPai.getId(), usuarioDAO, projetoDAO);
                        projetoPai.verificarConclusao(tarefasDoProjeto, tarefaDAO, projetoDAO);
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
        List<UsuarioDev> equipe = carregarEquipe();
        if (equipe.stream().noneMatch(d -> d.getId() == tarefa.getDevResponsavel().getId()) ||
                equipe.stream().noneMatch(d -> d.getId() == novoDev.getId())) {
            System.out.println("O dev antigo ou o novo dev não pertencem à sua equipe.");
            return;
        }

        // Captura o nome do desenvolvedor antigo ANTES de alterar
        String nomeAntigo = tarefa.getDevResponsavel().getNome();

        // Persistir a reatribuição no banco
        try {
            tarefaDAO.reatribuirDev(tarefa.getId(), novoDev.getId());
        } catch (SQLException e) {
            System.err.println("Erro ao reatribuir tarefa: " + e.getMessage());
            return;
        }
        // Atualizar o objeto em memória
        tarefa.setDevResponsavel(novoDev);
        System.out.println("Tarefa atrasada " + tarefa.getId() + " reatribuída de " + nomeAntigo + " para " + novoDev.getNome());
    }

    // Listar solicitações pendentes
    public void listarSolicitacoesPendentes() {
        Sistema sistema = Sistema.getInstance();
        List<SolicitacaoMudanca> pendentes = sistema.getSolicitacoes().stream()
                .filter(s -> s.getStatus() == StatusSolicitacao.PENDENTE)
                .collect(Collectors.toList());
        if (pendentes.isEmpty()) {
            System.out.println("Não há solicitações pendentes.");
        } else {
            System.out.println("--- Solicitações Pendentes ---");
            for (SolicitacaoMudanca s : pendentes) {
                System.out.println("ID: " + s.getId() + " | De: " + s.getSolicitante().getNome() + " | Justificativa: " + s.getJustificativa());
            }
        }
    }

    // Getters e setters
    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }
}