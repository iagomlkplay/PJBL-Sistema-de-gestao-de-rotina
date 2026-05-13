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
    public List<Tarefa> carregarTarefas() {
        try {
            return tarefaDAO.listarPorDev(this.getId(), usuarioDAO, projetoDAO);
        } catch (SQLException e) {
            System.err.println("Erro ao carregar tarefas do dev: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // RF04: visualizar seus itens (tarefas e projetos em que participa)
    public String visualizarPropriosProjetosTarefas() {
        StringBuilder sb = new StringBuilder();
        List<Tarefa> minhasTarefas = carregarTarefas();
        List<Projeto> projetosParticipados = minhasTarefas.stream()
                .map(Tarefa::getProjetoPai)
                .filter(p -> p != null)
                .distinct()
                .collect(Collectors.toList());

        sb.append("--- Projetos em que participo ---\n");
        for (Projeto p : projetosParticipados) {
            // Carregar tarefas do projeto para calcular progresso real
            try {
                List<Tarefa> tarefasProj = tarefaDAO.listarPorProjeto(p.getId(), usuarioDAO, projetoDAO);
                sb.append(p.getInformacoesDetalhadas(tarefasProj)).append("\n");
            } catch (SQLException e) {
                sb.append(p.getInformacoesDetalhadas()).append(" (erro ao carregar tarefas)\n");
            }
        }
        sb.append("--- Minhas Tarefas ---\n");
        for (Tarefa t : minhasTarefas) {
            sb.append(t.getInformacoesDetalhadas()).append("\n");
        }
        return sb.toString();
    }

    // RF04 (parte de colegas) - mostra todos os devs e seu progresso (baseado em tarefas)
    public String visualizarProgressoEquipe() {
        StringBuilder sb = new StringBuilder();
        Sistema sistema = Sistema.getInstance();
        sb.append("--- Progresso de todos os Desenvolvedores ---\n");
        for (UsuarioDev dev : sistema.getDevs()) {
            double progresso = dev.calcularProgressoTotal();
            sb.append(dev.getNome()).append(" (ID ").append(dev.getId()).append(") - Progresso geral: ")
                    .append(String.format("%.1f", progresso)).append("%\n");
        }
        return sb.toString();
    }

    // RF04 - visualizar detalhes de um colega específico (suas tarefas e projetos)
    public String visualizarDetalhesColega(UsuarioDev colega) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Detalhes de ").append(colega.getNome()).append(" ===\n");
        List<Tarefa> tarefasColega = colega.carregarTarefas();
        List<Projeto> projetosColega = tarefasColega.stream()
                .map(Tarefa::getProjetoPai)
                .filter(p -> p != null)
                .distinct()
                .collect(Collectors.toList());
        sb.append("--- Projetos ---\n");
        for (Projeto p : projetosColega) {
            // Carregar tarefas do projeto para exibir completo
            try {
                List<Tarefa> tarefasProj = tarefaDAO.listarPorProjeto(p.getId(), usuarioDAO, projetoDAO);
                sb.append(p.getInformacoesDetalhadas(tarefasProj)).append("\n");
            } catch (SQLException e) {
                sb.append(p.getInformacoesDetalhadas()).append(" (erro ao carregar tarefas)\n");
            }
        }
        sb.append("--- Tarefas ---\n");
        for (Tarefa t : tarefasColega) {
            sb.append(t.getInformacoesDetalhadas()).append("\n");
        }
        return sb.toString();
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

        // RF05: desenvolvedor só pode mudar de PENDENTE ou ATRASADO para FEITO
        if (!(tarefa.getStatus() == StatusTarefa.PENDENTE || tarefa.getStatus() == StatusTarefa.ATRASADO)
                || novoStatus != StatusTarefa.FEITO) {
            System.out.println("Desenvolvedor só pode alterar status de PENDENTE/ATRASADO para FEITO.");
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