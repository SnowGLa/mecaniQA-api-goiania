package br.com.mecaniQA.api.controller;

import br.com.mecaniQA.api.dto.OrdemServicoRequest;
import br.com.mecaniQA.api.dto.OrdemServicoResponse;
import br.com.mecaniQA.api.dto.StatusOrdemServicoRequest;
import br.com.mecaniQA.api.exception.ConflitoDadosException;
import br.com.mecaniQA.api.exception.DadosInvalidosException;
import br.com.mecaniQA.api.exception.EntidadeNaoEncontradaException;
import br.com.mecaniQA.api.exception.PersistenciaException;
import br.com.mecaniQA.api.mapper.OrdemServicoMapper;
import br.com.mecaniQA.api.service.OrdemServicoService;

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
@RequestMapping("/api/ordens-servico")
public class OrdemServicoController {
    private final OrdemServicoService service;

    public OrdemServicoController(OrdemServicoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<OrdemServicoResponse> criar(@RequestBody OrdemServicoRequest dados) {
        if (dados == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(OrdemServicoMapper.toResponse(service.criar(dados)));
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
    public ResponseEntity<List<OrdemServicoResponse>> listar() {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(service.listar().stream().map(OrdemServicoMapper::toResponse).toList());
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
    public ResponseEntity<OrdemServicoResponse> buscarPorId(@PathVariable Long codigo) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(OrdemServicoMapper.toResponse(service.buscar(codigo)));
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
    public ResponseEntity<OrdemServicoResponse> alterarStatus(
            @PathVariable Long codigo, @RequestBody StatusOrdemServicoRequest dados) {
        if (dados == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(OrdemServicoMapper.toResponse(service.alterarStatus(codigo, dados)));
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
