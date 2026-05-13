import java.util.Date;
import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;

/**
 * Teste robusto e abrangente do sistema.
 * Executa todas as funcionalidades, incluindo casos de borda e validações.
 *
 * Pré-requisito: executar script_mysql.sql para ter o banco limpo.
 */
public class MainTeste {

    public static void main(String[] args) {
        System.out.println("=========================================================");
        System.out.println("           TESTE ROBUSTO DO SISTEMA");
        System.out.println("=========================================================\n");

        try {
            Sistema sistema = Sistema.getInstance();

            // === 1. LIMPEZA OPCIONAL (opcional, mas recomendado para teste isolado) ===
            // Se desejar apagar dados antigos, descomente as linhas abaixo (cuidado!)
            // limparBanco(sistema);

            // === 2. CADASTRO DE USUÁRIOS ===
            System.out.println("----- 2. CADASTRO DE USUÁRIOS -----");
            UsuarioGestor gestor1 = new UsuarioGestor("Gestor A", "111.111.111-11", "gestorA@mail.com", "senhaA", "TI");
            UsuarioGestor gestor2 = new UsuarioGestor("Gestor B", "222.222.222-22", "gestorB@mail.com", "senhaB", "RH");

            UsuarioDev dev1 = new UsuarioDev("Dev1", "333.333.333-33", "dev1@mail.com", "dev1pass");
            dev1.setGestorId(gestor1.getId()); // será atualizado após cadastro, mas melhor setar após ID conhecido
            UsuarioDev dev2 = new UsuarioDev("Dev2", "444.444.444-44", "dev2@mail.com", "dev2pass");
            dev2.setGestorId(gestor1.getId());
            UsuarioDev dev3 = new UsuarioDev("Dev3", "555.555.555-55", "dev3@mail.com", "dev3pass");
            dev3.setGestorId(gestor2.getId());

            // Cadastrar gestores primeiro para obter IDs
            sistema.realizarCadastro(gestor1);
            sistema.realizarCadastro(gestor2);
            // Agora setar gestorId dos devs com os IDs reais
            dev1.setGestorId(gestor1.getId());
            dev2.setGestorId(gestor1.getId());
            dev3.setGestorId(gestor2.getId());
            sistema.realizarCadastro(dev1);
            sistema.realizarCadastro(dev2);
            sistema.realizarCadastro(dev3);

            System.out.println("✓ Usuários cadastrados:");
            System.out.println("  Gestor1: " + gestor1.getNome() + " (ID " + gestor1.getId() + ")");
            System.out.println("  Gestor2: " + gestor2.getNome() + " (ID " + gestor2.getId() + ")");
            System.out.println("  Dev1: " + dev1.getNome() + " (ID " + dev1.getId() + ")");
            System.out.println("  Dev2: " + dev2.getNome() + " (ID " + dev2.getId() + ")");
            System.out.println("  Dev3: " + dev3.getNome() + " (ID " + dev3.getId() + ")");

            // Teste de cadastro duplicado (e-mail já existe)
            UsuarioDev devDuplicado = new UsuarioDev("Fake", "999.999.999-99", "dev1@mail.com", "qualquer");
            boolean duplicado = sistema.realizarCadastro(devDuplicado);
            System.out.println("  Tentativa de cadastro com e-mail duplicado: " + (duplicado ? "FALHA (ok?)" : "REJEITADO (correto)"));

            // === 3. AUTENTICAÇÃO (válida e inválida) ===
            System.out.println("\n----- 3. AUTENTICAÇÃO -----");
            Usuario authOk = sistema.autenticar("dev1@mail.com", "dev1pass");
            Usuario authWrong = sistema.autenticar("dev1@mail.com", "senhaErrada");
            System.out.println("  Autenticação correta: " + (authOk != null ? "OK (" + authOk.getNome() + ")" : "FALHA"));
            System.out.println("  Autenticação incorreta: " + (authWrong == null ? "REJEITADA (correto)" : "ACEITA (erro)"));

            // Obter referências dos usuários logados
            UsuarioGestor gestorLogado1 = (UsuarioGestor) sistema.autenticar("gestorA@mail.com", "senhaA");
            UsuarioGestor gestorLogado2 = (UsuarioGestor) sistema.autenticar("gestorB@mail.com", "senhaB");
            UsuarioDev devLogado1 = (UsuarioDev) sistema.autenticar("dev1@mail.com", "dev1pass");
            UsuarioDev devLogado2 = (UsuarioDev) sistema.autenticar("dev2@mail.com", "dev2pass");
            UsuarioDev devLogado3 = (UsuarioDev) sistema.autenticar("dev3@mail.com", "dev3pass");

            // === 4. CRIAÇÃO DE PROJETOS ===
            System.out.println("\n----- 4. PROJETOS -----");
            Calendar cal = Calendar.getInstance();
            // Projeto 1 - prazo 2 meses
            cal.add(Calendar.MONTH, 2);
            Date prazoProj1 = cal.getTime();
            gestorLogado1.criarProjeto("Projeto Alpha", prazoProj1, NivelImportancia.ALTA);

            // Projeto 2 - prazo 1 mês (para gestor2)
            cal.add(Calendar.MONTH, -1); // volta 1 mês, fica 1 mês a partir de agora? melhor recalcular
            cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, 1);
            Date prazoProj2 = cal.getTime();
            gestorLogado2.criarProjeto("Projeto Beta", prazoProj2, NivelImportancia.URGENTE);

            // Projeto 3 - prazo já vencido (ontem)
            cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -1);
            Date prazoVencidoProj = cal.getTime();
            gestorLogado1.criarProjeto("Projeto Gama", prazoVencidoProj, NivelImportancia.MEDIA);

