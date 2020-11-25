package com.lucascabral.lfood.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.lucascabral.lfood.R;
import com.lucascabral.lfood.adapter.AdapterProduto;
import com.lucascabral.lfood.helper.ConfiguracaoFirebase;
import com.lucascabral.lfood.helper.UsuarioFirebase;
import com.lucascabral.lfood.listener.RecyclerItemClickListener;
import com.lucascabral.lfood.model.Empresa;
import com.lucascabral.lfood.model.ItemPedido;
import com.lucascabral.lfood.model.Pedido;
import com.lucascabral.lfood.model.Produto;
import com.lucascabral.lfood.model.Usuario;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class CardapioActivity extends AppCompatActivity {

    private RecyclerView recyclerProdutosCardapio;
    private ImageView imageEmpresaCardapio;
    private TextView textNomeEmpresaCardapio;
    private Empresa empresaSelecionada;
    private AlertDialog dialog;
    private Usuario usuario;
    private TextView textCarrinhoQtd, textCarrinhoTotal;

    private AdapterProduto adapterProduto;
    private List<Produto> produtos = new ArrayList<>();
    private List<ItemPedido> itensCarrinho = new ArrayList<>();
    private DatabaseReference firebaseRef;
    private String idEmpresa;
    private String idUsuarioLogado;
    private Pedido pedidoRecuperado;
    private int qtdItensCarrinho;
    private Double totalCarrinho;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cardapio);

        inicializarComponentes();

        configurandoToolbar();

        recuperaEmpresaSelecionada();

        configuraRecyclerView();
    }

    private void configuraRecyclerView() {

        recyclerProdutosCardapio.setLayoutManager(new LinearLayoutManager(this));
        recyclerProdutosCardapio.setHasFixedSize(true);
        adapterProduto = new AdapterProduto(produtos, this);
        recyclerProdutosCardapio.setAdapter(adapterProduto);

        // Criando evento de click
        recyclerProdutosCardapio.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        this,
                        recyclerProdutosCardapio,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                                confirmarPedido(position);
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                ));

        recuperarProdutos();

        recuperarDadosUsuario();
    }

    private void confirmarPedido(int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(CardapioActivity.this);
        builder.setTitle("Quantidade");
        builder.setMessage("Digite a quantidade!");

        EditText editQuantidade = new EditText(this);
        //editQuantidade.setInputType(1);
        editQuantidade.setText("1");

        builder.setView(editQuantidade);

        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //Recupera a quantidade
                String quantidade = editQuantidade.getText().toString();

                if (!quantidade.equals("0")) {

                    Produto produtoSelecionado = produtos.get(position);
                    ItemPedido itemPedido = new ItemPedido();
                    itemPedido.setIdProduto(produtoSelecionado.getIdProduto());
                    itemPedido.setNomeProduto(produtoSelecionado.getNome());
                    itemPedido.setPreco(produtoSelecionado.getPreco());
                    itemPedido.setQuantidade(Integer.parseInt(quantidade));

                    itensCarrinho.add(itemPedido);

                    if (pedidoRecuperado == null) {
                        pedidoRecuperado = new Pedido(idUsuarioLogado, idEmpresa);
                    }

                    pedidoRecuperado.setNomeCliente(usuario.getNome());
                    pedidoRecuperado.setEnderecoCliente(usuario.getEndereco());
                    pedidoRecuperado.setItens(itensCarrinho);
                    pedidoRecuperado.salvar();

                } else {

                    Toast.makeText(CardapioActivity.this,
                            "Digite uma quantidade!",
                            Toast.LENGTH_LONG).show();
                }


            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void recuperarDadosUsuario() {

        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Carregando dados")
                .setCancelable(false)
                .build();
        dialog.show();

        DatabaseReference usuariosRef = firebaseRef
                .child("usuarios")
                .child(idUsuarioLogado);
        usuariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.getValue() != null) {

                    usuario = snapshot.getValue(Usuario.class);
                }
                recuperarPedido();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void recuperarPedido() {

        DatabaseReference pedidoRef = firebaseRef
                .child("pedidos_usuario")
                .child(idEmpresa)
                .child(idUsuarioLogado);
        pedidoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                qtdItensCarrinho = 0;
                totalCarrinho = 0.0;
                itensCarrinho = new ArrayList<>();
                if (snapshot.getValue() != null) {

                    pedidoRecuperado = snapshot.getValue(Pedido.class);
                    itensCarrinho = pedidoRecuperado.getItens();

                    for (ItemPedido itemPedido : itensCarrinho) {

                        int qtde = itemPedido.getQuantidade();
                        Double preco = itemPedido.getPreco();

                        totalCarrinho += (qtde * preco);
                        qtdItensCarrinho += qtde;
                    }
                }

                DecimalFormat df = new DecimalFormat("0.00");

                textCarrinhoQtd.setText("qtd: " + String.valueOf(qtdItensCarrinho));
                textCarrinhoTotal.setText("R$ " + df.format(totalCarrinho));

                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void recuperarProdutos() {

        DatabaseReference produtosRef = firebaseRef
                .child("produtos")
                .child(idEmpresa);

        produtosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                produtos.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {

                    produtos.add(ds.getValue(Produto.class));
                }

                adapterProduto.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void recuperaEmpresaSelecionada() {

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

            empresaSelecionada = (Empresa) bundle.getSerializable("empresa");

            textNomeEmpresaCardapio.setText(empresaSelecionada.getNome());
            idEmpresa = empresaSelecionada.getIdUsuario();

            String url = empresaSelecionada.getUrlImagem();
            Picasso.with(getApplicationContext()).load(url).into(imageEmpresaCardapio);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_cardapio, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menuPedido:

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void inicializarComponentes() {

        //Componenetes
        recyclerProdutosCardapio = findViewById(R.id.recyclerProdutosCardapio);
        imageEmpresaCardapio = findViewById(R.id.cardapioEmpresaImage);
        textNomeEmpresaCardapio = findViewById(R.id.cardapioEmpresaNomeText);
        textCarrinhoQtd = findViewById(R.id.cardapioCarrinhoQtdText);
        textCarrinhoTotal = findViewById(R.id.cardapioCarrinhoTotalText);

        //Configuração firebase
        firebaseRef = ConfiguracaoFirebase.getFirebase();
        idUsuarioLogado = UsuarioFirebase.getIdUsuario();
    }

    private void configurandoToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Cardápio");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}