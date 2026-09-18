package com.evox.backend.service;

import com.evox.backend.dto.CategoriaResponse;
import com.evox.backend.repository.CategoriaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    /** GET /api/v1/categorias : lista las categorias del catalogo (publico). */
    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAll(Sort.by(Sort.Direction.ASC, "orden")).stream()
                .map(CategoriaResponse::desde)
                .toList();
    }
}