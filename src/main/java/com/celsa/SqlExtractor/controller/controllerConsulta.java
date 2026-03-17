package com.celsa.SqlExtractor.controller;


import com.celsa.SqlExtractor.dto.consultadto;
import com.celsa.SqlExtractor.entity.consulta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.celsa.SqlExtractor.repository.ExtranctorRepository;
import java.util.List;

@RestController
@RequestMapping("/api/v1/extractor")
public class controllerConsulta {

    @Autowired
    private ExtranctorRepository extranctorRepository;


    @GetMapping("/consulta")
    public ResponseEntity<List<consulta>> obtenerconsulta() {
        List<consulta> consulta1=extranctorRepository.getAllConsulta();
        return ResponseEntity.ok(consulta1);
    }
}
