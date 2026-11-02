package br.com.mecaniQA.api.service;

import br.com.mecaniQA.api.exception.DadosInvalidosException;

public final class ServiceRules {
    private ServiceRules() {}

    public static void exigir(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new DadosInvalidosException(mensagem);
        }
    }

    public static void texto(String valor, int limite) {
        exigir(valor == null || valor.length() <= limite, "Texto excede o tamanho permitido.");
    }

    public static String textoObrigatorio(String valor, int limite) {
        exigir(valor != null && !valor.isBlank(), "Informe o texto obrigatorio.");
        String normalizado = valor.trim();
        texto(normalizado, limite);
        return normalizado;
    }

    public static void moeda(Double valor) {
        exigir(
                valor == null || (Double.isFinite(valor) && valor >= 0 && valor <= 999999999999.99),
                "Valor monetario invalido.");
    }

    public static <E extends Enum<E>> E proximo(E atual, String nome, Class<E> tipo) {
        if (nome == null) {
            throw new DadosInvalidosException("Informe o status.");
        }
        E novo;
        try {
            novo = Enum.valueOf(tipo, nome);
        } catch (IllegalArgumentException excecao) {
            throw new DadosInvalidosException("Status invalido.", excecao);
        }
        exigir(novo.ordinal() == atual.ordinal() + 1, "O status deve seguir a proxima etapa.");
        return novo;
    }
}
