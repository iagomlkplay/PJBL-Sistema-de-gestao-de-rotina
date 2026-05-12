public class TesteUsuarioDAO {
    public static void main(String[] args) {
        try {
            UsuarioDAO dao = new UsuarioDAO();

            // 1. Criar um gestor
            UsuarioGestor gestor = new UsuarioGestor("Carlos Gestor", "111.111.111-11", "carlos@email.com", "123456", "TI");
            dao.inserir(gestor);
            System.out.println("Gestor cadastrado com ID: " + gestor.getId());

            // 2. Criar um desenvolvedor associado a esse gestor
            UsuarioDev dev = new UsuarioDev("Ana Silva", "222.222.222-22", "ana@email.com", "123456");
            dev.setGestorId(gestor.getId());
            dao.inserir(dev);
            System.out.println("Dev cadastrado com ID: " + dev.getId());

            // 3. Autenticar
            Usuario autenticado = dao.autenticar("ana@email.com", "123456");
            System.out.println("Autenticado: " + (autenticado != null ? autenticado.getNome() : "falha"));

            // 4. Listar devs do gestor
            System.out.println("Desenvolvedores do gestor " + gestor.getNome() + ":");
            for (UsuarioDev d : dao.listarDevsPorGestor(gestor.getId())) {
                System.out.println(" - " + d.getNome());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}