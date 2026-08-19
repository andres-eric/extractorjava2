error id: file:///W:/ComandosSQL/sql%20scripts-Esteban/IA%20calidad/SqlExtractor/src/main/java/com/celsa/SqlExtractor/entity/consulta.java:java/lang/String#
file:///W:/ComandosSQL/sql%20scripts-Esteban/IA%20calidad/SqlExtractor/src/main/java/com/celsa/SqlExtractor/entity/consulta.java
empty definition using pc, found symbol in pc: java/lang/String#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 317
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
@Entity

public class consulta {

    @Id
    private S@@tring proveedor;

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

empty definition using pc, found symbol in pc: java/lang/String#