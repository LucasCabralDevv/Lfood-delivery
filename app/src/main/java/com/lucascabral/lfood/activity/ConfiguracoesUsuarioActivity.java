package com.lucascabral.lfood.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.lucascabral.lfood.R;
import com.lucascabral.lfood.helper.ConfiguracaoFirebase;
import com.lucascabral.lfood.helper.UsuarioFirebase;
import com.lucascabral.lfood.model.Empresa;
import com.lucascabral.lfood.model.Usuario;
import com.squareup.picasso.Picasso;

public class ConfiguracoesUsuarioActivity extends AppCompatActivity {

    private EditText editUsuarioNome, editUsuarioEndereco;
    private String idUsuario;
    private DatabaseReference firebaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes_usuario);

        inicializarComponentes();

        configurandoToolbar();

        recuperarDadosUsuario();
    }

    public void validarDadosUsuario(View view) {

        // Recuperando dados dos campos preenchidos
        String nome = editUsuarioNome.getText().toString();
        String endereco = editUsuarioEndereco.getText().toString();

        if (!nome.isEmpty()) {
            if (!endereco.isEmpty()) {

                Usuario usuario = new Usuario();
                usuario.setIdUsuario(idUsuario);
                usuario.setNome(nome);
                usuario.setEndereco(endereco);
                usuario.salvar();

                exibirMensagem("Dados atualizados com sucesso");
                finish();
            } else {
                exibirMensagem("Digite seu endereço completo!");
            }
        } else {
            exibirMensagem("Digite seu nome!");
        }
    }

    private void recuperarDadosUsuario(){

        DatabaseReference usuarioRef = firebaseRef
                .child("usuarios")
                .child(idUsuario);
        usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if ( snapshot.getValue() != null ){

                    Usuario usuario = snapshot.getValue(Usuario.class);
                    editUsuarioNome.setText(usuario.getNome());
                    editUsuarioEndereco.setText(usuario.getEndereco());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void exibirMensagem(String texto) {

        Toast.makeText(ConfiguracoesUsuarioActivity.this,
                texto,
                Toast.LENGTH_LONG).show();
    }

    private void inicializarComponentes() {

        //componentes
        editUsuarioNome = findViewById(R.id.configUsuNomeEdit);
        editUsuarioEndereco = findViewById(R.id.configUsuEnderecoEdit);
        //config firebase
        idUsuario = UsuarioFirebase.getIdUsuario();
        firebaseRef = ConfiguracaoFirebase.getFirebase();
    }

    private void configurandoToolbar() {

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Configurações usuário");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}