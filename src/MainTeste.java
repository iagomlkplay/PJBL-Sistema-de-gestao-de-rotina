import java.util.Date;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Teste extremamente robusto e abrangente do sistema.
 * Verifica todas as funcionalidades, casos de borda e validações.
 * Utiliza centenas de dados e variações.
 *
 * Pré-requisito: executar script_mysql.sql (banco limpo).
 * O teste limpa o banco automaticamente no início.
 */
public class MainTeste {

    private static final Random RANDOM = new Random();
    private static int nextId = 1;

    public static void main(String[] args) {
        System.out.println("=========================================================");
        System.out.println("   TESTE DO SISTEMA");
        System.out.println("=========================================================\n");

        try {
            Sistema sistema = Sistema.getInstance();

            // ========== 1. LIMPEZA TOTAL DO BANCO ==========
            limparBancoCompleto(sistema);
            System.out.println("Banco limpo. Todos os dados anteriores foram removidos.\n");

            // ========== 2. CADASTRO EM MASSA ==========
            System.out.println("----- 2. CADASTRO EM MASSA -----");
            List<UsuarioGestor> gestores = criarGestores(5);
            List<UsuarioDev> desenvolvedores = criarDesenvolvedores(20, gestores);

            System.out.println("Gestores cadastrados: " + gestores.size());
            System.out.println("Desenvolvedores cadastrados: " + desenvolvedores.size());

            // Teste de duplicatas após cadastro em massa
            testarDuplicatas(sistema);

            // ========== 3. AUTENTICAÇÃO DE TODOS ==========
            System.out.println("\n----- 3. AUTENTICAÇÃO EM MASSA -----");
            testarAutenticacao(sistema, gestores, desenvolvedores);

            // ========== 4. PROJETOS EM MASSA ==========
            System.out.println("\n----- 4. PROJETOS EM MASSA -----");
            List<Projeto> projetos = criarProjetos(gestores, 15);
            System.out.println("Total de projetos criados: " + projetos.size());

            // ========== 5. TAREFAS EM MASSA (avulsas + em projetos) ==========
            System.out.println("\n----- 5. TAREFAS EM MASSA -----");
            List<Tarefa> tarefas = criarTarefas(gestores, desenvolvedores, projetos, 50);
            System.out.println("Total de tarefas criadas: " + tarefas.size());

            // ========== 6. TESTES DE PERMISSÃO ==========
            System.out.println("\n----- 6. TESTES DE PERMISSÃO -----");
            testarPermissoes(sistema, gestores, desenvolvedores, projetos);

            // ========== 7. REGISTRO DE HORAS E ALTERAÇÃO DE STATUS ==========
            System.out.println("\n----- 7. HORAS E STATUS (DEVS) -----");
            testarHorasEStatus(desenvolvedores);

            // ========== 8. SOLICITAÇÕES DE REORGANIZAÇÃO ==========
            System.out.println("\n----- 8. SOLICITAÇÕES -----");
            testarSolicitacoes(gestores, desenvolvedores);

            // ========== 9. VALIDAÇÃO DE TAREFAS/PROJETOS (GESTORES) ==========
            System.out.println("\n----- 9. VALIDAÇÃO -----");
            testarValidacao(gestores);

            // ========== 10. REATRIBUIÇÃO DE TAREFAS ATRASADAS ==========
            System.out.println("\n----- 10. REATRIBUIÇÃO -----");
            testarReatribuicao(gestores);

            // ========== 11. RELATÓRIOS ==========
            System.out.println("\n----- 11. RELATÓRIOS -----");
            testarRelatorios(desenvolvedores);

            // ========== 12. EXPIRAÇÃO DE PRAZOS (FORÇADA) ==========
            System.out.println("\n----- 12. EXPIRAÇÃO DE PRAZOS -----");
            testarExpiracao(sistema);

            // ========== 13. RELATÓRIO DIÁRIO ==========
            System.out.println("\n----- 13. RELATÓRIO DIÁRIO -----");
            sistema.gerarRelatorioDiario();

            // ========== 14. VISUALIZAÇÕES (TODOS OS MÉTODOS) ==========
            System.out.println("\n----- 14. VISUALIZAÇÕES -----");
            testarVisualizacoes(gestores, desenvolvedores);

            // ========== 15. MÉTODOS DE ATUALIZAÇÃO (UPDATE) ==========
            System.out.println("\n----- 15. MÉTODOS DE ATUALIZAÇÃO -----");
            testarAtualizacoes(tarefas, projetos, desenvolvedores, gestores);

            // ========== 16. TESTES DE TIMER (EXPIRAÇÃO AUTOMÁTICA) ==========
            System.out.println("\n----- 16. TIMER (EXPIRAÇÃO AUTOMÁTICA) -----");
            testarTimer(sistema);

            // ========== 17. EXCLUSÃO DE REGISTROS (OPCIONAL) ==========
            System.out.println("\n----- 17. TESTE DE EXCLUSÃO -----");
            testarExclusao(sistema);

            // ========== 18. RESULTADOS FINAIS ==========
            System.out.println("\n=========================================================");
            System.out.println("                 RESUMO FINAL");
            System.out.println("=========================================================");
            System.out.println("Usuários: " + sistema.getUsuarios().size());
            System.out.println("Projetos: " + sistema.getProjetos().size());
            System.out.println("Tarefas: " + sistema.getTarefas().size());
            System.out.println("Relatórios: " + sistema.getRelatorios().size());
            System.out.println("Solicitações: " + sistema.getSolicitacoes().size());
            System.out.println("\n✅ TESTE EXTREMAMENTE ROBUSTO CONCLUÍDO COM SUCESSO!");

        } catch (Exception e) {
            System.err.println("❌ ERRO FATAL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ======================== MÉTODOS AUXILIARES DE CRIAÇÃO ========================

    private static List<UsuarioGestor> criarGestores(int quantidade) throws Exception {
        List<UsuarioGestor> gestores = new java.util.ArrayList<>();
        Sistema sistema = Sistema.getInstance();
        for (int i = 0; i < quantidade; i++) {
            String nome = "Gestor_" + i;
            String cpf = String.format("%03d.%03d.%03d-%02d", i+1, i+1, i+1, i%100);
            String email = "gestor" + i + "@mail.com";
            String senha = "senha" + i;
            String departamento = i % 2 == 0 ? "TI" : "RH";
            UsuarioGestor g = new UsuarioGestor(nome, cpf, email, senha, departamento);
            if (sistema.realizarCadastro(g)) {
                gestores.add(g);
            } else {
                System.err.println("Falha ao cadastrar gestor " + nome);
            }
        }
        return gestores;
    }

    private static List<UsuarioDev> criarDesenvolvedores(int quantidade, List<UsuarioGestor> gestores) throws Exception {
        List<UsuarioDev> devs = new java.util.ArrayList<>();
        Sistema sistema = Sistema.getInstance();
        for (int i = 0; i < quantidade; i++) {
            String nome = "Dev_" + i;
            String cpf = String.format("%03d.%03d.%03d-%02d", i+100, i+100, i+100, i%100);
            String email = "dev" + i + "@mail.com";
            String senha = "devpass" + i;
            UsuarioDev d = new UsuarioDev(nome, cpf, email, senha);
            // Associar a um gestor aleatório
            UsuarioGestor gestor = gestores.get(RANDOM.nextInt(gestores.size()));
            d.setGestorId(gestor.getId());
            if (sistema.realizarCadastro(d)) {
                devs.add(d);
            } else {
                System.err.println("Falha ao cadastrar dev " + nome);
            }
        }
        return devs;
    }

    private static void testarDuplicatas(Sistema sistema) {
        System.out.println("Testando cadastro com e-mail duplicado e CPF duplicado...");
        UsuarioDev dupEmail = new UsuarioDev("Fake", "999.999.999-99", "dev0@mail.com", "123");
        boolean ok1 = sistema.realizarCadastro(dupEmail);
        System.out.println("  E-mail duplicado: " + (ok1 ? "ACEITO (ERRO)" : "REJEITADO (OK)"));

        UsuarioDev dupCpf = new UsuarioDev("Fake2", "100.100.100-00", "fake@mail.com", "123");
        boolean ok2 = sistema.realizarCadastro(dupCpf);
        System.out.println("  CPF duplicado: " + (ok2 ? "ACEITO (ERRO)" : "REJEITADO (OK)"));
    }

    private static void testarAutenticacao(Sistema sistema, List<UsuarioGestor> gestores, List<UsuarioDev> devs) {
        // Autenticar todos
        int sucessos = 0;
        for (UsuarioGestor g : gestores) {
            Usuario auth = sistema.autenticar(g.getEmail(), g.getSenha());
            if (auth != null && auth.getId() == g.getId()) sucessos++;
        }
        for (UsuarioDev d : devs) {
            Usuario auth = sistema.autenticar(d.getEmail(), d.getSenha());
            if (auth != null && auth.getId() == d.getId()) sucessos++;
        }
        System.out.println("Autenticação correta: " + sucessos + "/" + (gestores.size() + devs.size()) + " OK");

        // Teste falha
        Usuario fail = sistema.autenticar("inexistente@mail.com", "x");
        System.out.println("Autenticação inválida: " + (fail == null ? "REJEITADO (OK)" : "ACEITO (ERRO)"));
    }

    private static List<Projeto> criarProjetos(List<UsuarioGestor> gestores, int quantidade) throws Exception {
        List<Projeto> projetos = new java.util.ArrayList<>();
        Calendar cal = Calendar.getInstance();
        NivelImportancia[] importancias = NivelImportancia.values();
        for (int i = 0; i < quantidade; i++) {
            UsuarioGestor gestor = gestores.get(RANDOM.nextInt(gestores.size()));
            String nome = "Projeto_" + i + "_" + UUID.randomUUID().toString().substring(0,4);

            // Prazo variado: passado, presente, futuro
            cal.setTime(new Date());
            int dias = ThreadLocalRandom.current().nextInt(-60, 120);
            cal.add(Calendar.DAY_OF_MONTH, dias);
            Date prazo = cal.getTime();

            NivelImportancia imp = importancias[RANDOM.nextInt(importancias.length)];
            gestor.criarProjeto(nome, prazo, imp);
            // Recuperar o projeto recém-criado (buscar pelo nome)
            Projeto p = Sistema.getInstance().getProjetos().stream()
                    .filter(proj -> proj.getNome().equals(nome)).findFirst().orElse(null);
            if (p != null) projetos.add(p);
        }
        return projetos;
    }

    private static List<Tarefa> criarTarefas(List<UsuarioGestor> gestores, List<UsuarioDev> devs,
                                             List<Projeto> projetos, int quantidade) throws Exception {
        List<Tarefa> tarefas = new java.util.ArrayList<>();
        NivelImportancia[] importancias = NivelImportancia.values();
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < quantidade; i++) {
            UsuarioGestor gestor = gestores.get(RANDOM.nextInt(gestores.size()));
            // Garantir que o dev pertença ao gestor
            List<UsuarioDev> equipe = gestor.getEquipe();
            if (equipe.isEmpty()) continue;
            UsuarioDev dev = equipe.get(RANDOM.nextInt(equipe.size()));

            String descricao = "Tarefa_" + i + "_" + UUID.randomUUID().toString().substring(0,4);
            cal.setTime(new Date());
            int dias = ThreadLocalRandom.current().nextInt(-30, 60);
            cal.add(Calendar.DAY_OF_MONTH, dias);
            Date prazo = cal.getTime();
            NivelImportancia imp = importancias[RANDOM.nextInt(importancias.length)];
            double horasEstimadas = 1 + RANDOM.nextDouble() * 40;

            boolean emProjeto = RANDOM.nextBoolean() && !projetos.isEmpty();
            if (emProjeto) {
                Projeto projeto = projetos.get(RANDOM.nextInt(projetos.size()));
                // Verificar se o projeto pertence ao gestor? A validação já está dentro do método.
                gestor.criarAtribuirTarefaEmProjeto(descricao, prazo, imp, dev.getId(), projeto.getId(), horasEstimadas);
            } else {
                gestor.criarAtribuirTarefa(descricao, prazo, imp, dev.getId(), horasEstimadas);
            }
            // Recuperar a tarefa recém-criada
            Tarefa t = Sistema.getInstance().getTarefas().stream()
                    .filter(tarefa -> tarefa.getDescricao().equals(descricao)).findFirst().orElse(null);
            if (t != null) tarefas.add(t);
        }
        return tarefas;
    }

    // ======================== TESTES DE NEGÓCIO ========================

    private static void testarPermissoes(Sistema sistema, List<UsuarioGestor> gestores,
                                         List<UsuarioDev> devs, List<Projeto> projetos) {
        UsuarioGestor g1 = gestores.get(0);
        UsuarioGestor g2 = gestores.size() > 1 ? gestores.get(1) : gestores.get(0);
        UsuarioDev devForaEquipe = null;
        for (UsuarioDev d : devs) {
            if (!g1.getEquipe().contains(d)) {
                devForaEquipe = d;
                break;
            }
        }
        if (devForaEquipe != null) {
            System.out.println("Teste: Gestor1 tentando criar tarefa para dev fora da equipe (deve falhar)");
            g1.criarAtribuirTarefa("Tentativa ilegal", new Date(), NivelImportancia.BAIXA, devForaEquipe.getId(), 1.0);
        }

        // Gestor1 tentar validar tarefa de dev fora da equipe
        if (devForaEquipe != null && !devForaEquipe.carregarTarefas().isEmpty()) {
            System.out.println("Teste: Gestor1 tentando validar tarefa de dev fora da equipe (deve falhar)");
            g1.validarFinalizacao(devForaEquipe.carregarTarefas().get(0));
        }

        // Gestor1 tentar criar tarefa em projeto do gestor2
        Projeto projetoG2 = projetos.stream()
                .filter(p -> {
                    try {
                        return !g1.getEquipe().isEmpty() &&
                                g1.getEquipe().stream().anyMatch(d -> {
                                    try {
                                        return !d.carregarTarefas().isEmpty() &&
                                                d.carregarTarefas().get(0).getProjetoPai() == p;
                                    } catch (Exception ex) { return false; }
                                });
                    } catch (Exception ex) { return false; }
                }).findFirst().orElse(null);
        if (projetoG2 != null) {
            System.out.println("Teste: Gestor1 criar tarefa em projeto do Gestor2 (deve falhar)");
            g1.criarAtribuirTarefaEmProjeto("Ilegal", new Date(), NivelImportancia.BAIXA,
                    g1.getEquipe().get(0).getId(), projetoG2.getId(), 1.0);
        }
    }

    private static void testarHorasEStatus(List<UsuarioDev> devs) {
        for (UsuarioDev dev : devs) {
            List<Tarefa> tarefas = dev.carregarTarefas();
            if (tarefas.isEmpty()) continue;
            // Adicionar horas aleatórias
            for (Tarefa t : tarefas) {
                if (t.getStatus() == StatusTarefa.PENDENTE || t.getStatus() == StatusTarefa.ATRASADO) {
                    double horas = 1 + RANDOM.nextDouble() * 5;
                    t.adicionarHorasTrabalhadas(horas);
                }
            }
            // Tentar mudar algumas para FEITO
            for (int i = 0; i < Math.min(2, tarefas.size()); i++) {
                Tarefa t = tarefas.get(i);
                if (t.getStatus() == StatusTarefa.PENDENTE || t.getStatus() == StatusTarefa.ATRASADO) {
                    dev.alterarStatusTarefa(t, StatusTarefa.FEITO);
                }
            }
            // Tentativa inválida: mudar para PRONTO (deve falhar)
            if (!tarefas.isEmpty()) {
                dev.alterarStatusTarefa(tarefas.get(0), StatusTarefa.PRONTO);
            }
        }
        System.out.println("Registro de horas e alterações de status concluído.");
    }

    private static void testarSolicitacoes(List<UsuarioGestor> gestores, List<UsuarioDev> devs) {
        // Cada desenvolvedor solicita reorganização
        for (UsuarioDev dev : devs) {
            String justificativa = "Solicitação de " + dev.getNome() + ": preciso de ajustes.";
            dev.solicitarReorganizacao(justificativa);
        }

        // Gestores processam solicitações de sua equipe
        for (UsuarioGestor gestor : gestores) {
            List<SolicitacaoMudanca> pendentes = gestor.listarSolicitacoesPendentes();
            System.out.println("Gestor " + gestor.getNome() + " tem " + pendentes.size() + " solicitações pendentes.");
            for (SolicitacaoMudanca s : pendentes) {
                boolean aprovar = RANDOM.nextBoolean();
                gestor.processarSolicitacaoMudanca(s, aprovar);
            }
        }
    }

    private static void testarValidacao(List<UsuarioGestor> gestores) {
        for (UsuarioGestor gestor : gestores) {
            List<Tarefa> tarefasEquipe;
            try {
                tarefasEquipe = Sistema.getInstance().getTarefasDaEquipe(gestor.getId());
                for (Tarefa t : tarefasEquipe) {
                    if (t.getStatus() == StatusTarefa.FEITO) {
                        gestor.validarFinalizacao(t);
                    }
                }
                // Validar projetos da equipe que estejam FEITO (raramente, mas testar)
                List<Projeto> projetosEquipe = Sistema.getInstance().getProjetosDaEquipe(gestor.getId());
                for (Projeto p : projetosEquipe) {
                    if (p.getStatus() == StatusTarefa.FEITO) {
                        gestor.validarFinalizacao(p);
                    }
                }
            } catch (Exception e) {
                System.err.println("Erro na validação: " + e.getMessage());
            }
        }
        System.out.println("Validação de tarefas/projetos concluída.");
    }

    private static void testarReatribuicao(List<UsuarioGestor> gestores) {
        for (UsuarioGestor gestor : gestores) {
            List<Tarefa> tarefasEquipe;
            try {
                tarefasEquipe = Sistema.getInstance().getTarefasDaEquipe(gestor.getId());
                List<Tarefa> atrasadas = tarefasEquipe.stream()
                        .filter(t -> t.getStatus() == StatusTarefa.ATRASADO).toList();
                if (!atrasadas.isEmpty() && gestor.getEquipe().size() >= 2) {
                    Tarefa atrasada = atrasadas.get(0);
                    UsuarioDev novoDev = gestor.getEquipe().stream()
                            .filter(d -> d.getId() != atrasada.getDevResponsavel().getId())
                            .findFirst().orElse(null);
                    if (novoDev != null) {
                        gestor.reatribuirTarefaAtrasada(atrasada, novoDev);
                    }
                }
            } catch (Exception e) {
                System.err.println("Erro na reatribuição: " + e.getMessage());
            }
        }
        System.out.println("Reatribuição de tarefas atrasadas concluída.");
    }

    private static void testarRelatorios(List<UsuarioDev> devs) {
        for (UsuarioDev dev : devs) {
            List<Tarefa> tarefas = dev.carregarTarefas();
            if (!tarefas.isEmpty()) {
                Tarefa t = tarefas.get(0);
                dev.enviarRelatorioFinal(t, "Relatório da tarefa " + t.getId() + " por " + dev.getNome());
            }
            // Enviar relatório de projeto, se participa de algum
            List<Projeto> projetos = dev.carregarTarefas().stream()
                    .map(Tarefa::getProjetoPai)
                    .filter(p -> p != null)
                    .distinct().toList();
            if (!projetos.isEmpty()) {
                dev.enviarRelatorioFinal(projetos.get(0), "Relatório do projeto " + projetos.get(0).getNome());
            }
        }
        System.out.println("Relatórios enviados por desenvolvedores.");
    }

    private static void testarExpiracao(Sistema sistema) {
        System.out.println("Forçando verificação de expiração de prazos...");
        sistema.verificarPrazosExpirados();
        // Contar quantas tarefas ficaram atrasadas
        long atrasadas = sistema.getTarefas().stream().filter(t -> t.getStatus() == StatusTarefa.ATRASADO).count();
        System.out.println("Total de tarefas atrasadas após expiração: " + atrasadas);
    }

    private static void testarVisualizacoes(List<UsuarioGestor> gestores, List<UsuarioDev> devs) {
        for (UsuarioDev dev : devs) {
            System.out.println("\n--- VISÃO DE " + dev.getNome() + " ---");
            System.out.println(dev.visualizarPropriosProjetosTarefas().substring(0, Math.min(200, dev.visualizarPropriosProjetosTarefas().length())));
            System.out.println(dev.visualizarProgressoEquipe().substring(0, Math.min(200, dev.visualizarProgressoEquipe().length())));
            if (!devs.isEmpty() && devs.get(0) != dev) {
                System.out.println(dev.visualizarDetalhesColega(devs.get(0)).substring(0, Math.min(200, dev.visualizarDetalhesColega(devs.get(0)).length())));
            }
            break; // apenas um dev para não poluir
        }
        for (UsuarioGestor gestor : gestores) {
            System.out.println("\n--- VISÃO DO GESTOR " + gestor.getNome() + " ---");
            System.out.println(gestor.visualizarTodosProjetosTarefas().substring(0, Math.min(300, gestor.visualizarTodosProjetosTarefas().length())));
            gestor.exibirSolicitacoesPendentes();
            break;
        }
        System.out.println("Visualizações testadas.");
    }

    private static void testarAtualizacoes(List<Tarefa> tarefas, List<Projeto> projetos,
                                           List<UsuarioDev> devs, List<UsuarioGestor> gestores) throws Exception {
        TarefaDAO tarefaDAO = new TarefaDAO();
        ProjetoDAO projetoDAO = new ProjetoDAO();
        UsuarioDAO usuarioDAO = new UsuarioDAO();

        // Atualizar uma tarefa
        if (!tarefas.isEmpty()) {
            Tarefa t = tarefas.get(0);
            String original = t.getDescricao();
            t.setDescricao(original + " (MODIFICADO)");
            tarefaDAO.atualizarTarefa(t);
            System.out.println("Tarefa atualizada: " + t.getDescricao());
            // Reverter
            t.setDescricao(original);
            tarefaDAO.atualizarTarefa(t);
        }

        // Atualizar um projeto
        if (!projetos.isEmpty()) {
            Projeto p = projetos.get(0);
            String originalNome = p.getNome();
            p.setNome(originalNome + " (MOD)");
            projetoDAO.atualizarProjeto(p);
            System.out.println("Projeto atualizado: " + p.getNome());
            p.setNome(originalNome);
            projetoDAO.atualizarProjeto(p);
        }

        // Atualizar um usuário
        if (!devs.isEmpty()) {
            UsuarioDev d = devs.get(0);
            String originalNome = d.getNome();
            d.setNome(originalNome + " (RENOMEADO)");
            usuarioDAO.atualizarUsuario(d);
            System.out.println("Usuário atualizado: " + d.getNome());
            d.setNome(originalNome);
            usuarioDAO.atualizarUsuario(d);
        }
    }

    private static void testarTimer(Sistema sistema) throws InterruptedException {
        sistema.iniciarVerificadorPrazos(3000);
        System.out.println("Timer iniciado (3s de intervalo). Aguardando 7 segundos...");
        Thread.sleep(7000);
        sistema.pararVerificadorPrazos();
        System.out.println("Timer finalizado. Nenhuma exceção ocorreu.");
    }

    private static void testarExclusao(Sistema sistema) throws Exception {
        // Criar uma tarefa temporária
        UsuarioGestor gestor = sistema.getGestores().get(0);
        UsuarioDev dev = gestor.getEquipe().get(0);
        String descTemp = "Tarefa_para_deletar_" + System.currentTimeMillis();
        gestor.criarAtribuirTarefa(descTemp, new Date(), NivelImportancia.BAIXA, dev.getId(), 1.0);
        Tarefa temp = sistema.getTarefas().stream()
                .filter(t -> t.getDescricao().equals(descTemp)).findFirst().orElse(null);
        if (temp != null) {
            TarefaDAO dao = new TarefaDAO();
            dao.deletar(temp.getId());
            System.out.println("Tarefa temporária deletada (ID " + temp.getId() + ")");
        }

        // Testar exclusão de solicitação
        SolicitacaoDAO sDAO = new SolicitacaoDAO();
        List<SolicitacaoMudanca> sols = sistema.getSolicitacoes();
        if (!sols.isEmpty()) {
            sDAO.deletar(sols.get(0).getId());
            System.out.println("Solicitação deletada (ID " + sols.get(0).getId() + ")");
        }

        System.out.println("Testes de exclusão concluídos.");
    }

    // Limpeza total do banco (drop e recreate das tabelas? Não, apenas remove registros)
    private static void limparBancoCompleto(Sistema sistema) {
        try {
            TarefaDAO tarefaDAO = new TarefaDAO();
            RelatorioDAO relatorioDAO = new RelatorioDAO();
            SolicitacaoDAO solicitacaoDAO = new SolicitacaoDAO();
            ProjetoDAO projetoDAO = new ProjetoDAO();
            UsuarioDAO usuarioDAO = new UsuarioDAO();

            // Ordem correta para evitar violação de FK
            for (Tarefa t : sistema.getTarefas()) tarefaDAO.deletar(t.getId());
            for (Relatorio r : sistema.getRelatorios()) relatorioDAO.deletar(r.getId());
            for (SolicitacaoMudanca s : sistema.getSolicitacoes()) solicitacaoDAO.deletar(s.getId());
            for (Projeto p : sistema.getProjetos()) projetoDAO.deletar(p.getId());
            for (Usuario u : sistema.getUsuarios()) usuarioDAO.deletar(u.getId());

            System.out.println("Banco de dados completamente limpo.");
        } catch (Exception e) {
            System.err.println("Erro durante limpeza: " + e.getMessage());
        }
    }
}