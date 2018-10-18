package br.edu.ifspsaocarlos.chatfacil.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import br.edu.ifspsaocarlos.chatfacil.R;
import br.edu.ifspsaocarlos.chatfacil.data.MensagemData;
import br.edu.ifspsaocarlos.chatfacil.model.MensagemModel;
import br.edu.ifspsaocarlos.chatfacil.util.MensageiroUtil;


public class ListaMsgAdapter extends RecyclerView.Adapter<MsgViewHolder> {
    //links utilizados na elaboração do RecyclerView
    //https://medium.com/android-dev-br/listas-com-recyclerview-d3f41e0d653c
    //https://github.com/orafaaraujo/RxRecyclerExample
    //https://medium.com/android-dev-br/listas-com-recyclerview-d3f41e0d653c

    List<MensagemModel> lista;
    Context context;


    /**
     * Construtor do Adapter
     * @param context
     */
    public ListaMsgAdapter(Context context){
        this.context=context;

        ///////////////////////////
        /*lista = new ArrayList<>();

        for (int i=0;i<20;i++){
            MensagemModel m = new MensagemModel();
            if(lista.size()%2==0) {
                m.setId(MensageiroUtil.ContatoDestino.getId());
                m.setCorpo(String.valueOf(MensageiroUtil.ContatoDestino.getNome()+ lista.size()));
                m.setOrigemId(MensageiroUtil.ContatoDestino.getId());
            }else{
                m.setId(MensageiroUtil.ContatoLogado.getId());
                m.setCorpo(String.valueOf(MensageiroUtil.ContatoLogado.getNome()+ lista.size()));
                m.setOrigemId(MensageiroUtil.ContatoLogado.getId());
            }

            lista.add(m);
        }*/
        ///////////////////////////

        AtualizaListaMsg();
    }

    @NonNull
    @Override
    public MsgViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cel_chat, parent, false);
        return new MsgViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MsgViewHolder holder, int position) {
        MensagemModel m = lista.get(position);

        holder.msgTV.setText(m.getCorpo());

        //se foi o usuario logado
        if(m.getOrigemId().equals(MensageiroUtil.ContatoLogado.getId())){
            Drawable d = holder.msgTV.getContext().getResources().getDrawable(R.drawable.shape_round_1);


            //remove os parametros já setados
            //**por algum motivo estranho não estava funcionando corretamente fora do ID
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.msgTV.getLayoutParams();

            params.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.removeRule(RelativeLayout.ALIGN_PARENT_START);

            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.addRule(RelativeLayout.ALIGN_PARENT_END);
            params.rightMargin=20;


            holder.msgTV.setLayoutParams(params);


            holder.msgTV.setBackground(d);

        }else{
            Drawable d = holder.msgTV.getContext().getResources().getDrawable(R.drawable.shape_round_2);


            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.msgTV.getLayoutParams();


            params.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.removeRule(RelativeLayout.ALIGN_PARENT_END);

            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.addRule(RelativeLayout.ALIGN_PARENT_START);


            params.leftMargin=20;


            holder.msgTV.setLayoutParams(params);

            holder.msgTV.setBackground(d);
        }
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    /**
     * Buscar as msgs da conversa e notifica o adapter que houve alterações
     */
    public void AtualizaListaMsg(){
        lista = new MensagemData(context).ListarMensagens();

        ///////////////////////////
        /*MensagemModel m = new MensagemModel();
        if(lista.size()%2==0) {
            m.setOrigemId(MensageiroUtil.ContatoDestino.getId());
            m.setCorpo(String.valueOf(MensageiroUtil.ContatoDestino.getNome()+ lista.size()));
        }else{
            m.setOrigemId(MensageiroUtil.ContatoLogado.getId());
            m.setCorpo(String.valueOf(MensageiroUtil.ContatoLogado.getNome()+ lista.size()));
        }

        lista.add(m);*/
        ///////////////////

        notifyDataSetChanged();
    }
}


class MsgViewHolder extends RecyclerView.ViewHolder{

    final TextView msgTV;

    public MsgViewHolder(final View v) {
        super(v);

        msgTV = v.findViewById(R.id.cel_chat_msg);
    }
}