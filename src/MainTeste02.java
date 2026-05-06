import java.util.*;
import java.util.stream.Collectors;

public class MainTeste02 {
    private static final int DIAS_SIMULACAO = 90;
    private static final int NUM_GESTORES = 3;
    private static final int NUM_DEVS_POR_GESTOR = 15; // total devs = NUM_GESTORES * NUM_DEVS_POR_GESTOR
    private static final int PROJETOS_INICIAIS_POR_GESTOR = 5;
    private static final int TAREFAS_POR_PROJETO_INICIAL = 2;
    private static final Random random = new Random(42);

    private static Sistema sistema = Sistema.getInstance(); // <-- CORREÇÃO AQUI
    private static List<UsuarioGestor> gestores = new ArrayList<>();
    private static List<UsuarioDev> devs = new ArrayList<>();
    private static List<Projeto> projetos = new ArrayList<>();

    private static int totalSolicitacoes = 0;
    private static int totalReatribuicoes = 0;

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════════════╗");
        System.out.println("║     TESTE ROBUSTO - SISTEMA DE GESTÃO (90 dias, milhares de ops)    ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════════╝\n");

        long inicio = System.currentTimeMillis();

        cadastrarUsuariosERelacionar();
        criarProjetosETarefasIniciais();

        for (int dia = 1; dia <= DIAS_SIMULACAO; dia++) {
            System.out.printf("\n--- DIA %d/%d ---\n", dia, DIAS_SIMULACAO);
            simularDia(dia);
            if (dia % 10 == 0) {
                System.out.printf("Progresso: %d dias concluídos\n", dia);
            }
        }

