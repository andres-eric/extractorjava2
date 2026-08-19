error id: file:///W:/ComandosSQL/sql%20scripts-Esteban/IA%20calidad/SqlExtractor/src/main/java/com/celsa/SqlExtractor/entity/consulta.java:java/lang/Integer#
file:///W:/ComandosSQL/sql%20scripts-Esteban/IA%20calidad/SqlExtractor/src/main/java/com/celsa/SqlExtractor/entity/consulta.java
empty definition using pc, found symbol in pc: java/lang/Integer#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 442
uri: file:///W:/ComandosSQL/sql%20scripts-Esteban/IA%20calidad/SqlExtractor/src/main/java/com/celsa/SqlExtractor/entity/consulta.java
text:
```scala
package com.celsa.SqlExtractor.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Entity
@IdClass(consultaId.class)
public class consulta {

    @Id
    private String proveedor;

    @Column(name = "Total")
    private I@@nteger total;
    private Integer negra;
    private Integer roja;
    private Integer amarilla;
    private Integer verde;

    @Column(name = "PORCENTAJE_CUMPLIMIENTO_VERDE")
    private Double porcentajeCumplimientoVerde;

    @Column(name = "NIVEL_SERVICIO_TOTAL")
    private Double nivelServicioTotal;

    @Column(name = "CUMPLIMIENTO_CANTIDAD_ENTREGADA")
    private Double cumplimientoCantidadEntregada;

    @Column(name = "PORCENTAJE_CUMPLIMIENTO_PRIMERA_ENTREGA")
    private Double porcentajeCumplimientoPrimeraEntrega;

    @Id
    private Integer mes;
    @Id
    @Column(name = "Anio")
    private Integer anio;

}

```


#### Short summary: 

empty definition using pc, found symbol in pc: java/lang/Integer#