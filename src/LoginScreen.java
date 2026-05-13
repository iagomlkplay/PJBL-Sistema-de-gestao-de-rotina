import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

public class LoginScreen extends JFrame {
    private JTextField txtEmail;
    private JPasswordField txtSenha;
    private JButton btnEntrar;
    private JButton btnCadastrar;
    private JLabel lblMensagem;

    public LoginScreen() {
        setTitle("Sistema de Gestão de Rotina - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 350);
        setLocationRelativeTo(null);
        setResizable(false);
        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        // Email
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("E-mail:"), gbc);
        gbc.gridx = 1;
        txtEmail = new JTextField(20);
        panel.add(txtEmail, gbc);

        // Senha
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Senha:"), gbc);
        gbc.gridx = 1;
        txtSenha = new JPasswordField(20);
        panel.add(txtSenha, gbc);

        // Botões
        JPanel botoesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnEntrar = new JButton("Entrar");
        btnCadastrar = new JButton("Cadastrar");
        botoesPanel.add(btnEntrar);
        botoesPanel.add(btnCadastrar);
        gbc.gridx = 1; gbc.gridy = 2;
        panel.add(botoesPanel, gbc);

        // Mensagem
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        lblMensagem = new JLabel(" ");
        lblMensagem.setForeground(Color.RED);
        panel.add(lblMensagem, gbc);

        add(panel, BorderLayout.CENTER);

        // Ações
        btnEntrar.addActionListener(e -> realizarLogin());
        btnCadastrar.addActionListener(e -> abrirCadastro());
        txtSenha.addActionListener(e -> realizarLogin());
    }

    private void realizarLogin() {
        String email = txtEmail.getText().trim();
        String senha = new String(txtSenha.getPassword());

        if (email.isEmpty() || senha.isEmpty()) {
            lblMensagem.setText("Preencha e-mail e senha.");
            return;
        }

        Usuario usuario = Sistema.getInstance().autenticar(email, senha);

        if (usuario == null) {
            lblMensagem.setText("E-mail ou senha inválidos.");
            return;
        }

        // Abre o dashboard apropriado
        if (usuario instanceof UsuarioDev) {
            new Dashboard((UsuarioDev) usuario).setVisible(true);
        } else if (usuario instanceof UsuarioGestor) {
            new Dashboard((UsuarioGestor) usuario).setVisible(true);
        } else {
            lblMensagem.setText("Tipo de usuário desconhecido.");
            return;
        }
        dispose(); // fecha login
    }

