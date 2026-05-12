import java.util.Date;
import java.util.Calendar;
import java.util.List;
import java.sql.SQLException;

public class MainTeste {
    private static Sistema sistema = Sistema.getInstance();
    private static UsuarioDAO usuarioDAO = new UsuarioDAO();
    private static ProjetoDAO projetoDAO = new ProjetoDAO();
    private static TarefaDAO tarefaDAO = new TarefaDAO();
    private static RelatorioDAO relatorioDAO = new RelatorioDAO();
    private static SolicitacaoDAO solicitacaoDAO = new SolicitacaoDAO();

    // Altere para false se NÃO quiser limpar as tabelas no início
    private static final boolean LIMPAR_BANCO = true;

    public static void main(String[] args) {
        try {
            if (LIMPAR_BANCO) {
                System.out.println("=== LIMPANDO TABELAS (dados anteriores serão removidos) ===");
                limparBanco();
                System.out.println("Banco limpo.\n");
            }

            System.out.println("=== TESTE COMPLETO DO SISTEMA ===\n");

            // 1. Cadastro de usuários
            System.out.println("--- Cadastro de usuários ---");
            UsuarioGestor gestor = new UsuarioGestor("Gestor TI", "111.111.111-11", "ti@empresa.com", "123", "TI");
            sistema.realizarCadastro(gestor);
            System.out.println("Gestor cadastrado: " + gestor.getNome() + " (ID " + gestor.getId() + ")");

            UsuarioDev devAna = new UsuarioDev("Ana Souza", "222.222.222-22", "ana@empresa.com", "123");
            devAna.setGestorId(gestor.getId());
            sistema.realizarCadastro(devAna);
            System.out.println("Dev cadastrado: " + devAna.getNome() + " (ID " + devAna.getId() + ")");

            UsuarioDev devBruno = new UsuarioDev("Bruno Lima", "333.333.333-33", "bruno@empresa.com", "123");
            devBruno.setGestorId(gestor.getId());
            sistema.realizarCadastro(devBruno);
            System.out.println("Dev cadastrado: " + devBruno.getNome() + " (ID " + devBruno.getId() + ")");

            // 2. Criar projetos
            System.out.println("\n--- Criação de projetos ---");
            Date prazoLongo = addDias(30);
            Date prazoCurto = addDias(5);
            gestor.criarProjeto("Sistema de Vendas", prazoLongo, NivelImportancia.ALTA);
            gestor.criarProjeto("App Mobile", prazoCurto, NivelImportancia.URGENTE);

            Projeto projetoVendas = projetoDAO.listarTodos().get(0);
            Projeto projetoMobile = projetoDAO.listarTodos().get(1);
            System.out.println("Projetos criados: " + projetoVendas.getNome() + " (ID " + projetoVendas.getId() + "), "
                    + projetoMobile.getNome() + " (ID " + projetoMobile.getId() + ")");

            // 3. Criar tarefas
            System.out.println("\n--- Criação de tarefas ---");
            // Dentro do projeto Vendas
            gestor.criarAtribuirTarefaEmProjeto("Backend - Produtos", prazoLongo, NivelImportancia.ALTA,
                    devAna.getId(), projetoVendas.getId(), 20.0);
            gestor.criarAtribuirTarefaEmProjeto("Frontend - Carrinho", prazoLongo, NivelImportancia.MEDIA,
                    devBruno.getId(), projetoVendas.getId(), 15.0);
            // Dentro do projeto Mobile (prazo curto)
            gestor.criarAtribuirTarefaEmProjeto("Tela de login", prazoCurto, NivelImportancia.URGENTE,
                    devAna.getId(), projetoMobile.getId(), 8.0);
            gestor.criarAtribuirTarefaEmProjeto("Integração com API", prazoCurto, NivelImportancia.URGENTE,
                    devBruno.getId(), projetoMobile.getId(), 12.0);
            // Tarefa avulsa
            gestor.criarAtribuirTarefa("Documentação técnica", prazoLongo, NivelImportancia.BAIXA,
                    devAna.getId(), 5.0);

            // 4. Registrar horas trabalhadas
            System.out.println("\n--- Registro de horas trabalhadas ---");
            List<Tarefa> tarefasAna = tarefaDAO.listarPorDev(devAna.getId(), usuarioDAO, projetoDAO);
            for (Tarefa t : tarefasAna) {
                t.adicionarHorasTrabalhadas(3.0);
                System.out.println("   " + t.getDescricao() + " - horas: " + t.getHorasTrabalhadas());
            }

            // 5. Alterar status de uma tarefa (Backend) para FEITO
            System.out.println("\n--- Alteração de status para FEITO ---");
            Tarefa tarefaBackend = null;
            for (Tarefa t : tarefasAna) {
                if (t.getDescricao().contains("Backend")) {
                    tarefaBackend = t;
                    break;
                }
            }
            if (tarefaBackend != null) {
                // Precisamos carregar a tarefa completa com o dev responsável (já está ok)
                devAna.alterarStatusTarefa(tarefaBackend, StatusTarefa.FEITO);
            }

            // 6. Gestor valida a tarefa (FEITO -> PRONTO)
            System.out.println("\n--- Validação pelo gestor ---");
            if (tarefaBackend != null) {
                gestor.validarFinalizacao(tarefaBackend);
            }

            // 7. Verificar expiração de prazos (forçar)
            System.out.println("\n--- Verificação de prazos expirados ---");
            sistema.verificarPrazosExpirados();

            // 8. Reatribuir tarefa atrasada (se houver)
            System.out.println("\n--- Reatribuição de tarefa atrasada ---");
            List<Tarefa> todasTarefas = tarefaDAO.listarTodas();
            Tarefa atrasada = todasTarefas.stream()
                    .filter(t -> t.getStatus() == StatusTarefa.ATRASADO)
                    .findFirst().orElse(null);
            if (atrasada != null) {
                gestor.reatribuirTarefaAtrasada(atrasada, devBruno);
                System.out.println("Tarefa atrasada " + atrasada.getId() + " reatribuída para " + devBruno.getNome());
            } else {
                System.out.println("Nenhuma tarefa atrasada encontrada.");
            }

            // 9. Solicitação de reorganização
            System.out.println("\n--- Solicitação de reorganização ---");
            devBruno.solicitarReorganizacao("Preciso de ajuda com tarefas urgentes.");
            gestor.listarSolicitacoesPendentes();
            List<SolicitacaoMudanca> solicitacoes = solicitacaoDAO.listarTodos();
            if (!solicitacoes.isEmpty()) {
                gestor.processarSolicitacaoMudanca(solicitacoes.get(0), true);
            }

            // 10. Envio de relatório final
            System.out.println("\n--- Envio de relatório ---");
            if (tarefaBackend != null) {
                devAna.enviarRelatorioFinal(tarefaBackend, "Implementação do backend concluída com sucesso.");
            }

            // 11. Relatório diário
            System.out.println("\n--- Relatório Diário ---");
            sistema.gerarRelatorioDiario();

            // 12. Recarregar dados do banco (verificação de persistência)
            System.out.println("\n--- Recarregando dados do banco (verificação de persistência) ---");
            System.out.println("Total de usuários: " + usuarioDAO.listarTodos().size());
            System.out.println("Total de projetos: " + projetoDAO.listarTodos().size());
            System.out.println("Total de tarefas: " + tarefaDAO.listarTodas().size());
            System.out.println("Total de relatórios: " + relatorioDAO.listarTodos().size());
            System.out.println("Total de solicitações: " + solicitacaoDAO.listarTodos().size());

            // 13. Progresso final dos desenvolvedores
            System.out.println("\n--- Progresso dos desenvolvedores ---");
            System.out.printf("%s: %.2f%%\n", devAna.getNome(), devAna.calcularProgressoTotal());
            System.out.printf("%s: %.2f%%\n", devBruno.getNome(), devBruno.calcularProgressoTotal());

            System.out.println("\n=== TESTE CONCLUÍDO COM SUCESSO ===");

        } catch (Exception e) {
            System.err.println("ERRO: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void limparBanco() throws SQLException {
        try (var conn = DatabaseConnection.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            stmt.execute("TRUNCATE TABLE solicitacoes");
            stmt.execute("TRUNCATE TABLE relatorios");
            stmt.execute("TRUNCATE TABLE tarefas");
            stmt.execute("TRUNCATE TABLE projetos");
            stmt.execute("TRUNCATE TABLE usuarios");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

    private static Date addDias(int dias) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, dias);
        return cal.getTime();
    }
}