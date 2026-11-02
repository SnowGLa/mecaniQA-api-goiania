package br.com.mecaniQA.api.exception;

public class ConflitoDadosException extends RuntimeException {
    public ConflitoDadosException(String mensagem) {
        super(mensagem);
    }

    public ConflitoDadosException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
