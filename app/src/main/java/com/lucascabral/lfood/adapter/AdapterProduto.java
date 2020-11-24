package com.lucascabral.lfood.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lucascabral.lfood.R;
import com.lucascabral.lfood.model.Produto;
import com.squareup.picasso.Picasso;

import java.util.List;


public class AdapterProduto extends RecyclerView.Adapter<AdapterProduto.MyViewHolder>{

    private List<Produto> produtos;
    private Context context;

    public AdapterProduto(List<Produto> produtos, Context context) {
        this.produtos = produtos;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_produto, parent, false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Produto produto = produtos.get(position);
        holder.nome.setText(produto.getNome());
        holder.descricao.setText(produto.getDescricao());
        holder.valor.setText("R$ " + produto.getPreco());

        Picasso.with(holder.itemView.getContext())
                .load(produto.getUrlImagem()).into(holder.imagem);

    }

    @Override
    public int getItemCount() {
        return produtos.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView nome;
        TextView descricao;
        TextView valor;
        ImageView imagem;

        public MyViewHolder(View itemView) {
            super(itemView);

            nome = itemView.findViewById(R.id.adapterNomeProduto);
            descricao = itemView.findViewById(R.id.adapterDescricaoProduto);
            valor = itemView.findViewById(R.id.adapterPrecoProduto);
            imagem = itemView.findViewById(R.id.adapterImagemProduto);
        }
    }
}
