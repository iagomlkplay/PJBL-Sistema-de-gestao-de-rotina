import java.sql.SQLException;
import java.util.Date;
import java.util.Calendar;

public class TesteProjetoTarefaDAO {
    public static void main(String[] args) throws SQLException {
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        ProjetoDAO projetoDAO = new ProjetoDAO();
        TarefaDAO tarefaDAO = new TarefaDAO();

        // 1. Criar um gestor e um dev (já existentes no banco? Vamos usar os do teste anterior)
        UsuarioGestor gestor = new UsuarioGestor("Gestor Teste", "123.456.789-00", "gestor@teste.com", "123", "TI");
        usuarioDAO.inserir(gestor);
        UsuarioDev dev = new UsuarioDev("Dev Teste", "987.654.321-00", "dev@teste.com", "123");
        dev.setGestorId(gestor.getId());
        usuarioDAO.inserir(dev);

        // 2. Criar um projeto
        Date prazo = addDias(30);
        Projeto projeto = new Projeto("Sistema de Teste", prazo, NivelImportancia.ALTA);
        projetoDAO.inserir(projeto);
        System.out.println("Projeto criado com ID: " + projeto.getId());

        // 3. Criar tarefas dentro do projeto
        Tarefa tarefa1 = new Tarefa("Implementar login", prazo, NivelImportancia.ALTA, dev, 10.0);
        tarefa1.setProjetoPai(projeto);
        tarefaDAO.inserir(tarefa1);
        System.out.println("Tarefa 1 criada com ID: " + tarefa1.getId());

        Tarefa tarefa2 = new Tarefa("Criar banco", prazo, NivelImportancia.URGENTE, dev, 8.0);
        tarefa2.setProjetoPai(projeto);
        tarefaDAO.inserir(tarefa2);
        System.out.println("Tarefa 2 criada com ID: " + tarefa2.getId());

        // 4. Listar tarefas do dev
        System.out.println("\n--- Tarefas do dev " + dev.getNome() + " ---");
        for (Tarefa t : tarefaDAO.listarPorDev(dev.getId(), usuarioDAO, projetoDAO)) {
            System.out.println(t.getInformacoesDetalhadas());
        }

        // 5. Atualizar horas e status
        tarefaDAO.adicionarHorasTrabalhadas(tarefa1.getId(), 6.0);
        tarefaDAO.atualizarStatus(tarefa1.getId(), StatusTarefa.FEITO);
        System.out.println("Status da tarefa 1 atualizado para FEITO");

        // 6. Reatribuir tarefa (exemplo: criar outro dev)
        UsuarioDev dev2 = new UsuarioDev("Dev2 Teste", "111.222.333-44", "dev2@teste.com", "123");
        dev2.setGestorId(gestor.getId());
        usuarioDAO.inserir(dev2);
        tarefaDAO.reatribuirDev(tarefa2.getId(), dev2.getId());
        System.out.println("Tarefa 2 reatribuída para " + dev2.getNome());
    }

    private static Date addDias(int dias) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, dias);
        return cal.getTime();
    }
}