    private void abrirCadastro() {
        // Diálogo modal para cadastro
        JDialog dialog = new JDialog(this, "Novo Cadastro", true);
        dialog.setSize(400, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Campos comuns
        JTextField txtNome = new JTextField(15);
        JTextField txtCpf = new JTextField(15);
        JTextField txtEmail = new JTextField(15);
        JPasswordField txtSenha = new JPasswordField(15);
        JPasswordField txtConfirma = new JPasswordField(15);
        JComboBox<String> cbTipo = new JComboBox<>(new String[]{"DESENVOLVEDOR", "GESTOR"});

        // Campos específicos
        JTextField txtDepartamento = new JTextField(15);
        JComboBox<UsuarioGestor> cbGestor = new JComboBox<>();

        // Carregar gestores existentes
        for (UsuarioGestor g : Sistema.getInstance().getGestores()) {
            cbGestor.addItem(g);
        }

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row;
        form.add(new JLabel("Nome:"), gbc);
        gbc.gridx = 1;
        form.add(txtNome, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        form.add(new JLabel("CPF (000.000.000-00):"), gbc);
        gbc.gridx = 1;
        form.add(txtCpf, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        form.add(new JLabel("E-mail:"), gbc);
        gbc.gridx = 1;
        form.add(txtEmail, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        form.add(new JLabel("Senha:"), gbc);
        gbc.gridx = 1;
        form.add(txtSenha, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        form.add(new JLabel("Confirmar senha:"), gbc);
        gbc.gridx = 1;
        form.add(txtConfirma, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        form.add(new JLabel("Tipo:"), gbc);
        gbc.gridx = 1;
        form.add(cbTipo, gbc);

        // Campos condicionais (inicialmente invisíveis)
        JPanel condPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbcCond = new GridBagConstraints();
        gbcCond.insets = new Insets(5,5,5,5);
        gbcCond.gridx = 0; gbcCond.gridy = 0;
        condPanel.add(new JLabel("Departamento:"), gbcCond);
        gbcCond.gridx = 1;
        condPanel.add(txtDepartamento, gbcCond);
        gbcCond.gridx = 0; gbcCond.gridy = 1;
        condPanel.add(new JLabel("Gestor responsável:"), gbcCond);
        gbcCond.gridx = 1;
        condPanel.add(cbGestor, gbcCond);

        JPanel gestorPanel = new JPanel(new BorderLayout());
        gestorPanel.add(condPanel, BorderLayout.NORTH);
        gestorPanel.setVisible(false); // inicialmente visível apenas para GESTOR? Vamos controlar

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        form.add(gestorPanel, gbc);

        // Controle de visibilidade
        cbTipo.addActionListener(e -> {
            String tipo = (String) cbTipo.getSelectedItem();
            gestorPanel.setVisible("GESTOR".equals(tipo));
            dialog.pack();
        });

        // Botões
        JPanel botoes = new JPanel();
        JButton btnSalvar = new JButton("Salvar");
        JButton btnCancelar = new JButton("Cancelar");
        botoes.add(btnSalvar);
        botoes.add(btnCancelar);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(botoes, BorderLayout.SOUTH);

        btnCancelar.addActionListener(e -> dialog.dispose());

        btnSalvar.addActionListener(e -> {
            String nome = txtNome.getText().trim();
            String cpf = txtCpf.getText().trim();
            String email = txtEmail.getText().trim();
            String senha = new String(txtSenha.getPassword());
            String confirma = new String(txtConfirma.getPassword());
            String tipo = (String) cbTipo.getSelectedItem();

            if (nome.isEmpty() || cpf.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Preencha todos os campos.");
                return;
            }
            if (!senha.equals(confirma)) {
                JOptionPane.showMessageDialog(dialog, "As senhas não coincidem.");
                return;
            }

            boolean ok = false;
            if ("GESTOR".equals(tipo)) {
                String departamento = txtDepartamento.getText().trim();
                if (departamento.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Departamento obrigatório para gestor.");
                    return;
                }
                UsuarioGestor novo = new UsuarioGestor(nome, cpf, email, senha, departamento);
                ok = Sistema.getInstance().realizarCadastro(novo);
                if (ok) JOptionPane.showMessageDialog(dialog, "Gestor cadastrado com sucesso!");
            } else {
                // Desenvolvedor
                UsuarioGestor gestorSelecionado = (UsuarioGestor) cbGestor.getSelectedItem();
                if (gestorSelecionado == null) {
                    JOptionPane.showMessageDialog(dialog, "Selecione um gestor responsável.");
                    return;
                }
                UsuarioDev novo = new UsuarioDev(nome, cpf, email, senha);
                novo.setGestorId(gestorSelecionado.getId());
                ok = Sistema.getInstance().realizarCadastro(novo);
                if (ok) JOptionPane.showMessageDialog(dialog, "Desenvolvedor cadastrado com sucesso!");
            }

            if (ok) {
                dialog.dispose();
                // Opcional: limpar campos do login e focar no e-mail
                txtEmail.setText(email);
                txtSenha.setText("");
                lblMensagem.setText("Cadastro realizado! Faça login.");
            } else {
                JOptionPane.showMessageDialog(dialog, "Erro no cadastro. E-mail ou CPF já existentes.");
            }
        });

        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginScreen().setVisible(true));
    }
}