package br.edu.ifspsaocarlos.chatfacil.model;

import com.google.gson.annotations.SerializedName;

public class ContatoModel {

    public static final int CADASTRA_CONTATO = 2000;
    public static final String UUID = "3269537f-d7b8-483e-9815-1544P9d880ib";
    public static final int  FATOR_CRIPTOGRAFIA = 0;

    private String id="0";
    @SerializedName("nome_completo")
    private String nome;
    private String apelido;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {

        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getApelido() {
        return apelido;
    }

    public void setApelido(String apelido) {
        this.apelido = apelido;
    }
}
