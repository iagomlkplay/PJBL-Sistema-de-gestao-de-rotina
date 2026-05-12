import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.sql.SQLException;

public class UsuarioDev extends Usuario {
    private List<String> especialidades;
    private int gestorId;   // ID do gestor responsável
    private transient TarefaDAO tarefaDAO;
    private transient UsuarioDAO usuarioDAO;
    private transient ProjetoDAO projetoDAO;

    // Construtor com ID
    public UsuarioDev(int id, String nome, String cpf, String email, String senha) {
        super(id, nome, cpf, email, senha);
        this.especialidades = new ArrayList<>();
        this.tipoUsuario = TipoUsuario.DEV;
        inicializarDAOs();
    }

    // Construtor sem ID (novo cadastro)
    public UsuarioDev(String nome, String cpf, String email, String senha) {
        super(nome, cpf, email, senha);
        this.especialidades = new ArrayList<>();
        this.tipoUsuario = TipoUsuario.DEV;
        inicializarDAOs();
    }

    private void inicializarDAOs() {
        tarefaDAO = new TarefaDAO();
        usuarioDAO = new UsuarioDAO();
        projetoDAO = new ProjetoDAO();
    }

    // Busca as tarefas do dev diretamente do banco
    private List<Tarefa> carregarTarefas() {
        try {
            return tarefaDAO.listarPorDev(this.getId(), usuarioDAO, projetoDAO);
        } catch (SQLException e) {
            System.err.println("Erro ao carregar tarefas do dev: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // RF04: visualizar seus itens (tarefas e projetos em que participa)
    public void visualizarPropriosProjetosTarefas() {
        List<Tarefa> minhasTarefas = carregarTarefas();
        // Agrupar tarefas por projeto
        List<Projeto> projetosParticipados = minhasTarefas.stream()
                .map(Tarefa::getProjetoPai)
                .filter(p -> p != null)
                .distinct()
                .collect(Collectors.toList());

        System.out.println("--- Projetos em que participo ---");
        for (Projeto p : projetosParticipados) {
            System.out.println(p.getInformacoesDetalhadas() + " - Progresso: " + p.calcularProgresso() + "%");
        }
        System.out.println("--- Minhas Tarefas ---");
        for (Tarefa t : minhasTarefas) {
            System.out.println(t.getInformacoesDetalhadas() + " - Progresso: " + t.calcularProgresso() + "%");
        }
    }

    // RF04 (parte de colegas) - mostra todos os devs e seu progresso (baseado em tarefas)
    public void visualizarProgressoEquipe() {
        Sistema sistema = Sistema.getInstance();
        System.out.println("--- Progresso de todos os Desenvolvedores ---");
        for (UsuarioDev dev : sistema.getDevs()) {
            // O progresso de cada dev precisa ser calculado com base nas suas tarefas do banco
            double progresso = dev.calcularProgressoTotal();
            System.out.println(dev.getNome() + " (ID " + dev.getId() + ") - Progresso geral: " + progresso + "%");
        }
    }

    // RF04 - visualizar detalhes de um colega específico (suas tarefas e projetos)
    public void visualizarDetalhesColega(UsuarioDev colega) {
        System.out.println("=== Detalhes de " + colega.getNome() + " ===");
        List<Tarefa> tarefasColega = colega.carregarTarefas();
        // Projetos do colega (derivados das tarefas)
        List<Projeto> projetosColega = tarefasColega.stream()
                .map(Tarefa::getProjetoPai)
                .filter(p -> p != null)
                .distinct()
                .collect(Collectors.toList());
        System.out.println("--- Projetos ---");
        for (Projeto p : projetosColega) {
            System.out.println(p.getInformacoesDetalhadas());
        }
        System.out.println("--- Tarefas ---");
        for (Tarefa t : tarefasColega) {
            System.out.println(t.getInformacoesDetalhadas());
        }
    }

    // RF05: alterar status de tarefa
    public void alterarStatusTarefa(Tarefa tarefa, StatusTarefa novoStatus) {
        if (tarefa == null) {
            System.out.println("Tarefa inválida.");
            return;
        }
        // Verificar se a tarefa pertence a este dev (consulta ao banco)
        try {
            Tarefa tarefaBanco = tarefaDAO.buscarPorId(tarefa.getId(), usuarioDAO, projetoDAO);
            if (tarefaBanco == null || tarefaBanco.getDevResponsavel().getId() != this.getId()) {
                System.out.println("Você não pode alterar uma tarefa que não lhe foi atribuída.");
                return;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao verificar propriedade da tarefa: " + e.getMessage());
            return;
        }

        if (tarefa.getStatus() == StatusTarefa.PRONTO) {
            System.out.println("Não é possível alterar status de tarefa já validada (PRONTO).");
            return;
        }
        StatusTarefa antigo = tarefa.getStatus();
        tarefa.setStatus(novoStatus);

        // Persistir a mudança no banco de dados
        try {
            tarefaDAO.atualizarStatus(tarefa.getId(), novoStatus);
        } catch (SQLException e) {
            System.err.println("Erro ao salvar status da tarefa no banco: " + e.getMessage());
            tarefa.setStatus(antigo);
            return;
        }

        // Verificar projeto pai para atualizar conclusão
        Projeto projetoPai = tarefa.getProjetoPai();
        if (projetoPai != null) {
            // Para garantir, recarregamos o projeto do banco
            try {
                Projeto projetoAtualizado = projetoDAO.buscarPorId(projetoPai.getId());
                if (projetoAtualizado != null) {
                    projetoAtualizado.verificarConclusao(); // Este método agora não depende de lista interna
                    // Persistir possível mudança de status do projeto
                    if (projetoAtualizado.getStatus() == StatusTarefa.FEITO) {
                        projetoDAO.atualizarStatus(projetoAtualizado.getId(), StatusTarefa.FEITO);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Erro ao verificar conclusão do projeto: " + e.getMessage());
            }
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

    // Progresso total baseado apenas em tarefas (carregadas do banco)
    public double calcularProgressoTotal() {
        List<Tarefa> minhasTarefas = carregarTarefas();
        if (minhasTarefas.isEmpty()) return 0.0;
        double soma = 0.0;
        for (Tarefa t : minhasTarefas) {
            soma += t.calcularProgresso();
        }
        return soma / minhasTarefas.size();
    }

    // Visualizar tarefas de um projeto específico (consulta direta ao banco)
    public void visualizarTarefasDoProjeto(Projeto projeto) {
        try {
            List<Tarefa> tarefasProjeto = tarefaDAO.listarPorProjeto(projeto.getId(), usuarioDAO, projetoDAO);
            System.out.println("=== Tarefas do Projeto: " + projeto.getNome() + " ===");
            for (Tarefa t : tarefasProjeto) {
                System.out.println(t.getInformacoesDetalhadas());
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar tarefas do projeto: " + e.getMessage());
        }
    }

    // Getters e setters
    public List<String> getEspecialidades() { return especialidades; }
    public void setEspecialidades(List<String> especialidades) { this.especialidades = especialidades; }
    public int getGestorId() { return gestorId; }
    public void setGestorId(int gestorId) { this.gestorId = gestorId; }
}