import java.util.Date;
import java.util.Calendar;

public class MainTeste01 {
    private static Sistema sistema = Sistema.getInstance();
    private static UsuarioDev devAna, devBruno, devCarla;
    private static UsuarioGestor gestorTI;
    private static Projeto projetoEcommerce;

    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════════════════════════════╗");
        System.out.println("║     TESTE COMPLETO DE TODOS OS MÉTODOS DO SISTEMA (REFATORADO)   ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════════╝\n");

        testCadastroEAutenticacao();
        testCriacaoDeProjetosETarefas();
        testVisualizacoes();
        testEnvioDeRelatorios();
        testMudancaStatusENotificacoes();
        testValidacaoGestor();
        testSolicitacoesReorganizacao();
        testReatribuicoes();
        testPrazosExpirados();
        testRelatorioDiario();
        testMetricaDeHoras();
        testCasosBorda();
        testProgressoFinal();

        System.out.println("\n═══════════════════════════════════════════════════════════════════");
        System.out.println("✅ TESTE COMPLETO FINALIZADO - TODOS OS MÉTODOS VALIDADOS");
        System.out.println("═══════════════════════════════════════════════════════════════════");
    }

    private static void header(String titulo) {
        System.out.println("\n--- " + titulo + " ---");
    }

    private static void testCadastroEAutenticacao() {
        header("1. Cadastro e Autenticação");
        devAna = new UsuarioDev(0, "Ana Silva", "11111111111", "ana@email.com", "ana123");
        devBruno = new UsuarioDev(0, "Bruno Souza", "22222222222", "bruno@email.com", "bruno123");
        devCarla = new UsuarioDev(0, "Carla Lima", "33333333333", "carla@email.com", "carla123");
        gestorTI = new UsuarioGestor(0, "Felipe Gestor", "99999999999", "felipe@email.com", "felipe123", "TI");

        System.out.println("Cadastrando Ana: " + (sistema.realizarCadastro(devAna) ? "sucesso" : "falha"));
        System.out.println("Cadastrando Bruno: " + (sistema.realizarCadastro(devBruno) ? "sucesso" : "falha"));
        System.out.println("Cadastrando Carla: " + (sistema.realizarCadastro(devCarla) ? "sucesso" : "falha"));
        System.out.println("Cadastrando Felipe: " + (sistema.realizarCadastro(gestorTI) ? "sucesso" : "falha"));

        UsuarioDev duplicado = new UsuarioDev(0, "Duplicado", "00000000000", "ana@email.com", "xxxx");
        System.out.println("Cadastro duplicado: " + (sistema.realizarCadastro(duplicado) ? "sucesso (erro)" : "bloqueado - correto"));

        Usuario auth = sistema.autenticar("ana@email.com", "ana123");
        System.out.println("Autenticação correta: " + (auth != null ? "sucesso - " + auth.getNome() : "falha"));
        Usuario authInvalido = sistema.autenticar("ana@email.com", "senhaErrada");
        System.out.println("Autenticação com senha errada: " + (authInvalido == null ? "bloqueado - correto" : "falha"));

        gestorTI.getEquipe().add(devAna);
        gestorTI.getEquipe().add(devBruno);
        gestorTI.getEquipe().add(devCarla);
    }

    private static void testCriacaoDeProjetosETarefas() {
        header("2. Criação de Projetos e Tarefas");
        Date prazoFuturo = addDays(30);
        Date prazoVencido = addDays(-5);

        gestorTI.criarProjeto("E-commerce", prazoFuturo, NivelImportancia.ALTA);
        projetoEcommerce = sistema.getProjetos().stream().filter(p -> p.getNome().equals("E-commerce")).findFirst().orElse(null);
        gestorTI.criarProjeto("App Mobile", prazoVencido, NivelImportancia.URGENTE);

        gestorTI.criarAtribuirTarefa("Documentar API", prazoFuturo, NivelImportancia.MEDIA, devCarla.getId(), 5.0);
        gestorTI.criarAtribuirTarefa("Correção de bug", prazoVencido, NivelImportancia.URGENTE, devAna.getId(), 2.0);

        if (projetoEcommerce != null) {
            gestorTI.criarAtribuirTarefaEmProjeto("Backend - Produtos", prazoFuturo, NivelImportancia.ALTA, devAna.getId(), projetoEcommerce.getId(), 20.0);
            gestorTI.criarAtribuirTarefaEmProjeto("Frontend - Carrinho", prazoFuturo, NivelImportancia.ALTA, devBruno.getId(), projetoEcommerce.getId(), 15.0);
            gestorTI.criarAtribuirTarefaEmProjeto("Testes integrados", prazoFuturo, NivelImportancia.MEDIA, devCarla.getId(), projetoEcommerce.getId(), 8.0);
        }

        System.out.println("Total de projetos: " + sistema.getProjetos().size());
        System.out.println("Total de tarefas: " + sistema.getTarefas().size());
    }

    private static void testVisualizacoes() {
        header("3. Visualizações (RF04 e RF08)");
        System.out.println("--- Ana visualiza seus próprios itens ---");
        devAna.visualizarPropriosProjetosTarefas();
        System.out.println("\n--- Ana visualiza progresso da equipe ---");
        devAna.visualizarProgressoEquipe();
        System.out.println("\n--- Ana vê detalhes da colega Carla ---");
        devAna.visualizarDetalhesColega(devCarla);
        System.out.println("\n--- Gestor visualiza todos os projetos/tarefas da equipe ---");
        gestorTI.visualizarTodosProjetosTarefas();
    }

    private static void testEnvioDeRelatorios() {
        header("4. Envio de Relatórios (RF06)");
        Tarefa tarefa = devCarla.getTarefas().stream().filter(t -> t.getDescricao().equals("Documentar API")).findFirst().orElse(null);
        if (tarefa != null) devCarla.enviarRelatorioFinal(tarefa, "Documentação completa da API REST.");
        if (projetoEcommerce != null) devAna.enviarRelatorioFinal(projetoEcommerce, "Projeto E-commerce - arquitetura definida.");
        System.out.println("Relatórios enviados: " + sistema.getRelatorios().size() + " (esperado 2)");
    }

    private static void testMudancaStatusENotificacoes() {
        header("5. Mudança de Status e Notificações (RF05, RF13, RF16, RF17)");
        Tarefa tarefaAna = devAna.getTarefas().stream().filter(t -> t.getDescricao().equals("Correção de bug")).findFirst().orElse(null);
        if (tarefaAna != null) {
            System.out.println("---- Ana altera 'Correção de bug' para FEITO ----");
            devAna.alterarStatusTarefa(tarefaAna, StatusTarefa.FEITO);
        }
        Tarefa tarefaBruno = devBruno.getTarefas().stream().filter(t -> t.getDescricao().equals("Frontend - Carrinho")).findFirst().orElse(null);
        if (tarefaBruno != null) {
            System.out.println("---- Bruno tenta alterar tarefa que não lhe pertence ----");
            devAna.alterarStatusTarefa(tarefaBruno, StatusTarefa.FEITO);
        }
    }

    private static void testValidacaoGestor() {
        header("6. Validação do Gestor (RF11)");
        Tarefa tarefaFeita = devAna.getTarefas().stream().filter(t -> t.getStatus() == StatusTarefa.FEITO).findFirst().orElse(null);
        if (tarefaFeita != null) {
            System.out.println("Gestor valida tarefa FEITO -> PRONTO");
            gestorTI.validarFinalizacao(tarefaFeita);
            System.out.println("Status agora: " + tarefaFeita.getStatus());
        }
        if (projetoEcommerce != null) {
            System.out.println("Gestor tenta validar projeto que ainda não está FEITO:");
            gestorTI.validarFinalizacao(projetoEcommerce);
            System.out.println("Status do projeto: " + projetoEcommerce.getStatus());
            System.out.println("---- Concluindo todas as tarefas do projeto ----");
            for (Tarefa t : projetoEcommerce.getTarefas()) {
                if (t.getStatus() != StatusTarefa.PRONTO) {
                    UsuarioDev responsavel = t.getDevResponsavel();
                    responsavel.alterarStatusTarefa(t, StatusTarefa.FEITO);
                    gestorTI.validarFinalizacao(t);
                }
            }
            System.out.println("Status do projeto após todas tarefas PRONTO: " + projetoEcommerce.getStatus());
            System.out.println("Gestor valida projeto FEITO -> PRONTO");
            gestorTI.validarFinalizacao(projetoEcommerce);
            System.out.println("Status final do projeto: " + projetoEcommerce.getStatus());
        }
    }

    private static void testSolicitacoesReorganizacao() {
        header("7. Solicitações de Reorganização (RF07, RF10)");
        devBruno.solicitarReorganizacao("Muitas tarefas urgentes, preciso de ajuda.");
        devCarla.solicitarReorganizacao("Prazo do projeto App Mobile muito curto.");
        System.out.println("Total de solicitações: " + sistema.getSolicitacoes().size());
        gestorTI.listarSolicitacoesPendentes();
        if (sistema.getSolicitacoes().size() >= 2) {
            gestorTI.processarSolicitacaoMudanca(sistema.getSolicitacoes().get(0), true);
            gestorTI.processarSolicitacaoMudanca(sistema.getSolicitacoes().get(1), false);
        }
        System.out.println("--- Após processamento ---");
        gestorTI.listarSolicitacoesPendentes();
    }

    private static void testReatribuicoes() {
        header("8. Reatribuição de tarefas atrasadas (RF12)");
        sistema.verificarPrazosExpirados();
        Tarefa tarefaAtrasada = devBruno.getTarefas().stream().filter(t -> t.getStatus() == StatusTarefa.ATRASADO).findFirst().orElse(null);
        if (tarefaAtrasada != null) {
            System.out.println("Reatribuindo tarefa atrasada " + tarefaAtrasada.getId() + " de " + tarefaAtrasada.getDevResponsavel().getNome() + " para Ana");
            gestorTI.reatribuirTarefaAtrasada(tarefaAtrasada, devAna);
            System.out.println("Nova responsável: " + tarefaAtrasada.getDevResponsavel().getNome());
        } else {
            System.out.println("Nenhuma tarefa atrasada para reatribuir.");
        }
    }

    private static void testPrazosExpirados() {
        header("9. Expiração de prazos automática (RF15)");
        Date prazoPassado = addDays(-10);
        Tarefa tarefaQueExpira = new Tarefa("Tarefa esquecida", prazoPassado, NivelImportancia.BAIXA, devCarla, 3.0);
        sistema.adicionarTarefa(tarefaQueExpira);
        devCarla.getTarefas().add(tarefaQueExpira);
        System.out.println("Tarefa criada com prazo passado, status inicial: " + tarefaQueExpira.getStatus());
        sistema.verificarPrazosExpirados();
        System.out.println("Após verificação, status: " + tarefaQueExpira.getStatus());
    }

    private static void testRelatorioDiario() {
        header("10. Relatório Diário (RF14)");
        sistema.gerarRelatorioDiario();
    }

    private static void testMetricaDeHoras() {
        header("11. Métrica de horas trabalhadas");
        Tarefa tarefa = devAna.getTarefas().get(0);
        System.out.println("Tarefa: " + tarefa.getDescricao());
        System.out.println("Horas estimadas: " + tarefa.getHorasEstimadas());
        System.out.println("Horas trabalhadas: " + tarefa.getHorasTrabalhadas());
        System.out.println("Progresso inicial: " + tarefa.calcularProgresso() + "%");
        tarefa.adicionarHorasTrabalhadas(5.0);
        System.out.println("Progresso após 5h: " + tarefa.calcularProgresso() + "%");
        tarefa.adicionarHorasTrabalhadas(tarefa.getHorasEstimadas() - 5);
        System.out.println("Progresso após completar estimada: " + tarefa.calcularProgresso() + "%");
        tarefa.setStatus(StatusTarefa.FEITO);
        tarefa.adicionarHorasTrabalhadas(10);
        System.out.println("Progresso após tentar adicionar horas em tarefa FEITO: " + tarefa.calcularProgresso() + "% (deve permanecer 100%)");
    }

    private static void testCasosBorda() {
        header("12. Casos de borda e validações");
        UsuarioDev fakeDev = new UsuarioDev(0, "Fake", "000", "fake@email.com", "123");
        sistema.realizarCadastro(fakeDev);
        System.out.println("Tentativa de criar tarefa para dev fora da equipe:");
        gestorTI.criarAtribuirTarefa("Tarefa inválida", new Date(), NivelImportancia.BAIXA, fakeDev.getId(), 1.0);
        System.out.println("Tentativa de alterar status de tarefa nula:");
        devAna.alterarStatusTarefa(null, StatusTarefa.FEITO);
        Tarefa tarefaOk = devAna.getTarefas().stream().filter(t -> t.getStatus() != StatusTarefa.ATRASADO).findFirst().orElse(null);
        if (tarefaOk != null) {
            System.out.println("Tentativa de reatribuir tarefa não atrasada:");
            gestorTI.reatribuirTarefaAtrasada(tarefaOk, devBruno);
        }
        if (sistema.getSolicitacoes().size() > 0) {
            System.out.println("Tentativa de processar solicitação já processada:");
            gestorTI.processarSolicitacaoMudanca(sistema.getSolicitacoes().get(0), true);
        }
    }

    private static void testProgressoFinal() {
        header("13. Progresso final dos desenvolvedores");
        System.out.println("Ana: " + String.format("%.2f", devAna.calcularProgressoTotal()) + "%");
        System.out.println("Bruno: " + String.format("%.2f", devBruno.calcularProgressoTotal()) + "%");
        System.out.println("Carla: " + String.format("%.2f", devCarla.calcularProgressoTotal()) + "%");
        System.out.println("\n--- Detalhes de Bruno ---");
        devBruno.visualizarPropriosProjetosTarefas();
    }

    private static Date addDays(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, days);
        return cal.getTime();
    }
}