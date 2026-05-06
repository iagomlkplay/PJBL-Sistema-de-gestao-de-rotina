import java.util.ArrayList;
import java.util.List;

public class UsuarioDev extends Usuario {
    private List<String> especialidades;
    private List<Projeto> projetos;      // projetos atribuídos a este dev
    private List<Tarefa> tarefas;        // tarefas atribuídas diretamente

    public UsuarioDev(int id, String nome, String cpf, String email, String senha) {
        super(id, nome, cpf, email, senha);
        this.especialidades = new ArrayList<>();
        this.projetos = new ArrayList<>();
        this.tarefas = new ArrayList<>();
    }

    // Métodos do diagrama
    public void visualizarPropriosProjetosTarefas() {
        // Lógica para exibir projetos e tarefas do dev
        System.out.println("Projetos: " + projetos);
        System.out.println("Tarefas: " + tarefas);
    }

    public void alterarStatusTarefa(Tarefa tarefa, StatusTarefa novoStatus) {
        if (tarefa != null && tarefa.getStatus() != StatusTarefa.PRONTO) {
            tarefa.setStatus(novoStatus);
            System.out.println("Status da tarefa " + tarefa.getId() + " alterado para " + novoStatus);
            // RF13: notificar gestor será feito pelo sistema (chamada externa)
        } else {
            System.out.println("Não é possível alterar status de tarefa já validada ou inválida.");
        }
    }

    public void enviarRelatorioFinal(Object item, String conteudo) {
        // item pode ser Projeto ou Tarefa
        Relatorio relatorio = new Relatorio(0, new java.util.Date(), conteudo);
        // Salvar relatório e associar ao item (implementação posterior)
        System.out.println("Relatório final enviado para o item: " + item + " - Conteúdo: " + conteudo);
    }

    public void solicitarReorganizacao(String justificativa) {
        SolicitacaoMudanca solicitacao = new SolicitacaoMudanca(0, justificativa, StatusSolicitacao.PENDENTE, new java.util.Date());
        // Enviar solicitação para o gestor (armazenar em lista global)
        System.out.println("Solicitação de reorganização enviada. Justificativa: " + justificativa);
    }

    // Getters e setters adicionais
    public List<String> getEspecialidades() { return especialidades; }
    public void setEspecialidades(List<String> especialidades) { this.especialidades = especialidades; }
    public List<Projeto> getProjetos() { return projetos; }
    public void setProjetos(List<Projeto> projetos) { this.projetos = projetos; }
    public List<Tarefa> getTarefas() { return tarefas; }
    public void setTarefas(List<Tarefa> tarefas) { this.tarefas = tarefas; }
}