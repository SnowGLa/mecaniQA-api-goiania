package br.com.mecaniQA.api.controller;

import br.com.mecaniQA.api.dto.ItemPedidoPecaRequest;
import br.com.mecaniQA.api.dto.PedidoPecasRequest;
import br.com.mecaniQA.api.dto.PedidoPecasResponse;
import br.com.mecaniQA.api.dto.StatusPedidoPecasRequest;
import br.com.mecaniQA.api.exception.ConflitoDadosException;
import br.com.mecaniQA.api.exception.DadosInvalidosException;
import br.com.mecaniQA.api.exception.EntidadeNaoEncontradaException;
import br.com.mecaniQA.api.exception.PersistenciaException;
import br.com.mecaniQA.api.mapper.PedidoPecasMapper;
import br.com.mecaniQA.api.service.PedidoPecasService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos-pecas")
public class PedidoPecasController {
    private final PedidoPecasService service;

    public PedidoPecasController(PedidoPecasService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PedidoPecasResponse> criar(@RequestBody PedidoPecasRequest dados) {
        if (dados == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(PedidoPecasMapper.toResponse(service.criar(dados)));
        } catch (DadosInvalidosException excecao) {
            return ResponseEntity.badRequest().build();
        } catch (EntidadeNaoEncontradaException excecao) {
            return ResponseEntity.notFound().build();
        } catch (ConflitoDadosException excecao) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (PersistenciaException excecao) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<PedidoPecasResponse>> listar() {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(service.listar().stream().map(PedidoPecasMapper::toResponse).toList());
        } catch (DadosInvalidosException excecao) {
            return ResponseEntity.badRequest().build();
        } catch (EntidadeNaoEncontradaException excecao) {
            return ResponseEntity.notFound().build();
        } catch (ConflitoDadosException excecao) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (PersistenciaException excecao) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<PedidoPecasResponse> buscarPorId(@PathVariable Long codigo) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(PedidoPecasMapper.toResponse(service.buscar(codigo)));
        } catch (DadosInvalidosException excecao) {
            return ResponseEntity.badRequest().build();
        } catch (EntidadeNaoEncontradaException excecao) {
            return ResponseEntity.notFound().build();
        } catch (ConflitoDadosException excecao) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (PersistenciaException excecao) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{codigo}/status")
    public ResponseEntity<PedidoPecasResponse> alterarStatus(
            @PathVariable Long codigo, @RequestBody StatusPedidoPecasRequest dados) {
        if (dados == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(PedidoPecasMapper.toResponse(service.alterarStatus(codigo, dados)));
        } catch (DadosInvalidosException excecao) {
            return ResponseEntity.badRequest().build();
        } catch (EntidadeNaoEncontradaException excecao) {
            return ResponseEntity.notFound().build();
        } catch (ConflitoDadosException excecao) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (PersistenciaException excecao) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{codigo}/itens")
    public ResponseEntity<PedidoPecasResponse> adicionarItem(
            @PathVariable Long codigo, @RequestBody ItemPedidoPecaRequest dados) {
        if (dados == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(PedidoPecasMapper.toResponse(service.adicionarItem(codigo, dados)));
        } catch (DadosInvalidosException excecao) {
            return ResponseEntity.badRequest().build();
        } catch (EntidadeNaoEncontradaException excecao) {
            return ResponseEntity.notFound().build();
        } catch (ConflitoDadosException excecao) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (PersistenciaException excecao) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> deletar(@PathVariable Long codigo) {
        try {
            service.excluir(codigo);
            return ResponseEntity.noContent().build();
        } catch (DadosInvalidosException excecao) {
            return ResponseEntity.badRequest().build();
        } catch (EntidadeNaoEncontradaException excecao) {
            return ResponseEntity.notFound().build();
        } catch (ConflitoDadosException excecao) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (PersistenciaException excecao) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