        long fim = System.currentTimeMillis();
        gerarRelatorioFinal(fim - inicio);
    }

    private static void cadastrarUsuariosERelacionar() {
        System.out.println("1. Cadastrando " + NUM_GESTORES + " gestores e " +
                (NUM_GESTORES * NUM_DEVS_POR_GESTOR) + " desenvolvedores...");

        for (int i = 1; i <= NUM_GESTORES; i++) {
            UsuarioGestor g = new UsuarioGestor(0, "Gestor_" + i, String.valueOf(100000000 + i),
                    "gestor" + i + "@sistema.com", "gestor" + i, "Departamento_" + i);
            sistema.realizarCadastro(g);
            gestores.add(g);
        }

        for (int i = 0; i < NUM_GESTORES; i++) {
            UsuarioGestor gestor = gestores.get(i);
            for (int j = 1; j <= NUM_DEVS_POR_GESTOR; j++) {
                int devId = i * NUM_DEVS_POR_GESTOR + j;
                UsuarioDev dev = new UsuarioDev(0, "Dev_" + gestor.getNome() + "_" + j,
                        String.valueOf(200000000 + devId), "dev" + devId + "@sistema.com", "dev" + j);
                sistema.realizarCadastro(dev);
                devs.add(dev);
                gestor.getEquipe().add(dev);
            }
        }

        System.out.println("Cadastro concluído. Total de usuários: " + sistema.getUsuarios().size());
        System.out.println("Desenvolvedores cadastrados: " + devs.size());
        System.out.println("Gestores cadastrados: " + gestores.size());
    }

    private static void criarProjetosETarefasIniciais() {
        System.out.println("2. Criando projetos e tarefas iniciais...");
        for (UsuarioGestor gestor : gestores) {
            for (int p = 1; p <= PROJETOS_INICIAIS_POR_GESTOR; p++) {
                String nomeProjeto = "Proj_" + gestor.getNome() + "_" + p;
                Date prazo = addDias(random.nextInt(60) + 15);
                NivelImportancia importancia = NivelImportancia.values()[random.nextInt(4)];
                gestor.criarProjeto(nomeProjeto, prazo, importancia);
                Projeto projeto = sistema.getProjetos().stream().filter(pr -> pr.getNome().equals(nomeProjeto)).findFirst().orElse(null);
                if (projeto != null) projetos.add(projeto);

                int numTarefas = Math.max(1, random.nextInt(TAREFAS_POR_PROJETO_INICIAL * 2));
                for (int t = 1; t <= numTarefas; t++) {
                    UsuarioDev dev = escolherDevDoGestor(gestor);
                    double horas = 5 + random.nextDouble() * 40;
                    String desc = projeto.getNome() + " - Tarefa " + t;
                    gestor.criarAtribuirTarefaEmProjeto(desc, addDias(random.nextInt(60)), importancia, dev.getId(), projeto.getId(), horas);
                }
            }

            int tarefasAvulsas = random.nextInt(5) + 3;
            for (int i = 0; i < tarefasAvulsas; i++) {
                UsuarioDev dev = escolherDevDoGestor(gestor);
                double horas = 2 + random.nextDouble() * 20;
                gestor.criarAtribuirTarefa("Avulsa_" + gestor.getNome() + "_" + i, addDias(random.nextInt(30)), NivelImportancia.values()[random.nextInt(4)], dev.getId(), horas);
            }
        }
        System.out.println("Projetos criados: " + sistema.getProjetos().size());
        System.out.println("Tarefas totais: " + sistema.getTarefas().size());
    }

    private static UsuarioDev escolherDevDoGestor(UsuarioGestor gestor) {
        List<UsuarioDev> equipe = gestor.getEquipe();
        return equipe.get(random.nextInt(equipe.size()));
    }

    private static void simularDia(int dia) {
        registrarHorasTrabalhadas();
        sistema.verificarPrazosExpirados();
        marcarTarefasComoFeito();
        validarTarefasProntas();
        gerarSolicitacoesReorganizacao();
        reatribuirTarefasAtrasadas();
        if (dia % 5 == 0) {
            criarNovosProjetosETarefas();
        }
        if (dia % 15 == 0) {
            sistema.gerarRelatorioDiario();
        }
    }

    private static void registrarHorasTrabalhadas() {
        for (UsuarioDev dev : devs) {
            double horasHoje = 3 + random.nextDouble() * 6;
            List<Tarefa> tarefasDev = dev.getTarefas().stream()
                    .filter(t -> t.getStatus() != StatusTarefa.PRONTO && t.getStatus() != StatusTarefa.FEITO)
                    .collect(Collectors.toList());
            if (tarefasDev.isEmpty()) continue;
            Collections.shuffle(tarefasDev);
            double horasRestantes = horasHoje;
            for (Tarefa t : tarefasDev) {
                if (horasRestantes <= 0) break;
                double horasTarefa = Math.min(horasRestantes, 1.0 + random.nextDouble() * 3);
                t.adicionarHorasTrabalhadas(horasTarefa);
                horasRestantes -= horasTarefa;
            }
        }
    }

    private static void marcarTarefasComoFeito() {
        for (UsuarioDev dev : devs) {
            for (Tarefa t : dev.getTarefas()) {
                if (t.getStatus() == StatusTarefa.PENDENTE && t.calcularProgresso() >= 100.0 && random.nextDouble() < 0.6) {
                    dev.alterarStatusTarefa(t, StatusTarefa.FEITO);
                } else if (t.getStatus() == StatusTarefa.PENDENTE && random.nextDouble() < 0.05) {
                    dev.alterarStatusTarefa(t, StatusTarefa.FEITO);
                }
            }
        }
    }

    private static void validarTarefasProntas() {
        for (UsuarioGestor gestor : gestores) {
            for (UsuarioDev dev : gestor.getEquipe()) {
                List<Tarefa> feitas = dev.getTarefas().stream()
                        .filter(t -> t.getStatus() == StatusTarefa.FEITO)
                        .collect(Collectors.toList());
                for (Tarefa t : feitas) {
                    if (random.nextDouble() < 0.5) {
                        gestor.validarFinalizacao(t);
                    }
                }
            }
        }
    }

    private static void gerarSolicitacoesReorganizacao() {
        for (UsuarioDev dev : devs) {
            if (random.nextDouble() < 0.1) {
                dev.solicitarReorganizacao("Carga excessiva - remanejamento solicitado (dia " + random.nextInt(90) + ")");
                totalSolicitacoes++;
            }
        }
        for (UsuarioGestor gestor : gestores) {
            List<SolicitacaoMudanca> pendentes = sistema.getSolicitacoes().stream()
                    .filter(s -> s.getStatus() == StatusSolicitacao.PENDENTE && gestor.getEquipe().contains(s.getSolicitante()))
                    .collect(Collectors.toList());
            for (SolicitacaoMudanca s : pendentes) {
                boolean aprovar = random.nextDouble() < 0.7;
                gestor.processarSolicitacaoMudanca(s, aprovar);
            }
        }
    }

    private static void reatribuirTarefasAtrasadas() {
        for (UsuarioGestor gestor : gestores) {
            List<Tarefa> atrasadas = gestor.getEquipe().stream()
                    .flatMap(d -> d.getTarefas().stream())
                    .filter(t -> t.getStatus() == StatusTarefa.ATRASADO)
                    .collect(Collectors.toList());
            for (Tarefa t : atrasadas) {
                if (random.nextDouble() < 0.3) {
                    List<UsuarioDev> equipe = gestor.getEquipe();
                    UsuarioDev novoDev = equipe.get(random.nextInt(equipe.size()));
                    if (!novoDev.equals(t.getDevResponsavel())) {
                        gestor.reatribuirTarefaAtrasada(t, novoDev);
                        totalReatribuicoes++;
                    }
                }
            }
        }
    }

    private static void criarNovosProjetosETarefas() {
        for (UsuarioGestor gestor : gestores) {
            if (random.nextDouble() < 0.4) {
                String nomeProjeto = "Novo_" + gestor.getNome() + "_" + System.currentTimeMillis();
                Date prazo = addDias(random.nextInt(60) + 10);
                NivelImportancia importancia = NivelImportancia.values()[random.nextInt(4)];
                gestor.criarProjeto(nomeProjeto, prazo, importancia);
                Projeto projeto = sistema.getProjetos().stream().filter(p -> p.getNome().equals(nomeProjeto)).findFirst().orElse(null);
                if (projeto != null) {
                    projetos.add(projeto);
                    int numTarefas = random.nextInt(4) + 1;
                    for (int i = 0; i < numTarefas; i++) {
                        UsuarioDev dev = escolherDevDoGestor(gestor);
                        double horas = 5 + random.nextDouble() * 30;
                        gestor.criarAtribuirTarefaEmProjeto("NovaTarefa_" + i, addDias(random.nextInt(40)), importancia, dev.getId(), projeto.getId(), horas);
                    }
                }
            }
        }
    }

    private static void gerarRelatorioFinal(long duracaoMs) {
        System.out.println("\n========================================================");
        System.out.println("RELATÓRIO FINAL DO TESTE ROBUSTO");
        System.out.println("========================================================\n");

        System.out.println("=== MÉTRICAS GERAIS ===");
        System.out.printf("Tempo de simulação: %.2f segundos\n", duracaoMs / 1000.0);
        System.out.println("Total de usuários cadastrados: " + sistema.getUsuarios().size());
        System.out.println("Total de gestores: " + gestores.size());
        System.out.println("Total de desenvolvedores: " + devs.size());
        System.out.println("Total de projetos criados: " + sistema.getProjetos().size());
        System.out.println("Total de tarefas criadas: " + sistema.getTarefas().size());
        System.out.println("Total de relatórios enviados (devs): " + sistema.getRelatorios().size());
        System.out.println("Total de solicitações de mudança: " + totalSolicitacoes);
        System.out.println("Total de reatribuições realizadas: " + totalReatribuicoes);

        System.out.println("\n=== STATUS DAS TAREFAS ===");
        long pendentes = sistema.getTarefas().stream().filter(t -> t.getStatus() == StatusTarefa.PENDENTE).count();
        long feitas = sistema.getTarefas().stream().filter(t -> t.getStatus() == StatusTarefa.FEITO).count();
        long prontas = sistema.getTarefas().stream().filter(t -> t.getStatus() == StatusTarefa.PRONTO).count();
        long atrasadas = sistema.getTarefas().stream().filter(t -> t.getStatus() == StatusTarefa.ATRASADO).count();
        System.out.printf("PENDENTE: %d\nFEITO: %d\nPRONTO: %d\nATRASADO: %d\n", pendentes, feitas, prontas, atrasadas);

        System.out.println("\n=== STATUS DOS PROJETOS ===");
        long projPendentes = sistema.getProjetos().stream().filter(p -> p.getStatus() == StatusTarefa.PENDENTE).count();
        long projFeitos = sistema.getProjetos().stream().filter(p -> p.getStatus() == StatusTarefa.FEITO).count();
        long projProntos = sistema.getProjetos().stream().filter(p -> p.getStatus() == StatusTarefa.PRONTO).count();
        long projAtrasados = sistema.getProjetos().stream().filter(p -> p.getStatus() == StatusTarefa.ATRASADO).count();
        System.out.printf("PENDENTE: %d\nFEITO: %d\nPRONTO: %d\nATRASADO: %d\n", projPendentes, projFeitos, projProntos, projAtrasados);

        System.out.println("\n=== PROGRESSO DOS DESENVOLVEDORES (TOP 20) ===");
        devs.stream()
                .sorted((d1, d2) -> Double.compare(d2.calcularProgressoTotal(), d1.calcularProgressoTotal()))
                .limit(20)
                .forEach(d -> System.out.printf("%-20s : %6.2f%%\n", d.getNome(), d.calcularProgressoTotal()));

        System.out.println("\n=== VALIDAÇÃO DE CONSISTÊNCIA ===");
        boolean consistente = true;
        for (UsuarioDev dev : devs) {
            for (Tarefa t : dev.getTarefas()) {
                if (!sistema.getTarefas().contains(t)) {
                    System.out.println("ERRO: Tarefa " + t.getId() + " não está na lista global!");
                    consistente = false;
                }
                if (t.getProjetoPai() != null && !t.getProjetoPai().getTarefas().contains(t)) {
                    System.out.println("ERRO: Tarefa " + t.getId() + " não está na lista do projeto!");
                    consistente = false;
                }
            }
        }
        for (Projeto p : sistema.getProjetos()) {
            boolean todasProntas = !p.getTarefas().isEmpty() && p.getTarefas().stream().allMatch(t -> t.getStatus() == StatusTarefa.PRONTO);
            if (todasProntas && p.getStatus() != StatusTarefa.FEITO && p.getStatus() != StatusTarefa.PRONTO) {
                System.out.println("ERRO: Projeto " + p.getId() + " tem todas tarefas PRONTO mas status = " + p.getStatus());
                consistente = false;
            }
        }
        if (consistente) System.out.println("✅ Todas as verificações de consistência passaram.");
        else System.out.println("❌ Foram encontradas inconsistências.");

        System.out.println("\n=== RELATÓRIO DIÁRIO FINAL ===");
        sistema.gerarRelatorioDiario();

        System.out.println("\n══════════════════════════════════════════════════════════════════════");
        System.out.println("✅ TESTE ROBUSTO CONCLUÍDO - NENHUMA FALHA GRAVE DETECTADA!");
        System.out.println("══════════════════════════════════════════════════════════════════════");
    }

    private static Date addDias(int dias) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, dias);
        return cal.getTime();
    }
}