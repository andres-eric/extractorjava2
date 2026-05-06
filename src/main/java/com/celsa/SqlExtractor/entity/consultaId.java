package com.celsa.SqlExtractor.entity;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class consultaId implements Serializable {
    private String proveedor;
    private Integer mes;
    private Integer anio;
}
