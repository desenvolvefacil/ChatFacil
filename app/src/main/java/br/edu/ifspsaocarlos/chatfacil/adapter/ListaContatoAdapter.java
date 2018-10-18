package br.edu.ifspsaocarlos.chatfacil.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import br.edu.ifspsaocarlos.chatfacil.data.MensagemData;
import br.edu.ifspsaocarlos.chatfacil.model.ContatoModel;
import br.edu.ifspsaocarlos.chatfacil.R;
import br.edu.ifspsaocarlos.chatfacil.model.MensagemModel;
import br.edu.ifspsaocarlos.chatfacil.util.MensageiroUtil;

public class ListaContatoAdapter extends ArrayAdapter<ContatoModel> {

    private LayoutInflater inflador;

    /**
     * Construtor da classe
     * @param tela tela a receber o Adapter
     * @param listaContatos contatos que serão mostrados
     */
    public ListaContatoAdapter(Activity tela, List<ContatoModel> listaContatos) {
        super(tela, R.layout.cel_contato, listaContatos);

        inflador = (LayoutInflater) tela.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    /**
     * infla uma linha dentro do adapter
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;


        if (convertView == null) {
            // infla uma nova célula
            convertView = inflador.inflate(R.layout.cel_contato, null);

            holder = new ViewHolder();
            holder.contatoTextView = convertView.findViewById(R.id.cel_contato_nome);
            holder.ultimaMsgTextView = convertView.findViewById(R.id.cel_contato_ultima_msg);

            convertView.setTag(holder);
        } else {
            //pega um celular já existente
            holder = (ViewHolder) convertView.getTag();
        }

        //pega o contato da linha corrente
        ContatoModel contatoModel = getItem(position);

        MensagemModel m = new MensagemData(getContext()).pegarUltimaMsg(contatoModel);

        //reduz o tamanho da msg a ser mostrada para ser visualizada em tela
        if (m.getCorpo().length() > 45) {
            m.setCorpo(m.getCorpo().substring(0, 42) + "...");
        }

        //mostra o nome do contato
        holder.contatoTextView.setText(contatoModel.getNome());
        //mostra a útlima mensagem da conversa com este contato
        holder.ultimaMsgTextView.setText(m.getCorpo());

        //se a mensagem ainda não foi lidar mostra em negrito e em azul
        if (!m.isLida() && !m.getOrigemId().equals(MensageiroUtil.ContatoLogado.getId())) {
            //holder.contatoTextView.setTypeface(null, Typeface.BOLD);
            holder.contatoTextView.setTextColor(Color.BLUE);
            holder.ultimaMsgTextView.setTypeface(null, Typeface.BOLD);
        }

        return convertView;
    }

    /**
     * View Holder que contém os campos da tela
     */
    static class ViewHolder {
        public TextView contatoTextView;
        public TextView ultimaMsgTextView;
    }
}
