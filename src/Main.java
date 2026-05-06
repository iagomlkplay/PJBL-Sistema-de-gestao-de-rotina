import java.util.Date;

public class Main {
    public static void main(String[] args) {
        Sistema sistema = Sistema.getInstance();

        // Cadastro de usuários (RF01)
        UsuarioDev dev1 = new UsuarioDev(1, "joao", "123", "João", "joao@email.com");
        UsuarioGestor gestor1 = new UsuarioGestor(2, "maria", "456", "Maria", "maria@email.com", "TI");
        sistema.realizarCadastro(dev1);
        sistema.realizarCadastro(gestor1);
        gestor1.getEquipe().add(dev1);

        // Gestor cria e atribui tarefa (RF09)
        Date prazo = new Date(System.currentTimeMillis() + 86400000); // amanhã
        gestor1.criarAtribuirTarefa("Implementar login", prazo, NivelImportancia.ALTA, dev1.getId());

        // Dev altera status para FEITO (RF05) - precisa ter a tarefa na lista dele
        Tarefa tarefa = gestor1.getTodasTarefas().get(0);
        dev1.alterarStatusTarefa(tarefa, StatusTarefa.FEITO);

        // Gestor valida finalização (RF11)
        gestor1.validarFinalizacao(tarefa);

        // Sistema gera relatório diário (RF14)
        sistema.gerarRelatorioDiario();

        // Verificar prazos expirados (RF15)
        sistema.verificarPrazosExpirados();
    }
}