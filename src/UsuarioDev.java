import java.util.ArrayList;
import java.util.List;

public class UsuarioDev extends Usuario {
    private List<String> especialidades;
    private List<Projeto> projetos;      // projetos atribuídos (visão local)
    private List<Tarefa> tarefas;        // tarefas atribuídas (visão local)

    public UsuarioDev(int id, String nome, String cpf, String email, String senha) {
        super(id, nome, cpf, email, senha);
        this.especialidades = new ArrayList<>();
        this.projetos = new ArrayList<>();
        this.tarefas = new ArrayList<>();
        this.tipoUsuario = TipoUsuario.DESENVOLVEDOR;
    }

    // RF04: visualizar seus itens e também os dos colegas
    public void visualizarPropriosProjetosTarefas() {
        System.out.println("--- Meus Projetos ---");
        for (Projeto p : projetos) {
            System.out.println(p.getInformacoesDetalhadas() + " - Progresso: " + p.calcularProgresso() + "%");
        }
        System.out.println("--- Minhas Tarefas ---");
        for (Tarefa t : tarefas) {
            System.out.println(t.getInformacoesDetalhadas() + " - Progresso: " + t.calcularProgresso() + "%");
        }
    }

    // RF04 (parte de colegas) - mostra todos os devs e seu progresso
    public void visualizarProgressoEquipe() {
        Sistema sistema = Sistema.getInstance();
        System.out.println("--- Progresso de todos os Desenvolvedores ---");
        for (UsuarioDev dev : sistema.getDevs()) {
            double progresso = dev.calcularProgressoTotal();
            System.out.println(dev.getNome() + " (ID " + dev.getId() + ") - Progresso geral: " + progresso + "%");
        }
    }

    // RF04 - visualizar detalhes de um colega específico (ou todos)
    public void visualizarDetalhesColega(UsuarioDev colega) {
        System.out.println("=== Detalhes de " + colega.getNome() + " ===");
        System.out.println("--- Projetos ---");
        for (Projeto p : colega.getProjetos()) {
            System.out.println(p.getInformacoesDetalhadas());
        }
        System.out.println("--- Tarefas ---");
        for (Tarefa t : colega.getTarefas()) {
            System.out.println(t.getInformacoesDetalhadas());
        }
    }

    // RF05: alterar status de PENDENTE para FEITO (ou outros, mas só permite se não estiver PRONTO)
    public void alterarStatusTarefa(Tarefa tarefa, StatusTarefa novoStatus) {
        if (tarefa == null) {
            System.out.println("Tarefa inválida.");
            return;
        }
        // Verifica se a tarefa pertence a este dev
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

        // Verificar projetos que contêm esta tarefa (para atualizar status do projeto)
        Sistema sistema = Sistema.getInstance();
        for (Projeto projeto : sistema.getProjetos()) {
            if (projeto.getTarefas().contains(tarefa)) {
                projeto.verificarConclusao();
            }
        }

        System.out.println("Status da tarefa " + tarefa.getId() + " alterado de " + antigo + " para " + novoStatus);
        // RF13: notificar gestor imediatamente
        Sistema.getInstance().notificarGestorMudancaStatus(tarefa, this);
    }

    // RF06: enviar relatório final (associado a uma tarefa ou projeto)
    public void enviarRelatorioFinal(Object item, String conteudo) {
        Relatorio relatorio = new Relatorio(conteudo); // ID gerado automaticamente
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

    // RF07: solicitar reorganização (envia pedido ao sistema)
    public void solicitarReorganizacao(String justificativa) {
        SolicitacaoMudanca solicitacao = new SolicitacaoMudanca(justificativa, this);
        Sistema.getInstance().adicionarSolicitacao(solicitacao);
        System.out.println("Solicitação de reorganização enviada. Justificativa: " + justificativa);
    }

    // Calcula o progresso total baseado em tarefas (cada tarefa 100% quando FEITO/PRONTO)
    // e projetos (média das tarefas do projeto) - simplificado
    public double calcularProgressoTotal() {
        double total = 0;
        int count = 0;
        for (Tarefa t : tarefas) {
            total += t.calcularProgresso();
            count++;
        }
        for (Projeto p : projetos) {
            total += p.calcularProgresso();
            count++;
        }
        return count == 0 ? 0 : total / count;
    }

    // Vizualiza as tarefas de um projeto
    public void visualizarTarefasDoProjeto(Projeto projeto) {
        System.out.println("=== Tarefas do Projeto: " + projeto.getNome() + " ===");
        for (Tarefa t : projeto.getTarefas()) {
            System.out.println(t.getInformacoesDetalhadas());
        }
    }

    // Getters e setters
    public List<String> getEspecialidades() { return especialidades; }
    public void setEspecialidades(List<String> especialidades) { this.especialidades = especialidades; }
    public List<Projeto> getProjetos() { return projetos; }
    public void setProjetos(List<Projeto> projetos) { this.projetos = projetos; }
    public List<Tarefa> getTarefas() { return tarefas; }
    public void setTarefas(List<Tarefa> tarefas) { this.tarefas = tarefas; }
}