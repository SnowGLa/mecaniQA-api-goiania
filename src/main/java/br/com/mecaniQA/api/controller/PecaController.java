package br.com.mecaniQA.api.controller;

import br.com.mecaniQA.api.dto.PecaRequest;
import br.com.mecaniQA.api.dto.PecaResponse;
import br.com.mecaniQA.api.exception.ConflitoDadosException;
import br.com.mecaniQA.api.exception.DadosInvalidosException;
import br.com.mecaniQA.api.exception.EntidadeNaoEncontradaException;
import br.com.mecaniQA.api.exception.PersistenciaException;
import br.com.mecaniQA.api.mapper.PecaMapper;
import br.com.mecaniQA.api.service.PecaService;

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
@RequestMapping("/api/pecas")
public class PecaController {
    private final PecaService service;

    public PecaController(PecaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PecaResponse> criar(@RequestBody PecaRequest dados) {
        if (dados == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(PecaMapper.toResponse(service.criar(dados)));
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
    public ResponseEntity<List<PecaResponse>> listar() {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(service.listar().stream().map(PecaMapper::toResponse).toList());
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
    public ResponseEntity<PecaResponse> buscarPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(PecaMapper.toResponse(service.buscar(id)));
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
    public ResponseEntity<PecaResponse> atualizar(
            @PathVariable Long id, @RequestBody PecaRequest dados) {
        if (dados == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(PecaMapper.toResponse(service.atualizar(id, dados)));
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
