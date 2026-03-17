package com.celsa.SqlExtractor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class consultadto {

    private String proveedor;
    private Integer total;
    private Integer negra;
    private Integer roja;
    private Integer amarilla;
    private Integer verde;
    private Double cumplimientoCantidad;
    private Double porcentaje;
    private Double cumplimientoEntrega;
    private Integer mes;
    private Integer anio;

}
