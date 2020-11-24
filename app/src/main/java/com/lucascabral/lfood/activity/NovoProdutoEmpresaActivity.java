package com.lucascabral.lfood.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.lucascabral.lfood.R;
import com.lucascabral.lfood.helper.ConfiguracaoFirebase;
import com.lucascabral.lfood.helper.UsuarioFirebase;
import com.lucascabral.lfood.model.Produto;

import java.io.ByteArrayOutputStream;

public class NovoProdutoEmpresaActivity extends AppCompatActivity {

    private EditText editProdutoNome, editProdutoDescricao, editProdutoPreco;
    private ImageView imagePerfilProduto;

    private static final int SELECAO_GALERIA = 200;
    private StorageReference storageReference;
    private String idUsuarioLogado;
    private String urlImagemSelecionada2 = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novo_produto_empresa);

        // Configurações iniciais
        inicializarComponentes();

        configurandoToolbar();

        imagePerfilProduto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                );
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, SELECAO_GALERIA);
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Bitmap imagem = null;

            try {

                switch (requestCode) {
                    case SELECAO_GALERIA:
                        Uri localImagem = data.getData();
                        imagem = MediaStore.Images
                                .Media
                                .getBitmap(
                                        getContentResolver(),
                                        localImagem
                                );
                        break;
                }

                if (imagem != null) {

                    imagePerfilProduto.setImageBitmap(imagem);
                    // Fazer upload da imagem
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    StorageReference imagemRef = storageReference
                            .child("imagens")
                            .child("produtos")
                            .child(idUsuarioLogado)
                            .child(imagem.toString());

                    UploadTask upload = imagemRef.putBytes(dadosImagem);

                    upload.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(NovoProdutoEmpresaActivity.this,
                                    "Erro ao fazer upload da imagem",
                                    Toast.LENGTH_LONG).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {

                                    Uri url = task.getResult();
                                    urlImagemSelecionada2 = url.toString();
                                }
                            });

                            Toast.makeText(NovoProdutoEmpresaActivity.this,
                                    "Sucesso ao fazer upload da imagem",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void validarDadosProduto(View view) {

        //Recupera dados dos campos preenchidos
        String nomeProduto = editProdutoNome.getText().toString();
        String descricaoProduto = editProdutoDescricao.getText().toString();
        String precoProduto = editProdutoPreco.getText().toString();

        if (!nomeProduto.isEmpty()) {

            if (!descricaoProduto.isEmpty()) {

                if (!precoProduto.isEmpty()) {

                    if (!urlImagemSelecionada2.isEmpty()) {

                        Produto produto = new Produto();
                        produto.setIdUsuario(idUsuarioLogado);
                        produto.setUrlImagem(urlImagemSelecionada2);
                        produto.setNome(nomeProduto);
                        produto.setDescricao(descricaoProduto);
                        produto.setPreco(Double.parseDouble(precoProduto));
                        produto.salvar();
                        finish();
                        exibirMensagem("Produto salvo com sucesso");

                    } else {
                        exibirMensagem("Escolha uma imagem para o produto!");
                    }

                } else {
                    exibirMensagem("Digite um preço para o produto!");
                }

            } else {
                exibirMensagem("Digite uma descrição para o produto!");
            }

        } else {
            exibirMensagem("Digite um nome para o produto!");
        }

    }

    private void exibirMensagem(String texto) {

        Toast.makeText(NovoProdutoEmpresaActivity.this,
                texto,
                Toast.LENGTH_LONG).show();
    }

    private void inicializarComponentes() {

        // Configuração Firebase
        storageReference = ConfiguracaoFirebase.getFirebaseStorage();
        idUsuarioLogado = UsuarioFirebase.getIdUsuario();

        // Encontrando os Componentes pelo id
        imagePerfilProduto = findViewById(R.id.novoProdutoImageView);
        editProdutoNome = findViewById(R.id.novoProdutoNomeEdit);
        editProdutoDescricao = findViewById(R.id.novoProdutoDescricaoEdit);
        editProdutoPreco = findViewById(R.id.novoProdutoPrecoEdit);

    }

    private void configurandoToolbar() {

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Novo produto");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}