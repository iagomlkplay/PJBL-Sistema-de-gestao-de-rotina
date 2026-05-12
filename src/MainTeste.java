import java.sql.SQLException;
import java.util.Date;
import java.util.Calendar;
import java.util.List;

public class MainTeste {

    private static Sistema sistema = Sistema.getInstance();
    private static UsuarioDAO usuarioDAO = new UsuarioDAO();
    private static ProjetoDAO projetoDAO = new ProjetoDAO();
    private static TarefaDAO tarefaDAO = new TarefaDAO();
    private static RelatorioDAO relatorioDAO = new RelatorioDAO();
    private static SolicitacaoDAO solicitacaoDAO = new SolicitacaoDAO();

    public static void main(String[] args) {
        try {
            System.out.println("=== TESTE COMPLETO DO SISTEMA COM PERSISTÊNCIA ===\n");

            // 1. Reset do banco (opcional, com aviso)
            System.out.println("Limpando tabelas... (serão removidos todos os dados existentes)");
            limparBanco();
            System.out.println("Banco limpo.\n");

            // 2. Cadastrar usuários
            System.out.println("--- Cadastro de usuários ---");
            UsuarioGestor gestor1 = new UsuarioGestor("Gestor TI", "111.111.111-11", "ti@empresa.com", "123", "TI");
            sistema.realizarCadastro(gestor1);
            System.out.println("Gestor cadastrado: " + gestor1.getNome() + " (ID " + gestor1.getId() + ")");

            UsuarioDev dev1 = new UsuarioDev("Ana Souza", "222.222.222-22", "ana@empresa.com", "123");
            dev1.setGestorId(gestor1.getId());
            sistema.realizarCadastro(dev1);
            System.out.println("Dev cadastrado: " + dev1.getNome() + " (ID " + dev1.getId() + ")");

            UsuarioDev dev2 = new UsuarioDev("Bruno Lima", "333.333.333-33", "bruno@empresa.com", "123");
            dev2.setGestorId(gestor1.getId());
            sistema.realizarCadastro(dev2);
            System.out.println("Dev cadastrado: " + dev2.getNome() + " (ID " + dev2.getId() + ")");

            // 3. Criar projetos
            System.out.println("\n--- Criação de projetos ---");
            Date prazoCurto = addDias(5);
            Date prazoLongo = addDias(30);

            gestor1.criarProjeto("Sistema de Vendas", prazoLongo, NivelImportancia.ALTA);
            gestor1.criarProjeto("App Mobile", prazoCurto, NivelImportancia.URGENTE);

            // Recuperar projetos recém-criados
            Projeto projetoVendas = projetoDAO.listarTodos().get(0);
            Projeto projetoMobile = projetoDAO.listarTodos().get(1);
            System.out.println("Projetos criados: " + projetoVendas.getNome() + " (ID " + projetoVendas.getId() + "), "
                    + projetoMobile.getNome() + " (ID " + projetoMobile.getId() + ")");

            // 4. Criar tarefas (dentro dos projetos e avulsas)
            System.out.println("\n--- Criação de tarefas ---");

            // Tarefas do projeto Vendas
            gestor1.criarAtribuirTarefaEmProjeto("Backend - Produtos", prazoLongo, NivelImportancia.ALTA,
                    dev1.getId(), projetoVendas.getId(), 20.0);
            gestor1.criarAtribuirTarefaEmProjeto("Frontend - Carrinho", prazoLongo, NivelImportancia.MEDIA,
                    dev2.getId(), projetoVendas.getId(), 15.0);

            // Tarefas do projeto Mobile (prazo curto)
            gestor1.criarAtribuirTarefaEmProjeto("Tela de login", prazoCurto, NivelImportancia.URGENTE,
                    dev1.getId(), projetoMobile.getId(), 8.0);
            gestor1.criarAtribuirTarefaEmProjeto("Integração com API", prazoCurto, NivelImportancia.URGENTE,
                    dev2.getId(), projetoMobile.getId(), 12.0);

            // Tarefas avulsas
            gestor1.criarAtribuirTarefa("Documentação técnica", prazoLongo, NivelImportancia.BAIXA,
                    dev1.getId(), 5.0);

            // 5. Simular horas trabalhadas (pelo dev)
            System.out.println("\n--- Registro de horas trabalhadas ---");
            // Buscar as tarefas da Ana (dev1)
            List<Tarefa> tarefasAna = tarefaDAO.listarPorDev(dev1.getId(), usuarioDAO, projetoDAO);
            for (Tarefa t : tarefasAna) {
                t.adicionarHorasTrabalhadas(3.0);
                System.out.println("   " + t.getDescricao() + " - horas: " + t.getHorasTrabalhadas());
            }

            // 6. Marcar algumas tarefas como FEITO
            System.out.println("\n--- Alteração de status para FEITO ---");
            Tarefa tarefaBackend = null;
            for (Tarefa t : tarefasAna) {
                if (t.getDescricao().contains("Backend")) {
                    tarefaBackend = t;
                    break;
                }
            }
            if (tarefaBackend != null) {
                dev1.alterarStatusTarefa(tarefaBackend, StatusTarefa.FEITO);
            }

            // 7. Gestor valida tarefa FEITO -> PRONTO
            System.out.println("\n--- Validação pelo gestor ---");
            gestor1.validarFinalizacao(tarefaBackend);

            // 8. Testar expiração de prazos (forçar a passagem do tempo)
            System.out.println("\n--- Verificação de prazos expirados ---");
            sistema.verificarPrazosExpirados();

            // 9. Gestor reatribui uma tarefa atrasada (ex: tarefa do projeto mobile)
            List<Tarefa> tarefasAtrasadas = tarefaDAO.listarTodas().stream()
                    .filter(t -> t.getStatus() == StatusTarefa.ATRASADO)
                    .toList();
            if (!tarefasAtrasadas.isEmpty()) {
                Tarefa atrasada = tarefasAtrasadas.get(0);
                gestor1.reatribuirTarefaAtrasada(atrasada, dev2);
                System.out.println("Tarefa atrasada " + atrasada.getId() + " reatribuída para " + dev2.getNome());
            }

            // 10. Solicitação de reorganização
            System.out.println("\n--- Solicitação de reorganização ---");
            dev2.solicitarReorganizacao("Preciso de ajuda com tarefas urgentes.");
            gestor1.listarSolicitacoesPendentes();

            // Processar solicitação
            List<SolicitacaoMudanca> solicitacoes = solicitacaoDAO.listarTodos();
            if (!solicitacoes.isEmpty()) {
                gestor1.processarSolicitacaoMudanca(solicitacoes.get(0), true);
            }

            // 11. Envio de relatório final
            System.out.println("\n--- Envio de relatório ---");
            dev1.enviarRelatorioFinal(tarefaBackend, "Implementação do backend concluída com sucesso.");

            // 12. Gerar relatório diário
            System.out.println("\n--- Relatório Diário ---");
            sistema.gerarRelatorioDiario();

            // 13. Recarregar dados do banco para verificar persistência
            System.out.println("\n--- Recarregando dados do banco (verificação de persistência) ---");
            List<Usuario> usuarios = usuarioDAO.listarTodos();
            System.out.println("Total de usuários no banco: " + usuarios.size());
            List<Projeto> projetos = projetoDAO.listarTodos();
            System.out.println("Total de projetos: " + projetos.size());
            List<Tarefa> tarefas = tarefaDAO.listarTodas();
            System.out.println("Total de tarefas: " + tarefas.size());
            List<Relatorio> relatorios = relatorioDAO.listarTodos();
            System.out.println("Total de relatórios: " + relatorios.size());
            List<SolicitacaoMudanca> sols = solicitacaoDAO.listarTodos();
            System.out.println("Total de solicitações: " + sols.size());

            // 14. Verificar progresso dos devs
            System.out.println("\n--- Progresso dos desenvolvedores (após operações) ---");
            for (UsuarioDev dev : sistema.getDevs()) {
                double progresso = dev.calcularProgressoTotal();
                System.out.printf("%s: %.2f%%\n", dev.getNome(), progresso);
            }

            System.out.println("\n=== TESTE COMPLETO FINALIZADO COM SUCESSO ===");

        } catch (SQLException e) {
            System.err.println("ERRO no banco: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("ERRO inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void limparBanco() throws SQLException {
        // Desativa constraints para truncar tabelas na ordem correta
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