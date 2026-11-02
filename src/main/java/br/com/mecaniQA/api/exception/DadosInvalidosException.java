package br.com.mecaniQA.api.exception;

public class DadosInvalidosException extends IllegalArgumentException {
    public DadosInvalidosException(String mensagem) {
        super(mensagem);
    }

    public DadosInvalidosException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
