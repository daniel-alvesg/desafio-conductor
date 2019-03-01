package com.example.conductor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ExtratoAdapter extends ArrayAdapter<CartaoExtrato> {

    private final Context context;
    private final ArrayList<CartaoExtrato> extratos;

    public ExtratoAdapter(Context context, ArrayList<CartaoExtrato> extratos){
        super(context, R.layout.item, extratos);
        this.context = context;
        this.extratos = extratos;
    }

    // CRIANDO O ITEM DA LIST VIEW DA PAGINA INICIAL
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.item, parent, false);

        TextView data = rowView.findViewById(R.id.txt_data);
        TextView tipo = rowView.findViewById(R.id.txt_tipo);
        TextView valor = rowView.findViewById(R.id.txt_valor);

        data.setText(extratos.get(position).getData());
        tipo.setText(extratos.get(position).getTipo());
        valor.setText(extratos.get(position).getValor());

        return rowView;
    }

}
