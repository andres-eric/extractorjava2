package com.celsa.SqlExtractor.service;

import com.celsa.SqlExtractor.dto.consultadto;
import com.celsa.SqlExtractor.repository.ExtranctorRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class consultaService {
    @Autowired
    private ExtranctorRepository extranctorRepository;

    public List<consultadto> obtenerconsulta() {
        return extranctorRepository.getAllConsulta()
                .stream()
                .map(c -> new consultadto(
                        c.getProveedor(),
                        c.getTotal(),
                        c.getNegra(),
                        c.getRoja(),
                        c.getAmarilla(),
                        c.getVerde(),
                        c.getPorcentajeCumplimientoVerde(),
                        c.getNivelServicioTotal(),
                        c.getCumplimientoCantidadEntregada(),
                        c.getPorcentajeCumplimientoPrimeraEntrega(),
                        c.getMes(),
                        c.getAnio(),
                        c.getCantidadIngresada()))
                .collect(Collectors.toList());
    }
}
