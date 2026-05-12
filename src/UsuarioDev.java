import java.util.ArrayList;
import java.util.List;

public class UsuarioDev extends Usuario {
    private List<String> especialidades;
    private List<Tarefa> tarefas;        // apenas para uso em memória (opcional)
    private int gestorId;                //

    // Construtor para novo cadastro (sem id)
    public UsuarioDev(String nome, String cpf, String email, String senha) {
        super(nome, cpf, email, senha);
        this.tipoUsuario = TipoUsuario.DESENVOLVEDOR;
        this.especialidades = new ArrayList<>();
        this.tarefas = new ArrayList<>();
    }

    // Construtor para leitura do banco (com id e gestorId)
    public UsuarioDev(int id, String nome, String cpf, String email, String senha, int gestorId) {
        super(id, nome, cpf, email, senha);
        this.tipoUsuario = TipoUsuario.DESENVOLVEDOR;
        this.gestorId = gestorId;
        this.especialidades = new ArrayList<>();
        this.tarefas = new ArrayList<>();
    }

    // Getters e setters
    public int getGestorId() { return gestorId; }
    public void setGestorId(int gestorId) { this.gestorId = gestorId; }
    public List<String> getEspecialidades() { return especialidades; }
    public void setEspecialidades(List<String> especialidades) { this.especialidades = especialidades; }
    public List<Tarefa> getTarefas() { return tarefas; }
    public void setTarefas(List<Tarefa> tarefas) { this.tarefas = tarefas; }
}