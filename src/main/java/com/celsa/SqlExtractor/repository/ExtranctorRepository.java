package com.celsa.SqlExtractor.repository;

import com.celsa.SqlExtractor.entity.consulta;
import com.celsa.SqlExtractor.entity.consultaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;


public interface ExtranctorRepository extends JpaRepository<consulta, consultaId> {

    @Query(value = """
                    /* ==========================================================
   1. DEFINICIÓN DE PARÁMETROS
   ========================================================== */
DECLARE @Espacio_tiempo INT = 3;
DECLARE @Amortiguador   INT = 5;

/* ==========================================================
   2. CONSULTA PRINCIPAL (CTEs)
   ========================================================== */
WITH tabla_inicial AS (
    SELECT
        om.ORDNUM_10,
        om.ORDER_10,
        om.LINNUM_10,
        om.DELNUM_10,
        om.PRTNUM_10,
        th.TNXDTE_15,
        om.TYPE_10,
        om.STATUS_10,
        th.TNXCDE_15,
        vm.CNTRY_08,
        om.TRNDTE_10,
        vm.COMNAM_08 AS proveedor,
        om.CURPRM_10,
        th.TNXQTY_15 AS CANTIDAD_INGRESADA,
        om.curqty_10 AS CANTIDAD_PEDIDO,
        -- Extraemos el mes y año desde la raíz para el histórico
        YEAR(th.TNXDTE_15) AS anio,
        MONTH(th.TNXDTE_15) AS mes
    FROM MAXCELSASA.dbo.Order_Master AS om
    INNER JOIN MAXCELSASA.dbo.Transaction_History AS th
        ON om.ORDER_10 = th.ORDNUM_15
    INNER JOIN MAXCELSASA.dbo.Vendor_Master AS vm
        ON om.VENID_10 = vm.VENID_08
    WHERE
        om.STATUS_10 IN ('4', '5') AND
        om.TYPE_10 IN ('PO', 'SO') AND
        -- Trae el mes actual y los 2 meses anteriores basándose en GETDATE()
        th.TNXDTE_15 >= DATEADD(MONTH, -5, DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1)) AND
        th.TNXCDE_15 = 'R' AND
        th.TNXQTY_15 > 0
), ORIGEN AS (
    SELECT *,
        CASE
            WHEN CNTRY_08 = 'COLOMBIA' THEN 'NACIONAL'
            ELSE 'IMPORT'
        END AS origen_proveedor
    FROM tabla_inicial
), TABLA_FECHA AS (
    SELECT *,
        CASE
            WHEN origen_proveedor = 'NACIONAL' THEN TRNDTE_10
            ELSE CURPRM_10
        END AS fecha,
        CASE
            WHEN origen_proveedor = 'NACIONAL' THEN TNXDTE_15
            ELSE TRNDTE_10
        END AS fecha_entrega_real
    FROM ORIGEN
), DIAS AS (
    SELECT *,
        CASE
            WHEN DATEPART(dw, fecha) = 6 THEN DATEDIFF(day, fecha_entrega_real, fecha) + @Espacio_tiempo + 2
            WHEN DATEPART(dw, fecha) = 5 THEN DATEDIFF(day, fecha_entrega_real, fecha) + @Espacio_tiempo + 1
            WHEN DATEPART(dw, fecha) < 5 THEN DATEDIFF(day, fecha_entrega_real, fecha) + @Espacio_tiempo
            ELSE NULL
        END AS dias
    FROM TABLA_FECHA
), ZONA_NEGRA AS (
    SELECT *,
        CASE WHEN dias < 0 THEN 1 ELSE 0 END AS zona_negra
    FROM DIAS
), SKU AS (
    -- Agrupamos por proveedor, año y mes
    SELECT proveedor, anio, mes, COUNT(ORDNUM_10) AS conteo
    FROM ZONA_NEGRA
    GROUP BY proveedor, anio, mes
), ZONA_ROJA_AMARILLA AS (
    SELECT *,
        CASE
            WHEN dias < (@Amortiguador - (2.0 / 3.0 * @Amortiguador)) AND dias >= 0 THEN 1 ELSE 0
        END AS zona_roja,
        CASE
            WHEN dias BETWEEN (@Amortiguador / 3.0) AND (2.0 / 3.0 * @Amortiguador) THEN 1 ELSE 0
        END AS zona_amarilla,
        CASE
            WHEN dias > (2.0 / 3.0 * @Amortiguador) THEN 1 ELSE 0
        END AS zona_verde
    FROM ZONA_NEGRA
), RESUMEN_2 AS (
    SELECT *,
        ROW_NUMBER() OVER(PARTITION BY ORDER_10 ORDER BY TNXDTE_15 asc) AS N,
        MIN(TNXDTE_15) OVER(PARTITION BY ORDER_10) AS fecha_minima
    FROM ZONA_ROJA_AMARILLA
), TABLA_ACUMULADO_CATIDADA AS (
    SELECT *,
        CASE WHEN TNXDTE_15=fecha_minima THEN 0 ELSE 1 END AS conteo,
        SUM(CANTIDAD_INGRESADA) OVER (PARTITION BY ORDER_10 ORDER BY TNXDTE_15) AS suma_acumulada_ciudad
    FROM RESUMEN_2
), CUMPLE_PRIMERA_ENTREGA AS (
    SELECT *,
        CASE WHEN conteo=0 AND suma_acumulada_ciudad>=CANTIDAD_PEDIDO
            THEN 'CUMPLE' ELSE 'NO CUMPLE' END AS CUMPLE_PRIMERA_ENTREGA
    FROM TABLA_ACUMULADO_CATIDADA
), tabla_final as (
    SELECT *,
        CASE WHEN N=1 and CUMPLE_PRIMERA_ENTREGA='CUMPLE' THEN 1 ELSE 0 END CUMPLE_1_ENTREGA,
        CASE WHEN N=1 and CUMPLE_PRIMERA_ENTREGA='NO CUMPLE' THEN 1 ELSE 0 END NO_CUMPLE_1_ENTREGA
    FROM CUMPLE_PRIMERA_ENTREGA 
), TABLA_FINAL_ AS (
    -- Aseguramos pasar anio y mes
    SELECT
        ORDNUM_10, ORDER_10, PRTNUM_10, TNXDTE_15, TYPE_10, STATUS_10, TNXCDE_15, proveedor,
        CANTIDAD_INGRESADA, CANTIDAD_PEDIDO, origen_proveedor, fecha, fecha_entrega_real,
        ZONA_NEGRA, zona_roja, zona_amarilla, zona_verde, CUMPLE_1_ENTREGA, NO_CUMPLE_1_ENTREGA, N,
        anio, mes
    FROM tabla_final
), RESUMEN_UL AS (
    -- Agrupamos por proveedor, año y mes
    SELECT
        proveedor, anio, mes,
        SUM(zona_negra) AS negra,
        SUM(zona_roja) AS roja,
        SUM(zona_amarilla) AS amarilla,
        SUM(zona_verde) AS verde
    FROM TABLA_FINAL_
    WHERE origen_proveedor = 'NACIONAL'
    GROUP BY proveedor, anio, mes
), RESUMEN_F AS (
    -- Join compuesto
    SELECT
        RESUMEN_UL.proveedor, RESUMEN_UL.anio, RESUMEN_UL.mes,
        SKU.conteo AS Total,
        RESUMEN_UL.negra, RESUMEN_UL.roja, RESUMEN_UL.amarilla, RESUMEN_UL.verde
    FROM RESUMEN_UL
    LEFT JOIN SKU
        ON SKU.proveedor = RESUMEN_UL.proveedor 
        AND SKU.anio = RESUMEN_UL.anio 
        AND SKU.mes = RESUMEN_UL.mes
), CANTIDAD_CUMPLIMIENTO AS (
    -- Agrupamos incluyendo año y mes
    SELECT
        ORDER_10, proveedor, anio, mes,
        MAX(CANTIDAD_PEDIDO) AS cantidad_maxima,
        SUM(cantidad_ingresada) AS cantidad_ingresada,
        CASE WHEN SUM(cantidad_ingresada)>=MAX(CANTIDAD_PEDIDO) THEN 1 ELSE 0 END AS 'CUMPLIO_CANTIDAD',
        CASE WHEN SUM(cantidad_ingresada)<MAX(CANTIDAD_PEDIDO) THEN 1 ELSE 0 END AS 'INCUMPLIO_CANTIDAD'
    FROM TABLA_FINAL_
    GROUP BY ORDER_10, proveedor, anio, mes
), SUMA_CANTIDAD AS (
    -- Agrupamos por proveedor, año y mes
    SELECT
        proveedor, anio, mes,
        SUM(CUMPLIO_CANTIDAD) AS CUMPLIO_CANTIDAD,
        SUM(INCUMPLIO_CANTIDAD) AS INCUMPLIO_CANTIDAD
    FROM CANTIDAD_CUMPLIMIENTO
    GROUP BY proveedor, anio, mes
), PORCENTAJE AS (
    -- Join compuesto
    SELECT
        RESUMEN_F.proveedor, RESUMEN_F.anio, RESUMEN_F.mes,
        RESUMEN_F.Total, RESUMEN_F.negra, RESUMEN_F.roja, RESUMEN_F.amarilla, RESUMEN_F.verde,
        CASE
            WHEN (COALESCE(CUMPLIO_CANTIDAD, 0) + COALESCE(INCUMPLIO_CANTIDAD, 0)) = 0 THEN 0
            ELSE ROUND(1.0 * COALESCE(CUMPLIO_CANTIDAD, 0) / (COALESCE(CUMPLIO_CANTIDAD, 0) + COALESCE(INCUMPLIO_CANTIDAD, 0)), 3)
        END AS porcentaje_cumplimiento_cantidad,
        CUMPLIO_CANTIDAD,
        INCUMPLIO_CANTIDAD
    FROM RESUMEN_F
    LEFT JOIN SUMA_CANTIDAD
        ON RESUMEN_F.proveedor = SUMA_CANTIDAD.proveedor
        AND RESUMEN_F.anio = SUMA_CANTIDAD.anio
        AND RESUMEN_F.mes = SUMA_CANTIDAD.mes
), tabla_kpi AS (
    /* ==========================================================
       3. SALIDA FINAL
       ========================================================== */
    SELECT
        LTRIM(RTRIM(proveedor)) AS proveedor,
        anio, mes, -- Mantenemos la fecha original
        Total, negra, roja, amarilla, verde,
        CAST( (CAST(verde AS DECIMAL(18, 4)) / NULLIF(Total, 0)) * 100 AS DECIMAL(18, 2) ) AS PORCENTAJE_CUMPLIMIENTO_VERDE,
        100 - CAST( (CAST(negra AS DECIMAL(18, 4)) / NULLIF(Total, 0)) * 100 AS DECIMAL(18, 2) ) AS NIVEL_SERVICIO_TOTAL,
        CAST(porcentaje_cumplimiento_cantidad AS DECIMAL(10,2))*100 AS CUMPLIMIENTO_CANTIDAD_ENTREGADA
    FROM PORCENTAJE
), cumplen_entregas AS (
    SELECT *,
        CUMPLE_1_ENTREGA + NO_CUMPLE_1_ENTREGA AS TOTAL_ENTREGAS_SUMA
    FROM TABLA_FINAL_ 
    WHERE N = 1
), entregas_final AS (
    -- Agrupamos la primera entrega por proveedor, año y mes
    SELECT 
        LTRIM(RTRIM(proveedor)) AS proveedor, anio, mes,
        SUM(CUMPLE_1_ENTREGA) AS CUMPLE_1_ENTREGA,
        COUNT(*) AS total_entregas,
        CAST( (SUM(CUMPLE_1_ENTREGA) * 100.0) / NULLIF(COUNT(*), 0) AS DECIMAL(10, 2) ) AS PORCENTAJE_CUMPLIMIENTO_PRIMERA_ENTREGA
    FROM cumplen_entregas    
    GROUP BY proveedor, anio, mes
)
-- SELECT FINAL
SELECT
    k.proveedor,
    k.Total,
    k.negra,
    k.roja,
    k.amarilla,
    k.verde,
    k.PORCENTAJE_CUMPLIMIENTO_VERDE,
    k.NIVEL_SERVICIO_TOTAL,
    k.CUMPLIMIENTO_CANTIDAD_ENTREGADA,
    f.PORCENTAJE_CUMPLIMIENTO_PRIMERA_ENTREGA AS PORCENTAJE_CUMPLIMIENTO_PRIMERA_ENTREGA,
    k.mes,
    k.anio
FROM tabla_kpi AS k
INNER JOIN entregas_final AS f
    -- Cruce exacto por proveedor + tiempo
    ON k.proveedor = f.proveedor
    AND k.anio = f.anio
    AND k.mes = f.mes
ORDER BY 
	
	k.proveedor DESC,
    k.anio DESC, 
    k.mes DESC, 
    k.NIVEL_SERVICIO_TOTAL DESC;
                 """, nativeQuery = true)

    List<consulta> getAllConsulta();
}
