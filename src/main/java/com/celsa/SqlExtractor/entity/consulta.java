package com.celsa.SqlExtractor.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Entity

public class consulta {

    @Id
    private String proveedor;

    private Integer total;
    private Integer negra;
    private Integer roja;
    private Integer amarilla;
    private Integer verde;
    @Column(name = "%cumplimiento_cantidad")
    private Double porcentajeCumplimientoCantidad;
    private Double porcentaje;
    @Column(name = "cumplimiento_entrega")
    private Double cumplimientoEntrega;
    private Integer mes;
    private Integer anio;

}
