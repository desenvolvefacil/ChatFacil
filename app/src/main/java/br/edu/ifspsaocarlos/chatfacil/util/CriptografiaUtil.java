package br.edu.ifspsaocarlos.chatfacil.util;

import android.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class CriptografiaUtil {

    private static String calculaChave(int fator) {

        //fator 0 = Contato
        if(fator==0){
            fator=-9998586;
        }

        String chave = "";

        if(fator%2==0){
            chave = "%ZS853%4a3D%Op35".replaceAll("%",Integer.toString(fator));
        }else {
            chave = "P9s8%w%Qx9%3TpX%".replaceAll("%",Integer.toString(fator));
        }

        return chave.substring(0,16);
    }

    /**
     * Codifica uma String
     * @param texto
     * @param fator valor número para gerar a chave de criptografia
     * @return String codificada
     */
    public static String decodificar(String texto, int fator) {

        try {
            Cipher cipher = Cipher.getInstance("AES");

            String chave = calculaChave(fator);
            //chave = "MQ==";

            SecretKey secretKey = new SecretKeySpec(chave.getBytes(), "AES");

            byte[] encryptedTextByte = Base64.decode(texto,Base64.DEFAULT);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedByte = cipher.doFinal(encryptedTextByte);
            String decryptedText = new String(decryptedByte);
            return decryptedText;

        } catch (Exception e) {

            return null;
        }
    }

    /***
     * Decodifica uma String
     * @param texto
     * @param fator  valor número para gerar a chave de criptografia
     * @return texto decodificado
     */
    public static String codificar(String texto, int fator) {
        try {

            Cipher cipher = Cipher.getInstance("AES");

            String chave  = calculaChave(fator);
            //chave = "MQ==";

            SecretKey secretKey = new SecretKeySpec(chave.getBytes(), "AES");
            byte[] plainTextByte = texto.getBytes();
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedByte = cipher.doFinal(plainTextByte);
            //Base64.Encoder encoder = Base64.getEncoder();
            String encryptedText = Base64.encodeToString(encryptedByte,Base64.DEFAULT);
            return encryptedText;
        } catch (Exception e) {

            return null;
        }
    }
}