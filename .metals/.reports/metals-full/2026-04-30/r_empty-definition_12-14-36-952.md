error id: file:///W:/ComandosSQL/sql%20scripts-Esteban/IA%20calidad/SqlExtractor/src/main/java/com/celsa/SqlExtractor/entity/consulta.java:_empty_/Entity#
file:///W:/ComandosSQL/sql%20scripts-Esteban/IA%20calidad/SqlExtractor/src/main/java/com/celsa/SqlExtractor/entity/consulta.java
empty definition using pc, found symbol in pc: _empty_/Entity#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 261
uri: file:///W:/ComandosSQL/sql%20scripts-Esteban/IA%20calidad/SqlExtractor/src/main/java/com/celsa/SqlExtractor/entity/consulta.java
text:
```scala
package com.celsa.SqlExtractor.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Ent@@ity

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

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/Entity#