            List<Projeto> projetos = sistema.getProjetos();
            System.out.println("  Total de projetos criados: " + projetos.size());
            for (Projeto p : projetos) {
                System.out.println("    - " + p.getNome() + " (ID " + p.getId() + ", status " + p.getStatus() + ")");
            }

            // === 5. CRIAÇÃO DE TAREFAS (avulsas e dentro de projetos) ===
            System.out.println("\n----- 5. TAREFAS -----");
            cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 10);
            Date prazoTarefaNormal = cal.getTime();
            cal.add(Calendar.DAY_OF_MONTH, -20); // prazo vencido há 10 dias
            Date prazoTarefaVencida = cal.getTime();

            // Tarefas avulsas para dev1 e dev2 (gestor1)
            gestorLogado1.criarAtribuirTarefa("Tarefa A1", prazoTarefaNormal, NivelImportancia.MEDIA, devLogado1.getId(), 5.0);
            gestorLogado1.criarAtribuirTarefa("Tarefa A2", prazoTarefaNormal, NivelImportancia.ALTA, devLogado2.getId(), 3.0);
            gestorLogado1.criarAtribuirTarefa("Tarefa Vencida", prazoTarefaVencida, NivelImportancia.BAIXA, devLogado1.getId(), 2.0);

            // Tarefas dentro de projetos
            Projeto projetoAlpha = projetos.stream().filter(p -> p.getNome().equals("Projeto Alpha")).findFirst().get();
            Projeto projetoBeta = projetos.stream().filter(p -> p.getNome().equals("Projeto Beta")).findFirst().get();

            gestorLogado1.criarAtribuirTarefaEmProjeto("Tarefa P1", prazoTarefaNormal, NivelImportancia.URGENTE, devLogado1.getId(), projetoAlpha.getId(), 8.0);
            gestorLogado1.criarAtribuirTarefaEmProjeto("Tarefa P2", prazoTarefaNormal, NivelImportancia.MEDIA, devLogado2.getId(), projetoAlpha.getId(), 6.0);
            gestorLogado2.criarAtribuirTarefaEmProjeto("Tarefa Beta1", prazoTarefaNormal, NivelImportancia.ALTA, devLogado3.getId(), projetoBeta.getId(), 4.0);

            List<Tarefa> todasTarefas = sistema.getTarefas();
            System.out.println("  Total de tarefas criadas: " + todasTarefas.size());

            // === 6. REGISTRO DE HORAS E ALTERAÇÃO DE STATUS (DESENVOLVEDORES) ===
            System.out.println("\n----- 6. REGISTRO DE HORAS E ALTERAÇÃO DE STATUS -----");
            List<Tarefa> tarefasDev1 = devLogado1.carregarTarefas();
            List<Tarefa> tarefasDev2 = devLogado2.carregarTarefas();
            List<Tarefa> tarefasDev3 = devLogado3.carregarTarefas();

            // Dev1 adiciona horas e muda status de algumas
            if (tarefasDev1.size() >= 2) {
                Tarefa t1 = tarefasDev1.get(0);
                t1.adicionarHorasTrabalhadas(3.0);
                devLogado1.alterarStatusTarefa(t1, StatusTarefa.FEITO);

                Tarefa tVencida = tarefasDev1.stream().filter(t -> t.getDescricao().equals("Tarefa Vencida")).findFirst().orElse(null);
                if (tVencida != null && tVencida.getStatus() == StatusTarefa.ATRASADO) {
                    // Pode alterar de ATRASADO para FEITO (permitido)
                    devLogado1.alterarStatusTarefa(tVencida, StatusTarefa.FEITO);
                }
            }

            // Dev2 adiciona horas
            if (!tarefasDev2.isEmpty()) {
                Tarefa t2 = tarefasDev2.get(0);
                t2.adicionarHorasTrabalhadas(1.5);
                // Tentativa inválida: mudar para PRONTO diretamente (deve ser bloqueado)
                devLogado2.alterarStatusTarefa(t2, StatusTarefa.PRONTO);
                // Mudança correta: PENDENTE -> FEITO
                devLogado2.alterarStatusTarefa(t2, StatusTarefa.FEITO);
            }

            // Dev3
            if (!tarefasDev3.isEmpty()) {
                Tarefa t3 = tarefasDev3.get(0);
                t3.adicionarHorasTrabalhadas(2.0);
                devLogado3.alterarStatusTarefa(t3, StatusTarefa.FEITO);
            }

            // === 7. ENVIO DE RELATÓRIOS ===
            System.out.println("\n----- 7. RELATÓRIOS -----");
            if (!tarefasDev1.isEmpty()) {
                devLogado1.enviarRelatorioFinal(tarefasDev1.get(0), "Relatório detalhado da tarefa.");
            }
            devLogado2.enviarRelatorioFinal(projetoAlpha, "Relatório de progresso do projeto Alpha.");
            devLogado3.enviarRelatorioFinal(tarefasDev3.get(0), "Primeira parte concluída.");

            List<Relatorio> relatorios = sistema.getRelatorios();
            System.out.println("  Total de relatórios enviados: " + relatorios.size());

            // === 8. SOLICITAÇÕES DE REORGANIZAÇÃO ===
            System.out.println("\n----- 8. SOLICITAÇÕES -----");
            devLogado1.solicitarReorganizacao("Preciso de ajuda com a tarefa complexa.");
            devLogado2.solicitarReorganizacao("Revisão de escopo necessária.");

            List<SolicitacaoMudanca> solicitacoes = sistema.getSolicitacoes();
            System.out.println("  Total de solicitações: " + solicitacoes.size());

            // Gestor processa
            if (solicitacoes.size() >= 1) {
                gestorLogado1.processarSolicitacaoMudanca(solicitacoes.get(0), true);
                gestorLogado1.processarSolicitacaoMudanca(solicitacoes.get(1), false);
            }

            // Tentar processar solicitação já aprovada (deve falhar)
            if (!solicitacoes.isEmpty()) {
                gestorLogado1.processarSolicitacaoMudanca(solicitacoes.get(0), true);
            }

            // === 9. VALIDAÇÃO DE TAREFAS (GESTOR) ===
            System.out.println("\n----- 9. VALIDAÇÃO DE TAREFAS/PROJETOS -----");
            // Listar tarefas com status FEITO
            List<Tarefa> tarefasFeito = sistema.getTarefas().stream()
                    .filter(t -> t.getStatus() == StatusTarefa.FEITO).toList();
            System.out.println("  Tarefas com status FEITO: " + tarefasFeito.size());
            for (Tarefa tf : tarefasFeito) {
                gestorLogado1.validarFinalizacao(tf);
            }

            // Validar projeto (nenhum projeto está FEITO ainda)
            gestorLogado1.validarFinalizacao(projetoAlpha); // status PENDENTE -> não faz nada

            // === 10. REATRIBUIÇÃO DE TAREFAS ATRASADAS ===
            System.out.println("\n----- 10. REATRIBUIÇÃO -----");
            // Forçar expiração de prazos
            sistema.verificarPrazosExpirados();

            List<Tarefa> tarefasAtrasadas = sistema.getTarefas().stream()
                    .filter(t -> t.getStatus() == StatusTarefa.ATRASADO).toList();
            System.out.println("  Tarefas atrasadas: " + tarefasAtrasadas.size());
            if (!tarefasAtrasadas.isEmpty()) {
                Tarefa atrasada = tarefasAtrasadas.get(0);
                System.out.println("  Reatribuindo tarefa " + atrasada.getId() + " de " + atrasada.getDevResponsavel().getNome()
                        + " para " + devLogado2.getNome());
                gestorLogado1.reatribuirTarefaAtrasada(atrasada, devLogado2);
                // Tentativa de reatribuir tarefa não atrasada (deve falhar)
                Tarefa normal = sistema.getTarefas().stream().filter(t -> t.getStatus() == StatusTarefa.PENDENTE).findFirst().orElse(null);
                if (normal != null) {
                    gestorLogado1.reatribuirTarefaAtrasada(normal, devLogado3);
                }
            }

            // === 11. VISUALIZAÇÕES (retornam String) ===
            System.out.println("\n----- 11. VISUALIZAÇÕES -----");
            System.out.println("--- VISÃO DO DEV1 ---");
            System.out.println(devLogado1.visualizarPropriosProjetosTarefas());
            System.out.println("--- PROGRESSO DA EQUIPE (DEV1) ---");
            System.out.println(devLogado1.visualizarProgressoEquipe());
            System.out.println("--- DETALHES DO COLEGA DEV2 (VISÃO DO DEV1) ---");
            System.out.println(devLogado1.visualizarDetalhesColega(devLogado2));

            System.out.println("--- VISÃO DO GESTOR1 (EQUIPE) ---");
            System.out.println(gestorLogado1.visualizarTodosProjetosTarefas());
            System.out.println("--- SOLICITAÇÕES PENDENTES (GESTOR1) ---");
            gestorLogado1.listarSolicitacoesPendentes(); // ainda imprime no console, não String

            // Método adicional: visualizarTarefasDoProjeto (dev)
            System.out.println("--- TAREFAS DO PROJETO ALPHA (VISÃO DO DEV1) ---");
            devLogado1.visualizarTarefasDoProjeto(projetoAlpha);

            // === 12. RELATÓRIO DIÁRIO ===
            System.out.println("\n----- 12. RELATÓRIO DIÁRIO (GERADO AUTOMATICAMENTE) -----");
            sistema.gerarRelatorioDiario();

            // === 13. TESTES DE DELETE (OPCIONAL, MAS INCLUÍDO PARA COMPLETUDE) ===
            System.out.println("\n----- 13. EXCLUSÃO DE REGISTROS (TESTE) -----");
            // Criar uma tarefa temporária e deletá-la
            gestorLogado1.criarAtribuirTarefa("Tarefa para deletar", prazoTarefaNormal, NivelImportancia.BAIXA, devLogado1.getId(), 1.0);
            List<Tarefa> todasApos = sistema.getTarefas();
            Tarefa ultima = todasApos.get(todasApos.size() - 1);
            int idTemp = ultima.getId();
            System.out.println("  Tarefa temporária criada com ID " + idTemp);
            // Deletar via DAO diretamente (não há método no Sistema, mas podemos acessar)
            try {
                TarefaDAO tarefaDAO = new TarefaDAO();
                tarefaDAO.deletar(idTemp);
                System.out.println("  Tarefa deletada com sucesso.");
            } catch (Exception e) {
                System.out.println("  Erro ao deletar: " + e.getMessage());
            }

            // === 14. TESTE DE EXPIRAÇÃO AUTOMÁTICA (TIMER) ===
            System.out.println("\n----- 14. VERIFICAÇÃO AUTOMÁTICA DE PRAZOS (TIMER) -----");
            sistema.iniciarVerificadorPrazos(5000); // a cada 5 segundos
            System.out.println("  Timer iniciado. Aguarde 6 segundos para verificar se novas tarefas atrasam...");
            Thread.sleep(6000);
            sistema.pararVerificadorPrazos();

            // === 15. RESUMO FINAL ===
            System.out.println("\n=========================================================");
            System.out.println("                 RESUMO FINAL");
            System.out.println("=========================================================");
            System.out.println("Usuários no sistema: " + sistema.getUsuarios().size());
            System.out.println("Projetos: " + sistema.getProjetos().size());
            System.out.println("Tarefas: " + sistema.getTarefas().size());
            System.out.println("Relatórios: " + sistema.getRelatorios().size());
            System.out.println("Solicitações: " + sistema.getSolicitacoes().size());

            System.out.println("\n✅ TESTE ROBUSTO CONCLUÍDO SEM ERROS!");

        } catch (Exception e) {
            System.err.println("❌ ERRO DURANTE O TESTE: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Método auxiliar opcional para limpar banco (cuidado!)
    private static void limparBanco(Sistema sistema) {
        try {
            // Deletar todas as tarefas, relatórios, solicitações, projetos, usuários (ordem por FK)
            TarefaDAO tarefaDAO = new TarefaDAO();
            RelatorioDAO relatorioDAO = new RelatorioDAO();
            SolicitacaoDAO solicitacaoDAO = new SolicitacaoDAO();
            ProjetoDAO projetoDAO = new ProjetoDAO();
            UsuarioDAO usuarioDAO = new UsuarioDAO();

            for (Tarefa t : sistema.getTarefas()) tarefaDAO.deletar(t.getId());
            for (Relatorio r : sistema.getRelatorios()) relatorioDAO.deletar(r.getId());
            for (SolicitacaoMudanca s : sistema.getSolicitacoes()) solicitacaoDAO.deletar(s.getId());
            for (Projeto p : sistema.getProjetos()) projetoDAO.deletar(p.getId());
            for (Usuario u : sistema.getUsuarios()) usuarioDAO.deletar(u.getId());

            System.out.println("  Banco de dados limpo.");
        } catch (Exception e) {
            System.out.println("  Não foi possível limpar completamente: " + e.getMessage());
        }
    }
}