import javax.swing.*;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class Sistema {
    private static Sistema instance;
    private UsuarioDAO usuarioDAO;
    private ProjetoDAO projetoDAO;
    private TarefaDAO tarefaDAO;
    private RelatorioDAO relatorioDAO;
    private SolicitacaoDAO solicitacaoDAO;
    private Timer verificadorTimer;

    private Sistema() {
        usuarioDAO = new UsuarioDAO();
        projetoDAO = new ProjetoDAO();
        tarefaDAO = new TarefaDAO();
        relatorioDAO = new RelatorioDAO();
        solicitacaoDAO = new SolicitacaoDAO();
    }

    public static Sistema getInstance() {
        if (instance == null) {
            synchronized (Sistema.class) {
                if (instance == null) instance = new Sistema();
            }
        }
        return instance;
    }

    // === RF01: Cadastro e autenticação ===
    public boolean realizarCadastro(Usuario usuario) {
        try {
            if (usuarioDAO.buscarPorEmail(usuario.getEmail()) != null) return false;
            if (usuarioDAO.buscarPorCpf(usuario.getCpf()) != null) return false;
            usuarioDAO.inserir(usuario);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Usuario autenticar(String email, String senha) {
        try {
            return usuarioDAO.autenticar(email, senha);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // === Métodos de adição ===
    public void adicionarProjeto(Projeto p) {
        try { projetoDAO.inserir(p); } catch (Exception e) { e.printStackTrace(); }
    }

    public void adicionarTarefa(Tarefa t) {
        try { tarefaDAO.inserir(t); } catch (Exception e) { e.printStackTrace(); }
    }

    public void adicionarRelatorio(Relatorio r) {
        try { relatorioDAO.inserir(r); } catch (Exception e) { e.printStackTrace(); }
    }

    public void adicionarSolicitacao(Solicitacao s) {
        try { solicitacaoDAO.inserir(s); } catch (Exception e) { e.printStackTrace(); }
    }

    // === Buscas ===
    public UsuarioDev buscarDevPorId(int id) {
        try {
            Usuario u = usuarioDAO.buscarPorId(id);
            return (u instanceof UsuarioDev) ? (UsuarioDev) u : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Projeto buscarProjetoPorId(int id) {
        try { return projetoDAO.buscarPorId(id); } catch (Exception e) { e.printStackTrace(); return null; }
    }

    public UsuarioGestor buscarGestorPorDev(UsuarioDev dev) {
        try {
            int gestorId = dev.getGestorId();
            if (gestorId == 0) return null;
            Usuario u = usuarioDAO.buscarPorId(gestorId);
            return (u instanceof UsuarioGestor) ? (UsuarioGestor) u : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // === Listagens ===
    public List<UsuarioDev> getDevs() {
        try {
            List<UsuarioDev> devs = new ArrayList<>();
            for (Usuario u : usuarioDAO.listarTodos()) {
                if (u instanceof UsuarioDev) devs.add((UsuarioDev) u);
            }
            return devs;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<UsuarioGestor> getGestores() {
        try {
            List<UsuarioGestor> gestores = new ArrayList<>();
            for (Usuario u : usuarioDAO.listarTodos()) {
                if (u instanceof UsuarioGestor) gestores.add((UsuarioGestor) u);
            }
            return gestores;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Projeto> getProjetos() {
        try { return projetoDAO.listarTodos(); } catch (Exception e) { e.printStackTrace(); return new ArrayList<>(); }
    }

    public List<Tarefa> getTarefas() {
        try { return tarefaDAO.listarTodas(); } catch (Exception e) { e.printStackTrace(); return new ArrayList<>(); }
    }

    public List<Relatorio> getRelatorios() {
        try { return relatorioDAO.listarTodos(); } catch (Exception e) { e.printStackTrace(); return new ArrayList<>(); }
    }

    public List<Solicitacao> getSolicitacoes() {
        try { return solicitacaoDAO.listarTodos(); } catch (Exception e) { e.printStackTrace(); return new ArrayList<>(); }
    }

    // === Solicitações por gestor ===
    public List<Solicitacao> getSolicitacoesPorGestor(int gestorId) {
        try { return solicitacaoDAO.listarPorGestor(gestorId); } catch (Exception e) { e.printStackTrace(); return new ArrayList<>(); }
    }

    // === Tarefas e projetos por equipe ===
    public List<Tarefa> getTarefasDaEquipe(int gestorId) {
        try {
            List<UsuarioDev> equipe = usuarioDAO.listarDevsPorGestor(gestorId);
            Set<Integer> devIds = equipe.stream().map(UsuarioDev::getId).collect(Collectors.toSet());
            return tarefaDAO.listarTodas().stream()
                    .filter(t -> devIds.contains(t.getDevResponsavel().getId()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Projeto> getProjetosDaEquipe(int gestorId) {
        try { return projetoDAO.listarPorGestor(gestorId); } catch (Exception e) { e.printStackTrace(); return new ArrayList<>(); }
    }

    // === Notificações (console) ===
    public void notificarConsoleGestorMudancaStatus(Tarefa tarefa, UsuarioDev dev) {
        UsuarioGestor gestor = buscarGestorPorDev(dev);
        if (gestor != null) {
            System.out.println(">>> NOTIFICAÇÃO para gestor " + gestor.getNome() + " (ID " + gestor.getId() + "): " +
                    "O dev " + dev.getNome() + " alterou a tarefa " + tarefa.getId() + " para " + tarefa.getStatus());
        } else {
            System.out.println("Gestor não encontrado para o dev " + dev.getNome());
        }
    }

    // === Expiração de prazos ===
    public void verificarPrazosExpirados() {
        Date agora = new Date();
        try {
            // Tarefas
            List<Tarefa> todasTarefas = getTarefas();
            for (Tarefa t : todasTarefas) {
                if (t.getStatus() == StatusTarefa.PENDENTE && t.getPrazo().before(agora)) {
                    t.setStatus(StatusTarefa.ATRASADO);
                    tarefaDAO.atualizarStatus(t.getId(), StatusTarefa.ATRASADO);
                    System.out.println("Tarefa " + t.getId() + " expirou e foi marcada como ATRASADA.");
                    // Notifica o gestor
                    UsuarioGestor gestor = buscarGestorPorDev(t.getDevResponsavel());
                    if (gestor != null) {
                        System.out.println(">>> ALERTA para gestor " + gestor.getNome() + " (ID " + gestor.getId() + "): " +
                                "Tarefa '" + t.getDescricao() + "' (ID " + t.getId() + ") expirou.");
                    }
                }
            }
            // Projetos
            List<Projeto> todosProjetos = getProjetos();
            for (Projeto p : todosProjetos) {
                if (p.getStatus() == StatusTarefa.PENDENTE && p.getPrazo().before(agora)) {
                    p.setStatus(StatusTarefa.ATRASADO);
                    projetoDAO.atualizarStatus(p.getId(), StatusTarefa.ATRASADO);
                    System.out.println("Projeto " + p.getId() + " expirou e foi marcado como ATRASADO.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // === Relatório ===
    public String gerarRelatorioEquipe(int gestorId) {
        try {
            List<UsuarioDev> equipe = usuarioDAO.listarDevsPorGestor(gestorId);
            Set<Integer> devIds = equipe.stream().map(UsuarioDev::getId).collect(Collectors.toSet());

            // Tarefas da equipe
            List<Tarefa> tarefasEquipe = getTarefas().stream()
                    .filter(t -> devIds.contains(t.getDevResponsavel().getId()))
                    .collect(Collectors.toList());

            long tarefasCumpridas = tarefasEquipe.stream().filter(t -> t.getStatus() == StatusTarefa.PRONTO).count();
            long tarefasAtrasadas = tarefasEquipe.stream().filter(t -> t.getStatus() == StatusTarefa.ATRASADO).count();

            // Relatórios enviados pelos devs da equipe
            List<Relatorio> relatoriosEquipe = getRelatorios().stream()
                    .filter(r -> devIds.contains(r.getDevRemetente().getId()))
                    .collect(Collectors.toList());

            StringBuilder sb = new StringBuilder();
            sb.append("RELATÓRIO - ").append(new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())).append("\n");
            sb.append("========================================================\n\n");
            sb.append("RESUMO DA EQUIPE\n");
            sb.append("--------------------------------------------------------\n");
            sb.append("Tarefas cumpridas (PRONTO): ").append(tarefasCumpridas).append("\n");
            sb.append("Tarefas atrasadas: ").append(tarefasAtrasadas).append("\n");
            sb.append("Relatórios enviados pelos devs: ").append(relatoriosEquipe.size()).append("\n\n");
            sb.append("RELATÓRIOS DOS DESENVOLVEDORES\n");
            sb.append("========================================================\n");
            if (relatoriosEquipe.isEmpty()) {
                sb.append("Nenhum relatório enviado ainda.\n");
            } else {
                for (Relatorio r : relatoriosEquipe) {
                    sb.append("\nDesenvolvedor: ").append(r.getDevRemetente().getNome()).append("\n");
                    sb.append("Enviado em: ").append(new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(r.getDataEnvio())).append("\n");
                    if (r.getTarefaRelacionada() != null) {
                        sb.append("Tarefa: ").append(r.getTarefaRelacionada().getDescricao())
                                .append(" (ID ").append(r.getTarefaRelacionada().getId()).append(")\n");
                    } else if (r.getProjetoRelacionado() != null) {
                        sb.append("Projeto: ").append(r.getProjetoRelacionado().getNome())
                                .append(" (ID ").append(r.getProjetoRelacionado().getId()).append(")\n");
                    }
                    sb.append("Conteúdo: ").append(r.getConteudo()).append("\n");
                    sb.append("--------------------------------------------------------\n");
                }
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao gerar relatório: " + e.getMessage();
        }
    }

    // === Timer ===
    public void iniciarVerificadorPrazos(long intervaloMilissegundos) {
        if (verificadorTimer != null) verificadorTimer.cancel();
        verificadorTimer = new Timer(true);
        verificadorTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                verificarPrazosExpirados();
            }
        }, 0, intervaloMilissegundos);
    }

    public void pararVerificadorPrazos() {
        if (verificadorTimer != null) {
            verificadorTimer.cancel();
            verificadorTimer = null;
        }
    }
}