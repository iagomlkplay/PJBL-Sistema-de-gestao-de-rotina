import java.util.ArrayList;
import java.util.List;

public class UsuarioGestor extends Usuario {
    private String departamento;
    private List<UsuarioDev> equipe; // apenas referência em memória (opcional)

    // Construtor para novo cadastro (sem id)
    public UsuarioGestor(String nome, String cpf, String email, String senha, String departamento) {
        super(nome, cpf, email, senha);
        this.departamento = departamento;
        this.tipoUsuario = TipoUsuario.GESTOR;
        this.equipe = new ArrayList<>();
    }

    // Construtor para leitura do banco (com id)
    public UsuarioGestor(int id, String nome, String cpf, String email, String senha, String departamento) {
        super(id, nome, cpf, email, senha);
        this.departamento = departamento;
        this.tipoUsuario = TipoUsuario.GESTOR;
        this.equipe = new ArrayList<>();
    }

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }
    public List<UsuarioDev> getEquipe() { return equipe; }
    public void setEquipe(List<UsuarioDev> equipe) { this.equipe = equipe; }
}