package com.lucascabral.lfood.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

import com.lucascabral.lfood.R;
import com.lucascabral.lfood.helper.ConfiguracaoFirebase;
import com.lucascabral.lfood.helper.UsuarioFirebase;

public class AutenticacaoActivity extends AppCompatActivity {

    private Button buttonAcesso;
    private SwitchMaterial tipoAcesso, tipoUsuario;
    private EditText campoEmail, campoSenha;
    private LinearLayout linearTipoUsuario;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autenticacao);


        inicializarComponentes();

        //Deslogar usuário para testes no desenvolvimento
        //auth.signOut();

        verificarUsuarioLogado();

        tipoAcesso.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {// Tipo Login
                    linearTipoUsuario.setVisibility(View.VISIBLE);

                } else {           // Tipo Cadastro - Usuário ou Empresa
                    linearTipoUsuario.setVisibility(View.GONE);
                }
            }
        });

        buttonAcesso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                autenticarLoginOuCadastro();
            }
        });
    }

    private void autenticarLoginOuCadastro() {

        String email = campoEmail.getText().toString();
        String senha = campoSenha.getText().toString();

        if (!email.isEmpty()) {
            if (!senha.isEmpty()) {

                // Verificar o estado do switch
                if (tipoAcesso.isChecked()) {  // Cadastro

                    auth.createUserWithEmailAndPassword(email, senha)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (task.isSuccessful()) {

                                        String tipoUsuario = getTipoUsuario();

                                        UsuarioFirebase.atualizarTipoUsuario(tipoUsuario);

                                        Toast.makeText(AutenticacaoActivity.this,
                                                "Cadastro realizado com sucesso",
                                                Toast.LENGTH_SHORT).show();
                                        abrirTelaPrincipal(tipoUsuario);

                                    } else {

                                        // Criando tratamentos de exceções
                                        String excecao = "";
                                        try {
                                            throw task.getException();
                                        } catch (FirebaseAuthWeakPasswordException e) {
                                            excecao = "Por favor, digite uma senha mais forte!";
                                        } catch (FirebaseAuthInvalidCredentialsException e) {
                                            excecao = "Por favor, digite um e-mail válido!";
                                        } catch (FirebaseAuthUserCollisionException e) {
                                            excecao = "Esta conta já foi cadastrada";
                                        } catch (Exception e) {
                                            excecao = "Erro ao cadastrar usuário: " + e.getMessage();
                                            e.printStackTrace();
                                        }
                                        Toast.makeText(AutenticacaoActivity.this,
                                                excecao,
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                } else {  // Login

                    auth.signInWithEmailAndPassword(
                            email, senha
                    ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {

                                Toast.makeText(AutenticacaoActivity.this,
                                        "Bem-vindo ao Lfood",
                                        Toast.LENGTH_LONG).show();
                                String tipoUsuario = task.getResult().getUser().getDisplayName();
                                abrirTelaPrincipal(tipoUsuario);
                            } else {
                                Toast.makeText(AutenticacaoActivity.this,
                                        "Erro ao fazer login: " + task.getException(),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

            } else {
                Toast.makeText(AutenticacaoActivity.this,
                        "Preencha a senha!",
                        Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(AutenticacaoActivity.this,
                    "Preencha o e-mail!",
                    Toast.LENGTH_LONG).show();
        }
    }

    private String getTipoUsuario() {


        return tipoUsuario.isChecked() ? "E" : "U";
    }

    private void verificarUsuarioLogado() {

        FirebaseUser usuarioAtual = auth.getCurrentUser();
        if (usuarioAtual != null) {
            String tipoUsuario = usuarioAtual.getDisplayName();
            abrirTelaPrincipal(tipoUsuario);
        }
    }

    private void abrirTelaPrincipal(String tipoUsuario) {

        if (tipoUsuario.equals("E")) { // Empresa
            startActivity(new Intent(getApplicationContext(), EmpresaActivity.class));
        } else { // Usuario
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
        }
    }

    private void inicializarComponentes() {
        auth = ConfiguracaoFirebase.getFirebaseAutenticacao();
        buttonAcesso = findViewById(R.id.buttonAcesso);
        tipoAcesso = findViewById(R.id.switchTipoAcesso);
        tipoUsuario = findViewById(R.id.switchTipoUsuario);
        campoEmail = findViewById(R.id.authEmailEdit);
        campoSenha = findViewById(R.id.authSenhaEdit);
        linearTipoUsuario = findViewById(R.id.linearTipoUsuario);
    }
}