package br.com.mecaniQA.api.controller;

import br.com.mecaniQA.api.dto.ServicoRequest;
import br.com.mecaniQA.api.dto.ServicoResponse;
import br.com.mecaniQA.api.exception.ConflitoDadosException;
import br.com.mecaniQA.api.exception.DadosInvalidosException;
import br.com.mecaniQA.api.exception.EntidadeNaoEncontradaException;
import br.com.mecaniQA.api.exception.PersistenciaException;
import br.com.mecaniQA.api.mapper.ServicoMapper;
import br.com.mecaniQA.api.service.ServicoService;

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
@RequestMapping("/api/servicos")
public class ServicoController {
    private final ServicoService service;

    public ServicoController(ServicoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ServicoResponse> criar(@RequestBody ServicoRequest dados) {
        if (dados == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ServicoMapper.toResponse(service.criar(dados)));
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
    public ResponseEntity<List<ServicoResponse>> listar() {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(service.listar().stream().map(ServicoMapper::toResponse).toList());
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

    @GetMapping("/{id}")
    public ResponseEntity<ServicoResponse> buscarPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ServicoMapper.toResponse(service.buscar(id)));
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

    @PutMapping("/{id}")
    public ResponseEntity<ServicoResponse> atualizar(
            @PathVariable Long id, @RequestBody ServicoRequest dados) {
        if (dados == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ServicoMapper.toResponse(service.atualizar(id, dados)));
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        try {
            service.excluir(id);
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
