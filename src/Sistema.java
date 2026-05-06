import java.util.*;

public class Sistema {
    private static Sistema instance;
    private List<Usuario> usuarios;
    private List<UsuarioDev> devs;
    private List<UsuarioGestor> gestores;
    private List<Projeto> projetos;
    private List<Tarefa> tarefas;
    private List<Relatorio> relatorios;
    private List<SolicitacaoMudanca> solicitacoes;

    private int proximoIdUsuario = 1; // para simular ID incremental

    private Sistema() {
        usuarios = new ArrayList<>();
        devs = new ArrayList<>();
        gestores = new ArrayList<>();
        projetos = new ArrayList<>();
        tarefas = new ArrayList<>();
        relatorios = new ArrayList<>();
        solicitacoes = new ArrayList<>();
    }

    public static Sistema getInstance() {
        if (instance == null) instance = new Sistema();
        return instance;
    }

    // RF01: cadastro (gera ID automaticamente)
    public boolean realizarCadastro(Usuario usuario) {
        if (autenticar(usuario.getEmail(), usuario.getSenha()) == null) {
            usuario.setId(proximoIdUsuario++);
            usuarios.add(usuario);
            if (usuario instanceof UsuarioDev) devs.add((UsuarioDev) usuario);
            else if (usuario instanceof UsuarioGestor) gestores.add((UsuarioGestor) usuario);
            return true;
        }
        return false;
    }

    public Usuario autenticar(String email, String senha) {
        return usuarios.stream()
                .filter(u -> u.getEmail().equals(email) && u.getSenha().equals(senha))
                .findFirst()
                .orElse(null);
    }

    // Métodos para adicionar objetos (os IDs são gerados dentro das classes)
    public void adicionarProjeto(Projeto p) { projetos.add(p); }
    public void adicionarTarefa(Tarefa t) { tarefas.add(t); }
    public void adicionarRelatorio(Relatorio r) { relatorios.add(r); }
    public void adicionarSolicitacao(SolicitacaoMudanca s) { solicitacoes.add(s); }

    // Buscas
    public UsuarioDev buscarDevPorId(int id) {
        return devs.stream().filter(d -> d.getId() == id).findFirst().orElse(null);
    }

    public UsuarioGestor buscarGestorPorDev(UsuarioDev dev) {
        for (UsuarioGestor g : gestores) {
            if (g.getEquipe().contains(dev)) return g;
        }
        return null;
    }

    public Projeto buscarProjetoPorId(int id) {
        return projetos.stream().filter(p -> p.getId() == id).findFirst().orElse(null);
    }

    // RF13 + RF16 + RF17: notificações imediatas
    public void notificarGestorMudancaStatus(Tarefa tarefa, UsuarioDev dev) {
        UsuarioGestor gestor = buscarGestorPorDev(dev);
        if (gestor != null) {
            System.out.println(">>> NOTIFICAÇÃO para gestor " + gestor.getNome() +
                    ": O dev " + dev.getNome() + " alterou a tarefa " + tarefa.getId() +
                    " para " + tarefa.getStatus());
        }
        // Também verifica se há tarefas FEITO ou ATRASADO para notificar (RF16 e RF17)
        verificarItensFeitoEAtrasados(gestor);
    }

    private void verificarItensFeitoEAtrasados(UsuarioGestor gestor) {
        if (gestor == null) return;
        long feitos = tarefas.stream().filter(t -> t.getStatus() == StatusTarefa.FEITO).count();
        if (feitos > 0) {
            System.out.println(">>> NOTIFICAÇÃO: Existem " + feitos + " tarefas com status FEITO.");
        }
        long atrasados = tarefas.stream().filter(t -> t.getStatus() == StatusTarefa.ATRASADO).count();
        if (atrasados > 0) {
            System.out.println(">>> ALERTA: Existem " + atrasados + " tarefas com status ATRASADO.");
        }
    }

    // RF15: verificar prazos expirados (chamar no final do expediente)
    public void verificarPrazosExpirados() {
        Date agora = new Date();
        for (Tarefa t : tarefas) {
            if (t.getStatus() == StatusTarefa.PENDENTE && t.getPrazo().before(agora)) {
                t.setStatus(StatusTarefa.ATRASADO);
                System.out.println("Tarefa " + t.getId() + " expirou e foi marcada como ATRASADA.");
                // Notificar gestor imediatamente (RF17)
                UsuarioGestor gestor = buscarGestorPorDev(t.getDevResponsavel());
                if (gestor != null) {
                    System.out.println(">>> ALERTA: Tarefa atrasada notificada ao gestor " + gestor.getNome());
                }
            }
        }
        for (Projeto p : projetos) {
            if (p.getStatus() == StatusTarefa.PENDENTE && p.getPrazo().before(agora)) {
                p.setStatus(StatusTarefa.ATRASADO);
                System.out.println("Projeto " + p.getId() + " expirou e foi marcado como ATRASADO.");
            }
        }
    }

    // RF14: gerar relatório diário automático
    public void gerarRelatorioDiario() {
        Date hoje = new Date();
        long tarefasCumpridas = tarefas.stream().filter(t -> t.getStatus() == StatusTarefa.PRONTO).count();
        long tarefasAtrasadas = tarefas.stream().filter(t -> t.getStatus() == StatusTarefa.ATRASADO).count();
        long relatoriosEnviados = relatorios.size();

        StringBuilder conteudo = new StringBuilder();
        conteudo.append("Relatório Diário - ").append(hoje).append("\n");
        conteudo.append("Tarefas cumpridas (PRONTO): ").append(tarefasCumpridas).append("\n");
        conteudo.append("Tarefas atrasadas: ").append(tarefasAtrasadas).append("\n");
        conteudo.append("Relatórios enviados pelos devs: ").append(relatoriosEnviados).append("\n");
        conteudo.append("Detalhes dos relatórios:\n");
        for (Relatorio r : relatorios) {
            conteudo.append("- ").append(r.getConteudo()).append("\n");
        }

        Relatorio relatorioDiario = new Relatorio(conteudo.toString());
        relatorioDiario.setDataEnvio(hoje);
        relatorios.add(relatorioDiario);
        System.out.println(conteudo.toString());

        // RF16 e RF17 também são emitidos aqui (além das notificações imediatas)
        for (UsuarioGestor g : gestores) {
            verificarItensFeitoEAtrasados(g);
        }
    }

    // Getters para acesso externo (mas cuidado para não modificar diretamente)
    public List<Usuario> getUsuarios() { return usuarios; }
    public List<UsuarioDev> getDevs() { return devs; }
    public List<UsuarioGestor> getGestores() { return gestores; }
    public List<Projeto> getProjetos() { return projetos; }
    public List<Tarefa> getTarefas() { return tarefas; }
    public List<Relatorio> getRelatorios() { return relatorios; }
    public List<SolicitacaoMudanca> getSolicitacoes() { return solicitacoes; }
}