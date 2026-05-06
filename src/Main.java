import java.util.Date;

public class Main {
    public static void main(String[] args) {
        Sistema sistema = Sistema.getInstance();

        // RF01: Cadastro
        UsuarioDev dev1 = new UsuarioDev(0, "João Silva", "12345678900", "joao@email.com", "senha123");
        UsuarioDev dev2 = new UsuarioDev(0, "Maria Oliveira", "98765432100", "maria@email.com", "senha456");
        UsuarioGestor gestor = new UsuarioGestor(0, "Carlos Gestor", "11122233344", "carlos@email.com", "gestor123", "TI");

        sistema.realizarCadastro(dev1);
        sistema.realizarCadastro(dev2);
        sistema.realizarCadastro(gestor);

        // Associa devs à equipe do gestor
        gestor.getEquipe().add(dev1);
        gestor.getEquipe().add(dev2);

        System.out.println("=== Usuários cadastrados ===");
        System.out.println("Dev1: " + dev1.getNome() + " (ID " + dev1.getId() + ")");
        System.out.println("Dev2: " + dev2.getNome() + " (ID " + dev2.getId() + ")");
        System.out.println("Gestor: " + gestor.getNome() + " (ID " + gestor.getId() + ")");
        System.out.println();

        // RF09: Gestor cria e atribui tarefas
        Date prazoAmanha = new Date(System.currentTimeMillis() + 86400000);
        Date prazoOntem = new Date(System.currentTimeMillis() - 86400000); // já expirado

        gestor.criarAtribuirTarefa("Implementar login", prazoAmanha, NivelImportancia.ALTA, dev1.getId());
        gestor.criarAtribuirTarefa("Criar banco de dados", prazoOntem, NivelImportancia.URGENTE, dev2.getId());

        System.out.println();

        // RF04: Dev visualiza seus itens e progresso dos colegas
        dev1.visualizarPropriosProjetosTarefas();
        System.out.println();
        dev1.visualizarProgressoEquipe();
        System.out.println();

        // RF05: Dev altera status da tarefa para FEITO
        Tarefa tarefaDev1 = dev1.getTarefas().get(0);
        dev1.alterarStatusTarefa(tarefaDev1, StatusTarefa.FEITO);
        System.out.println();

        // RF06: Dev envia relatório final
        dev1.enviarRelatorioFinal(tarefaDev1, "Implementação do login concluída com sucesso.");
        System.out.println();

        // RF11: Gestor valida finalização
        gestor.validarFinalizacao(tarefaDev1);
        System.out.println();

        // RF07 e RF10: Dev solicita reorganização e gestor processa
        dev2.solicitarReorganizacao("Sobrecarga de tarefas urgentes.");
        System.out.println();
        gestor.listarSolicitacoesPendentes();
        SolicitacaoMudanca solicitacao = sistema.getSolicitacoes().get(0);
        gestor.processarSolicitacaoMudanca(solicitacao, true);
        System.out.println();

        // RF15: Verificar prazos expirados (a tarefa do dev2 deve ficar ATRASADA)
        sistema.verificarPrazosExpirados();
        System.out.println();

        // RF12: Gestor reatribui tarefa atrasada para dev1
        Tarefa tarefaAtrasada = dev2.getTarefas().get(0);
        gestor.reatribuirTarefaAtrasada(tarefaAtrasada, dev1);
        System.out.println();

        // RF14: Gerar relatório diário
        sistema.gerarRelatorioDiario();
        System.out.println();

        // RF08: Gestor visualiza todos os projetos/tarefas da equipe
        gestor.visualizarTodosProjetosTarefas();

        // Após toda a execução, no final do main:
        System.out.println("\n=== VALIDAÇÃO DE PROGRESSO ===");
        System.out.println("Progresso da tarefa do dev1: " + dev1.getTarefas().get(0).calcularProgresso() + "%");
        System.out.println("Progresso do dev1: " + dev1.calcularProgressoTotal() + "%");
        System.out.println("Progresso do dev2: " + dev2.calcularProgressoTotal() + "%");
    }
}