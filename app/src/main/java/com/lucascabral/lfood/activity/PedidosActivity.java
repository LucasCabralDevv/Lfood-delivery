package com.lucascabral.lfood.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lucascabral.lfood.R;
import com.lucascabral.lfood.adapter.AdapterPedido;
import com.lucascabral.lfood.helper.ConfiguracaoFirebase;
import com.lucascabral.lfood.helper.UsuarioFirebase;
import com.lucascabral.lfood.listener.RecyclerItemClickListener;
import com.lucascabral.lfood.model.Pedido;

import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class PedidosActivity extends AppCompatActivity {

    private RecyclerView recyclerPedidos;
    private AdapterPedido adapterPedido;
    private final List<Pedido> pedidos = new ArrayList<>();
    private AlertDialog dialog;
    private DatabaseReference firebaseRef;
    private String idEmpresa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedidos);

        inicializarComponentes();

        configurandoToolbar();

        configuraRecyclerView();

        recuperarPedidos();
    }

    private void recuperarPedidos() {

        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Esperando pedidos")
                .setCancelable(true)
                .build();
        dialog.show();

        DatabaseReference pedidoRef = firebaseRef
                .child("pedidos")
                .child(idEmpresa);
        Query pedidoPesquisa = pedidoRef.orderByChild("status")
                .equalTo("confirmado");

        pedidoPesquisa.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                pedidos.clear();
                if (snapshot.getValue() != null) {

                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Pedido pedido = ds.getValue(Pedido.class);
                        pedidos.add(pedido);
                    }

                    adapterPedido.notifyDataSetChanged();
                    dialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void configuraRecyclerView() {

        recyclerPedidos.setLayoutManager(new LinearLayoutManager(this));
        recyclerPedidos.setHasFixedSize(true);
        adapterPedido = new AdapterPedido(pedidos);
        recyclerPedidos.setAdapter(adapterPedido);

        //Criando evento de click para o recyclerView
        recyclerPedidos.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        this,
                        recyclerPedidos,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                                aceitarOuRecusarPedido(position);
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                                apagarPedido(position);
                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )
        );
    }

    private void apagarPedido(int position) {

        Pedido pedidoSelecionado = pedidos.get(position);
        androidx.appcompat.app.AlertDialog.Builder alertDialog = new androidx.appcompat.app.AlertDialog.Builder(PedidosActivity.this);

        alertDialog.setTitle("Apagar pedido");
        alertDialog.setMessage("Deseja realmente apagar o pedido? " +
                "Após apagado o pedido será cancelado.");
        alertDialog.setCancelable(false);

        alertDialog.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                pedidoSelecionado.removerConfirmado();
                adapterPedido.notifyItemRemoved(position);
                Toast.makeText(PedidosActivity.this,
                        "Pedido cancelado com sucesso",
                        Toast.LENGTH_SHORT).show();

            }
        });

        alertDialog.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        androidx.appcompat.app.AlertDialog alert = alertDialog.create();
        alert.show();
    }

    private void aceitarOuRecusarPedido(int position) {

        Pedido pedidoSelecionado = pedidos.get(position);
        androidx.appcompat.app.AlertDialog.Builder alertDialog = new androidx.appcompat.app.AlertDialog.Builder(PedidosActivity.this);

        alertDialog.setTitle("Aceitar o pedido");
        alertDialog.setMessage("Deseja realmente aceitar o pedido?");
        alertDialog.setCancelable(false);

        alertDialog.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                pedidoSelecionado.setStatus("Finalizado");
                pedidoSelecionado.atualizarStatus();
                adapterPedido.notifyDataSetChanged();
                Toast.makeText(PedidosActivity.this,
                        "Pedido aceito com sucesso",
                        Toast.LENGTH_SHORT).show();

            }
        });

        alertDialog.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Toast.makeText(PedidosActivity.this,
                        "Pedido não foi aceito. Click pressionado para cancelá-lo.",
                        Toast.LENGTH_LONG).show();
            }
        });

        androidx.appcompat.app.AlertDialog alert = alertDialog.create();
        alert.show();
    }

    private void inicializarComponentes() {

        //Configura firebase
        firebaseRef = ConfiguracaoFirebase.getFirebase();
        idEmpresa = UsuarioFirebase.getIdUsuario();

        //Componentes
        recyclerPedidos = findViewById(R.id.recyclerPedidos);
    }

    private void configurandoToolbar() {

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Pedidos");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}