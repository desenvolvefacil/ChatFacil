package br.edu.ifspsaocarlos.chatfacil.model;

import com.google.gson.annotations.SerializedName;

public class MensagemModel {
    //contantes para definir o tipo de msg
    public static final String TIPO_TEXTO = "X5EAS3D1=-$ASD654";
    public static final String TIPO_TEXTO_SPLIT = "X5EAS3D1=/$ASD654";
    public static final String TIPO_IMG ="X5EAS3D1=+$ASD654";
    public static final String TIPO_VIDEO ="X5EAS3D1=*$ASD654";


    private String id="";
    @SerializedName("origem_id")
    private String origemId="";
    @SerializedName("destino_id")
    private String destinoId="";
    private String assunto="";
    private String corpo="";
    private ContatoModel origem;
    private ContatoModel destino;
    private boolean lida=false;

    public boolean isLida() {
        return lida;
    }

    public void setLida(boolean lida) {
        this.lida = lida;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrigemId() {
        return origemId;
    }

    public void setOrigemId(String origemId) {
        this.origemId = origemId;
    }

    public String getDestinoId() {
        return destinoId;
    }

    public void setDestinoId(String destinoId) {
        this.destinoId = destinoId;
    }

    public String getAssunto() {
        return assunto;
    }

    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }

    public String getCorpo() {
        return corpo;
    }

    public void setCorpo(String corpo) {
        this.corpo = corpo;
    }

    public ContatoModel getOrigem() {
        return origem;
    }

    public void setOrigem(ContatoModel origem) {
        this.origem = origem;
        //seta o valor id da origem
        this.origemId = origem.getId();
    }

    public ContatoModel getDestino() {
        return destino;
    }

    public void setDestino(ContatoModel destino) {
        this.destino = destino;
        //seta o valor de id do destino
        this.destinoId=destino.getId();
    }


    //valor do fator pra criptografia
    public int getFator(){
        int idDest=0;
        int idOri=0;

        try {
            idDest = Integer.parseInt(destinoId);
            idOri = Integer.parseInt(origemId);
        }catch (NumberFormatException ne){

        }

        return (idDest + idOri);
    }
}
