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
        if (instance == null) instance = new Sistema();
        return instance;
    }

    // === RF01: Cadastro e autenticação ===
    public boolean realizarCadastro(Usuario usuario) {
        try {
            // Verifica se e-mail já existe
            if (usuarioDAO.buscarPorEmail(usuario.getEmail()) != null) {
                return false;
            }
            // Verifica se CPF já existe
            if (usuarioDAO.buscarPorCpf(usuario.getCpf()) != null) {
                return false;
            }
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
        try {
            projetoDAO.inserir(p);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void adicionarTarefa(Tarefa t) {
        try {
            tarefaDAO.inserir(t);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void adicionarRelatorio(Relatorio r) {
        try {
            relatorioDAO.inserir(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void adicionarSolicitacao(SolicitacaoMudanca s) {
        try {
            solicitacaoDAO.inserir(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        try {
            return projetoDAO.buscarPorId(id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
    public List<Usuario> getUsuarios() {
        try {
            return usuarioDAO.listarTodos();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

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
        try {
            return projetoDAO.listarTodos();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Tarefa> getTarefas() {
        try {
            return tarefaDAO.listarTodas();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Relatorio> getRelatorios() {
        try {
            return relatorioDAO.listarTodos();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<SolicitacaoMudanca> getSolicitacoes() {
        try {
            return solicitacaoDAO.listarTodos();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // === Solicitações por gestor ===
    public List<SolicitacaoMudanca> getSolicitacoesPorGestor(int gestorId) {
        try {
            return solicitacaoDAO.listarPorGestor(gestorId);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
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
        try {
            // Retorna projetos que pertencem ao gestor (gestor_id = gestorId)
            return projetoDAO.listarPorGestor(gestorId);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // === Notificações ===
    public void notificarGestorMudancaStatus(Tarefa tarefa, UsuarioDev dev) {
        UsuarioGestor gestor = buscarGestorPorDev(dev);
        if (gestor != null) {
            System.out.println(">>> NOTIFICAÇÃO para gestor " + gestor.getNome() +
                    ": O dev " + dev.getNome() + " alterou a tarefa " + tarefa.getId() +
                    " para " + tarefa.getStatus());
        }
        verificarItensFeitoEAtrasados(gestor);
    }

    private void verificarItensFeitoEAtrasados(UsuarioGestor gestor) {
        if (gestor == null) return;
        try {
            List<Tarefa> tarefasEquipe = getTarefasDaEquipe(gestor.getId());
            long feitos = tarefasEquipe.stream().filter(t -> t.getStatus() == StatusTarefa.FEITO).count();
            if (feitos > 0) {
                System.out.println(">>> NOTIFICAÇÃO: Existem " + feitos + " tarefas com status FEITO na sua equipe.");
            }
            long atrasados = tarefasEquipe.stream().filter(t -> t.getStatus() == StatusTarefa.ATRASADO).count();
            if (atrasados > 0) {
                System.out.println(">>> ALERTA: Existem " + atrasados + " tarefas com status ATRASADO na sua equipe.");
            }
        } catch (Exception e) {
            e.printStackTrace();
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
                        System.out.println(">>> ALERTA: Tarefa atrasada notificada ao gestor " + gestor.getNome());
                        verificarItensFeitoEAtrasados(gestor);
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
    public void gerarRelatorio() {
        Date hoje = new Date();
        try {
            List<Tarefa> todasTarefas = getTarefas();
            long tarefasCumpridas = todasTarefas.stream().filter(t -> t.getStatus() == StatusTarefa.PRONTO).count();
            long tarefasAtrasadas = todasTarefas.stream().filter(t -> t.getStatus() == StatusTarefa.ATRASADO).count();
            long relatoriosEnviados = getRelatorios().size();

            StringBuilder conteudo = new StringBuilder();
            conteudo.append("Relatório - ").append(hoje).append("\n");
            conteudo.append("Tarefas cumpridas (PRONTO): ").append(tarefasCumpridas).append("\n");
            conteudo.append("Tarefas atrasadas: ").append(tarefasAtrasadas).append("\n");
            conteudo.append("Relatórios enviados pelos devs: ").append(relatoriosEnviados).append("\n");
            conteudo.append("Detalhes dos relatórios dos devs:\n");
            for (Relatorio r : getRelatorios()) {
                conteudo.append("- ").append(r.getConteudo()).append("\n");
            }

            Relatorio relatorio = new Relatorio(conteudo.toString());
            relatorio.setDataEnvio(hoje);
            relatorioDAO.inserir(relatorio);
            System.out.println(conteudo.toString());

            for (UsuarioGestor g : getGestores()) {
                verificarItensFeitoEAtrasados(g);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Timer
    public void iniciarVerificadorPrazos(long intervaloMilissegundos) {
        if (verificadorTimer != null) {
            verificadorTimer.cancel();
        }
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