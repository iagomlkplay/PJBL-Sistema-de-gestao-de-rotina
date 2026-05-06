import java.util.Date;

public class Main {
    static void main(String[] args) {
        Sistema sistema = Sistema.getInstance();

        System.out.println("===== Sistema de Gestão de Rotina =====");

        // 1. Cadastro de usuários (RF01)
        System.out.println("\n1. Cadastrando usuários...");
        UsuarioDev dev1 = new UsuarioDev(0, "João Silva", "12345678900", "joao@email.com", "senha123");
        UsuarioDev dev2 = new UsuarioDev(0, "Maria Oliveira", "98765432100", "maria@email.com", "senha456");
        UsuarioGestor gestor = new UsuarioGestor(0, "Carlos Gestor", "11122233344", "carlos@email.com", "gestor123", "TI");

        sistema.realizarCadastro(dev1);
        sistema.realizarCadastro(dev2);
        sistema.realizarCadastro(gestor);
        gestor.getEquipe().add(dev1);
        gestor.getEquipe().add(dev2);

        System.out.println("Usuários cadastrados: " + sistema.getUsuarios().size());

        // 2. Teste de autenticação (RF01)
        System.out.println("\n2. Testando autenticação...");
        Usuario autenticado = sistema.autenticar("12345678900", "senha123");
        System.out.println(autenticado != null ? "Autenticado: " + autenticado.getNome() : "Falha na autenticação");

        // 3. Gestor cria projetos e tarefas (RF09)
        System.out.println("\n3. Gestor cria projetos e tarefas...");
        Date prazoFuturo = new Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000);
        Date prazoPassado = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);

        gestor.criarAtribuirProjeto("Sistema Web", prazoFuturo, NivelImportancia.ALTA, dev1.getId());
        gestor.criarAtribuirTarefa("Implementar login", prazoFuturo, NivelImportancia.ALTA, dev1.getId(), 10.0);
        gestor.criarAtribuirTarefaEmProjeto("Criar banco de dados", prazoFuturo, NivelImportancia.ALTA, dev1.getId(), 1, 8.0);
        gestor.criarAtribuirTarefaEmProjeto("Desenvolver API", prazoFuturo, NivelImportancia.ALTA, dev1.getId(), 1, 20.0);

        gestor.criarAtribuirProjeto("App Mobile", prazoPassado, NivelImportancia.URGENTE, dev2.getId());
        gestor.criarAtribuirTarefa("Tela de login", prazoPassado, NivelImportancia.URGENTE, dev2.getId(), 5.0);

        // 4. Visualizações (RF04, RF08)
        System.out.println("\n4. Visualizações...");
        dev1.visualizarPropriosProjetosTarefas();
        dev1.visualizarProgressoEquipe();
        dev1.visualizarDetalhesColega(dev2);
        gestor.visualizarTodosProjetosTarefas();

        // 5. Dev altera status de tarefa (RF05)
        System.out.println("\n5. Dev altera status de tarefa...");
        Tarefa tarefaLogin = dev1.getTarefas().get(0); // Implementar login
        dev1.alterarStatusTarefa(tarefaLogin, StatusTarefa.FEITO);

        // 6. Dev envia relatório (RF06)
        System.out.println("\n6. Dev envia relatório...");
        dev1.enviarRelatorioFinal(tarefaLogin, "Login implementado com sucesso.");

        // 7. Gestor valida finalização (RF11)
        System.out.println("\n7. Gestor valida finalização...");
        gestor.validarFinalizacao(tarefaLogin);

        // 8. Testar verificação de conclusão do projeto (todas tarefas PRONTO?)
        System.out.println("\n8. Verificando conclusão do projeto...");
        Projeto projetoWeb = gestor.getEquipe().get(0).getProjetos().get(0);
        System.out.println("Status do projeto antes de concluir tarefas: " + projetoWeb.getStatus());
        // Marcar as outras tarefas como FEITO
        for (Tarefa t : projetoWeb.getTarefas()) {
            if (t.getStatus() != StatusTarefa.PRONTO) {
                dev1.alterarStatusTarefa(t, StatusTarefa.FEITO);
                gestor.validarFinalizacao(t);
            }
        }
        // Agora todas devem estar PRONTO, projeto deve virar FEITO automaticamente
        System.out.println("Status do projeto após concluir tarefas: " + projetoWeb.getStatus());

        // 9. Gestor valida projeto como PRONTO
        System.out.println("\n9. Gestor valida projeto como PRONTO...");
        gestor.validarFinalizacao(projetoWeb);
        System.out.println("Status do projeto após validação: " + projetoWeb.getStatus());

        // 10. Solicitação de reorganização (RF07) e processamento (RF10)
        System.out.println("\n10. Solicitação de reorganização...");
        dev2.solicitarReorganizacao("Sobrecarga de tarefas urgentes");
        gestor.listarSolicitacoesPendentes();
        SolicitacaoMudanca sol = sistema.getSolicitacoes().get(0);
        gestor.processarSolicitacaoMudanca(sol, true);

        // 11. Reatribuição de tarefa atrasada (RF12)
        System.out.println("\n11. Reatribuição de tarefa atrasada...");
        Tarefa tarefaAtrasada = dev2.getTarefas().get(0); // Tela de login (prazo passado)
        // Forçar atraso via expiração (já está passado, mas pode chamar verificarPrazosExpirados)
        sistema.verificarPrazosExpirados();
        gestor.reatribuirTarefaAtrasada(tarefaAtrasada, dev1);
        System.out.println("Tarefa agora está com dev1: " + dev1.getTarefas().contains(tarefaAtrasada));

        // 12. Reatribuição de projeto atrasado (RF12)
        System.out.println("\n12. Reatribuição de projeto atrasado...");
        Projeto projetoAtrasado = dev2.getProjetos().get(0); // App Mobile
        gestor.reatribuirProjetoAtrasado(projetoAtrasado, dev1);
        System.out.println("Projeto agora está com dev1: " + dev1.getProjetos().contains(projetoAtrasado));

        // 13. Relatório diário (RF14)
        System.out.println("\n13. Gerando relatório diário...");
        sistema.gerarRelatorioDiario();

        // 14. Teste de horas trabalhadas
        System.out.println("\n14. Testando adição de horas trabalhadas...");
        Tarefa tarefaApi = projetoWeb.getTarefas().get(1); // Desenvolver API
        tarefaApi.adicionarHorasTrabalhadas(10);
        System.out.println("Progresso da tarefa após 10h: " + tarefaApi.calcularProgresso() + "%");
        tarefaApi.adicionarHorasTrabalhadas(10);
        System.out.println("Progresso após mais 10h (total 20h): " + tarefaApi.calcularProgresso() + "%");
        // Agora marcar como FEITO
        dev1.alterarStatusTarefa(tarefaApi, StatusTarefa.FEITO);
        gestor.validarFinalizacao(tarefaApi);
        System.out.println("Progresso final: " + tarefaApi.calcularProgresso() + "%");

        // 15. Teste de expiração de prazo (RF15) - criar tarefa com prazo passado e verificar
        System.out.println("\n15. Teste de expiração de prazo...");
        Tarefa tarefaExpirada = new Tarefa("Tarefa expirada", prazoPassado, NivelImportancia.BAIXA, dev2, 2.0);
        sistema.adicionarTarefa(tarefaExpirada);
        dev2.getTarefas().add(tarefaExpirada);
        sistema.verificarPrazosExpirados();
        System.out.println("Status da tarefa expirada: " + tarefaExpirada.getStatus());

        // 16. Teste de métrica de tempo e progresso total do dev
        System.out.println("\n16. Progresso total dos devs:");
        System.out.println("João (dev1): " + dev1.calcularProgressoTotal() + "%");
        System.out.println("Maria (dev2): " + dev2.calcularProgressoTotal() + "%");

        System.out.println("\n===== Fim dos testes =====");
    }
}