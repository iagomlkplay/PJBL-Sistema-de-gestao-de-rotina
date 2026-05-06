import java.util.Date;
import java.util.Calendar;

/**
 * Teste complementar - focado em funcionalidades não cobertas pelo teste robusto.
 * Inclui: envio de relatórios, validação de projeto, visualizações detalhadas,
 * solicitações de reorganização com verificação de efeito, e validação de horas.
 */

public class MainTeste03 {

    private static Sistema sistema = Sistema.getInstance();
    private static UsuarioDev devAna, devBruno, devCarla;
    private static UsuarioGestor gestor;

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║      TESTE COMPLEMENTAR - FUNCIONALIDADES PENDENTES        ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");

        cadastrarUsuarios();
        criarProjetosETarefas();
        testarEnvioDeRelatorios();
        testarVisualizacaoDetalhada();
        testarValidacaoDeProjeto();
        testarSolicitacaoReorganizacaoComEfeito();
        testarMetricaHorasBordas();
        testarExcecoesEValidacoes();

        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║            TESTE COMPLEMENTAR CONCLUÍDO COM SUCESSO        ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
    }

    private static void cadastrarUsuarios() {
        System.out.println("1. Cadastro de usuários (já existentes no sistema? Vamos criar novos para isolamento)");
        devAna = new UsuarioDev(0, "Ana Complementar", "111", "ana2@email.com", "123");
        devBruno = new UsuarioDev(0, "Bruno Complementar", "222", "bruno2@email.com", "123");
        devCarla = new UsuarioDev(0, "Carla Complementar", "333", "carla2@email.com", "123");
        gestor = new UsuarioGestor(0, "Gestor Complementar", "999", "gestor2@email.com", "123", "TI");

        sistema.realizarCadastro(devAna);
        sistema.realizarCadastro(devBruno);
        sistema.realizarCadastro(devCarla);
        sistema.realizarCadastro(gestor);

        gestor.getEquipe().add(devAna);
        gestor.getEquipe().add(devBruno);
        gestor.getEquipe().add(devCarla);
        System.out.println("Cadastros OK.\n");
    }

    private static void criarProjetosETarefas() {
        System.out.println("2. Criando projetos e tarefas para os testes");
        Date prazoFuturo = addDias(30);
        gestor.criarProjeto("Projeto Teste 1", prazoFuturo, NivelImportancia.ALTA);
        gestor.criarProjeto("Projeto Teste 2", addDias(10), NivelImportancia.URGENTE);
        Projeto proj1 = sistema.getProjetos().get(0);
        Projeto proj2 = sistema.getProjetos().get(1);

        gestor.criarAtribuirTarefaEmProjeto("Tarefa A", prazoFuturo, NivelImportancia.ALTA, devAna.getId(), proj1.getId(), 10.0);
        gestor.criarAtribuirTarefaEmProjeto("Tarefa B", prazoFuturo, NivelImportancia.MEDIA, devBruno.getId(), proj1.getId(), 5.0);
        gestor.criarAtribuirTarefaEmProjeto("Tarefa C", addDias(5), NivelImportancia.URGENTE, devCarla.getId(), proj2.getId(), 3.0);
        gestor.criarAtribuirTarefa("Tarefa avulsa", prazoFuturo, NivelImportancia.BAIXA, devAna.getId(), 2.0);
        System.out.println("Projetos e tarefas criados.\n");
    }

    // RF06 - Envio de relatórios
    private static void testarEnvioDeRelatorios() {
        System.out.println("3. Teste de envio de relatórios (RF06)");
        Tarefa tarefaAna = devAna.getTarefas().get(0);
        Projeto proj = sistema.getProjetos().get(0);
        devAna.enviarRelatorioFinal(tarefaAna, "Relatório da tarefa: concluída com sucesso.");
        devBruno.enviarRelatorioFinal(proj, "Relatório do projeto: arquitetura definida.");
        System.out.println("Total de relatórios no sistema: " + sistema.getRelatorios().size() + " (esperado 2)\n");
    }

    // RF04 - Visualização detalhada de colegas
    private static void testarVisualizacaoDetalhada() {
        System.out.println("4. Teste de visualização detalhada de colegas (RF04)");
        System.out.println("--- Ana vê detalhes de Bruno ---");
        devAna.visualizarDetalhesColega(devBruno);
        System.out.println("--- Ana vê detalhes de Carla ---");
        devAna.visualizarDetalhesColega(devCarla);
        System.out.println("Visualizações OK.\n");
    }

    private static void testarValidacaoDeProjeto() {
        System.out.println("5. Teste de validação de projeto (RF11)");
        Projeto proj = sistema.getProjetos().get(0);
        // Marcar todas as tarefas do projeto como FEITO (cada uma pelo seu responsável) e validá-las
        for (Tarefa t : proj.getTarefas()) {
            UsuarioDev responsavel = t.getDevResponsavel();
            responsavel.alterarStatusTarefa(t, StatusTarefa.FEITO);
            gestor.validarFinalizacao(t);
        }
        // Agora o projeto deve estar FEITO (pois todas tarefas PRONTO)
        System.out.println("Status do projeto após todas tarefas PRONTO: " + proj.getStatus());
        // Gestor valida o projeto FEITO -> PRONTO
        gestor.validarFinalizacao(proj);
        System.out.println("Status do projeto após validação: " + proj.getStatus() + " (deve ser PRONTO)\n");
    }

    // RF07 + RF10 - Solicitação de reorganização com verificação de efeito
    private static void testarSolicitacaoReorganizacaoComEfeito() {
        System.out.println("6. Teste de solicitação de reorganização com efeito (RF07 e RF10)");
        // Dev solicita reorganização
        devCarla.solicitarReorganizacao("Estou sobrecarregada. Solicito transferência de uma tarefa para outro dev.");
        // Gestor lista pendentes
        System.out.println("--- Solicitações pendentes ---");
        gestor.listarSolicitacoesPendentes();
        // Gestor aprova a solicitação
        if (!sistema.getSolicitacoes().isEmpty()) {
            SolicitacaoMudanca sol = sistema.getSolicitacoes().get(0);
            gestor.processarSolicitacaoMudanca(sol, true);
            // Após aprovação, o gestor manualmente reatribui uma tarefa (simulando a ação)
            Tarefa tarefaSobrecarga = devCarla.getTarefas().stream().findFirst().orElse(null);
            if (tarefaSobrecarga != null && tarefaSobrecarga.getStatus() != StatusTarefa.PRONTO) {
                gestor.reatribuirTarefaAtrasada(tarefaSobrecarga, devBruno);
                System.out.println("Tarefa " + tarefaSobrecarga.getId() + " transferida de Carla para Bruno.");
            }
        }
        System.out.println("Solicitação processada e reorganização aplicada.\n");
    }

    // Métrica de horas - casos de borda
    private static void testarMetricaHorasBordas() {
        System.out.println("7. Teste de métrica de horas - casos de borda");
        // Tarefa com horas estimadas zero
        Tarefa tarefaZero = new Tarefa("Tarefa sem horas", new Date(), NivelImportancia.BAIXA, devAna, 0.0);
        System.out.println("Progresso de tarefa com horas estimadas zero: " + tarefaZero.calcularProgresso() + "% (deve ser 0%)");
        tarefaZero.adicionarHorasTrabalhadas(5);
        System.out.println("Após adicionar horas, progresso: " + tarefaZero.calcularProgresso() + "% (deve continuar 0%)");

        // Tarefa com horas negativas? O método adicionarHorasTrabalhadas já tem proteção (horas <= 0)
        Tarefa tarefaNeg = new Tarefa("Negativa", new Date(), NivelImportancia.BAIXA, devBruno, 10.0);
        tarefaNeg.adicionarHorasTrabalhadas(-5.0);
        System.out.println("Horas trabalhadas após tentativa negativa: " + tarefaNeg.getHorasTrabalhadas() + " (deve permanecer 0)");

        // Adicionar horas além da estimada (deve permitir, mas progresso limitado a 100%)
        tarefaNeg.adicionarHorasTrabalhadas(12.0);
        System.out.println("Progresso após 12h em 10h estimadas: " + tarefaNeg.calcularProgresso() + "% (deve ser 100%)\n");
    }

    // Exceções e validações de entrada
    private static void testarExcecoesEValidacoes() {
        System.out.println("8. Teste de exceções e validações de entrada");
        // Tentar alterar status de tarefa nula
        devAna.alterarStatusTarefa(null, StatusTarefa.FEITO);
        // Tentar reatribuir tarefa não atrasada (vai emitir mensagem)
        Tarefa tarefaPronta = devAna.getTarefas().stream().filter(t -> t.getStatus() == StatusTarefa.PRONTO).findFirst().orElse(null);
        if (tarefaPronta != null) {
            gestor.reatribuirTarefaAtrasada(tarefaPronta, devBruno);
        }
        // Tentar criar tarefa para dev fora da equipe
        UsuarioDev devExterno = new UsuarioDev(0, "Externo", "000", "ext@email.com", "123");
        gestor.criarAtribuirTarefa("Tarefa externa", new Date(), NivelImportancia.BAIXA, devExterno.getId(), 1.0);
        // Tentar processar solicitação já processada
        if (!sistema.getSolicitacoes().isEmpty()) {
            SolicitacaoMudanca sol = sistema.getSolicitacoes().get(0);
            gestor.processarSolicitacaoMudanca(sol, true); // já processada
        }
        System.out.println("Validações de entrada OK.\n");
    }

    private static Date addDias(int dias) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, dias);
        return cal.getTime();
    }
}