import java.util.List;

public class UsuarioGestor extends Usuario {
    private String departamento;
    private List<UsuarioDev> equipe;      // devs sob sua gestão
    private List<Projeto> todosProjetos;
    private List<Tarefa> todasTarefas;
    private List<SolicitacaoMudanca> solicitacoesPendentes;

    public UsuarioGestor(int id, String nome, String cpf, String email, String senha, String departamento) {
        super(id, nome, cpf, email, senha);
        this.departamento = departamento;
        this.equipe = new java.util.ArrayList<>();
        this.todosProjetos = new java.util.ArrayList<>();
        this.todasTarefas = new java.util.ArrayList<>();
        this.solicitacoesPendentes = new java.util.ArrayList<>();
    }

    public void visualizarTodosProjetosTarefas() {
        System.out.println("Todos os projetos: " + todosProjetos);
        System.out.println("Todas as tarefas: " + todasTarefas);
    }

    public void criarAtribuirProjeto(String nome, java.util.Date prazo, NivelImportancia importancia, int devId) {
        UsuarioDev dev = buscarDevPorId(devId);
        if (dev != null) {
            Projeto projeto = new Projeto(0, nome, prazo, importancia, StatusTarefa.PENDENTE, dev);
            todosProjetos.add(projeto);
            dev.getProjetos().add(projeto);
            System.out.println("Projeto criado e atribuído ao dev " + dev.getNome());
        } else {
            System.out.println("Dev não encontrado.");
        }
    }

    public void criarAtribuirTarefa(String nome, java.util.Date prazo, NivelImportancia importancia, int devId) {
        UsuarioDev dev = buscarDevPorId(devId);
        if (dev != null) {
            Tarefa tarefa = new Tarefa(0, nome, prazo, importancia, StatusTarefa.PENDENTE, dev);
            todasTarefas.add(tarefa);
            dev.getTarefas().add(tarefa);
            System.out.println("Tarefa criada e atribuída ao dev " + dev.getNome());
        } else {
            System.out.println("Dev não encontrado.");
        }
    }

    public void processarSolicitacaoMudanca(SolicitacaoMudanca solicitacao, boolean aprovado) {
        solicitacao.setStatus(aprovado ? StatusSolicitacao.APROVADA : StatusSolicitacao.REJEITADA);
        System.out.println("Solicitação " + solicitacao.getId() + " " + (aprovado ? "aprovada" : "rejeitada"));
    }

    public void validarFinalizacao(Object item) {
        // item pode ser Projeto ou Tarefa
        if (item instanceof Tarefa) {
            Tarefa tarefa = (Tarefa) item;
            if (tarefa.getStatus() == StatusTarefa.FEITO) {
                tarefa.setStatus(StatusTarefa.PRONTO);
                System.out.println("Tarefa " + tarefa.getId() + " validada como PRONTA.");
            } else {
                System.out.println("Tarefa não está com status FEITO.");
            }
        } else if (item instanceof Projeto) {
            Projeto projeto = (Projeto) item;
            if (projeto.getStatus() == StatusTarefa.FEITO) {
                projeto.setStatus(StatusTarefa.PRONTO);
                System.out.println("Projeto " + projeto.getId() + " validado como PRONTO.");
            } else {
                System.out.println("Projeto não está com status FEITO.");
            }
        }
    }

    public void reatribuirTarefaAtrasada(Tarefa tarefa, UsuarioDev novoDev) {
        if (tarefa.getStatus() == StatusTarefa.ATRASADO) {
            // Remover do dev antigo e adicionar ao novo
            UsuarioDev antigoDev = tarefa.getDevResponsavel();
            if (antigoDev != null) antigoDev.getTarefas().remove(tarefa);
            tarefa.setDevResponsavel(novoDev);
            novoDev.getTarefas().add(tarefa);
            System.out.println("Tarefa atrasada " + tarefa.getId() + " reatribuída para " + novoDev.getNome());
        } else {
            System.out.println("Esta tarefa não está atrasada.");
        }
    }

    private UsuarioDev buscarDevPorId(int id) {
        for (UsuarioDev dev : equipe) {
            if (dev.getId() == id) return dev;
        }
        return null;
    }

    // Getters e setters
    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }
    public List<UsuarioDev> getEquipe() { return equipe; }
    public void setEquipe(List<UsuarioDev> equipe) { this.equipe = equipe; }
    public List<Projeto> getTodosProjetos() { return todosProjetos; }
    public List<Tarefa> getTodasTarefas() { return todasTarefas; }
    public List<SolicitacaoMudanca> getSolicitacoesPendentes() { return solicitacoesPendentes; }
}