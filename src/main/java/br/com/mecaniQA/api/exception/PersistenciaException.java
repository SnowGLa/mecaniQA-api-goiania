package br.com.mecaniQA.api.exception;

public class PersistenciaException extends RuntimeException {
    public PersistenciaException(String mensagem) {
        super(mensagem);
    }

    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
