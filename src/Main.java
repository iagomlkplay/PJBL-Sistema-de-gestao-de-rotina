import java.util.Date;
import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;

public class Main {
    private static Sistema sistema = Sistema.getInstance();
    private static List<UsuarioDev> devs = new ArrayList<>();
    private static List<UsuarioGestor> gestores = new ArrayList<>();
    private static List<Projeto> projetos = new ArrayList<>();
    private static List<Tarefa> tarefas = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║        TESTE EXTENSO DO SISTEMA DE GESTÃO DE ROTINA         ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");

        // 1. Cadastro de usuários (RF01)
        cadastrarUsuarios();

        // 2. Criação de projetos e tarefas pelos gestores (RF09)
        criarProjetosETarefas();

        // 3. Visualizações iniciais (RF04, RF08)
        visualizacoesIniciais();

        // 4. Simulação de 30 dias de trabalho (ciclo diário)
        simularTrabalhoPorDias(30);

        // 5. Relatório final consolidado
        relatorioFinal();

        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                     FIM DOS TESTES                          ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }

    private static void cadastrarUsuarios() {
        System.out.println("1. CADASTRANDO USUÁRIOS");
        System.out.println("--------------------------------------------------");

        // 3 desenvolvedores
        UsuarioDev devAna = new UsuarioDev(0, "Ana Souza", "11111111111", "ana@email.com", "ana123");
        UsuarioDev devBruno = new UsuarioDev(0, "Bruno Lima", "22222222222", "bruno@email.com", "bruno123");
        UsuarioDev devCarla = new UsuarioDev(0, "Carla Mendes", "33333333333", "carla@email.com", "carla123");
        UsuarioDev devDaniel = new UsuarioDev(0, "Daniel Rocha", "44444444444", "daniel@email.com", "daniel123");
        UsuarioDev devElisa = new UsuarioDev(0, "Elisa Santos", "55555555555", "elisa@email.com", "elisa123");

        // 2 gestores
        UsuarioGestor gestorTI = new UsuarioGestor(0, "Felipe Nogueira", "66666666666", "felipe@email.com", "felipe123", "TI");
        UsuarioGestor gestorProd = new UsuarioGestor(0, "Gabriela Rocha", "77777777777", "gabriela@email.com", "gabi123", "Produtos");

        sistema.realizarCadastro(devAna);
        sistema.realizarCadastro(devBruno);
        sistema.realizarCadastro(devCarla);
        sistema.realizarCadastro(devDaniel);
        sistema.realizarCadastro(devElisa);
        sistema.realizarCadastro(gestorTI);
        sistema.realizarCadastro(gestorProd);

        // Montar equipes
        gestorTI.getEquipe().add(devAna);
        gestorTI.getEquipe().add(devBruno);
        gestorTI.getEquipe().add(devCarla);
        gestorProd.getEquipe().add(devDaniel);
        gestorProd.getEquipe().add(devElisa);

        devs.add(devAna); devs.add(devBruno); devs.add(devCarla); devs.add(devDaniel); devs.add(devElisa);
        gestores.add(gestorTI); gestores.add(gestorProd);

        System.out.println("Total de usuários cadastrados: " + sistema.getUsuarios().size());
        System.out.println("DEVs: Ana, Bruno, Carla, Daniel, Elisa");
        System.out.println("Gestores: Felipe (TI), Gabriela (Produtos)\n");
    }

    private static void criarProjetosETarefas() {
        System.out.println("2. CRIANDO PROJETOS E TAREFAS");
        System.out.println("--------------------------------------------------");

        // Datas de referência
        Calendar cal = Calendar.getInstance();
        // Projetos com prazos diferentes (alguns já vencidos)

        // Gestor TI
        UsuarioGestor gestorTI = gestores.get(0);
        UsuarioDev ana = devs.get(0);
        UsuarioDev bruno = devs.get(1);
        UsuarioDev carla = devs.get(2);

        // Projeto 1: Sistema de Vendas (prazo 60 dias)
        cal.add(Calendar.DAY_OF_MONTH, 60);
        gestorTI.criarAtribuirProjeto("Sistema de Vendas", cal.getTime(), NivelImportancia.ALTA, ana.getId());
        Projeto projVendas = ana.getProjetos().get(ana.getProjetos().size()-1);
        projetos.add(projVendas);

        // Tarefas do projeto vendas
        gestorTI.criarAtribuirTarefaEmProjeto("Modelagem de banco de dados", addDays(30), NivelImportancia.ALTA, ana.getId(), projVendas.getId(), 20.0);
        gestorTI.criarAtribuirTarefaEmProjeto("API de produtos", addDays(40), NivelImportancia.URGENTE, ana.getId(), projVendas.getId(), 35.0);
        gestorTI.criarAtribuirTarefaEmProjeto("Frontend - cadastro de clientes", addDays(45), NivelImportancia.MEDIA, bruno.getId(), projVendas.getId(), 25.0);
        gestorTI.criarAtribuirTarefaEmProjeto("Relatórios de vendas", addDays(55), NivelImportancia.ALTA, bruno.getId(), projVendas.getId(), 15.0);
        gestorTI.criarAtribuirTarefaEmProjeto("Testes integrados", addDays(60), NivelImportancia.MEDIA, carla.getId(), projVendas.getId(), 10.0);

        // Projeto 2: App de Delivery (prazo 30 dias, já vencido)
        cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -5); // Vencido há 5 dias
        gestorTI.criarAtribuirProjeto("App Delivery", cal.getTime(), NivelImportancia.URGENTE, carla.getId());
        Projeto projDelivery = carla.getProjetos().get(carla.getProjetos().size()-1);
        projetos.add(projDelivery);

        gestorTI.criarAtribuirTarefaEmProjeto("Arquitetura mobile", addDays(-10), NivelImportancia.ALTA, carla.getId(), projDelivery.getId(), 15.0);
        gestorTI.criarAtribuirTarefaEmProjeto("Tela de login", addDays(-8), NivelImportancia.URGENTE, carla.getId(), projDelivery.getId(), 8.0);
        gestorTI.criarAtribuirTarefaEmProjeto("Integração com maps", addDays(-3), NivelImportancia.MEDIA, carla.getId(), projDelivery.getId(), 12.0);

        // Projeto 3: Migração de servidores (prazo curto, sem tarefas inicialmente)
        gestorTI.criarAtribuirProjeto("Migração Servidores", addDays(7), NivelImportancia.URGENTE, bruno.getId());
        Projeto projMigracao = bruno.getProjetos().get(bruno.getProjetos().size()-1);
        projetos.add(projMigracao);

        // Gestor Produtos
        UsuarioGestor gestorProd = gestores.get(1);
        UsuarioDev daniel = devs.get(3);
        UsuarioDev elisa = devs.get(4);

        gestorProd.criarAtribuirProjeto("Campanha de Marketing", addDays(20), NivelImportancia.MEDIA, daniel.getId());
        Projeto projMarketing = daniel.getProjetos().get(daniel.getProjetos().size()-1);
        projetos.add(projMarketing);

        gestorProd.criarAtribuirTarefaEmProjeto("Criar landing page", addDays(10), NivelImportancia.ALTA, daniel.getId(), projMarketing.getId(), 12.0);
        gestorProd.criarAtribuirTarefaEmProjeto("SEO básico", addDays(15), NivelImportancia.MEDIA, daniel.getId(), projMarketing.getId(), 6.0);
        gestorProd.criarAtribuirTarefaEmProjeto("Anúncios patrocinados", addDays(18), NivelImportancia.MEDIA, elisa.getId(), projMarketing.getId(), 10.0);
        gestorProd.criarAtribuirTarefaEmProjeto("Relatório de métricas", addDays(20), NivelImportancia.BAIXA, elisa.getId(), projMarketing.getId(), 5.0);

        // Tarefas avulsas (sem projeto) para os devs
        gestorTI.criarAtribuirTarefa("Correção de bug no sistema legado", addDays(3), NivelImportancia.URGENTE, ana.getId(), 4.0);
        gestorTI.criarAtribuirTarefa("Documentação técnica", addDays(15), NivelImportancia.BAIXA, bruno.getId(), 8.0);
        gestorProd.criarAtribuirTarefa("Pesquisa de satisfação", addDays(5), NivelImportancia.MEDIA, elisa.getId(), 6.0);

        System.out.println("Projetos criados: " + projetos.size());
        System.out.println("Total de tarefas no sistema: " + sistema.getTarefas().size());
        System.out.println();
    }

    private static void visualizacoesIniciais() {
        System.out.println("3. VISUALIZAÇÕES INICIAIS (antes do trabalho)");
        System.out.println("--------------------------------------------------");
        UsuarioDev ana = devs.get(0);
        ana.visualizarPropriosProjetosTarefas();
        System.out.println();
        ana.visualizarProgressoEquipe();
        System.out.println();
        gestores.get(0).visualizarTodosProjetosTarefas();
        System.out.println();
    }

    private static void simularTrabalhoPorDias(int dias) {
        System.out.println("4. SIMULAÇÃO DE TRABALHO POR " + dias + " DIAS");
        System.out.println("--------------------------------------------------");

        // Para cada dia, simular atividades aleatórias
        for (int dia = 1; dia <= dias; dia++) {
            System.out.println("\n--- DIA " + dia + " ---");

            // 1. Registrar horas trabalhadas (aleatório)
            registrarHorasTrabalhadas();

            // 2. Alguns devs marcam tarefas como FEITO (evento aleatório)
            marcarTarefasComoFeito();

            // 3. Gestor valida tarefas FEITO
            validarTarefasProntas();

            // 4. Processar solicitações de reorganização (quando houver)
            processarSolicitacoesPendentes();

            // 5. Reatribuir tarefas/projetos atrasados (se houver)
            reatribuirAtrasados();

            // 6. Verificar expiração de prazos (RF15)
            sistema.verificarPrazosExpirados();

            // 7. Relatório diário a cada 5 dias (RF14)
            if (dia % 5 == 0 || dia == dias) {
                sistema.gerarRelatorioDiario();
            }

            // Pequena pausa para não poluir muito o console (opcional)
            if (dia % 10 == 0) {
                System.out.println("... avançando para o dia " + (dia+1) + " ...");
            }
        }
    }

    private static void registrarHorasTrabalhadas() {
        // Cada dev trabalha entre 4 e 8 horas por dia, distribuídas em tarefas pendentes
        for (UsuarioDev dev : devs) {
            double horasHoje = 4 + Math.random() * 4; // 4 a 8 horas
            List<Tarefa> tarefasDev = dev.getTarefas();
            if (tarefasDev.isEmpty()) continue;

            // Distribuir horas entre tarefas não concluídas
            double horasRestantes = horasHoje;
            for (Tarefa t : tarefasDev) {
                if (t.getStatus() == StatusTarefa.PENDENTE || t.getStatus() == StatusTarefa.ATRASADO) {
                    double horasParaTarefa = Math.min(horasRestantes, 2.0 + Math.random() * 3);
                    if (horasParaTarefa > 0) {
                        t.adicionarHorasTrabalhadas(horasParaTarefa);
                        horasRestantes -= horasParaTarefa;
                        if (horasRestantes <= 0) break;
                    }
                }
            }
        }
    }

    private static void marcarTarefasComoFeito() {
        // 30% de chance de um dev marcar uma tarefa pendente como FEITO
        for (UsuarioDev dev : devs) {
            if (Math.random() < 0.3) {
                List<Tarefa> pendentes = dev.getTarefas().stream()
                        .filter(t -> t.getStatus() == StatusTarefa.PENDENTE && t.calcularProgresso() == 100.0)
                        .toList();
                if (!pendentes.isEmpty()) {
                    Tarefa tarefa = pendentes.get((int)(Math.random() * pendentes.size()));
                    dev.alterarStatusTarefa(tarefa, StatusTarefa.FEITO);
                }
            }
        }
    }

    private static void validarTarefasProntas() {
        // Gestor valida tarefas FEITO
        for (UsuarioGestor gestor : gestores) {
            for (UsuarioDev dev : gestor.getEquipe()) {
                List<Tarefa> feitas = dev.getTarefas().stream()
                        .filter(t -> t.getStatus() == StatusTarefa.FEITO)
                        .toList();
                for (Tarefa t : feitas) {
                    if (Math.random() < 0.5) { // valida metade das FEITO
                        gestor.validarFinalizacao(t);
                    }
                }
            }
        }
    }

    private static void processarSolicitacoesPendentes() {
        // A cada 10 dias, devs podem solicitar reorganização
        for (UsuarioDev dev : devs) {
            if (Math.random() < 0.1) {
                dev.solicitarReorganizacao("Necessidade de ajuste de prioridades - carga de trabalho elevada.");
            }
        }
        // Gestor processa as pendentes
        for (UsuarioGestor gestor : gestores) {
            List<SolicitacaoMudanca> pendentes = sistema.getSolicitacoes().stream()
                    .filter(s -> s.getStatus() == StatusSolicitacao.PENDENTE)
                    .toList();
            for (SolicitacaoMudanca s : pendentes) {
                if (gestor.getEquipe().contains(s.getSolicitante())) {
                    boolean aprova = Math.random() < 0.7; // 70% de aprovação
                    gestor.processarSolicitacaoMudanca(s, aprova);
                }
            }
        }
    }

    private static void reatribuirAtrasados() {
        // Gestor reatribui tarefas/projetos atrasados para outros devs da equipe
        for (UsuarioGestor gestor : gestores) {
            List<UsuarioDev> equipe = gestor.getEquipe();
            if (equipe.size() < 2) continue;

            // Reatribuir tarefas atrasadas
            for (UsuarioDev dev : equipe) {
                for (Tarefa t : dev.getTarefas()) {
                    if (t.getStatus() == StatusTarefa.ATRASADO && Math.random() < 0.4) {
                        UsuarioDev novoDev = equipe.get((int)(Math.random() * equipe.size()));
                        if (novoDev != dev) {
                            gestor.reatribuirTarefaAtrasada(t, novoDev);
                            break;
                        }
                    }
                }
            }

            // Reatribuir projetos atrasados
            for (UsuarioDev dev : equipe) {
                for (Projeto p : dev.getProjetos()) {
                    if (p.getStatus() == StatusTarefa.ATRASADO && Math.random() < 0.3) {
                        UsuarioDev novoDev = equipe.get((int)(Math.random() * equipe.size()));
                        if (novoDev != dev) {
                            gestor.reatribuirProjetoAtrasado(p, novoDev);
                            break;
                        }
                    }
                }
            }
        }
    }

    private static void relatorioFinal() {
        System.out.println("\n5. RELATÓRIO FINAL CONSOLIDADO");
        System.out.println("--------------------------------------------------");

        System.out.println("=== MÉTRICAS GERAIS ===");
        System.out.println("Total de usuários: " + sistema.getUsuarios().size());
        System.out.println("Total de projetos: " + sistema.getProjetos().size());
        System.out.println("Total de tarefas: " + sistema.getTarefas().size());
        System.out.println("Total de relatórios enviados: " + sistema.getRelatorios().size());
        System.out.println("Total de solicitações de mudança: " + sistema.getSolicitacoes().size());

        System.out.println("\n=== STATUS DAS TAREFAS ===");
        long pendentes = sistema.getTarefas().stream().filter(t -> t.getStatus() == StatusTarefa.PENDENTE).count();
        long feitas = sistema.getTarefas().stream().filter(t -> t.getStatus() == StatusTarefa.FEITO).count();
        long prontas = sistema.getTarefas().stream().filter(t -> t.getStatus() == StatusTarefa.PRONTO).count();
        long atrasadas = sistema.getTarefas().stream().filter(t -> t.getStatus() == StatusTarefa.ATRASADO).count();
        System.out.printf("PENDENTE: %d\nFEITO: %d\nPRONTO: %d\nATRASADO: %d\n", pendentes, feitas, prontas, atrasadas);

        System.out.println("\n=== STATUS DOS PROJETOS ===");
        pendentes = sistema.getProjetos().stream().filter(p -> p.getStatus() == StatusTarefa.PENDENTE).count();
        feitas = sistema.getProjetos().stream().filter(p -> p.getStatus() == StatusTarefa.FEITO).count();
        prontas = sistema.getProjetos().stream().filter(p -> p.getStatus() == StatusTarefa.PRONTO).count();
        atrasadas = sistema.getProjetos().stream().filter(p -> p.getStatus() == StatusTarefa.ATRASADO).count();
        System.out.printf("PENDENTE: %d\nFEITO: %d\nPRONTO: %d\nATRASADO: %d\n", pendentes, feitas, prontas, atrasadas);

        System.out.println("\n=== PROGRESSO DOS DESENVOLVEDORES ===");
        for (UsuarioDev dev : devs) {
            double progresso = dev.calcularProgressoTotal();
            System.out.printf("%-15s: %6.2f%%\n", dev.getNome(), progresso);
        }

        System.out.println("\n=== TAREFAS COM MAIS HORAS TRABALHADAS (TOP 5) ===");
        sistema.getTarefas().stream()
                .sorted((t1, t2) -> Double.compare(t2.getHorasTrabalhadas(), t1.getHorasTrabalhadas()))
                .limit(5)
                .forEach(t -> System.out.printf("Tarefa %d (%s) - %.1fh / %.1fh estimadas\n",
                        t.getId(), t.getDescricao(), t.getHorasTrabalhadas(), t.getHorasEstimadas()));

        System.out.println("\n=== PROJETOS COM MAIOR ATRASO ===");
        sistema.getProjetos().stream()
                .filter(p -> p.getStatus() == StatusTarefa.ATRASADO)
                .forEach(p -> System.out.println(p.getInformacoesDetalhadas()));

        // Último relatório diário completo (mesmo se já tiver sido gerado, força um)
        sistema.gerarRelatorioDiario();
    }

    // Utilitário para somar dias a partir de hoje
    private static Date addDays(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, days);
        return cal.getTime();
    }
}