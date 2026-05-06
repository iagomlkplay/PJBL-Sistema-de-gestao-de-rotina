import java.util.*;
import java.util.stream.Collectors;

public class Sistema {
    private static Sistema instance;
    private List<Usuario> usuarios;
    private List<UsuarioDev> devs;
    private List<UsuarioGestor> gestores;
    private List<Relatorio> relatorios;
    private List<SolicitacaoMudanca> solicitacoes;
    private List<Projeto> projetos;
    private List<Tarefa> tarefas;

    private Sistema() {
        usuarios = new ArrayList<>();
        devs = new ArrayList<>();
        gestores = new ArrayList<>();
        relatorios = new ArrayList<>();
        solicitacoes = new ArrayList<>();
        projetos = new ArrayList<>();
        tarefas = new ArrayList<>();
    }

    public static Sistema getInstance() {
        if (instance == null) instance = new Sistema();
        return instance;
    }

    // RF01
    public boolean realizarCadastro(Usuario usuario) {
        if (autenticar(usuario.getLogin(), usuario.getSenha()) == null) {
            usuarios.add(usuario);
            if (usuario instanceof UsuarioDev) devs.add((UsuarioDev) usuario);
            else if (usuario instanceof UsuarioGestor) gestores.add((UsuarioGestor) usuario);
            return true;
        }
        return false;
    }

    public Usuario autenticar(String login, String senha) {
        return usuarios.stream()
                .filter(u -> u.getLogin().equals(login) && u.getSenha().equals(senha))
                .findFirst()
                .orElse(null);
    }

    // RF14 - Gerar relatório diário automático
    public void gerarRelatorioDiario() {
        Date hoje = new Date();
        long tarefasCumpridas = tarefas.stream().filter(t -> t.getStatus() == StatusTarefa.PRONTO).count();
        long tarefasAtrasadas = tarefas.stream().filter(t -> t.getStatus() == StatusTarefa.ATRASADO).count();
        String relatorioConteudo = String.format(
                "Relatório Diário - %s\nTarefas cumpridas: %d\nTarefas atrasadas: %d\nRelatórios enviados por devs: %d",
                hoje, tarefasCumpridas, tarefasAtrasadas, relatorios.size()
        );
        Relatorio relatorio = new Relatorio(relatorios.size() + 1, hoje, relatorioConteudo);
        relatorios.add(relatorio);
        System.out.println("Relatório diário gerado: " + relatorioConteudo);
        // RF16: notificar gestor sobre tarefas FEITO
        notificarGestorTarefasFeito();
        // RF17: alertar sobre atrasadas
        alertarGestorAtrasadas();
    }

    // RF15 - Automático: alterar status de PENDENTE para ATRASADO ao expirar prazo
    public void verificarPrazosExpirados() {
        Date agora = new Date();
        for (Tarefa t : tarefas) {
            if (t.getStatus() == StatusTarefa.PENDENTE && t.getPrazo().before(agora)) {
                t.setStatus(StatusTarefa.ATRASADO);
                System.out.println("Tarefa " + t.getId() + " expirou e foi marcada como ATRASADA.");
            }
        }
        for (Projeto p : projetos) {
            if (p.getStatus() == StatusTarefa.PENDENTE && p.getPrazo().before(agora)) {
                p.setStatus(StatusTarefa.ATRASADO);
                System.out.println("Projeto " + p.getId() + " expirou e foi marcado como ATRASADO.");
            }
        }
    }

    // RF13 e RF16
    private void notificarGestorTarefasFeito() {
        List<Tarefa> feitas = tarefas.stream()
                .filter(t -> t.getStatus() == StatusTarefa.FEITO)
                .collect(Collectors.toList());
        if (!feitas.isEmpty() && !gestores.isEmpty()) {
            UsuarioGestor gestor = gestores.get(0); // notifica primeiro gestor
            System.out.println("NOTIFICAÇÃO para gestor " + gestor.getNome() + ": Existem tarefas com status FEITO: " + feitas);
        }
    }

    private void alertarGestorAtrasadas() {
        List<Tarefa> atrasadas = tarefas.stream()
                .filter(t -> t.getStatus() == StatusTarefa.ATRASADO)
                .collect(Collectors.toList());
        if (!atrasadas.isEmpty() && !gestores.isEmpty()) {
            UsuarioGestor gestor = gestores.get(0);
            System.out.println("ALERTA para gestor " + gestor.getNome() + ": Existem tarefas ATRASADAS: " + atrasadas);
        }
    }

    // Métodos auxiliares para persistência futura
    public List<Usuario> getUsuarios() { return usuarios; }
    public List<UsuarioDev> getDevs() { return devs; }
    public List<UsuarioGestor> getGestores() { return gestores; }
    public List<Relatorio> getRelatorios() { return relatorios; }
    public List<SolicitacaoMudanca> getSolicitacoes() { return solicitacoes; }
    public List<Projeto> getProjetos() { return projetos; }
    public List<Tarefa> getTarefas() { return tarefas; }
}