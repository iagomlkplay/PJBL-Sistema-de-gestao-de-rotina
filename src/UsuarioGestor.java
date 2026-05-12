import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class UsuarioGestor extends Usuario {
    private String departamento;
    private List<UsuarioDev> equipe;

    public UsuarioGestor(int id, String nome, String cpf, String email, String senha, String departamento) {
        super(id, nome, cpf, email, senha);
        this.departamento = departamento;
        this.equipe = new java.util.ArrayList<>();
        this.tipoUsuario = TipoUsuario.GESTOR;
    }

    // RF08: visualizar todos os projetos da equipe e tarefas (filtrados pelos devs da equipe)
    public void visualizarTodosProjetosTarefas() {
        Sistema sistema = Sistema.getInstance();
        System.out.println("--- Projetos da equipe ---");
        for (Projeto p : sistema.getProjetos()) {
            // Verifica se o projeto tem pelo menos uma tarefa atribuída a um dev da equipe
            boolean temTarefaNaEquipe = p.getTarefas().stream()
                    .anyMatch(t -> equipe.contains(t.getDevResponsavel()));
            if (temTarefaNaEquipe) {
                System.out.println(p.getInformacoesDetalhadas());
            }
        }
        System.out.println("--- Tarefas da equipe ---");
        for (UsuarioDev dev : equipe) {
            for (Tarefa t : dev.getTarefas()) {
                System.out.println(t.getInformacoesDetalhadas() + " - Responsável: " + dev.getNome());
            }
        }
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
        if (dev == null || !equipe.contains(dev)) {
            System.out.println("Dev não encontrado ou não está na sua equipe.");
            return;
        }
        Tarefa tarefa = new Tarefa(descricao, prazo, importancia, dev, horasEstimadas);
        sistema.adicionarTarefa(tarefa);
        dev.getTarefas().add(tarefa);
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
        if (dev == null || !equipe.contains(dev)) {
            System.out.println("Dev não encontrado ou não está na sua equipe.");
            return;
        }
        if (projeto == null) {
            System.out.println("Projeto não encontrado.");
            return;
        }
        // Gestor pode adicionar tarefa a qualquer projeto
        Tarefa tarefa = new Tarefa(descricao, prazo, importancia, dev, horasEstimadas);
        tarefa.setProjetoPai(projeto);
        sistema.adicionarTarefa(tarefa);
        dev.getTarefas().add(tarefa);
        projeto.getTarefas().add(tarefa);
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
            if (tarefa.getStatus() == StatusTarefa.FEITO) {
                tarefa.setStatus(StatusTarefa.PRONTO);
                System.out.println("Tarefa " + tarefa.getId() + " validada como PRONTA.");

                Projeto projetoPai = tarefa.getProjetoPai();
                if (projetoPai != null) {
                    projetoPai.verificarConclusao();
                }
            } else {
                System.out.println("Tarefa " + tarefa.getId() + " não está com status FEITO (atual: " + tarefa.getStatus() + ")");
            }
        } else if (item instanceof Projeto) {
            Projeto projeto = (Projeto) item;
            if (projeto.getStatus() == StatusTarefa.FEITO) {
                projeto.setStatus(StatusTarefa.PRONTO);
                System.out.println("Projeto " + projeto.getId() + " validado como PRONTO.");
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
        if (!equipe.contains(tarefa.getDevResponsavel()) || !equipe.contains(novoDev)) {
            System.out.println("O dev antigo ou o novo dev não pertencem à sua equipe.");
            return;
        }
        UsuarioDev antigoDev = tarefa.getDevResponsavel();
        antigoDev.getTarefas().remove(tarefa);
        tarefa.setDevResponsavel(novoDev);
        novoDev.getTarefas().add(tarefa);
        System.out.println("Tarefa atrasada " + tarefa.getId() + " reatribuída de " + antigoDev.getNome() + " para " + novoDev.getNome());
    }

    // Listar solicitações pendentes
    public void listarSolicitacoesPendentes() {
        Sistema sistema = Sistema.getInstance();
        List<SolicitacaoMudanca> pendentes = sistema.getSolicitacoes().stream().filter(s -> s.getStatus() == StatusSolicitacao.PENDENTE).collect(Collectors.toList());
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
    public List<UsuarioDev> getEquipe() { return equipe; }
    public void setEquipe(List<UsuarioDev> equipe) { this.equipe = equipe; }
}